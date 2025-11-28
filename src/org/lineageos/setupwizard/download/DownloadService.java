package org.lineageos.setupwizard.download;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;
import org.lineageos.setupwizard.util.SetupWizardUtils;
import android.content.Context;
import org.lineageos.setupwizard.R;
public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    private static final String SOCKET_CLOSED_ERROR_1 = "Socket is closed";
    private static final String SOCKET_CLOSED_ERROR_2 = "Socket Closed";
    private static final String CANCEL_ERROR = "cancel";

    private final Long DEFAULT_BYTE_SIZE = (long) 1 * 1024 * 1024;
    private DownloadBinder downloadBinder;
    private Map<String, Call> callMap;
    Context context ;

    // Binder for binding the service to a client component.
    public class DownloadBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    // // Cancel an ongoing download for a specific app.
    public boolean cancel(String appName) {
        if (!callMap.containsKey(appName)) {
            return false;
        }
        Call call = callMap.get(appName);
        if (call != null) {
            call.cancel();
        }
        callMap.remove(appName);
        return true;
    }

    // Initiates APK download and registers a callback for handling the response.
    public void downloadApk(String url, String appName, long size, String md5Checksum) {
        Log.w(TAG, "downloadApk url " + url + ", appName " + appName + ", size " + size + ", md5Checksum " + md5Checksum);
        if (callMap.containsKey(appName)) {
            return;
        }
        String yybAppName = context.getString(R.string.yyb);
        if(yybAppName.equals(appName)) {
            if(copyApkToDownloads(this, "yyb.apk", yybAppName+".apk") != null){
                EventBus.getDefault().post(new Event(EventType.DOWNLOAD_IN_PROGRESS, appName, 100));
                String apkName = appName + ".apk";
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadDir, apkName);
                EventBus.getDefault().post(new Event(EventType.DOWNLOAD_COMPLETED, appName));
                new Thread() {
                    @Override
                    public void run() {
                        ApkSilentInstaller.enqueueInstall(appName, file.getAbsolutePath(), DownloadService.this);
                    }
                }.start();
                return ;
            }
           
        }
        Call call = HttpUtils.get(url, new HttpUtils.HttpCallback() {
            @Override
            public void onResponse(Response response) {
                if (response == null || response.body() == null) {
                    onFailure(new IOException(appName + "response == null .apk Download Failed"));
                    return;
                }
                long contentLength = response.body().contentLength();
                Log.w(TAG, "downloadSuccess  contentLength " + contentLength);

                contentLength = contentLength > 0 ? contentLength : DEFAULT_BYTE_SIZE;
                long totalSize = contentLength; //size > 0 ? size : contentLength;

                try {
                    Log.w(TAG, "downloadSuccess  totalSize " + totalSize + ",appName " + appName);
                    saveApk(response, appName, totalSize, md5Checksum);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    onFailure(exception);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                exception.printStackTrace();
                downloadFailed(appName, exception);
            }

            @Override
            public void onError(IOException exception) {
                exception.printStackTrace();
                downloadFailed(appName, exception);
            }
        });
        EventBus.getDefault().post(new Event(EventType.DOWNLOAD_IN_PROGRESS, appName));
        callMap.put(appName, call);
    }

    // Handles download failures by posting a failure event, except for socket-related cancellations.
    private void downloadFailed(String appName, Exception exception) {
        String message = exception.getMessage();
        Log.w(TAG, "downloadFailed message " + message + ",exception " + exception);
        if (message != null && (SetupWizardUtils.containsIgnoreCase(message, SOCKET_CLOSED_ERROR_1) || SetupWizardUtils.containsIgnoreCase(message, SOCKET_CLOSED_ERROR_2) || SetupWizardUtils.containsIgnoreCase(message, CANCEL_ERROR))) {
            return;
        }
        EventBus.getDefault().post(new Event(EventType.DOWNLOAD_FAILED, appName));
    }

    // Saves the downloaded APK to the device's external storage and verifies it via MD5 checksum.
    private void saveApk(Response response, String appName, long totalSize, String md5Checksum) throws IOException, NoSuchAlgorithmException {
        String apkName = appName + ".apk";
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDir, apkName);
        long downloadedSize = 0;

        InputStream inputStream = response.body().byteStream();
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024 * 10];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedSize += bytesRead;
            int progress = (int) ((downloadedSize * 100) / totalSize);
            if (progress > 100) {
                progress = 100;
            }
            if (progress < 0) {
                progress = 0;
            }
            EventBus.getDefault().post(new Event(EventType.DOWNLOAD_IN_PROGRESS, appName, progress));
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();

        // EventBus.getDefault().post(new Event(EventType.DOWNLOAD_COMPLETED, appName));
        // if (md5Checksum != null || !md5Checksum.equals(SetupWizardUtils.getFileMD5(file))) {
        // if (StringUtils.isNotEmpty(md5Checksum) && !StringUtils.equals(md5Checksum, Utils.getFileMD5(file))) {
        // EventBus.getDefault().post(new Event(EventType.DOWNLOAD_FAILED, appName));
        // } else {
        EventBus.getDefault().post(new Event(EventType.DOWNLOAD_COMPLETED, appName));
        ApkSilentInstaller.enqueueInstall(appName, file.getAbsolutePath(), DownloadService.this);
        // }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        downloadBinder = new DownloadBinder();
        callMap = new HashMap<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (String appName : callMap.keySet()) {
            cancel(appName);
        }
        callMap.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return downloadBinder;
    }


     public static String copyApkToDownloads(Context context, String assetFileName, String destinationFileName) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadDir.exists()) {
            if (!downloadDir.mkdirs()) {
                Log.e(TAG, "Download directory creation failed: " + downloadDir.getAbsolutePath());
            }
        }
        
        File outputFile = new File(downloadDir, destinationFileName);
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        
        try {
            inputStream = context.getAssets().open(assetFileName);
            outputStream = new FileOutputStream(outputFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            Log.i(TAG, "APK copied successfully to: " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy APK from assets to Downloads", e);
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams", e);
            }
        }
    }
}

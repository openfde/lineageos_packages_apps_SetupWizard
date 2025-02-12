package org.lineageos.setupwizard.download;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.content.pm.PackageInstaller;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ApkSilentInstaller {

    /**
     * Installation method adapted for Android 9. Performs a full replacement
     * installation.
     *
     * @return
     */
    private static String TAG = "Download--ApkSilentInstaller";
    private static String APP_NAME = "appName";
    private static boolean isInstalling = false;
    private static Queue<InstallRequest> installQueue = new ConcurrentLinkedQueue<>();
    private static int mSessionId = -1;

    /**
     * PackageInstaller in a multi-threaded installation environment causes
     * broadcast messages about successful installations to become mixed up,
     * making it difficult to determine the status of each app's installation.
     * This notification mechanism converts parallel installations into serial
     * installations.
     *
     */
    public static synchronized void enqueueInstall(String appName, String apkFilePath, Context context) {
        installQueue.add(new InstallRequest(appName, apkFilePath));
        startNextAppInstallation(context);
    }

    public static synchronized void startNextAppInstallation(Context context) {
        if (installQueue.isEmpty() || isInstalling) {
            return;
        }
        isInstalling = true;
        InstallRequest installRequest = installQueue.poll();
        installApk(installRequest, context);
    }

    public static void setIsInstalling(boolean isInstalling) {
        ApkSilentInstaller.isInstalling = isInstalling;
    }

    public static Boolean installApk(InstallRequest installRequest, Context context) {
        // Looper.prepare();
        String appName = installRequest.getAppName();
        String apkFilePath = installRequest.getApkFilePath();
        EventBus.getDefault().post(new Event(EventType.INSTALL_STARTED, appName));
        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            return null;
        }

        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        sessionParams.setSize(apkFile.length());

        // packageInstaller.registerSessionCallback(new PackageInstaller.SessionCallback() {
        //     @Override
        //     public void onProgressChanged(int sessionId, float progress) {
        //         Log.w(TAG, "Installation progress: " + progress * 100 + "%");
        //     }
        //     @Override
        //     public void onFinished(int sessionId, boolean success) {
        //         if (success) {
        //             Log.w(TAG, "Installation finished successfully");
        //         } else {
        //             Log.w(TAG, "Installation failed");
        //         }
        //     }
        //     @Override
        //     public void onActiveChanged(int i, boolean b) {
        //         Log.w(TAG, "Installation onActiveChanged i" + i + ",b: " + b);
        //     }
        //     @Override
        //     public void onBadgingChanged(int sessionId) {
        //         Log.w(TAG, "Installation onBadgingChanged sessionId " + sessionId);
        //     }
        //     @Override
        //     public void onCreated(int sessionId) {
        //         Log.w(TAG, "Installation onCreated");
        //     }
        // });
        try {
            mSessionId = packageInstaller.createSession(sessionParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSessionId != -1) {
            Log.w(TAG, "mSessionId != -1");
            boolean copySuccess = onTransfersApkFile(context, apkFilePath);
            if (copySuccess) {
                execInstallAPP(appName, context);
            }
        }
        // Looper.loop();
        return null;
    }

    /**
     * Transfers the APK file via file streams.
     *
     * @param apkFilePath Path of the APK file
     * @return true if the transfer was successful, false otherwise
     */
    private static boolean onTransfersApkFile(Context context, String apkFilePath) {
        InputStream in = null;
        OutputStream out = null;
        PackageInstaller.Session session = null;
        boolean success = false;
        try {
            File apkFile = new File(apkFilePath);
            session = context.getPackageManager().getPackageInstaller().openSession(mSessionId);
            out = session.openWrite("base.apk", 0, apkFile.length());
            in = new FileInputStream(apkFile);
            int total = 0, c;
            byte[] buffer = new byte[1024 * 1024];
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Executes the installation and notifies the result.
     */
    private static void execInstallAPP(String appName, Context context) {
        PackageInstaller.Session session = null;
        try {
            session = context.getPackageManager().getPackageInstaller().openSession(mSessionId);
            Intent intent = new Intent(context, InstallResultReceiver.class);
            intent.setAction("org.lineageos.setupwizard.ACTION_INSTALL_RESULT");
            // intent.setAction(Intent.ACTION_PACKAGE_ADDED);
            intent.putExtra("SessionId", mSessionId);
            intent.putExtra(APP_NAME, appName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            session.commit(pendingIntent.getIntentSender());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
        }
    }
}

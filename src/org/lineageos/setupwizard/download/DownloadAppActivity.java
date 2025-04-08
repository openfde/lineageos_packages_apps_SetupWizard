package org.lineageos.setupwizard.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;

import org.lineageos.setupwizard.BaseSetupWizardActivity;
import android.content.Intent;
import android.os.Bundle;

import org.lineageos.setupwizard.FinishActivity;
import org.lineageos.setupwizard.R;
import android.util.Log;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.widget.Toast;
import android.content.IntentFilter;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.BroadcastReceiver;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;

import org.lineageos.setupwizard.util.SetupWizardUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DownloadAppActivity extends BaseSetupWizardActivity {

    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int ERROR = 3;

    public static final Boolean IS_SELECTED = true;
    public static final Boolean IS_INITIATED = true;
    public static final boolean IS_NOT_SELECTED = false;

    public static int DOWNLOAD_STATUS = 0;

    private final String TAG = "DownloadAppActivity";

    private Singleton singleton = Singleton.getInstance();
    private DownloadService downloadService;
    Intent intentService;

    Context context;

    private RecyclerView recyclerView;
    private RecyclerView downloadingRecyclerView;
    private RecyclerView noDownloadingRecyclerView;

    private AppAdapter appAdapter = new AppAdapter();
    private AppDownloadAdapter appDownloadAdapter = new AppDownloadAdapter();
    private AppNoDownloadAdapter appNoDownloadAdapter = new AppNoDownloadAdapter();

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w(TAG, "onServiceConnected.......... ");
            if (service instanceof DownloadService.DownloadBinder) {
                downloadService = ((DownloadService.DownloadBinder) service).getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "onServiceConnected.......... ");
        }
    };

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case SUCCESS:
                    appAdapter.setAppDownloadInfoList(singleton.getAppDownloadInfoList());
                    // recyclerView.setVisibility(View.VISIBLE);
                    break;
                case FAILURE:
                case ERROR:
                    // recyclerView.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        initView();
        EventBus.getDefault().register(this);
        intentService = new Intent(this, DownloadService.class);
        startService(intentService);
        bindService(intentService, connection, Context.BIND_AUTO_CREATE);

        initData();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        // intentFilter.addAction("org.lineageos.setupwizard.ACTION_INSTALL_RESULT");
        intentFilter.addDataScheme("package");
    }

    private void initView() {
        setNextText(R.string.start_download);
        recyclerView = findViewById(R.id.application_recycler_view);
        downloadingRecyclerView = findViewById(R.id.downloadingRecyclerView);
        noDownloadingRecyclerView = findViewById(R.id.noDownloadingRecyclerView);

        int spanCount = 3;
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(appAdapter);

        downloadingRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        downloadingRecyclerView.setAdapter(appDownloadAdapter);
        ((SimpleItemAnimator) downloadingRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        noDownloadingRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        noDownloadingRecyclerView.setAdapter(appNoDownloadAdapter);
        ((SimpleItemAnimator) downloadingRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

    }

    private void initData() {
        getAppInfoList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void handlerEvent(Event event) {
        String appName = event.getAppName();
        switch (event.eventType) {
            case DOWNLOAD_START:
                downloadStart(appName);
                break;
            case DOWNLOAD_IN_PROGRESS:
                updateProgress(appName, event.getProgress());
                break;
            case DOWNLOAD_STOP:
                downloadStop(appName);
                break;
            case DOWNLOAD_COMPLETED:
            case INSTALL_STARTED:
                installStart(appName);
                break;
            case INSTALL_COMPLETED:
                installComplete(appName);
                break;
            case DOWNLOAD_FAILED:
                downloadFailed(appName);
                break;
        }
    }

    private void getAppInfoList() {
        if (!singleton.hasNetworkRequestSucceeded()) {
            HttpUtils.get(HttpUtils.APP_INFO_URL, new HttpUtils.HttpCallback() {
                @Override
                public void onResponse(okhttp3.Response response) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        List<AppInfo> appInfoList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            AppInfo appInfo = new AppInfo();
                            appInfo.setPrimaryUrl(jsonObject.getString("primaryUrl"));
                            appInfo.setName(jsonObject.getString("name"));
                            appInfo.setIconString(jsonObject.getString("iconString"));
                            appInfo.setAvailable(SetupWizardUtils.ToBoolean(jsonObject.getString("isAvailable")));
                            appInfo.setBackupMd5Checksum("");
                            appInfo.setBackupSize(1);
                            appInfo.setBackupUrl("");
                            appInfo.setPrimaryMd5Checksum("");
                            appInfo.setPrimarySize(1);

                            appInfoList.add(appInfo);
                        }
                        List<AppDownloadInfo> appDownloadInfoList = new ArrayList<>();
                        for (AppInfo appInfo : appInfoList) {
                            appDownloadInfoList.add(new AppDownloadInfo(appInfo, IS_SELECTED, SetupWizardUtils.base64ToBitmap(appInfo.getIconString())));
                        }
                        singleton.setAppDownloadInfoList(appDownloadInfoList);
                        singleton.setRequestStatus(RequestStatus.REQUEST_SUCCESS);
                        if (appDownloadInfoList != null) {
                            Log.w(TAG, "appDownloadInfoList size " + appDownloadInfoList.size());
                        } else {
                            Log.w(TAG, "appDownloadInfoList is empty ");
                        }

                        handler.sendMessage(handler.obtainMessage(SUCCESS));
                        // EventBusUtils.sendButtonTextEvent(new ButtonTextEvent(getString(R.string.start_download)));
                    } catch (Exception e) {
                        singleton.setRequestStatus(RequestStatus.REQUEST_FAILED);
                        Log.e(TAG, "http onResponse exception = " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "http failure exception = " + e.getMessage());
                    handler.sendMessage(handler.obtainMessage(FAILURE));
                    // EventBusUtils.sendButtonTextEvent(new ButtonTextEvent(getString(R.string.done_button_text)));
                }

                @Override
                public void onError(IOException e) {
                    Log.e(TAG, "http error exception = " + e.getMessage());
                    handler.sendMessage(handler.obtainMessage(ERROR));
                    // EventBusUtils.sendButtonTextEvent(new ButtonTextEvent(getString(R.string.done_button_text)));
                }
            });
        } else {
            handler.sendMessage(handler.obtainMessage(SUCCESS));
        }
    }

    public void updateProgress(String appName, int progress) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        if (appDownloadInfo.getEventType() != EventType.DOWNLOAD_IN_PROGRESS || progress == appDownloadInfo.getProgress()) {
            return;
        }
        appDownloadInfo.setProgress(progress);
        appDownloadAdapter.updateProgress(appName);
    }

    public void downloadStart(String appName) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        EventType eventType = appDownloadInfo.getEventType();
        if (eventType != EventType.DOWNLOAD_PENDING && eventType != EventType.DOWNLOAD_FAILED) {
            return;
        }
        appDownloadInfo.setEventType(EventType.DOWNLOAD_IN_PROGRESS);
        EventBusUtils.sendEvent(new Event(EventType.DOWNLOAD_IN_PROGRESS, appName));

        AppInfo appInfo = appDownloadInfo.getAppInfo();
        appDownloadInfo.setSelected(IS_SELECTED);

        if (appInfo != null) {
            Log.w(TAG, "appInfo  " + appInfo.toString());
        } else {
            Log.w(TAG, "appInfo is null ");
        }

        if (downloadService == null) {
            Log.w(TAG, "downloadService is null ");
            return;
        }

        if (eventType == EventType.DOWNLOAD_FAILED && appInfo.getBackupUrl() != null) {
            downloadService.downloadApk(appInfo.getBackupUrl(), appInfo.getName(), appInfo.getBackupSize(), appInfo.getBackupMd5Checksum());
        } else {
            downloadService.downloadApk(appInfo.getPrimaryUrl(), appInfo.getName(), appInfo.getPrimarySize(), appInfo.getPrimaryMd5Checksum());
        }
        appDownloadAdapter.add(appDownloadInfo);
        appNoDownloadAdapter.remove(appDownloadInfo);
    }

    public void downloadStop(String appName) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        appDownloadInfo.setProgress(-1);
        appDownloadInfo.setSelected(IS_NOT_SELECTED);
        appDownloadInfo.setEventType(EventType.DOWNLOAD_PENDING);

        downloadService.cancel(appName);

        appDownloadAdapter.remove(appDownloadInfo);
        appNoDownloadAdapter.add(appDownloadInfo);
    }

    public void installStart(String appName) {
        Log.w(TAG, "installStart  appName " + appName);
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        appDownloadInfo.setEventType(EventType.INSTALL_STARTED);
        appDownloadAdapter.updateEventType(appName);
    }

    public void installComplete(String appName) {
        Log.w(TAG, "installComplete  appName " + appName);
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        if (appDownloadInfo == null) {
            Log.e(TAG, "appDownloadInfo is null");
        } else {
            appDownloadInfo.setEventType(EventType.INSTALL_COMPLETED);
            downloadService.cancel(appName);
        }
        appDownloadAdapter.updateEventType(appName);
    }

    public void downloadFailed(String appName) {
        Toast.makeText(context, appName + context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show();

        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        appDownloadInfo.setProgress(-1);
        appDownloadInfo.setEventType(EventType.DOWNLOAD_FAILED);

        downloadService.cancel(appName);

        appDownloadAdapter.updateEventType(appName);
    }

    public void installApp() {
        if (Singleton.getInstance().getAppDownloadInfoList() == null) {
            return;
        }

        for (int pos = 0; pos < Singleton.getInstance().getAppDownloadInfoList().size(); pos++) {
            AppDownloadInfo appDownloadInfo = Singleton.getInstance().getAppDownloadInfo(pos);
            AppInfo appInfo = appDownloadInfo.getAppInfo();
            if (appInfo.getPrimaryUrl() != null) {
                if (appDownloadInfo != null && appInfo != null && appDownloadInfo.isSelected()) {
                    downloadStart(appInfo.getName());
                } else {
                    appNoDownloadAdapter.add(appDownloadInfo);
                }
            }
        }
    }

    @Override
    protected void onPreviousPressed(){
        List<AppDownloadInfo> appDownloadInfoList = Singleton.getInstance().getAppDownloadInfoList();
        if(appDownloadInfoList != null){
            for (AppDownloadInfo appDownloadInfo : appDownloadInfoList) {
                downloadStop(appDownloadInfo.getAppInfo().getName());
                appDownloadInfo.setSelected(IS_SELECTED);
            }
        }
        finish();
    }
    @Override
    protected void onNextPressed() {
        if (DOWNLOAD_STATUS == 0) {
            DOWNLOAD_STATUS = 1;
            recyclerView.setVisibility(View.GONE);
            downloadingRecyclerView.setVisibility(View.VISIBLE);
            noDownloadingRecyclerView.setVisibility(View.VISIBLE);
            installApp();
            setNextText(R.string.done_button_text);
        } else {
            if (singleton.isNothingDownload()) {
                SetupWizardUtils.finishSetupWizard(DownloadAppActivity.this);
                finish();
            }else {
                showConfirmationDialog();
            }
        }
    }

    private void showConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog, null);

        Button downloadBtn = dialogView.findViewById(R.id.tv_download);
        Button proceedBtn = dialogView.findViewById(R.id.tv_proceed);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.215);
        params.gravity = Gravity.CENTER;
        params.y = 25;
        dialog.getWindow().setAttributes(params);

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                SetupWizardUtils.finishSetupWizard(DownloadAppActivity.this);
                finish();
            }
        });

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.download_app_page;
    }

    @Override
    protected int getTitleResId() {
        return R.string.download_app;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_location;
    }

    public DownloadService getDownloadService() {
        return downloadService;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w(TAG, "onActivityResult  requestCode " + requestCode + ",resultCode " + resultCode);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        DOWNLOAD_STATUS = 0;
        if (intentService != null) {
            stopService(intentService);
        }

        if (connection != null) {
            unbindService(connection);
        }

        super.onDestroy();
    }

}

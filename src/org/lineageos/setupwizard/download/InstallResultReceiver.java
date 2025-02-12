package org.lineageos.setupwizard.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class InstallResultReceiver extends BroadcastReceiver {

    private String TAG = "InstallResultReceiver";
    private boolean INSTALLED = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.w(TAG, "Download----InstallResultReceiver  action " + action);
        if (intent != null) { // 安装的广播
            final int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            int installResult = intent.getIntExtra(Intent.EXTRA_INSTALL_RESULT, -1);

            String appName = intent.getStringExtra("appName");
            Log.w(TAG, "InstallResultReceiver  appName " + appName + ",status " + status + ",installResult " + installResult);
            if (status == PackageInstaller.STATUS_SUCCESS) {
                EventBus.getDefault().post(new Event(EventType.INSTALL_COMPLETED, appName));
            } else {
                String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            }
            ApkSilentInstaller.setIsInstalling(INSTALLED);
            ApkSilentInstaller.startNextAppInstallation(context);
        }
    }
}

package org.lineageos.setupwizard.download;

public class InstallRequest {
    private String appName;
    private String apkFilePath;

    public InstallRequest(String appName, String apkFilePath) {
        this.appName = appName;
        this.apkFilePath = apkFilePath;
    }

    public String getAppName() {
        return appName;
    }

    public String getApkFilePath() {
        return apkFilePath;
    }
}
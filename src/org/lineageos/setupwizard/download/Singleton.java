package org.lineageos.setupwizard.download;

import java.util.ArrayList;
import java.util.List;

public class Singleton {

    private final String TAG = "Singleton";
    private RequestStatus requestStatus = RequestStatus.NOT_REQUESTED;
    private static Singleton instance = new Singleton();
    private List<AppDownloadInfo> appDownloadInfoList = new ArrayList<>();

    private Singleton() {
    }

    public static Singleton getInstance() {
        return instance;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public boolean hasNetworkRequestBeenInitiated() {
        return requestStatus != RequestStatus.NOT_REQUESTED;
    }

    public boolean hasNetworkRequestSucceeded() {
        return requestStatus == RequestStatus.REQUEST_SUCCESS;
    }

    public AppDownloadInfo getAppDownloadInfo(int position) {
        if (appDownloadInfoList == null || position < 0 || position >= appDownloadInfoList.size()) {
            return null;
        }
        return appDownloadInfoList.get(position);
    }

    public AppDownloadInfo getAppDownloadInfo(String appName) {
        if (appName == null || "".equals(appName)) {
            return null;
        }
        for (AppDownloadInfo appDownloadInfo : appDownloadInfoList) {
            if (appName.equals(appDownloadInfo.getAppInfo().getName())) {
                return appDownloadInfo;
            }
        }
        return null;
    }

    public List<AppDownloadInfo> getAppDownloadInfoList() {
        return appDownloadInfoList;
    }

    public void setAppDownloadInfoList(List<AppDownloadInfo> appDownloadInfoList) {
        this.appDownloadInfoList = appDownloadInfoList;
    }

    public int updateProgress(String appName, int progress) {
        for (int i = 0; i < appDownloadInfoList.size(); i++) {
            AppDownloadInfo appDownloadInfo = appDownloadInfoList.get(i);
            AppInfo appInfo = appDownloadInfo.getAppInfo();
            if (appInfo.getName().equals(appName)) {
                appDownloadInfo.setProgress(progress);
                return i;
            }
        }
        return -1;
    }

    public boolean isNothingSelected() {
        if (appDownloadInfoList == null) {
            return true;
        }
        for (AppDownloadInfo appDownloadInfo : appDownloadInfoList) {
            if (appDownloadInfo.isSelected()) {
                return false;
            }
        }
        return true;
    }

    public boolean isNothingDownload() {
        if (appDownloadInfoList == null) {
            return true;
        }
        for (AppDownloadInfo appDownloadInfo : appDownloadInfoList) {
            EventType eventType = appDownloadInfo.getEventType();
            if (eventType != EventType.DOWNLOAD_PENDING && eventType != EventType.INSTALL_COMPLETED && eventType != EventType.DOWNLOAD_FAILED && eventType != EventType.INSTALL_FAILED) {
                return false;
            }
        }
        return true;
    }

}

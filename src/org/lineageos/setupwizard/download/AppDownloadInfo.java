package org.lineageos.setupwizard.download;

import android.graphics.Bitmap;

public class AppDownloadInfo {

    private AppInfo appInfo;
    private boolean isSelected = true;
    private Bitmap bitmap;
    private int progress = -1;
    private EventType eventType;

    public AppDownloadInfo(AppInfo appInfo, boolean isSelected, Bitmap bitmap) {
        this(appInfo, isSelected, bitmap, 0);
    }

    public AppDownloadInfo(AppInfo appInfo, boolean isSelected, Bitmap bitmap, int progress) {
        this.appInfo = appInfo;
        this.isSelected = isSelected;
        this.bitmap = bitmap;
        this.progress = progress;
        eventType = EventType.DOWNLOAD_PENDING;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "AppDownloadInfo{"
                + "appInfo=" + appInfo
                + ", isSelected=" + isSelected
                + ", progress=" + progress
                + ", eventType=" + eventType
                + '}';
    }
}

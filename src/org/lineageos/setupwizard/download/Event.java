package org.lineageos.setupwizard.download;


public class Event {
    public EventType eventType;
    private String appName;
    private int progress;

    public Event(EventType eventType, String appName) {
        this.eventType = eventType;
        this.appName = appName;
    }

    public Event(EventType eventType, String appName, int progress) {
        this.eventType = eventType;
        this.appName = appName;
        this.progress = progress;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
package org.lineageos.setupwizard.download;

import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {

    private String name;
    private boolean isAvailable;
    private String iconString;

    private String primaryUrl;
    private long primarySize = -1L;
    private String primaryMd5Checksum = "";

    private String backupUrl = "";
    private long backupSize = -1L;
    private String backupMd5Checksum = "";

    public AppInfo() {
    }

    public AppInfo(Parcel in) {
        name = in.readString();
        isAvailable = in.readInt() != 0;
        primaryUrl = in.readString();
        primarySize = in.readLong();
        primaryMd5Checksum = in.readString();

        backupUrl = in.readString();
        backupSize = in.readLong();
        backupMd5Checksum = in.readString();
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(isAvailable ? 1 : 0);
        dest.writeString(primaryUrl);
        dest.writeLong(primarySize);
        dest.writeString(primaryMd5Checksum);
        dest.writeString(backupUrl);
        dest.writeLong(backupSize);
        dest.writeString(backupMd5Checksum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getIconString() {
        return iconString;
    }

    public void setIconString(String iconString) {
        this.iconString = iconString;
    }

    public String getPrimaryUrl() {
        return primaryUrl;
    }

    public void setPrimaryUrl(String primaryUrl) {
        this.primaryUrl = primaryUrl;
    }

    public long getPrimarySize() {
        return primarySize;
    }

    public void setPrimarySize(long primarySize) {
        this.primarySize = primarySize;
    }

    public String getPrimaryMd5Checksum() {
        return primaryMd5Checksum;
    }

    public void setPrimaryMd5Checksum(String primaryMd5Checksum) {
        this.primaryMd5Checksum = primaryMd5Checksum;
    }

    public String getBackupUrl() {
        return backupUrl;
    }

    public void setBackupUrl(String backupUrl) {
        this.backupUrl = backupUrl;
    }

    public long getBackupSize() {
        return backupSize;
    }

    public void setBackupSize(long backupSize) {
        this.backupSize = backupSize;
    }

    public String getBackupMd5Checksum() {
        return backupMd5Checksum;
    }

    public void setBackupMd5Checksum(String backupMd5Checksum) {
        this.backupMd5Checksum = backupMd5Checksum;
    }

    @Override
    public String toString() {
        return "AppInfo{"
                + "name='" + name + '\''
                + ", isAvailable=" + isAvailable
                // + ", iconString='" + iconString + '\''
                + ", primaryUrl='" + primaryUrl + '\''
                + ", primarySize=" + primarySize
                + ", primaryMd5Checksum='" + primaryMd5Checksum + '\''
                + ", backupUrl='" + backupUrl + '\''
                + ", backupSize=" + backupSize
                + ", backupMd5Checksum='" + backupMd5Checksum + '\''
                + '}';
    }
}

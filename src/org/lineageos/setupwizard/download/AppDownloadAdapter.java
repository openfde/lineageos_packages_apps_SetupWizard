package org.lineageos.setupwizard.download;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.lineageos.setupwizard.R;

public class AppDownloadAdapter extends RecyclerView.Adapter<AppDownloadAdapter.CustomViewHolder> {

    private final String TAG = "AppDownloadAdapter";
    public final boolean CLICKABLE = true;
    public final boolean UNCLICKABLE = false;
    private volatile List<AppDownloadInfo> appDownloadInfoList;

    public AppDownloadAdapter() {
        this(new ArrayList<>());
    }

    public AppDownloadAdapter(List<AppDownloadInfo> appDownloadInfoList) {
        this.appDownloadInfoList = appDownloadInfoList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application_downloading, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        AppDownloadInfo appDownloadInfo = appDownloadInfoList.get(position);
        EventType eventType = appDownloadInfo.getEventType();

        String appName = appDownloadInfo.getAppInfo().getName();
        Bitmap bitmap = appDownloadInfo.getBitmap();
        int progress = appDownloadInfo.getProgress();

        holder.appName.setText(appName);
        holder.appIcon.setImageBitmap(bitmap);

        holder.appProgressBar.setProgress(progress);
        //holder.appProgressBar.setProgressTintList(ColorStateList.valueOf(R.color.progressbar_completion_color));
        holder.stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new Event(EventType.DOWNLOAD_STOP, appName));
            }
        });

        if (eventType == EventType.INSTALL_STARTED) {
            holder.stopBtn.setClickable(UNCLICKABLE);
            holder.stopBtn.setText(R.string.installing);
        } else if (eventType == EventType.INSTALL_COMPLETED) {
            holder.stopBtn.setClickable(UNCLICKABLE);
            holder.stopBtn.setText(R.string.installed);
        } else if (eventType == EventType.DOWNLOAD_IN_PROGRESS) {
            holder.stopBtn.setClickable(CLICKABLE);
            holder.stopBtn.setText(R.string.cancel);
            //holder.appProgressBar.setProgressTintList(ColorStateList.valueOf(R.color.progressbar_completion_color));
            holder.stopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new Event(EventType.DOWNLOAD_STOP, appName));
                }
            });
        } else if (eventType == EventType.DOWNLOAD_FAILED) {
            holder.stopBtn.setClickable(CLICKABLE);
            holder.stopBtn.setText(R.string.retry);
            holder.appProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            holder.stopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new Event(EventType.DOWNLOAD_START, appName));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return appDownloadInfoList.size();
    }

    private int getIndexByAppName(String appName) {
        return IntStream.range(0, appDownloadInfoList.size())
                .filter(i -> appName.equalsIgnoreCase(appDownloadInfoList.get(i).getAppInfo().getName()))
                .findFirst()
                .orElse(-1);
    }

    public void updateProgress(String appName) {
        int position = getIndexByAppName(appName);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    public void updateEventType(String appName) {
        int position = getIndexByAppName(appName);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    public void remove(AppDownloadInfo appDownloadInfo) {
        boolean remove = appDownloadInfoList.remove(appDownloadInfo);
        notifyDataSetChanged();
    }

    public void add(AppDownloadInfo appDownloadInfo) {
        if (appDownloadInfoList.contains(appDownloadInfo)) {
            return;
        }
        appDownloadInfoList.add(appDownloadInfo);
        notifyDataSetChanged();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView appName;
        ProgressBar appProgressBar;
        Button stopBtn;

        public CustomViewHolder(View view) {
            super(view);
            appIcon = view.findViewById(R.id.appIcon);
            appName = view.findViewById(R.id.appName);
            appProgressBar = view.findViewById(R.id.appProgressBar);
            stopBtn = view.findViewById(R.id.stopBtn);
        }
    }
}

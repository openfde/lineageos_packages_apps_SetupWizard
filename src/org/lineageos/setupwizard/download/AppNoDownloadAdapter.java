package org.lineageos.setupwizard.download;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import org.lineageos.setupwizard.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AppNoDownloadAdapter extends RecyclerView.Adapter<AppNoDownloadAdapter.CustomViewHolder> {

    private final String TAG = "AppNoDownloadAdapter";
    private volatile List<AppDownloadInfo> appDownloadInfoList;

    public AppNoDownloadAdapter() {
        this(new ArrayList<>());
    }

    public AppNoDownloadAdapter(List<AppDownloadInfo> appDownloadInfoList) {
        this.appDownloadInfoList = appDownloadInfoList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application_no_downloading, parent, false);
        return new AppNoDownloadAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppNoDownloadAdapter.CustomViewHolder holder, int position) {
        AppDownloadInfo appDownloadInfo = appDownloadInfoList.get(position);
        String appName = appDownloadInfo.getAppInfo().getName();
        Bitmap bitmap = appDownloadInfo.getBitmap();
        holder.appName.setText(appName);
        holder.appIcon.setImageBitmap(bitmap);

        holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new Event(EventType.DOWNLOAD_START, appName));
            }
        });
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

    public void add(AppDownloadInfo appDownloadInfo) {
        if (appDownloadInfoList.contains(appDownloadInfo)) {
            return;
        }
        boolean add = appDownloadInfoList.add(appDownloadInfo);
        notifyDataSetChanged();
    }

    public void remove(AppDownloadInfo appDownloadInfo) {
        appDownloadInfoList.remove(appDownloadInfo);
        notifyDataSetChanged();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView appName;
        Button downloadBtn;

        public CustomViewHolder(View view) {
            super(view);
            appIcon = view.findViewById(R.id.appIcon);
            appName = view.findViewById(R.id.appName);
            downloadBtn = view.findViewById(R.id.downloadBtn);
        }
    }
}

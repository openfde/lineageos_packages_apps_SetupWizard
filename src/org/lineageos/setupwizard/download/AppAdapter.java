package org.lineageos.setupwizard.download;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import org.lineageos.setupwizard.R;
import org.lineageos.setupwizard.util.SetupWizardUtils;


public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ApplicationHolder> {

    private static final String TAG = "AppAdapter";
    private List<AppDownloadInfo> appDownloadInfoList = new ArrayList<>();
    private RecyclerView mRecyclerView;

    @Override
    public ApplicationHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_application, viewGroup, false);
        ApplicationHolder holder = new ApplicationHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ApplicationHolder applicationHolder, int position) {
        Context context = applicationHolder.itemView.getContext();
        AppDownloadInfo appDownloadInfo = appDownloadInfoList.get(position);
        applicationHolder.applicationIcon.setImageBitmap(appDownloadInfo.getBitmap());
        applicationHolder.applicationName.setText(appDownloadInfo.getAppInfo().getName());
        applicationHolder.checkedIcon.setVisibility(appDownloadInfo.isSelected() ? View.VISIBLE : View.INVISIBLE);
        applicationHolder.parentView.setSelected(appDownloadInfo.isSelected());
        applicationHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasYybApp = SetupWizardUtils.isAssetHasApp(context,context.getString(R.string.yyb));
                if(hasYybApp && context.getString(R.string.yyb).equals(appDownloadInfo.getAppInfo().getName())) {
                    return;
                }
                if (applicationHolder.checkedIcon.getVisibility() == View.INVISIBLE) {
                    applicationHolder.checkedIcon.setVisibility(View.VISIBLE);
                    view.setSelected(true);
                    appDownloadInfo.setSelected(true);
                } else if (applicationHolder.checkedIcon.getVisibility() == View.VISIBLE) {
                    applicationHolder.checkedIcon.setVisibility(View.INVISIBLE);
                    appDownloadInfo.setSelected(false);
                    view.setSelected(false);
                } else { // GONE
                }
                EventBusUtils.sendButtonTextEvent(new ButtonTextEvent(generateButtonText(context)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return appDownloadInfoList.size();
    }

    class ApplicationHolder extends RecyclerView.ViewHolder {

        ImageView applicationIcon;
        TextView applicationName;
        ImageView checkedIcon;

        View parentView;

        public ApplicationHolder(View view) {
            super(view);
            parentView = view;
            applicationIcon = view.findViewById(R.id.appIcon);
            applicationName = view.findViewById(R.id.applicationName);
            checkedIcon = view.findViewById(R.id.checked);
        }
    }

    private String generateButtonText(Context context) {
        return isNothingSelected() ? context.getString(R.string.done_button_text) : context.getString(R.string.start_download);
    }

    private boolean isNothingSelected() {
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

    public void setAppDownloadInfoList(List<AppDownloadInfo> appDownloadInfoList) {
        this.appDownloadInfoList.clear();
        this.appDownloadInfoList.addAll(appDownloadInfoList);
        notifyDataSetChanged();
    }

}

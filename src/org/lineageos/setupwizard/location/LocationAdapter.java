package org.lineageos.setupwizard.location;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import org.lineageos.setupwizard.R;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.Holder> {
    Context context;
    List<String> list;
    ItemClick itemClick;

    public interface ItemClick {
        void setOnItemClick(int pos);
    }

    public LocationAdapter(Context context, List<String> list, ItemClick itemClick) {
        this.context = context;
        this.list = list;
        this.itemClick = itemClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationAdapter.Holder(LayoutInflater.from(context).inflate(R.layout.item_location, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.txtName.setText(list.get(position));
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick.setOnItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView txtName;
        LinearLayout rootView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            rootView = itemView.findViewById(R.id.rootView);
        }
    }


}
package org.lineageos.setupwizard;
import android.widget.Spinner;
import android.widget.SimpleAdapter;
import java.util.Map;
import java.util.List;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;

public class CustomSimpleAdapter extends SimpleAdapter {
    private final Spinner spinner;

    public CustomSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource,
                               String[] from, int[] to, Spinner spinner) {
        super(context, data, resource, from, to);
        this.spinner = spinner;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ImageView imageView = view.findViewById(R.id.iv_selected_indicator);
        if (imageView != null) {
            imageView.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        ImageView imageView = view.findViewById(R.id.iv_selected_indicator);
        if (imageView != null) {
            int selectedPosition = spinner.getSelectedItemPosition();
            imageView.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
        }
        return view;
    }
}
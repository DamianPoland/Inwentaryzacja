package com.wolfmobileapps.inwentaryzacja;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class OverWiewArrayAdapter extends ArrayAdapter<OverViewItem> {

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OverViewItem currentItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_for_ovew_wiev, parent, false);
        }

        ImageView pictureSQLData = convertView.findViewById(R.id.imageViewPicture);
        TextView idSQLData = convertView.findViewById(R.id.textViewID);
        TextView dateSQLData = convertView.findViewById(R.id.textViewDate);
        TextView descriptionSQLData = convertView.findViewById(R.id.textViewDescription);

        // set picture if is not null
        if (currentItem.getPictureSQLData() != null) {
            pictureSQLData.setImageBitmap(currentItem.getPictureSQLData());
        }

        // set rest
        idSQLData.setText(currentItem.getIdSQLData());
        dateSQLData.setText(currentItem.getDateSQLData());
        descriptionSQLData.setText(currentItem.getDescriptionSQLData());


        return convertView;
    }


    public OverWiewArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public OverWiewArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public OverWiewArrayAdapter(@NonNull Context context, int resource, @NonNull OverViewItem[] objects) {
        super(context, resource, objects);
    }

    public OverWiewArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull OverViewItem[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public OverWiewArrayAdapter(@NonNull Context context, int resource, @NonNull List<OverViewItem> objects) {
        super(context, resource, objects);
    }

    public OverWiewArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<OverViewItem> objects) {
        super(context, resource, textViewResourceId, objects);
    }
}

package com.example.ashsaccount.safewordproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<String> {

    public CustomAdapter(@NonNull Context context, String[] rowText) {
        super(context,R.layout.custom_row, rowText);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_row,parent, false);

        String singleItem=getItem(position);
        TextView rowTextView= (TextView) customView.findViewById(R.id.rowText);
        ImageView image= (ImageView) customView.findViewById(R.id.itemImage);
        rowTextView.setText(singleItem);
        image.setImageResource(R.drawable.ic_launcher_foreground);
        return customView;


    }
}

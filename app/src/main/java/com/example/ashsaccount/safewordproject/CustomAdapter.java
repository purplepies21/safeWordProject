package com.example.ashsaccount.safewordproject;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<RowData> {

    public CustomAdapter(@NonNull Context context, int resource, List<RowData> objects) {
        super(context,R.layout.custom_row, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.custom_row, parent, false);

        final RowData singleItem = getItem(position);
        final ImageView lockButton = customView.findViewById(R.id.lockImage);
        TextView rowTextView = (TextView) customView.findViewById(R.id.rowText);
        ImageView image = (ImageView) customView.findViewById(R.id.itemImage);
        rowTextView.setText(singleItem.getText());
        image.setImageResource(R.drawable.ic_launcher_foreground);

        boolean isPhoto = singleItem.getPhotoUrl() != null;
        if (isPhoto) {
            if (singleItem.getLock() == false) {
                Glide.with(image.getContext())
                        .load(singleItem.getPhotoUrl())
                        .into(image);
            }
            rowTextView.setText(singleItem.getText());
        } else{
            Glide.with(image.getContext())
                    .load(R.drawable.txtthumb)
                    .into(image);
        }


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(singleItem.getLock()){
                   

// Intent intent= new Intent();
//                    startActivity(intent);
//                    finish();
//

                }
            }
        });
        if (singleItem.getText() != null) {
            rowTextView.setText(singleItem.getText());

        }

        if (singleItem.getLock() == true) {

            lockButton.setImageResource(R.drawable.locked);
        }
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleItem.getLock() == true) {
                   singleItem.setLock(false);
                    lockButton.setImageResource(R.drawable.unlocked);
                }else {
                    singleItem.setLock(true);
                    lockButton.setImageResource(R.drawable.locked);
                }
            }
        });



        return customView;

    }



}

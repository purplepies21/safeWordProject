package com.example.ashsaccount.safewordproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<RowData> {

    private RowData singleItem;
    private ArrayList<RowData> items = new ArrayList<RowData>();
    private Context context;

    public CustomAdapter(@NonNull Context context) {
        super(context,R.layout.custom_row);
        this.context=context;
    }

    public RowData getSingleItem(int position) {
        return items.get(position);
    }

    public void setSingleItem(RowData r, int position) {
        items.remove(position);
        items.add(position, r);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void addItem(RowData rowData) {
        items.add(rowData);
        Log.v("potato", "adding row : " + rowData.getText());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.custom_row, parent, false);

        singleItem = getSingleItem(position);
        final ImageView lockButton = customView.findViewById(R.id.lockImage);
        TextView rowTextView = (TextView) customView.findViewById(R.id.rowText);
        ImageView image = (ImageView) customView.findViewById(R.id.itemImage);
        rowTextView.setText(singleItem.getText());
        image.setImageResource(R.drawable.lockedicon);

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




        if (singleItem.getText() != null) {
            rowTextView.setText(singleItem.getText());

        }
        Log.v("testing", "is row "+ position+" locked?"+ singleItem.getLock());

        if (singleItem.getLock() == true) {
            lockButton.setImageResource(R.drawable.locked);
        }
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleItem.getLock() == true) {
                    Toast.makeText(context, "File is locked.",Toast.LENGTH_LONG).show();

                }else {
                    singleItem.setLock(true);
                    MainActivity.updateItem(getSingleItem(position).getFileID(),getSingleItem(position).getText(),getSingleItem(position).getPhotoUrl(),getSingleItem(position).getLock());
                    lockButton.setImageResource(R.drawable.locked);
                    Toast.makeText(context, "File is now locked.",Toast.LENGTH_LONG).show();

                }
            }
        });

    rowTextView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Log.v("testing", "is row locked?"+ singleItem.getLock());
        if(singleItem.getLock()==true) {

            Intent intent = new Intent(context, InputPasswordActivity.class);
            intent.putExtra("position", position);
            context.startActivity(intent);
        }else{
            Intent intent = new Intent(context, FileActivity.class);
            intent.putExtra("position", position);
            context.startActivity(intent);

        }



    }
});

        setSingleItem(singleItem, position);
        return customView;

    }

    public void showUpdateDialog(final String fileId, String text){
        AlertDialog.Builder dialogBuilder= new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView= inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);
        final EditText editTextTitle= (EditText) dialogView.findViewById(R.id.editTextTitle);
        final Button buttonUpdate= (Button) dialogView.findViewById(R.id.buttonUpdate);
        final Button buttonDelete= (Button) dialogView.findViewById(R.id.buttonDelete);

        dialogBuilder.setTitle("Updating title "+singleItem.getText());
       final  AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(!editTextTitle.getText().toString().isEmpty()){
                if(singleItem.getPhotoUrl()!=null) {
                    MainActivity.updateItem(fileId, editTextTitle.getText().toString().trim(), singleItem.getPhotoUrl(), singleItem.getLock());
                }else{
                    MainActivity.updateItem(fileId,editTextTitle.getText().toString().trim(), null, singleItem.getLock());

                }
                alertDialog.dismiss();

            }
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.deleteItem(singleItem.getFileID(), singleItem.getPhotoUrl());
            }
        });



    }







}

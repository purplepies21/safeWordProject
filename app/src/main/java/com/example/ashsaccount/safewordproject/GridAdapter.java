package com.example.ashsaccount.safewordproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class GridAdapter extends ArrayAdapter<RowData>{

    private RowData singleItem;
    private ArrayList<RowData> items = new ArrayList<RowData>();
    private Context context;
    private ProgressBar progressBar;
    //private int position;

    public GridAdapter(@NonNull Context context) {
        super(context,R.layout.single_grid);
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
    public void clear() {
        super.clear();
        items.clear();
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

    public boolean hasItemWithFileId(String id) {
        for(RowData item : items){
            if(item.getFileID()==id){
                return false;

            }

        }
        return true;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.single_grid, parent, false);
progressBar=customView.findViewById(R.id.progressBar2);
        singleItem = getSingleItem(position);
        final ImageView lockButton = customView.findViewById(R.id.lockImage);
        TextView rowTextView = (TextView) customView.findViewById(R.id.rowText);
        ImageView image = (ImageView) customView.findViewById(R.id.itemImage);
        rowTextView.setText(singleItem.getText());
        image.setImageResource(R.drawable.lockedicon);
      //  this.position=position;

        boolean isPhoto = singleItem.getPhotoUrl() != null;
        if (isPhoto) {
            if (getSingleItem(position).getLock() == false) {
                Glide.with(image.getContext())
                        .load(singleItem.getPhotoUrl()).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                        .into(image);
            }progressBar.setVisibility(View.GONE);
            rowTextView.setText(getSingleItem(position).getText());
        } else{
            progressBar.setVisibility(View.GONE);
            Glide.with(image.getContext())
                    .load(R.drawable.txtthumb)
                    .into(image);
        }




        if (getSingleItem(position).getText() != null) {
            rowTextView.setText(getSingleItem(position).getText());

        }
        Log.v("testing", "is row "+ position+" locked?"+ getSingleItem(position).getLock());

        if (singleItem.getLock() == true) {
            lockButton.setImageResource(R.drawable.locked);
        }
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSingleItem(position).getLock() == true) {
                    Toast.makeText(context, "File is locked.",Toast.LENGTH_LONG).show();

                }else {
                    getSingleItem(position).setLock(true);
                    MainActivity.updateItem(getSingleItem(position).getFileID(),getSingleItem(position).getText(),getSingleItem(position).getPhotoUrl(),getSingleItem(position).getLock());
                    lockButton.setImageResource(R.drawable.locked);
                    Toast.makeText(context, "File is now locked.",Toast.LENGTH_LONG).show();

                }
            }
        });

        rowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("testing", "is row locked?"+ getSingleItem(position).getLock());
                if(getSingleItem(position).getLock()==true) {

                    Intent intent = new Intent(context, InputPasswordActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("adapterType",true);
                    context.startActivity(intent);
                }else{

                        Intent intent = new Intent(context, FileActivity.class);
                        intent.putExtra("position", position);
                        intent.putExtra("adapterType",true);
                        context.startActivity(intent);




                }



            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("testing", "is row locked?"+ getSingleItem(position).getLock());
                if(getSingleItem(position).getLock()==true) {

                    Intent intent = new Intent(context, InputPasswordActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("adapterType",true);
                    context.startActivity(intent);
                }else{

                    Intent intent = new Intent(context, FileActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("adapterType",true);
                    context.startActivity(intent);




                }



            }
        });

        setSingleItem(getSingleItem(position), position);
        return customView;

    }

    public void showUpdateDialog(final String fileId, String text, final int position){
        AlertDialog.Builder dialogBuilder= new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView= inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);
        final EditText editTextTitle= (EditText) dialogView.findViewById(R.id.editTextTitle);
        final Button buttonUpdate= (Button) dialogView.findViewById(R.id.buttonUpdate);
        final Button buttonDelete= (Button) dialogView.findViewById(R.id.buttonDelete);

        dialogBuilder.setTitle("Updating title "+getSingleItem(position).getText());
        final  AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editTextTitle.getText().toString().isEmpty()){
                    if(getSingleItem(position).getPhotoUrl()!=null) {
                        MainActivity.updateItem(fileId, editTextTitle.getText().toString().trim(), getSingleItem(position).getPhotoUrl(), getSingleItem(position).getLock());
                    }else{
                        MainActivity.updateItem(fileId,editTextTitle.getText().toString().trim(), null, getSingleItem(position).getLock());

                    }
                    alertDialog.dismiss();

                }
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.deleteItem(getSingleItem(position).getFileID(), getSingleItem(position).getPhotoUrl(), position);
                notifyDataSetChanged();
            }
        });



    }
}

package com.example.ashsaccount.safewordproject;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FileActivity extends AppCompatActivity{
    private TextView fileNameTextView;
    private RowData row;
    private int position;
    private boolean adapterType;
    private ImageView image;
    private ProgressBar progressBar;

    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);


        Intent intent= getIntent();

        fileNameTextView= findViewById(R.id.textView);
        image= findViewById(R.id.imageView2);
        progressBar= findViewById(R.id.progressBar);


        if(intent!=null) {
            position = intent.getExtras().getInt("position");
            adapterType = intent.getExtras().getBoolean("adapterType");

            Log.v("potato", "the position is " + position);
//            Log.v("potato", "item at position is "+ MainActivity.customAdapter.getSingleItem(position).getText() );


            if (adapterType == false){
                row = MainActivity.customAdapter.getSingleItem(position);
        }else{
            row = MainActivity.gridViewAdapter.getSingleItem(position);


        }
       fileNameTextView.setText(row.getText());
            if (row.getPhotoUrl() != null) {

                    Glide.with(image.getContext())
                            .load(row.getPhotoUrl()).listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                            .into(image);



            }


            }

        }

    }


package com.example.ashsaccount.safewordproject;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FileActivity extends AppCompatActivity{
    private TextView fileNameTextView;
    private RowData row;
    private int position;
    private ImageView image;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance().getInstance();
        StorageReference  textRef = firebaseStorage.getReference().child("user").child(firebaseAuth.getUid()).child("textFiles");;
        StorageReference imageRef = firebaseStorage.getReference().child("user").child(firebaseAuth.getUid()).child("images");

        Intent intent= getIntent();

        fileNameTextView= findViewById(R.id.textView);
        image= findViewById(R.id.imageView2);



        if(intent!=null){
            position=intent.getExtras().getInt("position");

            Log.v("potato", "the position is "+ position);
            Log.v("potato", "item at position is "+ MainActivity.customAdapter.getSingleItem(position).getText() );



       row = MainActivity.customAdapter.getSingleItem(position);
       fileNameTextView.setText(row.getText());
            if (row.getPhotoUrl() != null) {

                    Glide.with(image.getContext())
                            .load(row.getPhotoUrl())
                            .into(image);



            } else {


            }

        }

    }
}

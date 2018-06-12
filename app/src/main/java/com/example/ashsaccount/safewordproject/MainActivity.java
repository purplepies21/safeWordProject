package com.example.ashsaccount.safewordproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY= "friendly_msg_length";

    private static final int RC_SIGN_IN = 123;
    private ChildEventListener childEventListener;
    CustomAdapter customAdapter;
    ListView messageListView;
    FloatingActionButton imageButton;
    private static final int RC_PHOTO_PICKER =  2;



    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private String mUsername;
    private DatabaseReference databaseReference;
    StorageReference imageRef;
    StorageReference textRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        String[] tempArray = {"abc", "123", "456", "yfgvc",};

        mUsername = ANONYMOUS;
        firebaseDatabase=FirebaseDatabase.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        textRef = firebaseStorage.getReference().child("textFiles");
        imageRef = firebaseStorage.getReference().child("images");
        databaseReference=firebaseDatabase.getReference().child("files");
        uploadData();
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        List<RowData> rowData = new ArrayList<>();
        StorageReference storageReference;
        customAdapter = new CustomAdapter(this, tempArray);


        messageListView = (ListView) findViewById(R.id.listView);
        messageListView.setAdapter(customAdapter);


        imageButton = (FloatingActionButton) findViewById(R.id.fab);

        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String food = String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(MainActivity.this, food, Toast.LENGTH_LONG).show();
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    //user is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);


                }


            }
        };

        imageButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                               intent.setType("image/jpeg");    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                               startActivityForResult(Intent.createChooser(intent,"complete action using"), RC_PHOTO_PICKER);


                                           }
                                       }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            if(requestCode==RESULT_OK){
                Toast.makeText(this,"Signed in!", Toast.LENGTH_SHORT).show();

            }else if(requestCode==RESULT_CANCELED){
                Toast.makeText(this,"Sign in cancelled!", Toast.LENGTH_SHORT).show();
                finish();

            }}else if (requestCode==RC_PHOTO_PICKER && resultCode== RESULT_OK){
            //photo button was clicked
            Uri selectedImageUri=data.getData();
            //Get a reference to store file at chat_photos/<fileName>
            final StorageReference photoRef= imageRef.child(selectedImageUri.getLastPathSegment());

            //upload file to Firebase storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //When the image has successfully uploaded, get its download URL
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri dlUri = uri;
                            RowData rowData= new RowData(null, mUsername, dlUri.toString());
                            databaseReference.push().setValue(rowData);
                        }
                    });
                }
            });


        }

    }



    private void onSignedOutCleanup(){
        mUsername=ANONYMOUS;
        customAdapter.clear();
    }

    private void onSignedInInitialize(String displayName) {
        mUsername=displayName;
        attachDatabaseReadListener();

    }
    private void attachDatabaseReadListener(){
        if(childEventListener==null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    RowData row= dataSnapshot.getValue(RowData.class);
                    customAdapter.add(row.getName().toString());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    RowData row = dataSnapshot.getValue(RowData.class);

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

        }
    }    private void detachDatabaseReadListener() {
        if(childEventListener!=null) {
            childEventListener=null;

        }
    }
    private void uploadData(){
        RowData rowData= new RowData("Hello!", mUsername, null);
        databaseReference.push().setValue(rowData);
        // Clear input box

    }
}




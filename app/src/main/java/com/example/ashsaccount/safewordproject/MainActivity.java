package com.example.ashsaccount.safewordproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        firebaseDatabase=FirebaseDatabase.getInstance().getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        textRef = firebaseStorage.getReference().child("textFiles");
        imageRef = firebaseStorage.getReference().child("images");
        databaseReference=firebaseDatabase.getReference().child("files");

        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        List<RowData> rowData = new ArrayList<>();
        StorageReference storageReference;
        customAdapter = new CustomAdapter(this,R.layout.custom_row, rowData);


        messageListView = (ListView) findViewById(R.id.listView);
        messageListView.setAdapter(customAdapter);
        attachDatabaseReadListener();

        imageButton = (FloatingActionButton) findViewById(R.id.fab);

        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(MainActivity.this, item, Toast.LENGTH_LONG).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.new_text_file_menu:
                //Need to create or find a simple text editor!
                return true;

            default:
                return super.onOptionsItemSelected(item);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener!=null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        detachDatabaseReadListener();
        customAdapter.clear();
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
                            RowData rowData= new RowData(photoRef.getName() +".jpg", mUsername, dlUri.toString(), null);
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
                    customAdapter.add(row);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


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
            }; databaseReference.addChildEventListener(childEventListener);


        }
    }    private void detachDatabaseReadListener() {
        if(childEventListener!=null) {
            childEventListener=null;

        }
    }
    private void uploadData(){
        RowData rowData= new RowData("Hello!", mUsername, null, null);
        databaseReference.push().setValue(rowData);
        // Clear input box

    }
}




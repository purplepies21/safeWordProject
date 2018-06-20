package com.example.ashsaccount.safewordproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final String ANONYMOUS = "anonymous";
//    public static final String REQUEST_CODE_ENABLE= "requestCode";

    private static final int RC_SIGN_IN = 123;
    private ChildEventListener childEventListener;
    public static CustomAdapter customAdapter;
    ListView messageListView;
    FloatingActionButton imageButton;
    ImageButton lockButton;
    private static final int RC_PHOTO_PICKER =  2;



    private static FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private static FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private static String mUsername;
    private DatabaseReference databaseReference;
    private StorageReference imageRef;
    private StorageReference textRef;
    private Handler handler;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//TODO Lock specific files from being accessed without fingerprint or pin authentication
        String[] tempArray = {"abc", "123", "456", "yfgvc",};

        mUsername = ANONYMOUS;
         firebaseDatabase=FirebaseDatabase.getInstance().getInstance();

        ///////////////////////////


        firebaseStorage = FirebaseStorage.getInstance();



        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);

        //rowData = new ArrayList<RowData>();

        StorageReference storageReference;
        ///
        handler = new Handler();

        progressBar=findViewById(R.id.seekBar);

        messageListView = (ListView) findViewById(R.id.listView);




        imageButton = (FloatingActionButton) findViewById(R.id.fab);
        try {
            databaseReference = firebaseDatabase.getReference().child("/users").child(firebaseAuth.getUid()).child("files");
            textRef = firebaseStorage.getReference().child("user").child(firebaseAuth.getUid()).child("textFiles");

            imageRef = firebaseStorage.getReference().child("user").child(firebaseAuth.getUid()).child("images");

        }catch(NullPointerException e)
        {
//            databaseReference = firebaseDatabase.getReference().child("/users").child("Unauthorized").child("files");
//            textRef = firebaseStorage.getReference().child("user").child("Unauthorized").child("textFiles");
//
//            imageRef = firebaseStorage.getReference().child("user").child("Unauthorized").child("images");
//            attachDatabaseReadListener();

        }        ///

        customAdapter = new CustomAdapter(this);
        customAdapter.clear();









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

        messageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                RowData row= customAdapter.getSingleItem(position);
                customAdapter.showUpdateDialog(row.getFileID(),row.getText());
                return false;
            }
        });


    }

    public static void updateItem(String id, String name, String photoURL, boolean locked){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("/users").child(firebaseAuth.getUid()).child("files").child(id);
        RowData row =new RowData(name, mUsername, photoURL, locked);
        databaseReference.setValue(row);
        Toast.makeText(customAdapter.getContext(), " updated successfully!",Toast.LENGTH_LONG).show();

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
                uploadData();
                return true;
            case R.id.new_pin_menu:
                Intent intent= new Intent(getApplicationContext(), CreatePasswordActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        firebaseAuth.addAuthStateListener(authStateListener);
        customAdapter.clear();

        messageListView.setAdapter(customAdapter);


    }
    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener!=null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        detachDatabaseReadListener();
        messageListView.setAdapter(null);


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
            //Get a reference to store file at photos/<fileName>
            final StorageReference photoRef= imageRef.child(selectedImageUri.getLastPathSegment());

            //upload file to Firebase storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  progressBar.setVisibility(View.INVISIBLE);
                    //When the image has successfully uploaded, get its download URL
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                        @Override
                        public void onSuccess(Uri uri) {
                            Uri dlUri = uri;
                            Log.v("potato", "adding photo: " + photoRef.getName() );
                            RowData rowData= new RowData(photoRef.getName() +".jpg", mUsername, dlUri.toString(), false);
                            databaseReference.push().setValue(rowData);

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("upload is "+progress+"% done");
                    int currentProgress=(int)progress;
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(currentProgress);

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
                    RowData row = dataSnapshot.getValue(RowData.class);


                    row.setFileID(dataSnapshot.getKey());
                    if(customAdapter.hasItemWithFileId(row.getFileID())) {
                        customAdapter.addItem(row);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {






                customAdapter.notifyDataSetChanged();
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
        RowData rowData= new RowData("Hello!", mUsername, null,  true);
        databaseReference.push().setValue(rowData);
        // Clear input box

    }

    public static void deleteItem(String fileID, String photoURL) {

        DatabaseReference drFile= FirebaseDatabase.getInstance().getReference("users").child(firebaseAuth.getUid()).child("files").child(fileID);
       if(photoURL!=null){
        StorageReference photoRef = firebaseStorage.getReferenceFromUrl(photoURL);

        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(customAdapter.getContext(), "File deleted successfully from storage", Toast.LENGTH_SHORT).show();
            }
        });}
        drFile.removeValue();



    }



}




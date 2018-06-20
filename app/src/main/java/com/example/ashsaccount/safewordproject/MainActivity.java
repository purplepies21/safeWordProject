/*
* This class contains the bulk of the app. Using Firebase authenticator, Firebase Storage and Firebase database in order to populate the custom gridView or listview. before reaching activity_main, authenticator asks the user to log in and remembers the user.
* Added a button in the menu that allows the user to switch views from listView GridView and vice-versa.
* This app allows the user to upload images to the cloud and lock them from being viewed by those who are not supposed to see them (kids or friends that are using your phone but are a bit too snoopy)
* With fingerprint authenication and/or Pattern authenication, the user can feel that their photos are safe from prying eyes.
* */
package com.example.ashsaccount.safewordproject;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    public static final String ANONYMOUS = "anonymous";
//    public static final String REQUEST_CODE_ENABLE= "requestCode";


    ////////////

    private ViewStub stubGrid;
    private ViewStub stubList;
    private GridView gridView;
    public static GridAdapter gridViewAdapter;
    private static int currentViewMode=0;

    static final int VIEW_MODE_LISTVIEW = 0;
    static final int VIEW_MODE_GRIDVEIW=1;

    private static final int RC_SIGN_IN = 123;
    private ChildEventListener childEventListener;
    public static CustomAdapter customAdapter;
    ListView messageListView;
    FloatingActionButton imageButton;
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


        SharedPreferences sharedPreferences= getSharedPreferences("ViewMode", MODE_PRIVATE);
        currentViewMode=sharedPreferences.getInt("currentViewMode", VIEW_MODE_LISTVIEW);



        firebaseStorage = FirebaseStorage.getInstance();



        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        stubList=findViewById(R.id.stub_list);
        stubGrid=findViewById(R.id.stub_grid);
        stubGrid.inflate();
        stubList.inflate();
        //rowData = new ArrayList<RowData>();

        StorageReference storageReference;
        ///
        handler = new Handler();

        progressBar=findViewById(R.id.seekBar);

        messageListView = (ListView) findViewById(R.id.mylistview);
        gridView=(GridView)findViewById(R.id.mygridview);



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

        if(VIEW_MODE_LISTVIEW==currentViewMode) {
            customAdapter = new CustomAdapter(this);
          if(gridViewAdapter!=null) {
              gridViewAdapter.clear();

          }
          }else{
           if(customAdapter!=null) {
               customAdapter.clear();
           }
            gridViewAdapter = new GridAdapter(this);

        }
        switchView();








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
//This listener is to open the menu that can edit each file.

        messageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(VIEW_MODE_LISTVIEW==currentViewMode) {

                    RowData row = customAdapter.getSingleItem(position);

                    customAdapter.showUpdateDialog(row.getFileID(), row.getText(), position);
                }else{

                    RowData row = gridViewAdapter.getSingleItem(position);

                    gridViewAdapter.showUpdateDialog(row.getFileID(), row.getText(),position);
                }
                return false;
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(VIEW_MODE_LISTVIEW==currentViewMode) {

                    RowData row = customAdapter.getSingleItem(position);

                    customAdapter.showUpdateDialog(row.getFileID(), row.getText(),position);
                }else{

                    RowData row = gridViewAdapter.getSingleItem(position);

                    gridViewAdapter.showUpdateDialog(row.getFileID(), row.getText(),position);
                }
                return false;
            }
        });


    }

    public static void updateItem(String id, String name, String photoURL, boolean locked){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("/users").child(firebaseAuth.getUid()).child("files").child(id);
        RowData row =new RowData(name, mUsername, photoURL, locked);
        databaseReference.setValue(row);
        if(VIEW_MODE_LISTVIEW==currentViewMode) {
            Toast.makeText(customAdapter.getContext(), " updated successfully!", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(gridViewAdapter.getContext(), " updated successfully!", Toast.LENGTH_LONG).show();

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    //This method is to ensure all
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.swapView:
                if(VIEW_MODE_LISTVIEW==currentViewMode){
                    currentViewMode=VIEW_MODE_GRIDVEIW;


                }else{
                    currentViewMode=VIEW_MODE_LISTVIEW;

                }

                SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putInt("currentViewMode",currentViewMode);
                editor.commit();
                switchView();
                break;
            case R.id.new_pin_menu:
                Intent intent= new Intent(getApplicationContext(), CreatePasswordActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);


        }
        return true;
    }

    private void switchView()  {
        if(VIEW_MODE_LISTVIEW==currentViewMode) {
            stubList.setVisibility(View.VISIBLE);
            stubGrid.setVisibility(View.GONE);
        }else{
            stubList.setVisibility(View.GONE);
            stubGrid.setVisibility(View.VISIBLE);


        }
        setAdapters();



    }

    private void setAdapters() {
        if(VIEW_MODE_LISTVIEW==currentViewMode) {


           customAdapter= new CustomAdapter(this);
            messageListView.setAdapter(customAdapter);

            customAdapter.notifyDataSetChanged();

        }else{

            gridViewAdapter= new GridAdapter(this);
            gridView.setAdapter(gridViewAdapter);
            gridViewAdapter.notifyDataSetChanged();
        }
//        onSignedInInitialize(mUsername);

    }

    @Override
    protected void onResume() {
        super.onResume();

        firebaseAuth.addAuthStateListener(authStateListener);
attachDatabaseReadListener();


    }
    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener!=null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        detachDatabaseReadListener();
        messageListView.setAdapter(null);
//        if(VIEW_MODE_LISTVIEW==currentViewMode) {
//
//            customAdapter.clear();
//        }else {
//            gridViewAdapter.clear();
//        }

    }

    //handles ActivityResults. the 3 used so far are for when the user signs in, signs out, or uploads an image.
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
                //progressbar for uploading images to the database
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("upload is "+progress+"% done");
                    int currentProgress=(int)progress;
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(currentProgress);

                }
            });


        }

    }
//clears the adapters after the user has signed out.
    private void onSignedOutCleanup(){

        mUsername=ANONYMOUS;
        if(VIEW_MODE_LISTVIEW==currentViewMode) {

            customAdapter.clear();
        }else {
            gridViewAdapter.clear();
        }
    }
//prepares the activity to be populated after the user has signed in.
    private void onSignedInInitialize(String displayName) {


        mUsername=displayName;
attachDatabaseReadListener();
    }

    //keeps track of and refreshes data when the database is updated. populates the adapters.
    private void attachDatabaseReadListener(){
        if(childEventListener==null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    RowData row = dataSnapshot.getValue(RowData.class);


                    row.setFileID(dataSnapshot.getKey());
                    if(VIEW_MODE_LISTVIEW==currentViewMode) {

                        if(customAdapter.hasItemWithFileId(row.getFileID())) {
                            customAdapter.addItem(row);
                        customAdapter.notifyDataSetChanged();
                        }
                    }else{
                        if(gridViewAdapter.hasItemWithFileId(row.getFileID())) {
                            gridViewAdapter.addItem(row);
                        gridViewAdapter.notifyDataSetChanged();
                    }
                }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {





                    if(VIEW_MODE_LISTVIEW==currentViewMode) {

                        customAdapter.notifyDataSetChanged();
                    }else {
                        gridViewAdapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


                    if(VIEW_MODE_LISTVIEW==currentViewMode) {

                        customAdapter.notifyDataSetChanged();
                    }else {
                        gridViewAdapter.notifyDataSetChanged();

                    }


                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };  if(databaseReference!=null) {
                databaseReference.addChildEventListener(childEventListener);
            }

        }
    }    private void detachDatabaseReadListener() {
        if(childEventListener!=null) {
            childEventListener=null;

        }
    }

    //this method was used for testing. not necessary to keep around but kept it commented out in order to check in future use.
//    private void uploadData(){
//        RowData rowData= new RowData("Hello!", mUsername, null,  true);
//        databaseReference.push().setValue(rowData);
//        // Clear input box
//
//    }

// this method deletes data from the database and the image from firebase storage that is linked by that database.
    public static void deleteItem(String fileID, String photoURL, int position) {

        DatabaseReference drFile= FirebaseDatabase.getInstance().getReference("users").child(firebaseAuth.getUid()).child("files").child(fileID);
       if(photoURL!=null){
        StorageReference photoRef = firebaseStorage.getReferenceFromUrl(photoURL);

        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(VIEW_MODE_LISTVIEW==currentViewMode) {

                    Toast.makeText(customAdapter.getContext(), "File deleted successfully from storage", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(gridViewAdapter.getContext(), "File deleted successfully from storage", Toast.LENGTH_SHORT).show();

                }
            }
        });}
        drFile.removeValue();



    }



}




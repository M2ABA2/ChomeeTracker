package com.makoele.chomeetracker.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.makoele.chomeetracker.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

  private   EditText createCode, groupName;
    private TextView username;
    private  Button createGroup;
    private  CircleImageView profilePicture;

    private  String name, email, password, date, issharing, code;
    private  ProgressDialog progressDialog;

    private RecyclerView recyclerView;
    private  FirebaseAuth auth;
    private  FirebaseUser user;
    private  DatabaseReference databaseReference, groupRef, mStorageRef;
    private  String userId;
    private   String setUsername;

    Uri imageUri;
    private final int PICK_IMAGE =100;

    //Firebase Storage
    private  FirebaseStorage storage;
    private  StorageReference storageReference;
    private  StorageTask storageTask;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        username = (TextView)v.findViewById(R.id.txtUsername);
        profilePicture =(CircleImageView) v.findViewById(R.id.profile_image);
        createCode = (EditText)v.findViewById(R.id.txtCode);
        groupName =(EditText)v.findViewById(R.id.txtGroupName);
        createGroup = (Button)v.findViewById(R.id.btnCreateGroup);

        progressDialog = new ProgressDialog(getActivity());

        //Firebase Init
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        //Fetch name from database and display
        databaseReference.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                username.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Fetch Current Profile Picture from the database


        //Save Profile Picture
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedImage();

                imageUploader();
            }
        });

        //Saving Code That's been generated
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createGroup();
            }
        });

        return v;
    }

//Create Group
    public void createGroup(){

        final String setGroupName = groupName.getText().toString().toLowerCase();
        final String setCode = createCode.getText().toString();
        String user_id = FirebaseAuth.getInstance().getUid();

        if(TextUtils.isEmpty(setGroupName) && TextUtils.isEmpty(setCode))
        {
            Toast.makeText(getActivity(), "Please Fill in the Required Fields", Toast.LENGTH_SHORT).show();
        }
        else
        {
            groupRef = FirebaseDatabase.getInstance().getReference("Groups").push();
        try {

            groupRef.child("groupname").setValue(setGroupName);
            groupRef.child("groupcode").setValue(setCode);
            groupRef.child("admin").setValue(user_id);
            groupRef.child("members").setValue(auth.getCurrentUser().getEmail());

            Toast.makeText(getActivity(), "Group Succesfully Created!", Toast.LENGTH_SHORT).show();
            groupName.setText("");
            createCode.setText("");
        }catch (Exception e)
        {
            Toast.makeText(getActivity(), "Sorry, something happened. try in a few seconds", Toast.LENGTH_SHORT).show();
        }

        }

    }

//Uploads image into the Firebase database
    private void imageUploader()
    {
       if(imageUri !=null)
       {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        StorageReference ref = storageReference.child("image/"+ UUID.randomUUID().toString());
        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Upload Failed"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
                });
       }

    }


    //Image method
    private void selectedImage() {

       Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

       if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
               null != data && data.getData() != null)
       {
           imageUri = data.getData();
           profilePicture.setImageURI(imageUri);

                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getActivity());

                Toast.makeText(getActivity(),"Profile Picture Updated!",Toast.LENGTH_SHORT);

        }

       if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                imageUri = data.getData();
                profilePicture.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }
}
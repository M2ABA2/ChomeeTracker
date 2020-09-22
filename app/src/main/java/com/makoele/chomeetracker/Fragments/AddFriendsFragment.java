package com.makoele.chomeetracker.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makoele.chomeetracker.R;
import com.makoele.chomeetracker.Model.User;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class AddFriendsFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

   private Pinview pinview;
   DatabaseReference reference,currentReference,userRef;
   private FirebaseUser user;
   private FirebaseAuth auth;

   private String current_user_id, join_user_id;
   private Button joinCircle, inviteFriend;
   private EditText getGroupName;

    private static final int REQUEST_INVITE = 0;
    private static final String TAG = "Add Friends";

    private GoogleApiClient googleApiClient;

    public AddFriendsFragment() {
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
        View v = inflater.inflate(R.layout.fragment_add_friends, container, false);

        pinview = (Pinview)v.findViewById(R.id.pinview);
        getGroupName=(EditText)v.findViewById(R.id.txtGetGroupName);
        joinCircle = (Button)v.findViewById(R.id.btnJoinCircle);
        inviteFriend =(Button)v.findViewById(R.id.btnInvite);

        //Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String user_id = auth.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(user_id).child("email");


        //Invite Friend
        inviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onInviteClicked();
            }
        });



        //Join Group
        joinCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String groupName = getGroupName.getText().toString().toLowerCase();
                final String code = pinview.getValue();

                reference = FirebaseDatabase.getInstance().getReference().child("Groups");


                if(TextUtils.isEmpty(groupName) && TextUtils.isEmpty(code) ){
                    Toast.makeText(getActivity(), "Please fill in the fields!", Toast.LENGTH_SHORT).show();
                }
               else{
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ArrayList<String> groupChatNames = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {

                                    if(groupName == child.getValue() && code == child.getValue()){

                                        reference.child("members").setValue(userRef);
                                        Toast.makeText(getContext(), "You've been Added!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }
            }
        });

        return v;
    }

    //Sends Invite to Download App
    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder("Chomee App Invitation")
                .setMessage("Dumelang! Try this awesome tracking app...")
                .setDeepLink(Uri.parse("http://google.com"))
                .setCallToActionText("Invitation CTA")
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_INVITE)
        {
            if (resultCode == RESULT_OK)
            {
                String [] ids = AppInviteInvitation.getInvitationIds(requestCode,data);

                for(String id:ids)
                {
                    System.out.println("AddFriendsFragment.onActivityResult:" +id);
                }
            }
            else
            {
                Toast.makeText(getActivity(), "Sorry, something went wrong yaz ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

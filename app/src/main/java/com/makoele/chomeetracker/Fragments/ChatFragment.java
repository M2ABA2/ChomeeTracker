package com.makoele.chomeetracker.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makoele.chomeetracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class ChatFragment extends Fragment {

    private ImageButton sendMsg;
    private EditText inputChat ;
    private TextView viewConversation,view;


    private DatabaseReference groupRef,userRef;

    String chatName,userName;

    public ChatFragment() {
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
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        sendMsg = (ImageButton) v.findViewById(R.id.btnSend);
        inputChat= (EditText)v.findViewById(R.id.txtChat);
        viewConversation =(TextView)v.findViewById(R.id.viewChat);


         //Retrieve chatName from Group Fragment
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            chatName = bundle.getString("room_name");
        }
        getActivity().setTitle("Room - "+chatName);

        //Database reference
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String user_id = auth.getUid();
        groupRef = FirebaseDatabase.getInstance().getReference("Chats").child("Friends");
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(user_id).child("name");

        //Convert userRef to a string
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userName = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        // send message
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,Object> map = new HashMap<String,Object>();
                String temp_key = groupRef.push().getKey();
                groupRef.updateChildren(map);

                DatabaseReference message_root =  groupRef.child(temp_key);
                Map<String,Object>map2 = new HashMap<String, Object>();
                map2.put("name",userName);
                map2.put("msg",inputChat.getText().toString());
                inputChat.setText("");

                //confirm changes to update children
                message_root.updateChildren(map2);
            }
        });


        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                append_chat_conversation(dataSnapshot);
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
        });


        return v;
    }

    //Chat conversations
    private String chat_msg, chat_user_name;

    private  void append_chat_conversation(DataSnapshot dataSnapshot){

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext()){
            //read value from child
            chat_msg = (String)((DataSnapshot)i.next()).getValue();
            chat_user_name = (String)((DataSnapshot)i.next()).getValue();

            viewConversation.append(chat_user_name +" : "+chat_msg + "\n");
        }
    }

}

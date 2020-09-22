package com.makoele.chomeetracker.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makoele.chomeetracker.Interfaces.ItemClickListener;
import com.makoele.chomeetracker.MapsActivity;
import com.makoele.chomeetracker.Model.Group;
import com.makoele.chomeetracker.Model.Tracking;
import com.makoele.chomeetracker.Model.User;
import com.makoele.chomeetracker.R;
import com.makoele.chomeetracker.Tracking.ListOnlineViewHolder;
import com.makoele.chomeetracker.Tracking.TrackingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.facebook.FacebookSdk.getApplicationContext;


public class GroupFragment extends Fragment {

   private DatabaseReference root;

   private ListView listChatGroups;
   private EditText room_name;
   private Button add_room;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();

    private String name;

    public GroupFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_group, container, false);

        //Set toolbar
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar)v.findViewById(R.id.toolBar);
        toolbar.setTitle("Group Chats");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        listChatGroups = (ListView)v.findViewById(R.id.listGroups);
        room_name = (EditText)v.findViewById(R.id.txtAddChat);
        add_room = (Button)v.findViewById(R.id.btnAddGhat);



        //Database reference
        root = FirebaseDatabase.getInstance().getReference("Chats");

        arrayAdapter = new ArrayAdapter<>(getActivity(),R.layout.group_layout, list_of_groups);
        listChatGroups.setAdapter(arrayAdapter);


        //button clicked
        add_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String,Object> map = new HashMap<String, Object>();
                map.put(room_name.getText().toString(),"");
                root.updateChildren(map);
                Toast.makeText(getActivity(), "Chat Group Successfully Added!", Toast.LENGTH_SHORT).show();
                room_name.setText("");
            }
        });

        //view list of groups
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while (i.hasNext()){
                    set.add(((DataSnapshot)i.next()).getKey());
                }
                list_of_groups.clear();
                list_of_groups.addAll(set);

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //enter chat
        listChatGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ChatFragment chatFragment = new ChatFragment ();
                Bundle args = new Bundle();
                args.putString("room_name", listChatGroups.getItemAtPosition(i).toString() );

                chatFragment.setArguments(args);

                //Inflate the fragment
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, chatFragment).commit();


            }
        });


        return v;
    }



}

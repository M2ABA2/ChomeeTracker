package com.makoele.chomeetracker.Tracking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makoele.chomeetracker.MapsActivity;
import com.makoele.chomeetracker.Model.Tracking;
import com.makoele.chomeetracker.Model.User;
import com.makoele.chomeetracker.R;

public class TrackingActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private EditText mSearchField;
    private ImageButton mSearchBtn;

    private RecyclerView mRecyclerView;

    private DatabaseReference mUserDatabase,locations;
    private GoogleApiClient googleApiClient;
    private Location  mLastLocation;

    private  static final int PLAY_SERVICES_RES_REQUEST = 7172;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        mSearchField = (EditText)findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);

        mRecyclerView = (RecyclerView)findViewById(R.id.result_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Database Reference
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");
        //location servicces
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Search Button Clicked
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = mSearchField.getText().toString();
                firebaseUserSearch(searchText);
            }
        });



    }

    private void firebaseUserSearch(String searchText) {

        String userID = FirebaseAuth.getInstance().getUid();
        final DatabaseReference getLocation = FirebaseDatabase.getInstance()
                .getReference("Users");

        getLocation.child("lat");


        Toast.makeText(TrackingActivity.this, "Started Search", Toast.LENGTH_LONG).show();

        Query firebaseSearchQuery = mUserDatabase.orderByChild("email").startAt(searchText);

        FirebaseRecyclerOptions<User> options =new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(firebaseSearchQuery,User.class)
                .build();


        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @NonNull

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder usersViewHolder, final int position, @NonNull final User user) {
                usersViewHolder.setDetails(getApplicationContext(), user.getEmail());

                //when email is clicked
                usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        final String vistUser = getRef(position).getKey();

                        getLocation.child(vistUser).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String email = (String)dataSnapshot.child("email").getValue();
                                Double lat = (Double)dataSnapshot.child("location").child("lat").getValue();
                                Double lng = (Double)dataSnapshot.child("location").child("lng").getValue();

                                Intent map = new Intent(TrackingActivity.this, MapsActivity.class);
                                map.putExtra(map.EXTRA_EMAIL,email);
                                map.putExtra("lat", lat);
                                map.putExtra("lng", lng);

                                startActivity(map);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                       /* if (!user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {

                            Intent map = new Intent(TrackingActivity.this, HomeFragment.class);
                            map.putExtra(map.EXTRA_EMAIL,vistUser);
                            map.putExtra("lat", mLastLocation.getLatitude());
                            map.putExtra("lng", mLastLocation.getLongitude()); */


                        }


                    });


            }
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_layout, viewGroup,false);
                UsersViewHolder usersViewHolder = new UsersViewHolder(view);

                return usersViewHolder;
            }


        };

        firebaseRecyclerAdapter.startListening();
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this, PLAY_SERVICES_RES_REQUEST).show();

            }
            else{
                Toast.makeText(this,"This device is not supported!",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void displayLocation() {

        String userID = FirebaseAuth.getInstance().getUid();
        DatabaseReference currentDBcordinates = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("location");
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        currentDBcordinates.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if(mLastLocation != null)
                {
                    //Update to Firebase
                    locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    String.valueOf(mLastLocation.getLatitude()),
                                    String.valueOf(mLastLocation.getLongitude())
                            ));
                }
                else{
                   // Toast.makeText(this,"Could not get location",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onClick(View view) {

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(googleApiClient != null)
            googleApiClient.connect();
    }

    // View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDetails(Context ctx, String userEmail){

            TextView user_name = (TextView) mView.findViewById(R.id.name_text);

            user_name.setText(userEmail);



        }


    }



}

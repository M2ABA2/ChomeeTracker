package com.makoele.chomeetracker.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.makoele.chomeetracker.MapsActivity;
import com.makoele.chomeetracker.Model.Tracking;
import com.makoele.chomeetracker.Model.User;
import com.makoele.chomeetracker.R;
import com.makoele.chomeetracker.Services.TrackingService;
import com.makoele.chomeetracker.Services.GPSTracker;
import com.makoele.chomeetracker.Tracking.TrackingActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class HomeFragment extends Fragment implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    GPSTracker gps;

    GoogleApiClient client;
    DatabaseReference locations, myRef;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseUser user;

    private String email;
    private ImageButton btnSearch;
    private EditText mSearchField;
    private RecyclerView mRecyclerView;
    String myLocation;

    Double lat, lng;

    LocationManager locationManager;
    LocationRequest request;
    LocationListener listener;



    public HomeFragment() {

        //Sending location to database as default

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        //ToolBar
        Toolbar homeToolbar = (Toolbar)v.findViewById(R.id.toolbarHome);
       // ((AppCompatActivity)getActivity()).setSupportActionBar(homeToolbar);

        //Recycler View
        mRecyclerView = (RecyclerView)v.findViewById(R.id.result_list);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        //Search
        mSearchField = (EditText)v.findViewById(R.id.search_field);
       btnSearch = (ImageButton)v.findViewById(R.id.btnSearch);
       btnSearch.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               String searchText = mSearchField.getText().toString();
               firebaseUserSearch(searchText);
           }
       });


        //Request permission
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
        }


        //Reference to Firebase
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        locations = FirebaseDatabase.getInstance().getReference("Users").child(user_id);


        return v;
    }

    private void firebaseUserSearch(String searchText) {

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference userLocation = FirebaseDatabase.getInstance().getReference("Users")
                .child(user_id)
                .child("location");

        //location of friend
        final DatabaseReference getLocation = FirebaseDatabase.getInstance()
                .getReference("Users");


        Toast.makeText(getActivity(), "Started Search", Toast.LENGTH_LONG).show();

        Query firebaseSearchQuery = getLocation.orderByChild("email").startAt(searchText);

        FirebaseRecyclerOptions<User> options =new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(firebaseSearchQuery,User.class)
                .build();


        FirebaseRecyclerAdapter<User, TrackingActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, TrackingActivity.UsersViewHolder>(options) {
            @NonNull

            @Override
            protected void onBindViewHolder(@NonNull final TrackingActivity.UsersViewHolder usersViewHolder, final int position, @NonNull final User user) {
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

                                LatLng friendLocation = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions()
                                        .position(friendLocation)
                                        .title(email)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                                mMap.setMyLocationEnabled(true);
                                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                mMap.getUiSettings().setAllGesturesEnabled(true);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLocation,15));


                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(friendLocation);

                                /**initialize the padding for map boundary*/
                                int padding = 50;
                                /**create the bounds from latlngBuilder to set into map camera*/
                                LatLngBounds bounds = builder.build();
                                /**create the camera with bounds and padding to set into map*/
                                final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                /**call the map call back to know map is loaded or not*/
                                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                    @Override
                                    public void onMapLoaded() {
                                        /**set animated zoom camera into map*/
                                        mMap.animateCamera(cu);
                                    }
                                });

                                //Load Google Navigation to get direction to friend location when location is clicked



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                    }


                });


            }
            @Override
            public TrackingActivity.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_layout, viewGroup,false);
                TrackingActivity.UsersViewHolder usersViewHolder = new TrackingActivity.UsersViewHolder(view);

                return usersViewHolder;
            }


        };

        firebaseRecyclerAdapter.startListening();
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    public void loadNavigationView(String lat,String lng){
        Uri navigation = Uri.parse("google.navigation:q="+lat+","+lng+"");
        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigation);
        navigationIntent.setPackage("com.google.android.apps.maps");
        startActivity(navigationIntent);
    }

    private double distance(Location currentUser, Location friend) {
        double theta = currentUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude()))
                * Math.sin(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(currentUser.getLatitude()))
                * Math.cos(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 150.0);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        client = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        client.connect();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
       // request.setInterval(1000); //1 second
        request.setInterval(100000); //1 min

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);

        //Start tracking services

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location == null) {
            Toast.makeText(getApplicationContext(), "Could not get Location", Toast.LENGTH_SHORT).show();
        } else {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);

            //Initialize Tracking constructor to add into Firebase database
            Tracking tracking = new Tracking(latitude,longitude);

            //Add latitude and longitude into Firebase Database
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    FirebaseDatabase.getInstance().getReference("Users").child(currentUid).child("location").setValue(tracking)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                               /* Toast.makeText(getActivity(),"Location Added",Toast.LENGTH_SHORT).show(); */
                            }
                            else{
                                Toast.makeText(getActivity(),"Location Not Saved",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


            //Instantiate class into Geocoder
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                String str = addressList.get(0).getLocality() + ",";
                str += addressList.get(0).getCountryName();

                //displays location and name of location
                mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
                mMap.getMaxZoomLevel();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //If the permission has been granted...//

        if (requestCode == MY_PERMISSION_REQUEST_CODE && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//...then start the GPS tracking service//
            gps = new GPSTracker(getActivity());



            if (gps.canGetLocation()) {
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("Users");

                String user_id = auth.getCurrentUser().getUid();
                DatabaseReference current_user_db = myRef.child(user_id);

                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();

              // myRef.setValue(latitude + "," + longitude);
                current_user_db.child("latitude").setValue(latitude);
                current_user_db.child("longitude").setValue(longitude);
            } else {

//If the user denies the permission request, then display a toast with some more information//

                Toast.makeText(getActivity(), "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
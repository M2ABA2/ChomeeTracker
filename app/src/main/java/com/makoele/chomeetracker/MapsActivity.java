package com.makoele.chomeetracker;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makoele.chomeetracker.Fragments.HomeFragment;
import com.makoele.chomeetracker.Model.Tracking;
import com.makoele.chomeetracker.Model.User;
import com.makoele.chomeetracker.Tracking.TrackingActivity;

import android.Manifest;

import java.io.IOException;
import java.security.Permission;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private SearchView searchLocation;

    private GoogleMap mMap;
    GoogleApiClient client;
    DatabaseReference databaseReference,locations;
    FirebaseAuth auth;

    private RecyclerView mRecyclerView;
    LocationManager locationManager;
    LocationRequest request;
    LocationListener locationListener;
    String current_user_name;
    private String email;

    Double lat,lng;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);
        auth = FirebaseAuth.getInstance();

        searchLocation = (SearchView)findViewById(R.id.searchLocation);
        mRecyclerView = (RecyclerView)findViewById(R.id.result_list);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Ref to Firebase First
        String userId = auth.getUid();

        locations = FirebaseDatabase.getInstance().getReference("Users");

        //Search Button Clicked
       searchLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String s) {

               Toast.makeText(MapsActivity.this, "Started Search", Toast.LENGTH_LONG).show();

           /*   String location = searchLocation.getQuery().toString();
               List<Address> addressList = null;
               if(location != null || !location.equals("")){
                   Geocoder geocoder = new Geocoder(MapsActivity.this);

                   try{
                       addressList = geocoder.getFromLocationName(location,1);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   Address address = addressList.get(0);
                   LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                   mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
               } */
               return false;
           }

           @Override
           public boolean onQueryTextChange(String s) {



               return true;
           }
       });
       mapFragment.getMapAsync(this);
        //Get Intent from ListOnline Activity
      /*  if(getIntent() !=null)
        {
            email = getIntent().getStringExtra("email");
            lat = getIntent().getDoubleExtra("lat",0);
            lng = getIntent().getDoubleExtra("lng",0);
        }
        if(!TextUtils.isEmpty(email))
            loadLocationForThisUser(email); */



    }




    private void loadLocationForThisUser(String email) {
        Query user_location = locations.orderByChild("email").equalTo(email);

        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Tracking tracking = postSnapShot.getValue(Tracking.class);

                    //Add marker for friends location
                    LatLng friendLocation = new LatLng(Double.parseDouble(String.valueOf(tracking.getLat())),
                            Double.parseDouble(String.valueOf(tracking.getLng())));

                    //Create location from user coordinates
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    //Create location from friend coordinates
                    Location friend = new Location("");
                    friend.setLongitude(Double.parseDouble(String.valueOf(tracking.getLat())));
                    friend.setLongitude(Double.parseDouble(String.valueOf(tracking.getLng())));

                    //Clear all old markers
                    mMap.clear();


                    //Add friend marker on Map
                    mMap.addMarker(new MarkerOptions()
                                .position(friendLocation)
                                .title(tracking.getEmail())
                                .snippet("Distance" + new DecimalFormat("#.#").format( currentUser.distanceTo(friend)/1000)+"km")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
                }

                //Create marker for current user
                LatLng current = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(current).title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });
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
        return (rad * 180 /Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 150.0);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       client = new GoogleApiClient.Builder(this)
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
       request.setInterval(1000); //1 second

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
       LocationServices.FusedLocationApi.requestLocationUpdates(client,request,this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(location == null){
            Toast.makeText(getApplicationContext(),"Could not get Location",Toast.LENGTH_SHORT).show();
        }else
        {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);

            //Instantiate class into Geocoder
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                String str = addressList.get(0).getLocality() + ",";
                str += addressList.get(0).getCountryName();

                //displays location and name of location
                mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
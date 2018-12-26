package com.example.projectversion2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private double latitude,longitude;
    private String userId;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Marker marker;
    private LocationListener locationListener;
    private DatabaseReference dbReferenceMain;  //Reference of 'User' Node
    private DatabaseReference dbReference;      //Reference of a Particular User
    HashMap<String,Marker> tempMarker=new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        markCurrentLocation();
        initializeFirebase();
        createFirebaseUser();
        setValuesInFirebase();


        Button helpButton=(Button)findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.removeUpdates(locationListener);
                getNearbyMarkers();
            }
        });


        //getNearbyMarkers();
    }

    @Override
    protected void onPause(){
        super.onPause();
        //dbReferenceMain.child(userId).removeValue();
    }
    @Override
    protected void onStop() {
        super.onStop();
      locationManager.removeUpdates(locationListener);
      dbReferenceMain.child(userId).removeValue();


    }

    private void getNearbyMarkers(){

        dbReferenceMain.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot locationSnapshot:dataSnapshot.getChildren()){
                    Double tempLatitude=locationSnapshot.child("Latitude").getValue(Double.class);
                    Double tempLongitude=locationSnapshot.child("Longitude").getValue(Double.class);
                    System.out.println("Latitude Retrieved "+tempLatitude);
                    System.out.println("Longitude Retrieved "+tempLongitude);
                    MarkerOptions tempMarkerOptions=new MarkerOptions();
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    String key=locationSnapshot.getKey();

                    LatLng userLatLng=new LatLng(latitude,longitude);
                    try{
                        LatLng tempLatLng=new LatLng(tempLatitude, tempLongitude);
                        tempMarkerOptions.visible(false);
                        List<Address> tempAddresses = geocoder.getFromLocation(tempLatitude, tempLongitude, 1);
                        String result = tempAddresses.get(0).getLocality() + ":";
                        result += tempAddresses.get(0).getCountryName();
                        tempMarkerOptions.position(tempLatLng).title(result);


                        if(SphericalUtil.computeDistanceBetween(tempLatLng, tempLatLng) < 5) {
                            System.out.println("Entering:   !(tempMarker.containsKey(key))");
                            if(!(tempMarker.containsKey(key))){
                                tempMarker.put(key,mMap.addMarker(tempMarkerOptions));
                            }
                            else if (tempMarker.containsKey(key)){
                                System.out.println("Entered:   (tempMarker.containsKey(key))");
                                if(tempMarker.get(key)!=null){
                                    try{
                                        tempMarker.get(key).remove();
                                    }catch (NullPointerException e){
                                        e.printStackTrace();
                                    }
                                    tempMarker.put(key,mMap.addMarker(tempMarkerOptions));
                                }

                                else{
                                    tempMarker.put(key,mMap.addMarker(tempMarkerOptions));

                                }



                            }
                            tempMarker.get(key).setVisible(true);
                        }

                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initializeFirebase(){
        //Initializing FirebaseApp
        FirebaseApp.initializeApp(getApplicationContext());
        System.out.println("Firebase app Initialized");

        //Get reference of 'Users' Node of Firebase.
        dbReferenceMain=FirebaseDatabase.getInstance().getReference().child("Users");
        System.out.println("Firebase: Get 'Users' instance");
    }
    private void createFirebaseUser(){
        //Create New User in Firebase
        dbReference=dbReferenceMain.push();
        System.out.println("Firebase: Create new User");

        //Get UserId of Current User
        userId=dbReference.getKey();
    }
    private void setValuesInFirebase(){

        //Set(Update) Lat, Long of current User Location in Firebase
        dbReference.child("Latitude").setValue(latitude);
        dbReference.child("Longitude").setValue(longitude);
    }

    private void markCurrentLocation(){

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Get location permission
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            System.out.println("If Location Permission not granted, get it");
        }


        //Firebase
        initializeFirebase();
        createFirebaseUser();

        //Location Listener
        locationListener = new LocationListener() {

            //On Location Changed
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("Entered onLocationChanged");
                //Get Lat, Long on Location Changed
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                System.out.println("Latitude:"+latitude+"Longitude:"+longitude);


                //Firebase
                setValuesInFirebase();
                //get the location name from latitude and longitude of Current User
                Geocoder geocoder = new Geocoder(getApplicationContext());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String result = addresses.get(0).getLocality() + ":";
                    result += addresses.get(0).getCountryName();
                    System.out.println("Get Location Information 'result'");
                    LatLng latLng = new LatLng(latitude, longitude);
                    if (marker != null) {
                        marker.remove();
                        marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result));
                        mMap.setMaxZoomPreference(20);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                        System.out.println("Add Marker");
                    } else {
                        marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result));
                        mMap.setMaxZoomPreference(20);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                        System.out.println("Add Marker");
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 2, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
        }catch(SecurityException e){
            System.out.println("Yasir Java Security Exception");
        }
    }


}

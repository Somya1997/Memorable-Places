package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.memorableplaces.MainActivity.arrayAdapter;
import static com.example.memorableplaces.MainActivity.locations;
import static com.example.memorableplaces.MainActivity.places;

public class AddedLocation extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    public void centerMapONLocation(Location location,String title){
        LatLng userLoc=new LatLng(location.getLatitude(),location.getLongitude());
//        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLoc).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc,13));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapONLocation(lastKnownLocation,"Your Last Location");

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_added_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent=getIntent();
        int place=intent.getIntExtra("placeNumber",0);
        if(place==0){
            locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    LatLng userLoc=new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLoc).title("Your Location"));
                }
            };
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapONLocation(lastKnownLocation,"Your Last Location");
            }

        }
        else{
            Location placeLocation=new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(locations.get(intent.getIntExtra("placeNumber",0)).longitude);
            centerMapONLocation(placeLocation,places.get(intent.getIntExtra("placeNumber",0)));
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";
        try {
            List<Address> addresses=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            Log.i("address",addresses.get(0).toString());
            if(addresses!=null && addresses.size()>0){
                if(addresses.get(0).getAddressLine(0)!=null) {
                    address+=addresses.get(0).getAddressLine(0);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        if(address.equals("")){
            SimpleDateFormat date=new SimpleDateFormat("HH:mm yyyy-mm-dd");
            address+=date.format(new Date());
        }
        places.add(address);
        locations.add(latLng);
        arrayAdapter.notifyDataSetChanged();
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);

        try {
            ArrayList<String> latitudes= new ArrayList<>();
            ArrayList<String> longitudes= new ArrayList<>();

            for(LatLng coord:locations){
                latitudes.add(Double.toString(coord.latitude));
                longitudes.add(Double.toString(coord.longitude));
            }
            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(places)).apply();
            sharedPreferences.edit().putString("latitudes",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes",ObjectSerializer.serialize(longitudes)).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Location Added Successfully", Toast.LENGTH_SHORT).show();

        mMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
    }
}
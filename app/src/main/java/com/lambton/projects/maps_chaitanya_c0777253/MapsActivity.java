package com.lambton.projects.maps_chaitanya_c0777253;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final int REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Marker mPreviousMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkPermissions();
    }

    private void checkPermissions() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
//            startUpdateLocation();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        MarkerOptions option = getMarkerOption(latLng);
        Marker marker = mMap.addMarker(option);
        dump(mPreviousMarker);
        if(mPreviousMarker != null)
        {
            System.out.println("Adding polyline");
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(10)
                    .add(marker.getPosition(),mPreviousMarker.getPosition());
            mMap.addPolyline(polylineOptions);
        }
        mPreviousMarker = marker;
//        dump(option);
    }

    private MarkerOptions getMarkerOption(LatLng latLng) {
        String [] str = getTitleSnippet(latLng);
        System.out.println(Arrays.toString(str));
        MarkerOptions option = new MarkerOptions().position(latLng);
        if (str == null)
        {
            option.title("Unknown Location");
        }
        else
        {
            option.title(str[0]).snippet(str[1]);
        }
        return option;
    }

    private String[] getTitleSnippet(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        String [] result = null;
        try {
            Address address = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0);
            result = getFormattedAddress(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String[] getFormattedAddress(Address address) {
        StringBuilder title = new StringBuilder("");
        StringBuilder snippet = new StringBuilder("");
        if(address.getSubThoroughfare() != null)
        {
            title.append(address.getSubThoroughfare());
        }
        if(address.getThoroughfare() != null)
        {
            if(!title.equals("") || !title.equals(" "))
            {
                title.append(", ");
            }
            title.append(address.getThoroughfare());
        }
        if(address.getPostalCode() != null)
        {
            if(!title.equals("") || !title.equals(" "))
            {
                title.append(", ");
            }
            title.append(address.getPostalCode());
        }
        if(address.getLocality() != null)
        {
            snippet.append(address.getLocality());
        }
        if(address.getAdminArea() != null)
        {
            if(!snippet.equals("") || !snippet.equals(" "))
            {
                snippet.append(", ");
            }
            snippet.append(address.getAdminArea());
        }
        return new String[]{title.toString(),snippet.toString()};
    }

    private static void dump(Object o)
    {
        if(o==null)
        {
            System.out.println("dump object is null");
            return;
        }
        Field[] fields = o.getClass().getDeclaredFields();
        for (int i=0; i<fields.length; i++)
        {
            try
            {
                System.out.println(fields[i].getName() + " - " + fields[i].get(o));
            }
            catch(Exception e)
            {
//                e.printStackTrace();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission()
    {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
//                startUpdateLocation();
            }
        }
    }
}
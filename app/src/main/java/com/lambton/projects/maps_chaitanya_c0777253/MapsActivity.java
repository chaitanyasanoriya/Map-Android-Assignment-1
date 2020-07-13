package com.lambton.projects.maps_chaitanya_c0777253;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener
{

    private static final int REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM_LEVEL = 10.0f;
    private final static int POLYGON_SIDES = 4;

    private GoogleMap mMap;
    private Marker mPreviousMarker;
    private List<Marker> mMarkerList = new ArrayList<>();
    private List<Polyline> mPolylineList = new ArrayList<>();
    private Marker mInfoMarker;
    private LocationManager mLocationManager;
    private Polygon mPolygon;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
        {
            mapFragment.getMapAsync(this);
        }
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        checkPermissions();
    }

    private void checkPermissions()
    {
        if (!hasLocationPermission())
        {
            requestLocationPermission();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        setMapOnClickListeners();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestLocationPermission();
            return;
        }
        enableUserLocationAndZoom();
    }

    private void setMapOnClickListeners()
    {
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        if (mMarkerList.size() == POLYGON_SIDES)
        {
            Marker marker = mMarkerList.remove(0);
            marker.remove();
            removePolyline(marker);
            removePolygon();
        }
        MarkerOptions option = getMarkerOption(latLng);
        Marker marker = mMap.addMarker(option);
        drawPolyline(marker);
        setTitleSnippet(latLng, marker);
        mPreviousMarker = marker;
        mMarkerList.add(marker);
        drawPolygon();
    }

    private void drawPolygon()
    {
        if (mMarkerList.size() == POLYGON_SIDES)
        {
            PolygonOptions option = new PolygonOptions();
            List<LatLng> latLngList = new ArrayList<>();
            Marker marker;
            for (int i = 0; i < 4; i++)
            {
                marker = mMarkerList.get(i);
                setText(marker, i);
                latLngList.add(marker.getPosition());
            }
            removePolylines();
            latLngList = Utils.orderRectCorners(latLngList);
            addPolyLines(latLngList);
            for (LatLng latLng : latLngList)
            {
                option.add(latLng);
            }
//            option.strokeColor(Color.RED);
            option.fillColor(Color.argb(89, 0, 255, 0));
            mPolygon = mMap.addPolygon(option);
            mPolygon.setClickable(true);
        }
    }

    private void addPolyLines(List<LatLng> latLngList)
    {
        for (int i = 0; i < 4; i++)
        {
            if (i == 0)
            {
                drawPolyline(latLngList.get(3), latLngList.get(i));
            } else
            {
                drawPolyline(latLngList.get(i), latLngList.get(i - 1));
            }
        }
    }

    private void drawPolyline(LatLng latLng, LatLng latLng1)
    {
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(20)
                .add(latLng, latLng1);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polyline.setClickable(true);
        mPolylineList.add(polyline);
    }

    private void removePolylines()
    {
        for (Polyline polyline : mPolylineList)
        {
            polyline.remove();
        }
        mPolylineList.clear();
    }

    private void setText(Marker marker, int i)
    {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(150, 173, conf);
        Canvas canvas1 = new Canvas(bmp);

        Paint color = new Paint();
        color.setTextSize(58);
        color.setColor(Color.BLACK);

        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.mipmap.marker), 0, 0, color);
        canvas1.drawText(String.valueOf((char) (i + 65)), 105, 86, color);

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
        marker.setAnchor(0.32f, 0.88f);
    }

    private void drawPolyline(Marker marker)
    {
        if (mPreviousMarker != null)
        {
            System.out.println("Adding polyline");
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(20)
                    .add(marker.getPosition(), mPreviousMarker.getPosition());
            mPolylineList.add(mMap.addPolyline(polylineOptions));
        }
    }

    private MarkerOptions getMarkerOption(LatLng latLng)
    {
        MarkerOptions option = new MarkerOptions().position(latLng);
        option.icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker));
        option.draggable(true);
        option.anchor(0.4f, 1);
        return option;
    }


    private void setTitleSnippet(final LatLng latLng, final Marker marker)
    {
        new Thread(() ->
        {
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            String[] result = null;
            try
            {
                Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                result = Utils.getFormattedAddress(address);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            if (result == null)
            {
                MapsActivity.this.runOnUiThread(() -> marker.setTitle("Unknown Location"));
            } else
            {
                System.out.println("Adding Title");
                final String[] finalResult = result;
                MapsActivity.this.runOnUiThread(() ->
                {
                    marker.setTitle(finalResult[0]);
                    marker.setSnippet(finalResult[1]);
                });
            }
        }).start();
    }

    private void requestLocationPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission()
    {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (mMap != null)
                {
                    enableUserLocationAndZoom();
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocationAndZoom()
    {
        mMap.setMyLocationEnabled(true);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM_LEVEL));
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline)
    {
        System.out.println("clicked");
        Toast.makeText(this, "hello world", Toast.LENGTH_SHORT).show();
        LatLng place1 = polyline.getPoints().get(0);
        LatLng place2 = polyline.getPoints().get(1);
        LatLng mid_point = Utils.midPoint(place1.latitude, place1.longitude, place2.latitude, place2.longitude);
        double distance = Utils.distance(place1.latitude, place1.longitude, place2.latitude, place2.longitude);
        showDistanceMarker(mid_point, distance, null);
    }

    private void showDistanceMarker(LatLng latLng, double distance, String snippet)
    {
        if (mInfoMarker != null)
        {
            mInfoMarker.remove();
        }
        BitmapDescriptor transparent = BitmapDescriptorFactory.fromResource(R.mipmap.transparent);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(String.format(Locale.CANADA,"%.2f Km", distance))
                .snippet(snippet)
                .icon(transparent)
                .anchor((float) 0.5, (float) 0.5); //puts the info window on the polyline

        mInfoMarker = mMap.addMarker(options);
        mInfoMarker.showInfoWindow();
    }

    @Override
    public void onPolygonClick(Polygon polygon)
    {
        System.out.println("polygon: "+ mPolylineList.size());
        double distance = 0;
        LatLng latLng = null;
        LatLng mid_point = null;
        for (LatLng latLng1 : polygon.getPoints())
        {
            if (latLng != null)
            {
                distance += Utils.distance(latLng.latitude, latLng.longitude, latLng1.latitude, latLng1.longitude);
                mid_point = Utils.midPoint(latLng.latitude, latLng.longitude, latLng1.latitude, latLng1.longitude);
            }
            latLng = latLng1;
        }
        showDistanceMarker(mid_point, distance, "A - B - C - D");
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        for (Marker marker : mMarkerList)
        {
            if (Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05)
            {
                Toast.makeText(MapsActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show(); //do some stuff
                marker.remove();
                mMarkerList.remove(marker);
                removePolyline(marker);
                removePolygon();
                break;
            }
        }
        if(mMarkerList.size()>0)
        {
            mPreviousMarker = mMarkerList.get(mMarkerList.size()-1);
        }
        else
        {
            mPreviousMarker = null;
        }
    }

    private void removePolygon()
    {
        if (mPolygon != null)
        {
            mPolygon.remove();
        }
        mPolygon = null;
    }

    private void removePolyline(Marker marker)
    {
        for (Polyline polyline : mPolylineList)
        {
            if (polyline.getPoints().get(0).equals(marker.getPosition()) || polyline.getPoints().get(1).equals(marker.getPosition()))
            {
                polyline.remove();
                mPolylineList.remove(polyline);
                removePolyline(marker);
                break;
            }
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {
    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        System.out.println(marker.getPosition());
        setTitleSnippet(marker.getPosition(), marker);
        removePolylines();
        drawAllPolylines();
        removePolygon();
        drawPolygon();
    }

    private void drawAllPolylines()
    {
        Marker marker = null;
        for (Marker marker1 : mMarkerList)
        {
            if (marker != null)
            {
                drawPolyline(marker.getPosition(), marker1.getPosition());
            }
            marker = marker1;
        }
    }
}
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMapLongClickListener {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        enableUserLocationAndZoom();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mMarkerList.size() < POLYGON_SIDES) {
            MarkerOptions option = getMarkerOption(latLng);
            Marker marker = mMap.addMarker(option);
            marker.setAnchor(0.4f, 1);
            drawPolyline(marker);
            setTitleSnippet(latLng, marker);
            mPreviousMarker = marker;
            mMarkerList.add(marker);
            drawPolygon();
        }
    }

    private void drawPolygon() {
        if (mMarkerList.size() == POLYGON_SIDES) {
            PolygonOptions option = new PolygonOptions();
            List<LatLng> latLngList = new ArrayList<>();
//            LatLng [] latLngs = new LatLng[]{mMarkerList.get(0).getPosition(), mMarkerList.get(1).getPosition(), mMarkerList.get(2).getPosition(), mMarkerList.get(3).getPosition()};
            Marker marker;
            for (int i = 0; i < 4; i++) {
                marker = mMarkerList.get(i);
                setText(marker, i);
                latLngList.add(marker.getPosition());
//                option.add(marker.getPosition());
            }
            removePolylines();
//            for(Marker marker: mMarkerList)
//            {
//                latLngList.add(marker.getPosition());
//            }
            latLngList = orderRectCorners(latLngList);
            addPolyLines(latLngList);
            for (LatLng latLng : latLngList) {
                option.add(latLng);
            }
            option.strokeColor(Color.RED);
            option.fillColor(Color.argb(89, 0, 255, 0));
            mPolygon = mMap.addPolygon(option);
            mPolygon.setClickable(true);
        }
    }

    private void addPolyLines(List<LatLng> latLngList) {
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                drawPolyline(latLngList.get(3), latLngList.get(i));
            } else {
                drawPolyline(latLngList.get(i), latLngList.get(i - 1));
            }
        }
    }

    private void drawPolyline(LatLng latLng, LatLng latLng1) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(20)
                .add(latLng, latLng1);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polyline.setClickable(true);
        mPolylineList.add(polyline);
    }

    private void removePolylines() {
        for (Polyline polyline : mPolylineList) {
            polyline.remove();
        }
        mPolylineList.clear();
    }

    List<LatLng> orderRectCorners(List<LatLng> corners) {

        List<LatLng> ordCorners = orderPointsByRows(corners);

        if (ordCorners.get(0).latitude > ordCorners.get(1).latitude) { // swap points
            LatLng tmp = ordCorners.get(0);
            ordCorners.set(0, ordCorners.get(1));
            ordCorners.set(1, tmp);
        }

        if (ordCorners.get(2).latitude < ordCorners.get(3).latitude) { // swap points
            LatLng tmp = ordCorners.get(2);
            ordCorners.set(2, ordCorners.get(3));
            ordCorners.set(3, tmp);
        }
        return ordCorners;
    }

    List<LatLng> orderPointsByRows(List<LatLng> points) {
        Collections.sort(points, new Comparator<LatLng>() {
            public int compare(LatLng p1, LatLng p2) {
                if (p1.longitude < p2.longitude) return -1;
                if (p1.longitude > p2.longitude) return 1;
                return 0;
            }
        });
        return points;
    }

    private void setText(Marker marker, int i) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(150, 173, conf);
        Canvas canvas1 = new Canvas(bmp);

// paint defines the text color, stroke width and size
        Paint color = new Paint();
        color.setTextSize(58);
        color.setColor(Color.BLACK);

// modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.mipmap.marker), 0, 0, color);
        canvas1.drawText(String.valueOf((char) (i + 65)), 105, 86, color);

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
//        marker.setAnchor(0.5f,1);
        marker.setAnchor(0.32f, 0.88f);
    }

    private void drawPolyline(Marker marker) {
        if (mPreviousMarker != null) {
            System.out.println("Adding polyline");
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(20)
                    .add(marker.getPosition(), mPreviousMarker.getPosition());
            mPolylineList.add(mMap.addPolyline(polylineOptions));
        }
    }

    private MarkerOptions getMarkerOption(LatLng latLng) {
        MarkerOptions option = new MarkerOptions().position(latLng);
        option.icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker));
        return option;
    }


    private void setTitleSnippet(final LatLng latLng, final Marker marker) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            String[] result = null;
            try {
                Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                result = getFormattedAddress(address);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result == null) {
                MapsActivity.this.runOnUiThread(() -> marker.setTitle("Unknown Location"));
            } else {
                System.out.println("Adding Title");
                final String[] finalResult = result;
                MapsActivity.this.runOnUiThread(() -> {
                    marker.setTitle(finalResult[0]);
                    marker.setSnippet(finalResult[1]);
                });
            }
        }).start();
    }

    private String[] getFormattedAddress(Address address) {
        StringBuilder title = new StringBuilder("");
        StringBuilder snippet = new StringBuilder("");
        if (address.getSubThoroughfare() != null) {
            title.append(address.getSubThoroughfare());
        }
        if (address.getThoroughfare() != null) {
            if (!title.equals("") || !title.equals(" ")) {
                title.append(", ");
            }
            title.append(address.getThoroughfare());
        }
        if (address.getPostalCode() != null) {
            if (!title.equals("") || !title.equals(" ")) {
                title.append(", ");
            }
            title.append(address.getPostalCode());
        }
        if (address.getLocality() != null) {
            snippet.append(address.getLocality());
        }
        if (address.getAdminArea() != null) {
            if (!snippet.equals("") || !snippet.equals(" ")) {
                snippet.append(", ");
            }
            snippet.append(address.getAdminArea());
        }
        return new String[]{title.toString(), snippet.toString()};
    }

    private static void dump(Object o) {
        if (o == null) {
            System.out.println("dump object is null");
            return;
        }
        Field[] fields = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                System.out.println(fields[i].getName() + " - " + fields[i].get(o));
            } catch (Exception e) {
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    enableUserLocationAndZoom();
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocationAndZoom() {
        mMap.setMyLocationEnabled(true);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM_LEVEL));
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        System.out.println("clicked");
        Toast.makeText(this, "hello world", Toast.LENGTH_SHORT).show();
        LatLng place1 = polyline.getPoints().get(0);
        LatLng place2 = polyline.getPoints().get(1);
        LatLng mid_point = midPoint(place1.latitude,place1.longitude,place2.latitude,place2.longitude);
        double distance = distance(place1.latitude,place1.longitude,place2.latitude,place2.longitude);
        showDistanceMarker(mid_point, distance,null);
    }

    private void showDistanceMarker(LatLng latLng, double distance, String snippet) {
        if(mInfoMarker != null)
        {
            mInfoMarker.remove();
        }
        BitmapDescriptor transparent = BitmapDescriptorFactory.fromResource(R.mipmap.transparent);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(String.format("%.2f Km",distance))
                .snippet(snippet)
                .icon(transparent)
                .anchor((float) 0.5, (float) 0.5); //puts the info window on the polyline

        mInfoMarker = mMap.addMarker(options);
        mInfoMarker.showInfoWindow();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public LatLng midPoint(double lat1,double lon1,double lat2,double lon2){

        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        //print out in degrees
        System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
        return new LatLng(Math.toDegrees(lat3),Math.toDegrees(lon3));
    }


    @Override
    public void onPolygonClick(Polygon polygon) {
        System.out.println("polygon");
        double distance = 0;
        LatLng latLng = null;
        LatLng mid_point = null;
        for(LatLng latLng1: polygon.getPoints())
        {
            if(latLng!=null)
            {
                distance += distance(latLng.latitude,latLng.longitude,latLng1.latitude,latLng1.longitude);
                mid_point = midPoint(latLng.latitude,latLng.longitude,latLng1.latitude,latLng1.longitude);
            }
            latLng = latLng1;
        }
        showDistanceMarker(mid_point,distance,"A - B - C - D");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        for(Marker marker : mMarkerList) {
            if(Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05) {
                Toast.makeText(MapsActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show(); //do some stuff
                marker.remove();
                mMarkerList.remove(marker);
                removePolyline(marker);
                removePolygon();
                break;
            }
        }
    }

    private void removePolygon() {
        if(mPolygon != null)
        {
            mPolygon.remove();
        }
    }

    private void removePolyline(Marker marker) {
        for(Polyline polyline: mPolylineList)
        {
            if(polyline.getPoints().get(0).equals(marker.getPosition()) || polyline.getPoints().get(1).equals(marker.getPosition()))
            {
                polyline.remove();
                mPolylineList.remove(polyline);
                removePolyline(marker);
                break;
            }
        }
    }
}
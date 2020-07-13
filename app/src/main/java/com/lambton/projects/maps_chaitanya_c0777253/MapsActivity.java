package com.lambton.projects.maps_chaitanya_c0777253;

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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener
{

    private static final int REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM_LEVEL = 10.0f;
    private final static int POLYGON_SIDES = 4;

    private GoogleMap mMap;
    private Marker mPreviousMarker;
    private final List<Marker> mMarkerList = new ArrayList<>();
    private final List<Polyline> mPolylineList = new ArrayList<>();
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

    /**
     * Method that checks User Location access and request permission
     */
    private void checkPermissions()
    {
        if (!hasLocationPermission())
        {
            requestLocationPermission();
        }
    }

    /**
     * Method that is called when Map is Ready
     * @param googleMap - Object of the Google Maps
     */
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

    /**
     * Setting the Click Listeners for Google Map
     */
    private void setMapOnClickListeners()
    {
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    /**
     * Method Called when the Map is clicked
     * @param latLng - Latitude and Longitude of Point Clicked
     */
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

    /**
     * Method to draw Polygon on the map
     */
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
            option.fillColor(Color.argb(89, 0, 255, 0));
            mPolygon = mMap.addPolygon(option);
            mPolygon.setClickable(true);
        }
    }

    /**
     * Method to drawn Polylines on Map
     * @param latLngList - List of LatLng Objects
     */
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

    /**
     * Method to draw Polyline between two LatLng Objects
     * @param latLng - First set of latitude and longitude
     * @param latLng1 - Second set of latitude and longitude
     */
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

    /**
     * Method to remove all the Polylines from the Map and List
     */
    private void removePolylines()
    {
        for (Polyline polyline : mPolylineList)
        {
            polyline.remove();
        }
        mPolylineList.clear();
    }

    /**
     * Method to add A B C D text next to marker
     * @param marker - Marker to add character beside
     * @param i - Index of the Marker
     */
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

    /**
     * Method to draw Polyline between the newly added Marker and previously added Marker
     * @param marker - the new Marker
     */
    private void drawPolyline(Marker marker)
    {
        if (mPreviousMarker != null)
        {

            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(20)
                    .add(marker.getPosition(), mPreviousMarker.getPosition());
            mPolylineList.add(mMap.addPolyline(polylineOptions));
        }
    }

    /**
     * Method that creates a MarkerOptions and returns
     * @param latLng - Position of the Marker
     * @return - a newly created MarkerOptions object with passed Position
     */
    private MarkerOptions getMarkerOption(LatLng latLng)
    {
        MarkerOptions option = new MarkerOptions().position(latLng);
        option.icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker));
        option.draggable(true);
        option.anchor(0.4f, 1);
        return option;
    }

    /**
     * Method to Asynchronously add Title and Snippet to Marker
     * @param latLng - Location of the Marker
     * @param marker - Marker object to add title and snippet to
     */
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

                final String[] finalResult = result;
                MapsActivity.this.runOnUiThread(() ->
                {
                    marker.setTitle(finalResult[0]);
                    marker.setSnippet(finalResult[1]);
                });
            }
        }).start();
    }

    /**
     * Method to request Location Permission
     */
    private void requestLocationPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    /**
     * Method to check if the app has User Location Permission
     * @return - True if the app has User Location Permission
     */
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

    /**
     * Method to show User Location on Map and Zoom onto it
     */
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

    /**
     * Method Called when a Polyline is clicked. Presents a Marker between the two points of Polyline with the distance between them
     * @param polyline - Polyline that is clicked
     */
    @Override
    public void onPolylineClick(Polyline polyline)
    {
        LatLng place1 = polyline.getPoints().get(0);
        LatLng place2 = polyline.getPoints().get(1);
        LatLng mid_point = Utils.midPoint(place1.latitude, place1.longitude, place2.latitude, place2.longitude);
        double distance = Utils.distance(place1.latitude, place1.longitude, place2.latitude, place2.longitude);
        showDistanceMarker(mid_point, distance, null);
    }

    /**
     * Method to show Marker at Location will distance and snippet
     * @param latLng - Location of the Marker
     * @param distance - Distance to be displayed
     * @param snippet - Snippet to be displayed
     */
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

    /**
     * Method called when polygon is clicked. Shows a Marker in the middle of the polygon with A to B to C to D distance
     * @param polygon - Polygon that is clicked
     */
    @Override
    public void onPolygonClick(Polygon polygon)
    {

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

    /**
     * Method called when Map is Long clicked. Checks if Long click is near a marker, and removes it
     * @param latLng - LatLng of the location where Long Pressed
     */
    @Override
    public void onMapLongClick(LatLng latLng)
    {
        for (Marker marker : mMarkerList)
        {
            if (Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05)
            {
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

    /**
     * Method to remove displayed polygon
     */
    private void removePolygon()
    {
        if (mPolygon != null)
        {
            mPolygon.remove();
        }
        mPolygon = null;
    }

    /**
     * Method to remove Polyline associated with a marker
     * @param marker - Marker whose associated Polylines need to be removed
     */
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

    /**
     * Method call when a marker has started dragging
     * @param marker - which has started dragging
     */
    @Override
    public void onMarkerDragStart(Marker marker)
    {
    }

    /**
     * Method call when a marker is dragging
     * @param marker - Marker being dragged
     */
    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    /**
     * Method call when Marker has stopped dragging. Redraws Polylines and Polygon and Asynchronously adds Title and Snippet
     * @param marker - Marker that has stopped dragging
     */
    @Override
    public void onMarkerDragEnd(Marker marker)
    {

        setTitleSnippet(marker.getPosition(), marker);
        removePolylines();
        drawAllPolylines();
        removePolygon();
        drawPolygon();
    }

    /**
     * Method that draws all the Polylines
     */
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
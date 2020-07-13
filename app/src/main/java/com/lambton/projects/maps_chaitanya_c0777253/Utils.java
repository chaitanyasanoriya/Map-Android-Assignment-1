package com.lambton.projects.maps_chaitanya_c0777253;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils
{
    public static List<LatLng> orderRectCorners(List<LatLng> corners)
    {

        List<LatLng> ordCorners = orderPointsByRows(corners);

        if (ordCorners.get(0).latitude > ordCorners.get(1).latitude)
        {
            LatLng tmp = ordCorners.get(0);
            ordCorners.set(0, ordCorners.get(1));
            ordCorners.set(1, tmp);
        }

        if (ordCorners.get(2).latitude < ordCorners.get(3).latitude)
        {
            LatLng tmp = ordCorners.get(2);
            ordCorners.set(2, ordCorners.get(3));
            ordCorners.set(3, tmp);
        }
        return ordCorners;
    }

    private static List<LatLng> orderPointsByRows(List<LatLng> points)
    {
        Collections.sort(points, (p1, p2) -> Double.compare(p1.longitude,p2.longitude));
        return points;
    }

    public static String[] getFormattedAddress(Address address)
    {
        StringBuilder title = new StringBuilder();
        StringBuilder snippet = new StringBuilder();
        if (address.getSubThoroughfare() != null)
        {
            title.append(address.getSubThoroughfare());
        }
        if (address.getThoroughfare() != null)
        {
            if (!title.toString().equals("") || !title.toString().equals(" "))
            {
                title.append(", ");
            }
            title.append(address.getThoroughfare());
        }
        if (address.getPostalCode() != null)
        {
            if (!title.toString().equals("") || !title.toString().equals(" "))
            {
                title.append(", ");
            }
            title.append(address.getPostalCode());
        }
        if (address.getLocality() != null)
        {
            snippet.append(address.getLocality());
        }
        if (address.getAdminArea() != null)
        {
            if (!snippet.toString().equals("") || !snippet.toString().equals(" "))
            {
                snippet.append(", ");
            }
            snippet.append(address.getAdminArea());
        }
        return new String[]{title.toString(), snippet.toString()};
    }

    public static void dump(Object o)
    {
        if (o == null)
        {
            System.out.println("dump object is null");
            return;
        }
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            try
            {
                System.out.println(field.getName() + " - " + field.get(o));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2)
    {
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

    private static double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }

    public static LatLng midPoint(double lat1, double lon1, double lat2, double lon2)
    {

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
        return new LatLng(Math.toDegrees(lat3), Math.toDegrees(lon3));
    }
}

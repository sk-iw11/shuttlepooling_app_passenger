package com.example.trynumbertwo;

import android.content.res.Resources;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutePolyLines {

    private static final Map<String, List<LatLng>> polyLines = new HashMap<>();

    private static boolean loaded = false;

    public static synchronized void load(Resources resources) {
        if (loaded)
            return;
        loadPolyLine(resources, "skoltech_technopark", R.raw.skoltech_technopark);
        loadPolyLine(resources, "technopark_nobel_street", R.raw.technopark_nobel);
        loadPolyLine(resources, "technopark_usadba", R.raw.technopark_usadba);
        loadPolyLine(resources, "technopark_parking", R.raw.technopark_parking);
        loaded = true;
    }

    public static synchronized boolean isLoaded() {
        return loaded;
    }

    public static List<LatLng> getPolyLine(String departure, String destination) {
        String name = departure + "_" + destination;
        if (polyLines.containsKey(name))
            return polyLines.get(name);
        name = destination + "_" + departure;
        return polyLines.get(name);
    }

    private static void loadPolyLine(Resources resources, String name, int id) {
        String line = null;
        List<LatLng> polyLine = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resources.openRawResource(id)))) {
            while ((line = reader.readLine()) != null) {
                String[] coords = line.split(";");
                polyLine.add(new LatLng(Double.valueOf(coords[0]), Double.valueOf(coords[1])));
            }
        } catch (IOException e) {
            polyLines.put(name, new ArrayList<LatLng>());
            return;
        }
        polyLines.put(name, polyLine);
    }

}

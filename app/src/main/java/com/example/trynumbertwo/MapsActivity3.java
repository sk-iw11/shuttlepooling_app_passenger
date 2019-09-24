package com.example.trynumbertwo;

import androidx.fragment.app.FragmentActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity3 extends FragmentActivity implements OnMapReadyCallback {
    ProgressDialog pd;
    private Handler fHandler = new Handler();
    private GoogleMap mMap;
    private Button button_cancel;

    private Map<String, LatLng> coordinateMap = new HashMap<>();

    private TextView busLabelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coordinateMap.put("skoltech", new LatLng(55.698128, 37.359803));
        coordinateMap.put("technopark", new LatLng(55.690135, 37.348110));
        coordinateMap.put("parking", new LatLng(55.687824, 37.354213));
        coordinateMap.put("usadba", new LatLng(55.687925, 37.345907));
        coordinateMap.put("nobel_street", new LatLng(55.684283, 37.341396));

        setContentView(R.layout.activity_maps3);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        busLabelView = findViewById(R.id.bus_label_text_view);
        button_cancel = findViewById(R.id.button_cancel);
        // При нажатии "Cancel"
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(MapsActivity3.this, "", "Cancelling your ride...",
                        true);
                pd.show();
                fHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                }, 1500);

                fHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 3s = 3000ms
                        Toast.makeText(getApplicationContext(), "Bus order cancelled", Toast.LENGTH_SHORT).show();
                    }
                }, 1700);
                Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(activity2Intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent activity2Intent = getIntent();
        String start_point = activity2Intent.getStringExtra("start_point");
        String finish_point = activity2Intent.getStringExtra("finish_point");
        LatLng start_p=coordinateMap.get(start_point);
        LatLng fin_p=coordinateMap.get(finish_point);

        // Add a marker in Sydney and move the camera
        float zoomLevel = 15.0f; //This goes up to 21
        mMap.addMarker(new MarkerOptions().position(start_p).title(start_point));
        mMap.addMarker(new MarkerOptions().position(fin_p).title(finish_point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start_p,zoomLevel));

        busLabelView.setText("Waiting for bus: " + activity2Intent.getStringExtra("bus_label"));

        String[] route = activity2Intent.getStringExtra("route").split(";");
        for (int i = 0; i < route.length - 1; i++) {
            String currentStation = route[i];
            String nextStation = route[i+1];
            googleMap.addPolyline(new PolylineOptions().clickable(false).color(0xf542f200)
                    .addAll(RoutePolyLines.getPolyLine(currentStation, nextStation)));
            if (finish_point.equals(nextStation))
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
    }
}

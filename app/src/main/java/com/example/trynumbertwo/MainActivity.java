package com.example.trynumbertwo;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.Bundle;

import com.example.trynumbertwo.network.RestServiceFactory;
import com.example.trynumbertwo.network.model.AssignedBus;
import com.example.trynumbertwo.network.model.BusDemand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final Logger LOG = Logger.getLogger(MainActivity.class.getName());


    ProgressDialog pd;
    private Handler mHandler = new Handler();
    String[] bus_stops_data_1 = {"Skoltech","Techno Park","Parking Skolkovskaya","Usadba","Nobel St."}; // Bus Stops Variable
    String[] bus_stops_data_2 = {"Skoltech","Techno Park","Parking Skolkovskaya","Usadba","Nobel St."}; // Bus Stops Variable

    private static final Map<String, String> namesMap;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    static {
        namesMap = new HashMap<>();
        namesMap.put("Skoltech", "skoltech");
        namesMap.put("Techno Park", "technopark");
        namesMap.put("Parking Skolkovskaya", "parking");
        namesMap.put("Usadba", "usadba");
        namesMap.put("Nobel St.", "nobel_street");
    }

    private Spinner spinner1;
    private Spinner spinner2;

    private Button button_call;
    private Button button_can;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoutePolyLines.load(this.getResources());

        setContentView(R.layout.activity_main);

        spinner1 = findViewById(R.id.spinner_current_location);
        spinner2 = findViewById(R.id.spinner_destination);

        button_can=findViewById(R.id.button_cancel_old);
        button_call = findViewById(R.id.button_calculate);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bus_stops_data_1);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bus_stops_data_2);

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);
        int spinner2Position = adapter2.getPosition("Techno Park");
        spinner2.setSelection(spinner2Position); // выставляет на второй спинер остановку "техно-парк"

        // при нажатии "Order bus"
        button_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(MainActivity.this, "", "Ordering...",
                        true);
                pd.show();

                String dep = namesMap.get(spinner1.getSelectedItem().toString());
                String dest = namesMap.get(spinner2.getSelectedItem().toString());
                Call<Void> postDemand = RestServiceFactory.getApiService().postDemand(new BusDemand(dep, dest));
                postDemand.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.code() == 200)
                            LOG.info("Have a route");
                        else
                            LOG.info("Accepted for proceed");

                        button_can.setEnabled(true);
                        button_can.setVisibility(View.VISIBLE);
                        button_call.setEnabled(false);
                        button_call.setVisibility(View.INVISIBLE);
                        spinner1.setEnabled(false);
                        spinner2.setEnabled(false);

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                polling();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        LOG.info(call.request().body().toString());
                        LOG.info(t.getMessage());
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Networking error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // При нажатии "Cancel"
        button_can.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(MainActivity.this, "", "Cancelling your ride...",
                        true);
                pd.show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                }, 1500);

                //button_can.setEnabled(false);
                //button_can.setVisibility(View.INVISIBLE);
                button_call.setEnabled(true);
                button_call.setVisibility(View.VISIBLE);
                spinner1.setEnabled(true);
                spinner2.setEnabled(true);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 3s = 3000ms
                        Toast.makeText(getApplicationContext(), "Bus order cancelled", Toast.LENGTH_SHORT).show();
                    }
                }, 1700);
            }
        });

        OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text1 = spinner1.getSelectedItem().toString();
                String text2 = spinner2.getSelectedItem().toString();
                if(text1.equals(text2)){
                    button_call.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "You picked same bus-stops. Please, change your choice.", Toast.LENGTH_SHORT).show();
                } else {
                    button_call.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner1.setOnItemSelectedListener(itemSelectedListener);
        spinner2.setOnItemSelectedListener(itemSelectedListener);
    }

    private void polling() {
        String dep = namesMap.get(spinner1.getSelectedItem().toString());
        String dest = namesMap.get(spinner2.getSelectedItem().toString());

        while (true) {
            Call<AssignedBus> getBus = RestServiceFactory.getApiService().getBus(dep, dest);
            Response<AssignedBus> response = null;
            try {
                response = getBus.execute();
            } catch (IOException e) {
                LOG.info(e.getMessage());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Networking error", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }

            if (response.code() == 404) {
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            if (response.code() != 200) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }

            pd.dismiss();

            StringBuilder builder = new StringBuilder();
            Iterator<String> it = response.body().getRoute().iterator();
            builder.append(it.next());
            while (it.hasNext()) {
                builder.append(";" + it.next());
            }

            final Intent activity2Intent = new Intent(getApplicationContext(), MapsActivity3.class);
            activity2Intent.putExtra("start_point", dep);
            activity2Intent.putExtra("finish_point", dest);
            activity2Intent.putExtra("bus_label", response.body().getName());
            activity2Intent.putExtra("route", builder.toString());

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startActivity(activity2Intent);
                }
            });

            return;
        }

        pd.dismiss();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                button_call.setEnabled(true);
                button_call.setVisibility(View.VISIBLE);
                spinner1.setEnabled(true);
                spinner2.setEnabled(true);
            }
        });

    }
}
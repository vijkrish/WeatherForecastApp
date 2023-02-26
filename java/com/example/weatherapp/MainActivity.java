package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String LAT_TEXT = "weatherApp.latitude";
    public static final String LON_TEXT = "weatherApp.longitude";
    private FusedLocationProviderClient client;

    private RequestQueue mQueue;
    private String locationAddress;
    private Double latitude;
    private Double longitude;

    private String serverAddress = "http://vijaysai-hw8.us-east-2.elasticbeanstalk.com/";

    private void startProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        TextView textView = (TextView) findViewById(R.id.progressBarTitle);
        textView.setVisibility(View.GONE);
    }

    String current_city;
    /* TODO: Complete this method */
    private void setLatLong() {
        latitude = 34.0522;
        longitude = -118.2437;
        current_city = "Los Angeles, CA, USA";

        startProgressBar();
        String url = "http://ip-api.com/json/?";
        Log.d(MainActivity.class.getSimpleName(), url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                             Log.d(MainActivity.class.getSimpleName(), "Got resp");
                            latitude = response.getDouble("lat");
                            longitude = response.getDouble("lon");
                            current_city = response.getString("city") + ", "+ response.getString("region")
                                    + ", " + response.getString("country");
                        } catch (JSONException error) {
                            error.printStackTrace();
                        }
                        getCurrentWeather(latitude, longitude, current_city);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );

        mQueue.add(request);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(MainActivity.class.getSimpleName(),"No permission for location services");
            return;
        }

        mQueue = Volley.newRequestQueue(this);
        setLatLong();
    }

    private void handleWeatherResponse(JSONObject response) {
        stopProgressBar();
        try {
            response.put("current_location", locationAddress);
        } catch (JSONException error) {
            error.printStackTrace();
        }
        Intent intent = new Intent(MainActivity.this, FavoriteTabActivity.class);
        intent.putExtra("JSON_OBJ", response.toString());
        startActivity(intent);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private String convertToUrl(String url) {
        url = url.trim();
        url = url.replaceAll("\\s", "+");
        return url;
    }

    private void getCurrentWeather(Double lat, Double lon, String current_city) {
        String url = serverAddress + "getWeatherInfo/" +
                lat.toString() + "/" + lon.toString() + "/" + current_city;

        url = convertToUrl(url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    handleWeatherResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }
        );

        mQueue.add(request);
    }

}

package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchResult extends AppCompatActivity {

    private HashMap<String, Integer> map = new HashMap<>();
    private String weatherString;
    private String location_address;
    private String temperature;
    public JSONObject weatherObj;
    private Toolbar toolbar;

    private static final String SHARED_PREFS = "weather_pref";

    String serverAddress = "http://vijaysai-hw8.us-east-2.elasticbeanstalk.com/";
//    private String serverAddress = "http://neuroimagetest.usc.edu:8081/";

    private void setHashValues() {
        map.put("clear-day", R.mipmap.weather_sunny_clear);
        map.put("clear-night", R.mipmap.weather_night);
        map.put("rain", R.mipmap.weather_rainy);
        map.put("sleet", R.mipmap.weather_snowy_rainy);
        map.put("snow", R.mipmap.weather_snowy);
        map.put("wind", R.mipmap.weather_windy_variant);
        map.put("fog", R.mipmap.weather_fog);
        map.put("cloudy", R.mipmap.weather_cloudy);
        map.put("partly-cloudy-night", R.mipmap.weather_night_partly_cloudy);
        map.put("partly-cloudy-day", R.mipmap.weather_partly_cloudy);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isFavoriteCity(String city) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String json_string = sharedPreferences.getString(city, null);

        if (json_string == null)
            return false;

        return true;
    }

    private void addToFavorites(String city) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(city, weatherString);
        editor.apply();

        Log.d(SearchResult.class.getSimpleName(), Boolean.toString(isFavoriteCity(city)));

        ImageView img = findViewById(R.id.favBtn);
        img.setImageResource(R.mipmap.map_marker_minus_foreground);

        Toast.makeText(SearchResult.this, location_address + " was added to favorites", Toast.LENGTH_LONG).show();
    }

    private void removeFromFavorites(String city) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(city);
        editor.apply();

        Log.d(SearchResult.class.getSimpleName(), Boolean.toString(isFavoriteCity(city)));

        ImageView img = findViewById(R.id.favBtn);
        img.setImageResource(R.mipmap.map_marker_plus_foreground);

        Toast.makeText(SearchResult.this, location_address + " was removed from favorites", Toast.LENGTH_LONG).show();
    }

    public void handleFavoriteButtonClick(View view) {
        if (isFavoriteCity(location_address)) {
            removeFromFavorites(location_address);
        } else {
            addToFavorites(location_address);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);


        JSONObject dailyObj;
        Intent intent = getIntent();
        weatherString = intent.getStringExtra("JSON_OBJ");

        try {
            weatherObj = new JSONObject(weatherString);
            dailyObj = weatherObj.getJSONObject("currently");
            Long temperature_val =  Math.round(dailyObj.getDouble("temperature"));
            temperature = temperature_val.toString() + "\u2109";
            location_address = convertFromUrlToText(weatherObj.getString("location_address"));
        } catch (JSONException error) {
            error.printStackTrace();
            location_address = "";
            dailyObj = null;
        }

        if (!isFavoriteCity(location_address)) {
            ImageView img = findViewById(R.id.favBtn);
            img.setImageResource(R.mipmap.map_marker_plus_foreground);
        } else {
            ImageView img = findViewById(R.id.favBtn);
            img.setImageResource(R.mipmap.map_marker_minus_foreground);
        }

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView cityTitle = (TextView) findViewById(R.id.appbar_location_address);
        cityTitle.setText(location_address);


        Log.d(SearchResult.class.getSimpleName(),"search_result page");

        setHashValues();
        setCurrentStats(dailyObj, location_address);

    }

    private String getConvertedValue(double value) {
        return String.format("%.2f", value);
    }

    private void setCurrentStats(JSONObject currently, String location_address) {
        try {
            /* Get the views */
            TextView current_temperature = findViewById(R.id.search_result_temperature);
            TextView current_summary = findViewById(R.id.search_result_summary);
            TextView current_humdity = findViewById(R.id.search_result_humidity);
            TextView current_windspeed = findViewById(R.id.search_result_windspeed);
            TextView current_visibility = findViewById(R.id.search_result_visibility);
            TextView current_pressure = findViewById(R.id.search_result_pressure);
            TextView current_location = findViewById(R.id.search_result_location);

            Long temperature =  Math.round(currently.getDouble("temperature"));
            String summary = currently.getString("summary");
            String humdity = getConvertedValue(currently.getDouble("humidity") * 100);
            String pressure = getConvertedValue(currently.getDouble("pressure"));
            String windspeed = getConvertedValue(currently.getDouble("windSpeed"));
            String visibility = getConvertedValue(currently.getDouble("visibility"));
            location_address = convertFromUrlToText(location_address);

            Log.d(SearchResult.class.getSimpleName(), temperature.toString() + ", " +
                    summary + " " + humdity + " " + windspeed + " " + visibility + " " + pressure);
            current_temperature.setText(temperature.toString() + "\u2109");
            current_summary.setText(summary);
            current_humdity.setText(humdity + "%");
            current_pressure.setText(pressure +" mb");
            current_windspeed.setText(windspeed + " mph");
            current_visibility.setText(visibility + " miles");
            current_location.setText(location_address);

            setMinAndMaxTempForWeek(weatherObj.getJSONObject("daily"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getPixels(int dp) {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public void showDetailedWeather(View view) {
        Intent intent = new Intent(SearchResult.this, SwipeTabsActivity.class);
        intent.putExtra("JSON_OBJ", weatherString);
        startActivity(intent);
    }

    private void setMinAndMaxTempForWeek(JSONObject dailyObj) {
        try {
            JSONArray dailyArr = dailyObj.getJSONArray("data");
            TableLayout dailyTable = (TableLayout) findViewById(R.id.tableSpace);
            dailyTable.removeAllViewsInLayout();

            Integer height = dailyTable.getHeight();

            Integer array_length = dailyArr.length();
            Log.d(SearchResult.class.getSimpleName(), array_length.toString());
            for (int i = 0; i < dailyArr.length(); i++) {
                TableRow tr = new TableRow(SearchResult.this);
                JSONObject obj = dailyArr.getJSONObject(i);

                tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT));
                tr.setMinimumHeight(height/5);

                TextView dateCol = new TextView(SearchResult.this);
                long timestamp = obj.getLong("time");
                Date dateObj = new Date(timestamp * 1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                String date = dateFormat.format(dateObj);
                dateCol.setText(date);
                dateCol.setWidth(getPixels(160));
                dateCol.setHeight(getPixels(60));
                dateCol.setTextColor(Color.WHITE);
                dateCol.setPadding(getPixels(10), getPixels(20), 0 , getPixels(10));
                dateCol.setTextSize(getPixels(7));

                ImageView iconCol = new ImageView(SearchResult.this);
                String iconString = obj.getString("icon");
                iconCol.setImageResource(map.get(iconString));

                TextView tempLowView = new TextView(SearchResult.this);
                Integer tempLow = obj.getInt("temperatureMin");
                tempLowView.setText(tempLow.toString());
                tempLowView.setPadding(getPixels(10), getPixels(20),0, getPixels(10));
                tempLowView.setWidth(getPixels(60));
                tempLowView.setTextColor(Color.WHITE);
                tempLowView.setTextSize(getPixels(7));

                TextView tempHighView = new TextView(SearchResult.this);
                Integer tempHigh = obj.getInt("temperatureMax");
                tempHighView.setText(tempHigh.toString());
                tempHighView.setPadding(getPixels(10), getPixels(20),0, getPixels(10));
                tempHighView.setWidth(getPixels(60));
                tempHighView.setTextColor(Color.WHITE);
                tempHighView.setTextSize(getPixels(7));

                tr.addView(dateCol);
                tr.addView(iconCol);
                tr.addView(tempLowView);
                tr.addView(tempHighView);

                dailyTable.addView(tr);

                View vline = new View(SearchResult.this);
                vline.setLayoutParams( new
                        TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                vline.setBackgroundColor(Color.WHITE);

                dailyTable.addView(vline);

                Log.d(SearchResult.class.getSimpleName(), date + " " + iconString + " " +
                        tempLow.toString() + " " + tempHigh.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String convertFromUrlToText(String url) {
        url = url.replaceAll("\\+", " ");
        return url;
    }

}

package com.example.weatherapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class homeActivity extends AppCompatActivity {

    private RequestQueue mQueue;
    private HashMap<String, Integer> map = new HashMap<>();
    private JSONObject jsonObj;
    private List<String> dataArr;
    private String locationAddress;

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

    private String serverAddress = "http://vijaysai-hw8.us-east-2.elasticbeanstalk.com/";
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);

        // Get the search menu.
        MenuItem searchMenu = menu.findItem(R.id.action_search);

        // Get SearchView object.
        final SearchView searchView = (SearchView) searchMenu.getActionView();

        // Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(Color.GRAY);
        searchAutoComplete.setTextColor(Color.WHITE);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.white);

        autoSuggestAdapter = new AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);

        searchAutoComplete.setThreshold(1);
        searchAutoComplete.setAdapter(autoSuggestAdapter);
        searchAutoComplete.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    String queryString=(String)parent.getItemAtPosition(position);
                    searchAutoComplete.setText("" + queryString);
                    Toast.makeText(homeActivity.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();

                    String[] location_tokens = queryString.split(",");
                    getCurrentWeatherFromCity(location_tokens[0], location_tokens[1], location_tokens[2]);
                    searchView.clearFocus();
                }
            });

        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        makeApiCall(searchAutoComplete.getText().toString());
                    }
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);


    }


    private void makeApiCall(String text) {
        ApiCall.make(this, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("predictions");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        stringList.add(row.getString("description"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra(MainActivity.LAT_TEXT, 0);
        Double longitude = intent.getDoubleExtra(MainActivity.LON_TEXT, 0);
        Log.d(homeActivity.class.getSimpleName(),"Home page");

        dataArr = new ArrayList<>();
        setHashValues();

        mQueue = Volley.newRequestQueue(this);
        getCurrentWeather(latitude, longitude);

    }

    private String getConvertedValue(double value) {
        return String.format("%.2f", value);
    }

    private void setCurrentStats(JSONObject currently, String location_address) {
        try {
            /* Get the views */
            TextView current_temperature = findViewById(R.id.home_temperature);
            TextView current_summary = findViewById(R.id.home_summary);
            TextView current_humdity = findViewById(R.id.home_humidity);
            TextView current_windspeed = findViewById(R.id.home_windspeed);
            TextView current_visibility = findViewById(R.id.home_visibility);
            TextView current_pressure = findViewById(R.id.home_pressure);
            TextView current_location = findViewById(R.id.home_location);

            Long temperature =  Math.round(currently.getDouble("temperature"));
            String summary = currently.getString("summary");
            String humdity = getConvertedValue(currently.getDouble("humidity") * 100);
            String pressure = getConvertedValue(currently.getDouble("pressure"));
            String windspeed = getConvertedValue(currently.getDouble("windSpeed"));
            String visibility = getConvertedValue(currently.getDouble("visibility"));
            location_address = convertFromUrlToText(location_address);

            Log.d(homeActivity.class.getSimpleName(), temperature.toString() + ", " +
                    summary + " " + humdity + " " + windspeed + " " + visibility + " " + pressure);
            current_temperature.setText(temperature.toString() + "\u2109");
            current_summary.setText(summary);
            current_humdity.setText(humdity + "%");
            current_pressure.setText(pressure +" mb");
            current_windspeed.setText(windspeed + " mph");
            current_visibility.setText(visibility + " miles");
            current_location.setText(location_address);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private int getPixels(int dp) {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public void showDetailedWeather(View view) {
        Intent intent = new Intent(homeActivity.this, SwipeTabsActivity.class);
        intent.putExtra("JSON_OBJ", jsonObj.toString());
        startActivity(intent);
    }

    private void setMinAndMaxTempForWeek(JSONObject dailyObj) {
        try {
            JSONArray dailyArr = dailyObj.getJSONArray("data");
            TableLayout dailyTable = (TableLayout) findViewById(R.id.tableSpace);
            dailyTable.removeAllViewsInLayout();

            Integer height = dailyTable.getHeight();

            Integer array_length = dailyArr.length();
            Log.d(homeActivity.class.getSimpleName(), array_length.toString());
            for (int i = 0; i < dailyArr.length(); i++) {
                TableRow tr = new TableRow(homeActivity.this);
                JSONObject obj = dailyArr.getJSONObject(i);

                tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT));
                tr.setMinimumHeight(height/5);

                TextView dateCol = new TextView(homeActivity.this);
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

                ImageView iconCol = new ImageView(homeActivity.this);
                String iconString = obj.getString("icon");
                iconCol.setImageResource(map.get(iconString));

                TextView tempLowView = new TextView(homeActivity.this);
                Integer tempLow = obj.getInt("temperatureMin");
                tempLowView.setText(tempLow.toString());
                tempLowView.setPadding(getPixels(10), getPixels(20),0, getPixels(10));
                tempLowView.setWidth(getPixels(60));
                tempLowView.setTextColor(Color.WHITE);
                tempLowView.setTextSize(getPixels(7));

                TextView tempHighView = new TextView(homeActivity.this);
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

                View vline = new View(homeActivity.this);
                vline.setLayoutParams( new
                        TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                vline.setBackgroundColor(Color.WHITE);

                dailyTable.addView(vline);

                Log.d(homeActivity.class.getSimpleName(), date + " " + iconString + " " +
                        tempLow.toString() + " " + tempHigh.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void handleWeatherResponse(JSONObject response) {
        try {
            locationAddress = convertFromUrlToText(response.getString("location_address"));
            String timezone = response.getString("timezone");
            Log.d(homeActivity.class.getSimpleName(),timezone);
            jsonObj = response;
            JSONObject currently = response.getJSONObject("currently");
            setCurrentStats(currently, response.getString("location_address"));
            setMinAndMaxTempForWeek(response.getJSONObject("daily"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getCurrentWeatherFromCity(String city,  String state, String country) {
        String url = serverAddress + "getLoc/" +
                city + "/" + state + "/" + country;

        Log.d(homeActivity.class.getSimpleName(), url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Intent intent = new Intent(homeActivity.this, SearchResult.class);
                    intent.putExtra("JSON_OBJ", response.toString());
                    startActivity(intent);
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


    private String convertToUrl(String url) {
        url = url.trim();
        url = url.replaceAll("\\s", "+");
        return url;
    }

    private String convertFromUrlToText(String url) {
        url = url.replaceAll("\\+", " ");
        return url;
    }

    private String getLocationAddress(Double latitude, Double longitude) {
        locationAddress = "Los Angeles, CA, USA";
        return locationAddress;
    }

    private void getCurrentWeather(Double lat, Double lon) {
        String location_address = getLocationAddress(lat, lon);

        String url = serverAddress + "getWeatherInfo/" +
                lat.toString() + "/" + lon.toString() + "/" + location_address;

        url = convertToUrl(url);
        Log.d(homeActivity.class.getSimpleName(), url);
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

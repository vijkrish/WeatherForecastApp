package com.example.weatherapp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment {

    private HashMap<String, Integer> map = new HashMap<>();
    private JSONObject weatherObj;
    private String locationAddress;

    private String getConvertedValue(double value) {
        return String.format("%.2f", value);
    }

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

    public FavoriteFragment() {
        // Required empty public constructor
    }

    private static final String SHARED_PREFS = "weather_pref";
    private void removeFromFavorites(String city) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(city);
        editor.apply();
    }

    public void removeCurrentFragment(View view) {
        removeFromFavorites(locationAddress);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String jsonString = getArguments().getString("weather_string");
        int position = getArguments().getInt("position");

        Log.d(FavoriteTabActivity.class.getSimpleName(), jsonString);
        setHashValues();

        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        try {
            weatherObj = new JSONObject(jsonString);
            locationAddress = convertFromUrlToText(weatherObj.getString("location_address"));
            setCurrentStats(weatherObj.getJSONObject("currently"), locationAddress, view);
            setMinAndMaxTempForWeek(weatherObj.getJSONObject("daily"), view);
        } catch (JSONException error) {
            locationAddress = "";
            error.printStackTrace();
        }

        ConstraintLayout card = view.findViewById(R.id.card1);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(FavoriteTabActivity.class.getSimpleName(), "Clicked on fragment");
                Intent intent = new Intent(getActivity(), SwipeTabsActivity.class);
                intent.putExtra("JSON_OBJ", weatherObj.toString());
                startActivity(intent);
            }
        });

        ImageView favButton = view.findViewById(R.id.favBtn);
        if (position == 0) {
            favButton.setVisibility(View.INVISIBLE);
        } else {
            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeCurrentFragment(v);
                }
            });
        }

        return view;
    }


    private void setCurrentStats(JSONObject currently, String location_address, View view) {
        try {
            /* Get the views */
            TextView current_temperature = view.findViewById(R.id.home_temperature);
            TextView current_summary = view.findViewById(R.id.home_summary);
            TextView current_humdity = view.findViewById(R.id.home_humidity);
            TextView current_windspeed = view.findViewById(R.id.home_windspeed);
            TextView current_visibility = view.findViewById(R.id.home_visibility);
            TextView current_pressure = view.findViewById(R.id.home_pressure);
            TextView current_location = view.findViewById(R.id.home_location);
            ImageView current_icon = view.findViewById(R.id.home_icon);

            Long temperature =  Math.round(currently.getDouble("temperature"));
            String summary = currently.getString("summary");
            String humdity = getConvertedValue(currently.getDouble("humidity") * 100);
            String pressure = getConvertedValue(currently.getDouble("pressure"));
            String windspeed = getConvertedValue(currently.getDouble("windSpeed"));
            String visibility = getConvertedValue(currently.getDouble("visibility"));
            location_address = convertFromUrlToText(location_address);
            String icon_img = currently.getString("icon");

            Log.d(homeActivity.class.getSimpleName(), temperature.toString() + ", " +
                    summary + " " + humdity + " " + windspeed + " " + visibility + " " + pressure + " " + icon_img);
            current_temperature.setText(temperature.toString() + "\u2109");
            current_summary.setText(summary);
            current_humdity.setText(humdity + "%");
            current_pressure.setText(pressure +" mb");
            current_windspeed.setText(windspeed + " mph");
            current_visibility.setText(visibility + " miles");
            current_location.setText(location_address);
            current_icon.setImageResource(map.get(icon_img));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String convertFromUrlToText(String url) {
        url = url.replaceAll("\\+", " ");
        return url;
    }

    private int getPixels(int dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public void showDetailedWeather(View view) {
        Intent intent = new Intent(getActivity(), SwipeTabsActivity.class);
        intent.putExtra("JSON_OBJ", weatherObj.toString());
        startActivity(intent);
    }

    private void setMinAndMaxTempForWeek(JSONObject dailyObj, View view) {
        try {
            JSONArray dailyArr = dailyObj.getJSONArray("data");
            TableLayout dailyTable = (TableLayout) view.findViewById(R.id.tableSpace);
            dailyTable.removeAllViewsInLayout();

            Integer height = dailyTable.getHeight();

            Integer array_length = dailyArr.length();
            for (int i = 0; i < dailyArr.length(); i++) {
                TableRow tr = new TableRow(getActivity());
                JSONObject obj = dailyArr.getJSONObject(i);

                tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT));
                tr.setMinimumHeight(height/5);

                TextView dateCol = new TextView(getActivity());
                long timestamp = obj.getLong("time");
                Date dateObj = new Date(timestamp * 1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                String date = dateFormat.format(dateObj);
                dateCol.setText(date);
                dateCol.setWidth(getPixels(160));
                dateCol.setHeight(getPixels(60));
                dateCol.setTextColor(Color.WHITE);
                dateCol.setPadding(getPixels(10), getPixels(20), 0 , getPixels(10));
                dateCol.setTextSize(getPixels(6));

                ImageView iconCol = new ImageView(getActivity());
                String iconString = obj.getString("icon");
                iconCol.setImageResource(map.get(iconString));

                TextView tempLowView = new TextView(getActivity());
                Integer tempLow = obj.getInt("temperatureLow");
                tempLowView.setText(tempLow.toString());
                tempLowView.setPadding(getPixels(10), getPixels(20),0, getPixels(10));
                tempLowView.setWidth(getPixels(60));
                tempLowView.setTextColor(Color.WHITE);
                tempLowView.setTextSize(getPixels(6));

                TextView tempHighView = new TextView(getActivity());
                Integer tempHigh = obj.getInt("temperatureHigh");
                tempHighView.setText(tempHigh.toString());
                tempHighView.setPadding(getPixels(10), getPixels(20),0, getPixels(10));
                tempHighView.setWidth(getPixels(60));
                tempHighView.setTextColor(Color.WHITE);
                tempHighView.setTextSize(getPixels(6));

                tr.addView(dateCol);
                tr.addView(iconCol);
                tr.addView(tempLowView);
                tr.addView(tempHighView);

                dailyTable.addView(tr);

                View vline = new View(getActivity());
                vline.setLayoutParams( new
                        TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                vline.setBackgroundColor(Color.WHITE);

                dailyTable.addView(vline);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

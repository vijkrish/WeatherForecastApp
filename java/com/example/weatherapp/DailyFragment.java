package com.example.weatherapp;


import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment {

    private JSONObject currently;
    private HashMap<String, Integer> map = new HashMap<>();

    public DailyFragment() {
        // Required empty public constructor
    }

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

    private void setData(View view) {
        try {

            /* Get the objects */
            TextView windspeed = (TextView) view.findViewById(R.id.windspeed);
            TextView pressure = (TextView) view.findViewById(R.id.pressure);
            TextView precipitation = (TextView) view.findViewById(R.id.precipitation);

            TextView temperature = (TextView) view.findViewById(R.id.temperature);
            TextView summary = (TextView) view.findViewById(R.id.summary);
            ImageView summary_icon = (ImageView) view.findViewById(R.id.summary_icon);
            TextView humdity = (TextView) view.findViewById(R.id.humidity);

            TextView visibility = (TextView) view.findViewById(R.id.visibility);
            TextView cloudcover = (TextView) view.findViewById(R.id.cloudcover);
            TextView ozone = (TextView) view.findViewById(R.id.ozone);

            /* Get data from json */

            String windspeed_val = getConvertedValue(currently.getDouble("windSpeed"));
            String pressure_val = getConvertedValue(currently.getDouble("pressure"));
            String precipitation_val = getConvertedValue(currently.getDouble("precipIntensity"));

            Long temperature_val =  Math.round(currently.getDouble("temperature"));
            String summary_val = currently.getString("summary");
            String summar_icon_val = currently.getString("icon");
            String humdity_val = getConvertedValue(currently.getDouble("humidity") * 100);

            String visibility_val = getConvertedValue(currently.getDouble("visibility"));
            Long cloudcover_val = Math.round(currently.getDouble("cloudCover") * 100);
            String ozone_val = getConvertedValue(currently.getDouble("ozone"));


            /* Set the data */
            Log.d(DailyFragment.class.getSimpleName(), temperature_val.toString() + ", " +
                    summary_val + " " + humdity_val + " " + windspeed_val + " " + visibility_val + " " + pressure_val);

            windspeed.setText(windspeed_val + " mph");
            pressure.setText(pressure_val +" mb");
            precipitation.setText(precipitation_val + " mmph");

            temperature.setText(temperature_val.toString() + "\u2109");
            summary.setText(summary_val);
            summary_icon.setImageResource(map.get(summar_icon_val));
            humdity.setText(humdity_val + "%");

            visibility.setText(visibility_val + " miles");
            cloudcover.setText(cloudcover_val + "%");
            ozone.setText(ozone_val + " DU");
        } catch (JSONException error) {
            error.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String jsonString = getArguments().getString("JSON_OBJ");
        Log.d(DailyFragment.class.getSimpleName(), jsonString);
        View view = inflater.inflate(R.layout.fragment_daily2, container, false);
        setHashValues();

        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            currently = jsonObj.getJSONObject("currently");
        } catch (JSONException error) {
            error.printStackTrace();
        }

        setData(view);
        return view;
    }

}

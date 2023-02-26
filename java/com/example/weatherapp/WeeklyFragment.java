package com.example.weatherapp;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeeklyFragment extends Fragment {
    private JSONObject weekly;
    private HashMap<String, Integer> map = new HashMap<>();
    private LineChart weeklyChart;
    List<Entry> minTemperatureList;
    List<Entry> maxTemperatureList;
    List<ILineDataSet> dataSets;

    public WeeklyFragment() {
        // Required empty public constructor
    }

    private void setGraphData() {
        try {
            JSONArray weeklyJSONArray = weekly.getJSONArray("data");
            for (int i = 0; i < weeklyJSONArray.length(); i++) {
                JSONObject obj = weeklyJSONArray.getJSONObject(i);
                Integer tempLow = obj.getInt("temperatureMin");
                Integer tempHigh = obj.getInt("temperatureMax");

                minTemperatureList.add(new Entry(i, tempLow));
                maxTemperatureList.add(new Entry(i, tempHigh));
            }
            LineDataSet minTemperatureDataSet = new LineDataSet(minTemperatureList, "Minimum Temperature");
            LineDataSet maxTemperatureDataSet = new LineDataSet(maxTemperatureList, "Maximum Temperature");

            minTemperatureDataSet.setColor(Color.rgb(238, 130, 238));
            maxTemperatureDataSet.setColor(Color.rgb(255,165,0));

            dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(minTemperatureDataSet);
            dataSets.add(maxTemperatureDataSet);


        } catch (JSONException error) {
            error.printStackTrace();
        }
    }

    private void displayGraph(View view) {
        weeklyChart =  (LineChart) view.findViewById(R.id.linechart);
        LineData data = new LineData(dataSets);
        weeklyChart.setData(data);

//        weeklyChart.getAxisRight().setGridColor(Color.rgb(192,192,192));
//        weeklyChart.getAxisLeft().setGridColor(Color.rgb(192,192,192));
//        weeklyChart.getXAxis().setGridColor(Color.rgb(192,192,192));

        weeklyChart.getAxisRight().setGridColor(Color.rgb(29,29,29));
        weeklyChart.getAxisLeft().setGridColor(Color.rgb(29,29,29));
        weeklyChart.getXAxis().setGridColor(Color.rgb(29,29,29));

        weeklyChart.getAxisLeft().setTextColor(Color.WHITE);
        weeklyChart.getAxisRight().setTextColor(Color.WHITE);
        weeklyChart.getXAxis().setTextColor(Color.WHITE);

        weeklyChart.getLegend().setTextColor(Color.WHITE);
        weeklyChart.getLegend().setTextSize(14);

        weeklyChart.invalidate();
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

    private void setSummaryCard(View view) {
        ImageView icon = (ImageView) view.findViewById(R.id.summary_icon);
        TextView summary = (TextView) view.findViewById(R.id.summary);

        try {
            icon.setImageResource(map.get(weekly.getString("icon")));
            summary.setText(weekly.getString("summary"));
        } catch (JSONException error) {
            error.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String jsonString = getArguments().getString("JSON_OBJ");
        setHashValues();

        minTemperatureList = new ArrayList<Entry>();
        maxTemperatureList = new ArrayList<Entry>();

        View view =  inflater.inflate(R.layout.fragment_weekly, container, false);
        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            weekly = jsonObj.getJSONObject("daily");
        } catch (JSONException error) {
            error.printStackTrace();
        }

        setGraphData();
        setSummaryCard(view);
        displayGraph(view);

        return view;
    }

}

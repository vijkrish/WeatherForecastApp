package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SwipeTabsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private String weatherString;
    private TabLayout tabLayout;
    private String temperature;
    private String location_address;


    private int getIconForSelectedTabPosition(int position) {
        if (position == 0) {
            return R.mipmap.calendar_today_active;
        } else if (position == 1) {
            return R.mipmap.trending_up_active;
        }
        return R.mipmap.google_photos_active;
    }

    private int getIconForUnSelectedTabPosition(int position) {
        if (position == 0) {
            return R.mipmap.calendar_today_inactive;
        } else if (position == 1) {
            return R.mipmap.trending_up_inactive;
        }
        return R.mipmap.google_photos_inactive;
    }

    private String convertToUrl(String url) {
        url = url.trim();
        url = url.replaceAll("\\s", "%20");
        return url;
    }

    public void tweetWeather(View view) {
        String city = location_address;
        String tweetMessage = "Check Out " + city + "'s Weather! It is " + temperature + "!";
        String hashtag = "CSCI571WeatherSearch";

        String url = "https://twitter.com/intent/tweet?text=" + tweetMessage + "&hashtags=" + hashtag;
        url = convertToUrl(url);
        Log.d(SwipeTabsActivity.class.getSimpleName(), url);

        Intent tweetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(tweetIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_tabs);

        Intent intent = getIntent();
        weatherString = intent.getStringExtra("JSON_OBJ");

        try {
            JSONObject weatherObj = new JSONObject(weatherString);
            JSONObject dailyObj = weatherObj.getJSONObject("currently");
            Long temperature_val =  Math.round(dailyObj.getDouble("temperature"));
            temperature = temperature_val.toString() + "\u2109";
            location_address = convertFromUrlToText(weatherObj.getString("location_address"));
        } catch (JSONException error) {
            error.printStackTrace();
            location_address = "";
        }

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView cityTitle = (TextView) findViewById(R.id.appbar_location_address);
        cityTitle.setText(location_address);

        viewPager = findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    tab.setIcon(getIconForSelectedTabPosition(position));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    tab.setIcon(getIconForUnSelectedTabPosition(position));
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            }
        );

    }

    private void setupTabIcons() {
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.mipmap.calendar_today_active);
        tabLayout.getTabAt(1).setIcon(R.mipmap.trending_up_inactive);
        tabLayout.getTabAt(2).setIcon(R.mipmap.google_photos_inactive);

        tabLayout.setTabTextColors(Color.GRAY, Color.WHITE);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new DailyFragment(), "TODAY");
        adapter.addFrag(new WeeklyFragment(), "WEEKLY");
        adapter.addFrag(new PhotosFragment(), "PHOTOS");
        adapter.setWeatherData(weatherString);
        viewPager.setAdapter(adapter);
    }

    private String convertFromUrlToText(String url) {
        url = url.replaceAll("\\+", " ");
        return url;
    }
}

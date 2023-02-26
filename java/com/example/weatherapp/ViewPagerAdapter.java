package com.example.weatherapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private String weatherString;

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        if (position == 0) {
            DailyFragment dailyObj = new DailyFragment();
            bundle.putString("JSON_OBJ", weatherString);
            dailyObj.setArguments(bundle);
            return dailyObj;
        } else if (position == 1) {
            WeeklyFragment weeklyObj = new WeeklyFragment();
            bundle.putString("JSON_OBJ", weatherString);
            weeklyObj.setArguments(bundle);
            return weeklyObj;
        }
        PhotosFragment photosObj = new PhotosFragment();
        bundle.putString("JSON_OBJ", weatherString);
        photosObj.setArguments(bundle);
        return photosObj;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void setWeatherData(String jsonString) {
        weatherString = jsonString;
    }

}
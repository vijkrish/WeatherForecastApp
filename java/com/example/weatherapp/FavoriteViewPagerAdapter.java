package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FavoriteViewPagerAdapter extends FragmentPagerAdapter {
    private final List<FavoriteFragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private List<String> favCityList = new ArrayList<>();
    private String weatherString;

    public FavoriteViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        FavoriteFragment fragment = mFragmentList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("weather_string", favCityList.get(position));
        bundle.putInt("position", position);
        Log.d(FavoriteViewPagerAdapter.class.getSimpleName(), "Position: " + position + " weather_string = "  + favCityList.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return favCityList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public String getFavCity(int position) {
        return favCityList.get(position);
    }

    public void addToFavList(String weatherString) {
        favCityList.add(weatherString);
        mFragmentList.add(new FavoriteFragment());
    }

    public void resetFavList() {
        favCityList.clear();
        mFragmentList.clear();
    }
}

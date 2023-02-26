package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoriteTabActivity extends AppCompatActivity {
    private Toolbar toolbar;
    public static ViewPager viewPager;
    private String weatherString;
    private TabLayout tabLayout;
    private static final String SHARED_PREFS = "weather_pref";
    private Integer numTabs = 1;
    private List<String> favCityList = new ArrayList();
    public static String currentLocationWeather;

    /* Autocomplete */
    private RequestQueue mQueue;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    private List<String> dataArr;

    /* favorites */
    public static FavoriteViewPagerAdapter adapter;


    String serverAddress = "http://vijaysai-hw8.us-east-2.elasticbeanstalk.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_tab);

        Intent intent = getIntent();
        weatherString = intent.getStringExtra("JSON_OBJ");

        dataArr = new ArrayList<>();
        mQueue = Volley.newRequestQueue(this);

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
                        tab.setIcon(R.mipmap.circle_enabled);
                        currentLocationWeather = favCityList.get(position);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        tab.setIcon(R.mipmap.circle_disabled);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                }
        );


    }

    @Override
    protected  void onRestart() {
        super.onRestart();
    }

    private void setupTabIcons() {
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.mipmap.circle_enabled);

        for (int i = 1; i < numTabs; i++) {
            tabLayout.getTabAt(i).setIcon(R.mipmap.circle_disabled);
        }
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

    private void addFavListToCard() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        adapter.addToFavList(currentLocationWeather);
        favCityList.add(currentLocationWeather);

        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry: keys.entrySet()) {
            Log.d(FavoriteTabActivity.class.getSimpleName(),
                    entry.getKey() + ": " + entry.getValue().toString());
            adapter.addToFavList(entry.getValue().toString());
            favCityList.add(entry.getValue().toString());
        }
        numTabs = adapter.getCount();
        viewPager.setAdapter(adapter);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new FavoriteViewPagerAdapter(getSupportFragmentManager());
        currentLocationWeather = weatherString;
        addFavListToCard();
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
                    getCurrentWeatherFromCity(queryString);
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

    private void getCurrentWeatherFromCity(String city) {
        String url = serverAddress + "getLoc/" +
                city;

        Log.d(homeActivity.class.getSimpleName(), url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent intent = new Intent(FavoriteTabActivity.this, SearchResult.class);
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
}

package com.example.weatherapp;


import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.example.weatherapp.PhotosAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotosFragment extends Fragment {

    JSONArray photosList;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public PhotosFragment() {
        // Required empty public constructor
    }

    private Drawable loadImageFromUrl(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable img = Drawable.createFromStream(is, "webresource");
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        photosList = new JSONArray();
        String jsonString = getArguments().getString("JSON_OBJ");
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        List<String> photos_url_list = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            photosList = jsonObj.getJSONArray("cityImageUrl");

            for (int i = 0; i < 8; i++) {
                JSONObject imgObj = photosList.getJSONObject(i);
                JSONObject imageObj = imgObj.getJSONObject("image");
                String url = imgObj.getString("link");

                boolean result_val = url.contains("https://cdn.wonderfulengineering.com/");
                Log.d(PhotosFragment.class.getSimpleName(), "Url = " + url + " val = " + Boolean.toString(result_val));
                if (url.contains("https://cdn.wonderfulengineering.com/")) {
                    url = imageObj.getString("thumbnailLink");
                }

                photos_url_list.add(url);
            }

            setRvAdapter(photos_url_list);

        } catch (Exception error) {
            error.printStackTrace();
        }


        return view;
    }


    private void setRvAdapter(List<String> photos_url_list) {
        PhotosAdapter myAdapter = new PhotosAdapter(getActivity(), photos_url_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);
    }
}

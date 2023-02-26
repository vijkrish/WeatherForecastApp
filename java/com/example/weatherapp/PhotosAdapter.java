package com.example.weatherapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.MyViewHolder>{
    RequestOptions options ;
    private Context mContext ;
    private List<String> mData ;


    public PhotosAdapter(Context mContext, List<String> lst) {
        this.mContext = mContext;
        this.mData = lst;
        options = new RequestOptions()
                .fitCenter()
                .error(R.drawable.google_photos_card);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.image_card,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // load image from the internet using Glide
        String url = mData.get(position);
        Log.d(PhotosAdapter.class.getSimpleName(), "Url = " + url + "\n");
        Glide.with(mContext).load(url).apply(options).into(holder.city_photo);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView city_photo;


        public MyViewHolder(View itemView) {
            super(itemView);
            city_photo = itemView.findViewById(R.id.album_photo);
        }
    }
}

package com.example.todolist;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> imageList;

    // Constructor to initialize context and the list of image resources
    public ImageAdapter(Context context, List<Integer> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // If convertView is null, create a new ImageView
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));  // Set image size
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        // Set the image resource for the imageView
        imageView.setImageResource(imageList.get(position));
        return imageView;
    }
}

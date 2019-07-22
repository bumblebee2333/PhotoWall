package com.example.photowall.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.example.photowall.ImageUtil.ImageLoader;
import com.example.photowall.R;
import com.example.photowall.Widget.SquareImageView;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private List<String> imagePaths;
    private Drawable mDefaultDrawable;
    private LayoutInflater mInflater;
    private ImageLoader imageLoader;
    private boolean isGridViewIdle;

    public ImageAdapter(Context context, List<String> imagePaths,ImageLoader imageLoader,boolean isGridViewIdle){
        this.imagePaths = imagePaths;
        this.imageLoader = imageLoader;
        this.isGridViewIdle = isGridViewIdle;
        mInflater = LayoutInflater.from(context);
        mDefaultDrawable = context.getResources().getDrawable(R.drawable.background);
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public String getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.grideview_item,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.background);
            convertView.setTag(viewHolder);//设置对象标签 根据标签，取回viewholder对象
        }else {
            viewHolder = (ViewHolder) convertView.getTag();//提高复用性
        }
        SquareImageView imageView = viewHolder.imageView;
        final String tag = (String) imageView.getTag();
        final String url = getItem(position);
        if(!url.equals(tag)){
           imageView.setImageDrawable(mDefaultDrawable);
        }
        if(isGridViewIdle) {
            imageView.setTag(url);
            imageLoader.bindBitmap(url,imageView);
        }
        return convertView;
    }

    private class ViewHolder{
        SquareImageView imageView;
    }
}

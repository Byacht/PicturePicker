package com.byacht.picturepicker.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.byacht.picturepicker.R;

import java.util.List;

/**
 * Created by dn on 2017/10/1.
 */

public class PictureShowPagerAdapter extends PagerAdapter {

    private List<String> mPicturePathList;

    public PictureShowPagerAdapter(List<String> picturePathList) {
        mPicturePathList = picturePathList;
    }

    @Override
    public int getCount() {
        return mPicturePathList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.picture_show_item, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.img_picture_show);
        Glide.with(container.getContext())
                .load(mPicturePathList.get(position))
                .into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}

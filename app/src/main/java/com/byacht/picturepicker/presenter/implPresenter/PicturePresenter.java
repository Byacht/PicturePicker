package com.byacht.picturepicker.presenter.implPresenter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.byacht.picturepicker.presenter.IPicturePresenter;
import com.byacht.picturepicker.presenter.implView.IPictureView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dn on 2017/9/24.
 */

public class PicturePresenter implements IPicturePresenter {

    //Map 的 key 存放图片的父路径， value 存放该路径下的所有图片
    private Map<String, List<String>> mPictureGroupMap;
    private Context mContext;
    private static final int SUCCEED = 0;

    public PicturePresenter(Context context) {
        mContext = context;
    }

    @Override
    public void getPictures() {
        mPictureGroupMap = new HashMap<String, List<String>>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                ContentResolver mContentResolver = mContext.getContentResolver();
                Cursor mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);
                if(mCursor == null){
                    return;
                }

                while (mCursor.moveToNext()) {
                    //获取图片的路径
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //获取图片的父路径
                    String parentName = new File(path).getParentFile().getName();

                    if (!mPictureGroupMap.containsKey(parentName)) {
                        List<String> pictureList = new ArrayList<String>();
                        pictureList.add(path);
                        mPictureGroupMap.put(parentName, pictureList);
                    } else {
                        mPictureGroupMap.get(parentName).add(path);
                    }
                }
                mHandler.sendEmptyMessage(SUCCEED);
                mCursor.close();
            }
        }).start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCEED:
                    if (mContext instanceof IPictureView) {
                        ((IPictureView)mContext).showPictures(sortMap(mPictureGroupMap));
                    }
                    break;
            }
        }
    };

    private Map<String, List<String>> sortMap(Map<String, List<String>> map) {
        List<Map.Entry<String, List<String>>> list; list = new ArrayList<Map.Entry<String, List<String>>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, List<String>>>() {
            //升序排序
            public int compare(Map.Entry<String,  List<String>> o1,
                               Map.Entry<String,  List<String>> o2) {
                return o1.getValue().size() > o2.getValue().size() ? -1 : 1;
            }

        });

        Map<String, List<String>> sortedMap = new LinkedHashMap<String, List<String>>();
        for(Map.Entry<String, List<String>> mapEntry : list){
            //若该路径下的图片数量大于 0，则初始化对应列表的数据
            if (mapEntry.getValue().size() > 0) {
                sortedMap.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        return sortedMap;
    }
}

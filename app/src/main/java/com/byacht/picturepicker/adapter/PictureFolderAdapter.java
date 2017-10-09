package com.byacht.picturepicker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.byacht.picturepicker.R;
import com.byacht.picturepicker.bean.PictureFolderEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dn on 2017/9/30.
 */

public class PictureFolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<String> mFolderList;
    private List<String> mFirstPictureList;
    private int[] mPictureNumbers;
    private int mCurrentPosition;

    public PictureFolderAdapter(Context context, List<String> folderList, List<String> firstPictureList, int[] pictureNumbers, int position) {
        mContext = context;
        mFolderList = folderList;
        mFirstPictureList = firstPictureList;
        mPictureNumbers = pictureNumbers;
        mCurrentPosition = position;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_picture_folder_item, parent, false);
        return new PictureFolderVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ((PictureFolderVH)holder).tvFolderName.setText(mFolderList.get(position) + "(" + mPictureNumbers[position] + ")");
        Glide.with(mContext)
                .load(mFirstPictureList.get(position))
                .into(((PictureFolderVH) holder).imageView);
        ((PictureFolderVH)holder).layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new PictureFolderEvent(position));
            }
        });
        if (mCurrentPosition == position) {
            ((PictureFolderVH) holder).tvFolderName.setTextColor(((PictureFolderVH) holder).colorAccent);

        } else {
            ((PictureFolderVH) holder).tvFolderName.setTextColor(((PictureFolderVH) holder).colorText);
        }
    }

    @Override
    public int getItemCount() {
        if (mFolderList != null) {
            return mFolderList.size();
        }
        return 0;
    }

    class PictureFolderVH extends RecyclerView.ViewHolder {
        @BindView(R.id.img_picture_folder)
        ImageView imageView;
        @BindView(R.id.tv_picture_folder_name)
        TextView tvFolderName;
        @BindView(R.id.layout_choose_picture_folder)
        LinearLayout layout;
        @BindColor(R.color.colorAccent)
        int colorAccent;
        @BindColor(R.color.textColor)
        int colorText;

        public PictureFolderVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

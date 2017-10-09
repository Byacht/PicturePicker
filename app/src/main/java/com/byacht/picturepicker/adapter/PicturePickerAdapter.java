package com.byacht.picturepicker.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.byacht.picturepicker.R;
import com.byacht.picturepicker.activity.PicturePickActivity;
import com.byacht.picturepicker.activity.PictureShowActivity;
import com.byacht.picturepicker.bean.DeleteEvent;
import com.byacht.picturepicker.bean.SelectedNumberEvent;
import com.byacht.picturepicker.bean.SelectedPicturesEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dn on 2017/9/29.
 */

public class PicturePickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<String> mPicturePathList;
    private Map<Integer, String> mSelectedPictureMap;
    private int mAdapterType;

    public static final int PICKER_TYPE = 0;
    public static final int GALLERY_TYPE = 1;

    public PicturePickerAdapter(Context context, List<String> picturePathList, int type) {
        mContext = context;
        mPicturePathList = picturePathList;
        mAdapterType = type;
        mSelectedPictureMap = new LinkedHashMap<Integer, String>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.picture_item, parent, false);
        return new PicturePickerVH(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //若 type 为 GALLERY 且为最后一张图片
        if (mPicturePathList != null && position == mPicturePathList.size()) {
            ((PicturePickerVH) holder).imageView.setImageResource(R.drawable.add);
            ((PicturePickerVH) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PicturePickActivity.class);
                    ((Activity)mContext).startActivityForResult(intent, 0);
                }
            });
        } else {
            Glide.with(mContext)
                    .load(mPicturePathList.get(position))
                    .into(((PicturePickerVH) holder).imageView);

            ((PicturePickerVH) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goPictureShowActivity((PicturePickerVH) holder, position);
                }
            });

            ((PicturePickerVH) holder).imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mAdapterType = PICKER_TYPE;
                    mSelectedPictureMap.put(position, mPicturePathList.get(position));
                    notifyDataSetChanged();
                    //通知 MainActivity 进行图片删除操作
                    EventBus.getDefault().post(new DeleteEvent(true));
                    return true;
                }
            });
        }

        if (mAdapterType == PICKER_TYPE) {
            ((PicturePickerVH) holder).cbPictureSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ((PicturePickerVH) holder).pictureMasking.setBackgroundColor(((PicturePickerVH) holder).translucentColor);
                        mSelectedPictureMap.put(position, mPicturePathList.get(position));
                    } else {
                        ((PicturePickerVH) holder).pictureMasking.setBackgroundColor(((PicturePickerVH) holder).transparentColor);
                        mSelectedPictureMap.remove(position);
                    }
                    //通知 PicturePickerActivity 更新选中图片的数量
                    EventBus.getDefault().post(new SelectedNumberEvent(mSelectedPictureMap.size()));
                }
            });
            ((PicturePickerVH) holder).cbPictureSelected.setVisibility(View.VISIBLE);
            notifyCheckBoxChange(position, (PicturePickerVH) holder);
        } else {
            ((PicturePickerVH) holder).pictureMasking.setBackgroundColor(((PicturePickerVH) holder).transparentColor);
            ((PicturePickerVH) holder).cbPictureSelected.setVisibility(View.GONE);
        }

    }

    private void goPictureShowActivity(PicturePickerVH holder, int position) {
        Intent intent = new Intent(mContext, PictureShowActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("type", mAdapterType);
        intent.putStringArrayListExtra("allPictures", (ArrayList<String>) mPicturePathList);
        if (mAdapterType == PICKER_TYPE) {
            intent.putExtra("selectedPictures", (Serializable) mSelectedPictureMap);
        }
        mContext.startActivity(intent);
    }

    private void notifyCheckBoxChange(int position, PicturePickerVH holder) {
        if (mSelectedPictureMap.containsKey(position)) {
            holder.cbPictureSelected.setChecked(true);
        } else {
            holder.cbPictureSelected.setChecked(false);
        }
    }

    //更换图片路径
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changePictureFolder(SelectedPicturesEvent event) {
        if (mSelectedPictureMap != null) {
            mSelectedPictureMap.clear();
            mSelectedPictureMap.putAll(event.selectedPicturesMap);
            notifyDataSetChanged();
            EventBus.getDefault().post(new SelectedNumberEvent(mSelectedPictureMap.size()));
        }
    }

    @Override
    public int getItemCount() {
        //若 mAdapterType 为 GALLERY，则多展示一张图片，此图片用于跳转到图片选择
        if (mPicturePathList != null) {
            return mPicturePathList.size() + mAdapterType;
        } else {
            return mAdapterType;
        }
    }

    public Map<Integer, String> getSelectedPictures() {
        return mSelectedPictureMap;
    }

    public void setAdapterType(int type) {
        mAdapterType = type;
    }

    class PicturePickerVH extends RecyclerView.ViewHolder{
        @BindView(R.id.img_picture)
        ImageView imageView;
        @BindView(R.id.cb_picture_selected)
        CheckBox cbPictureSelected;
        @BindView(R.id.picture_masking)
        ImageView pictureMasking;

        @BindColor(R.color.transparent)
        int transparentColor;
        @BindColor(R.color.translucent)
        int translucentColor;

        public PicturePickerVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        EventBus.getDefault().unregister(this);
    }

}

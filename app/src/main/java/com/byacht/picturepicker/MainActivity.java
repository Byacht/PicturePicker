package com.byacht.picturepicker;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.byacht.picturepicker.adapter.PicturePickerAdapter;
import com.byacht.picturepicker.bean.DeleteEvent;
import com.byacht.picturepicker.ui.PictureItemDecoration;
import com.byacht.picturepicker.utils.SortUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_picture_gallery)
    Toolbar mToolbar;
    @BindView(R.id.rv_picture_main)
    RecyclerView mRvPictures;
    @BindView(R.id.delete_layout)
    LinearLayout mDeleteLayout;

    private List<String> mPictureList;
    private PicturePickerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupToolbar();
        setupRv();
    }

    private void setupToolbar() {
        mToolbar.setTitle("Hello World!");
        setSupportActionBar(mToolbar);
    }

    private void setupRv() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mRvPictures.setLayoutManager(layoutManager);

        mPictureList = new ArrayList<String>();
        mAdapter = new PicturePickerAdapter(this, mPictureList, PicturePickerAdapter.GALLERY_TYPE);
        mRvPictures.setAdapter(mAdapter);
        mRvPictures.addItemDecoration(new PictureItemDecoration(this));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showOrHideDeleteLayout(DeleteEvent event) {
        if (event.visiable) {
            showDeleteLayout();
        } else {
            hideDeleteLayout();
        }
    }

    private void showDeleteLayout() {
        mDeleteLayout.setAlpha(0);
        mDeleteLayout.setVisibility(View.VISIBLE);
        mDeleteLayout.animate()
                .alpha(1)
                .setDuration(1000)
                .setListener(null);
    }

    private void hideDeleteLayout() {
        mDeleteLayout.setAlpha(1);
        mDeleteLayout.animate()
                .alpha(0)
                .setDuration(1000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mDeleteLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

    }

    @OnClick(R.id.img_delete)
    public void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setMessage(mAdapter.getSelectedPictures().size() > 0 ? "您确定要删除吗？" : "请选择您要删除的照片")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePictures();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deletePictures() {
        //获取选中的图片
        Map<Integer, String> deletePictureMap = mAdapter.getSelectedPictures();
        if (deletePictureMap.size() > 0) {
            //获取图片的 key 值，对 key 值按递减顺序排列，以便正确删除图片
            int[] keys = getPictureKeys(deletePictureMap);
            SortUtils.bubbleSort(keys);

            for (int j = 0; j < keys.length; j++) {
                mPictureList.remove(keys[j]);
            }
            //清空选中的图片
            deletePictureMap.clear();

            mAdapter.setAdapterType(PicturePickerAdapter.GALLERY_TYPE);
            mAdapter.notifyDataSetChanged();
            mDeleteLayout.setVisibility(View.GONE);
        }
    }

    private int[] getPictureKeys(Map<Integer, String> deletePictureMap) {
        int[] keys = new int[deletePictureMap.size()];
        int i = 0;
        for (Integer key : deletePictureMap.keySet()) {
            keys[i] = key;
            i++;
        }
        return keys;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                //获取图片选择器选中的图片
                mPictureList.addAll(data.getStringArrayListExtra("selectedPictures"));
                //清空 adapter 中选中图片列表
                mAdapter.getSelectedPictures().clear();
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDeleteLayout.getVisibility() == View.VISIBLE) {
            hideDeleteLayout();
            mAdapter.setAdapterType(PicturePickerAdapter.GALLERY_TYPE);
            mAdapter.getSelectedPictures().clear();
            mAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

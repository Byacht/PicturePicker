package com.byacht.picturepicker.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.byacht.picturepicker.R;
import com.byacht.picturepicker.adapter.PictureFolderAdapter;
import com.byacht.picturepicker.adapter.PicturePickerAdapter;
import com.byacht.picturepicker.bean.PictureFolderEvent;
import com.byacht.picturepicker.bean.SelectedNumberEvent;
import com.byacht.picturepicker.presenter.implPresenter.PicturePresenter;
import com.byacht.picturepicker.presenter.implView.IPictureView;
import com.byacht.picturepicker.ui.DialogItemDecoration;
import com.byacht.picturepicker.ui.PictureItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PicturePickActivity extends AppCompatActivity implements IPictureView {

    @BindView(R.id.toolbar_picture_picker)
    Toolbar mToolbar;
    @BindView(R.id.tv_folder_name_toolbar)
    TextView mTvFolderName;
    @BindView(R.id.rv_picture_picker)
    RecyclerView mRvPicturePicker;
    @BindView(R.id.btn_picture_select_finish)
    Button mBtnSelectFinish;
    @BindView(R.id.tv_preview)
    TextView mTvPreview;
    @BindView(R.id.tv_folder_name)
    TextView mTvChooseFolderName;

    private PicturePickerAdapter mAdapter;
    private Map<String, List<String>> mAllPictureMap;
    //所有图片路径名称
    private List<String> mPictureFolderList = new ArrayList<String>();
    //当前图片路径下的所有图片
    private List<String> mPictureList;
    //每个图片路径下的第一张图片
    private List<String> mFirstPictureList;
    //每个图片路径下的图片数量
    private int[] mPictureNumbers;

    //当前位于第几个路径
    private int mCurrentFolderPosition = 0;

    private PicturePresenter mPresenter;

    private Dialog chooseFolderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_pick);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        setupToolbar();
        setupRv();

        mPresenter = new PicturePresenter(this);
        mPresenter.getPictures();
    }

    private void setupToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupRv() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mRvPicturePicker.setLayoutManager(layoutManager);

        mPictureList = new ArrayList<String>();
        mAdapter = new PicturePickerAdapter(this, mPictureList, PicturePickerAdapter.PICKER_TYPE);
        mRvPicturePicker.setAdapter(mAdapter);
        mRvPicturePicker.addItemDecoration(new PictureItemDecoration(this));
    }

    @Override
    public void showPictures(Map<String, List<String>> map) {
        if (map != null && map.size() > 0) {
            mAllPictureMap = map;
            mFirstPictureList = new ArrayList<String>();
            //根据每个路径下图片的数量按降序排列
            int i = 0;
            mPictureNumbers = new int[map.size()];
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (i == 0) {
                    //显示图片数量最多的路径下的所有图片
                    List<String> pictureList = entry.getValue();
                    mPictureList.addAll(pictureList);
                    mAdapter.notifyDataSetChanged();
                    //更新对应路径的名称
                    mTvFolderName.setText(entry.getKey());
                    mTvChooseFolderName.setText(entry.getKey());
                }
                mPictureFolderList.add(entry.getKey());
                mFirstPictureList.add(entry.getValue().get(0));
                mPictureNumbers[i] = entry.getValue().size();
                i++;
            }
        }
    }

    @OnClick(R.id.btn_picture_select_finish)
    public void goBackPictureGallery() {
        Intent intent = new Intent();
        //携带选中的图片的数据返回到 MainActivity
        intent.putStringArrayListExtra("selectedPictures", convertMapToList(mAdapter.getSelectedPictures()));
        this.setResult(1, intent);
        finish();
    }

    private ArrayList<String> convertMapToList(Map<Integer, String> map) {
        Set set = map.keySet();
        Iterator iterator = set.iterator();
        ArrayList<String> list = new ArrayList<String>();
        for (String path : map.values()) {
            list.add(path);
        }
        return list;
    }

    //选择其它图片的路径
    @OnClick(R.id.tv_folder_name)
    public void choosePictureFolder() {
        showDialog();
    }

    private void showDialog() {
        chooseFolderDialog = new Dialog(this, R.style.BottomDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_picture_folder, null);
        chooseFolderDialog.setContentView(dialogView);

        //设置 dialog 宽度为全屏
        ViewGroup.LayoutParams layoutParams = dialogView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        dialogView.setLayoutParams(layoutParams);

        //设置 dialog 内容列表
        setupDialogRv(dialogView);

        //设置 dialog 进场动画及位置
        Window dialogWindow = chooseFolderDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setWindowAnimations(R.style.BottomDialog_Animation);

        chooseFolderDialog.show();
    }

    private void setupDialogRv(View dialogView) {
        RecyclerView rvDialog = (RecyclerView) dialogView.findViewById(R.id.rv_picture_folder);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvDialog.setLayoutManager(layoutManager);
        PictureFolderAdapter folderAdapter = new PictureFolderAdapter(this, mPictureFolderList, mFirstPictureList, mPictureNumbers, mCurrentFolderPosition);
        rvDialog.setAdapter(folderAdapter);
        rvDialog.addItemDecoration(new DialogItemDecoration(PicturePickActivity.this));
    }

    //预览选中的图片
    @OnClick(R.id.tv_preview)
    public void previewPicture() {
        if (mAdapter.getSelectedPictures().size() > 0) {
            Intent intent = new Intent(PicturePickActivity.this, PictureShowActivity.class);
            intent.putExtra("type", PicturePickerAdapter.PICKER_TYPE);
            intent.putExtra("isPreview", true);
            intent.putStringArrayListExtra("allPictures", convertMapToList(mAdapter.getSelectedPictures()));
            intent.putExtra("selectedPictures", (Serializable) mAdapter.getSelectedPictures());
            startActivity(intent);
        }
    }

    //选中图片时更新相关的 ui
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateUIEvent(SelectedNumberEvent event) {
        if (event.selectedNumber == 0) {
            mBtnSelectFinish.setText("取消");
            mTvPreview.setTextColor(getResources().getColor(R.color.textColor));
        } else {
            mBtnSelectFinish.setText("完成(" + event.selectedNumber + ")");
            mTvPreview.setTextColor(getResources().getColor(R.color.colorAccent));
        }

    }

    //更换图片路径
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void replaceFolder(PictureFolderEvent event) {
        if (event.position != mCurrentFolderPosition) {
            String folderName = null;
            int i = 0;
            for (Map.Entry<String, List<String>> entry : mAllPictureMap.entrySet()) {
                if (i == event.position) {
                    folderName = entry.getKey();
                    mPictureList.clear();
                    mPictureList.addAll(entry.getValue());
                    break;
                }
                i++;
            }
            mAdapter.getSelectedPictures().clear();
            mAdapter.notifyDataSetChanged();

            mTvFolderName.setText(folderName);
            chooseFolderDialog.dismiss();
            mCurrentFolderPosition = event.position;
            mTvChooseFolderName.setText(folderName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

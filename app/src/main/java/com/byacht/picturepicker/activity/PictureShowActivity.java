package com.byacht.picturepicker.activity;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byacht.picturepicker.R;
import com.byacht.picturepicker.adapter.PicturePickerAdapter;
import com.byacht.picturepicker.adapter.PictureShowPagerAdapter;
import com.byacht.picturepicker.bean.SelectedPicturesEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PictureShowActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_picture_show)
    Toolbar mToolbar;
    @BindView(R.id.vp_picture_show)
    ViewPager mViewPager;
    @BindView(R.id.cb_picture_choose_show)
    CheckBox mCbPictureChoose;
    @BindView(R.id.tv_picture_choose)
    TextView mTvPictureChoose;
    @BindView(R.id.layout_picture_show)
    RelativeLayout mLayoutPictureShow;

    private PictureShowPagerAdapter mAdapter;
    //展示的图片
    private List<String> mPicturePathList;

    //是否为预览模式
    private boolean isPreview = false;
    //展示的类型，若为 GALLERY，则隐藏下方的选择控件
    private int type;

    //选中的图片
    private Map<Integer, String> mSelectedPictureMap;
    private Map<Integer, String> mTempMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);
        ButterKnife.bind(this);

        mSelectedPictureMap = new LinkedHashMap<Integer, String>();

        int position = getIntent().getIntExtra("position", 0);
        type = getIntent().getIntExtra("type", PicturePickerAdapter.GALLERY_TYPE);
        isPreview = getIntent().getBooleanExtra("isPreview", false);
        if (type == PicturePickerAdapter.PICKER_TYPE) {
            mSelectedPictureMap.putAll((Map<Integer, String>) getIntent().getSerializableExtra("selectedPictures"));
            if (isPreview) {
                mTempMap = new LinkedHashMap<Integer, String>();
                mTempMap.putAll(mSelectedPictureMap);
            }
        }
        mPicturePathList = getIntent().getStringArrayListExtra("allPictures");

        if (type == PicturePickerAdapter.GALLERY_TYPE) {
            mLayoutPictureShow.setVisibility(View.GONE);
        } else {
            mLayoutPictureShow.setVisibility(View.VISIBLE);
        }
        mToolbar.setTitle(position + 1 + "/" + mPicturePathList.size());
        updateCheckBox(position);
        updateTv();
        setupToolbar();
        setupViewPager(position);

        mCbPictureChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int currentPosition = mViewPager.getCurrentItem();
                /**
                 * 根据是否为预览模式对 mSelectedPictureMap 进行相对应的操作
                 * 在非预览模式下，图片的 key 值与 currentPosition 相对应，无需进行额外操作
                 * 在预览模式下，需根据 currentPosition 先获取图片的 value 值，再根据此 value 找到对应的 key
                 */
                if (isPreview) {
                    String value = mPicturePathList.get(currentPosition);
                    if (isChecked) {
                        if (!mSelectedPictureMap.containsValue(value)) {
                            mSelectedPictureMap.put(getKey(mTempMap, value), value);
                            updateTv();
                        }
                    } else {
                        if (mSelectedPictureMap.containsValue(value)) {
                            mSelectedPictureMap.remove(getKey(mTempMap, value));
                            updateTv();
                        }
                    }
                } else {
                    if (isChecked) {
                        if (!mSelectedPictureMap.containsKey(currentPosition)) {
                            mSelectedPictureMap.put(currentPosition, mPicturePathList.get(currentPosition));
                            updateTv();
                        }
                    } else {
                        if (mSelectedPictureMap.containsKey(currentPosition)) {
                            mSelectedPictureMap.remove(currentPosition);
                            updateTv();
                        }
                    }
                }
            }
        });

    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == PicturePickerAdapter.PICKER_TYPE) {
                    EventBus.getDefault().post(new SelectedPicturesEvent(mSelectedPictureMap));
                }
                finish();
            }
        });
    }

    private void setupViewPager(int position) {
        mAdapter = new PictureShowPagerAdapter(mPicturePathList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCheckBox(position);
                mToolbar.setTitle(position + 1 + "/" + mPicturePathList.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private int getKey(Map<Integer, String> map, String value) {
        int key = 0;
        Set<Map.Entry<Integer, String>> set = map.entrySet();
        for(Map.Entry<Integer, String> entry : set){
            if(entry.getValue().equals(value)){
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

    @OnClick(R.id.tv_picture_choose)
    public void goBackPickerActivity() {
        if (type == PicturePickerAdapter.PICKER_TYPE) {
            EventBus.getDefault().post(new SelectedPicturesEvent(mSelectedPictureMap));
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (type == PicturePickerAdapter.PICKER_TYPE) {
            EventBus.getDefault().post(new SelectedPicturesEvent(mSelectedPictureMap));
        }
    }

    private void updateCheckBox(int position) {
        if (isPreview) {
            if (mSelectedPictureMap.containsValue(mPicturePathList.get(position))) {
                mCbPictureChoose.setChecked(true);
            } else {
                mCbPictureChoose.setChecked(false);
            }
        } else {
            if (mSelectedPictureMap.containsKey(position)) {
                mCbPictureChoose.setChecked(true);
            } else {
                mCbPictureChoose.setChecked(false);
            }
        }
    }

    private void updateTv() {
        if (mSelectedPictureMap.size() == 0) {
            mTvPictureChoose.setText("请选择");
            mTvPictureChoose.setTextColor(getResources().getColor(R.color.textColor));
        } else {
            mTvPictureChoose.setText("完成(" + mSelectedPictureMap.size() + ")");
            mTvPictureChoose.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

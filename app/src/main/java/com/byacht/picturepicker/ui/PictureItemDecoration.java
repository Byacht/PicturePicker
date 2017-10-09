package com.byacht.picturepicker.ui;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.byacht.picturepicker.R;

/**
 * Created by dn on 2017/9/30.
 */

public class PictureItemDecoration extends RecyclerView.ItemDecoration {

    private int mDivider;

    public PictureItemDecoration(Context context) {
        mDivider = context.getResources().getDimensionPixelSize(R.dimen.divider_height);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int pos = parent.getChildAdapterPosition(view);
        if (pos % 3 == 2) {
            outRect.set(mDivider, mDivider, mDivider, 0);
        } else {
            outRect.set(mDivider, mDivider, 0, 0);
        }
    }

}

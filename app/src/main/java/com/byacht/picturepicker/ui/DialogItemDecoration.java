package com.byacht.picturepicker.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.byacht.picturepicker.R;

/**
 * Created by dn on 2017/10/8.
 */

public class DialogItemDecoration extends RecyclerView.ItemDecoration {

    private int mDividerHeight;
    private Paint mPaint;
    private int mPadding = 10;

    public DialogItemDecoration(Context context) {
        mPaint = new Paint();
        mPaint.setColor(context.getResources().getColor(R.color.dividerColor));
        mDividerHeight = context.getResources().getDimensionPixelSize(R.dimen.divider_height);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount - 1; i++) {
            View view = parent.getChildAt(i);
            float top = view.getBottom();
            float bottom = view.getBottom() + mDividerHeight;
            c.drawRect(left + mPadding, top, right - mPadding, bottom, mPaint);
        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = mDividerHeight;
    }
}

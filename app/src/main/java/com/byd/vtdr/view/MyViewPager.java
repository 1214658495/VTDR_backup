package com.byd.vtdr.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by byd_tw on 2017/11/22.
 */

public class MyViewPager extends ViewPager {
    private static final String TAG = "MyImagesViewPager";

    private OnClickListener mOnClickListener;
    public MyViewPager(Context context) {
        super(context);
        setup();
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
//        改为如下则photoview无法监听点击事件。
//        try {
//            return super.onTouchEvent(ev);
//        } catch (IllegalArgumentException ex) {
//            ex.printStackTrace();
//        }
//        return false;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
////        try {
////            return super.onTouchEvent(ev);
////        } catch (IllegalArgumentException ex) {
////            ex.printStackTrace();
////        }
////        return false;
//
//        try {
//            return super.onInterceptTouchEvent(ev);
//        } catch (IllegalArgumentException e) {
//            return true;
//        }
//    }

//
    public interface OnClickListener {
        void onViewPagerClick(ViewPager viewPager);
    }

    private void setup() {
        final GestureDetector gestureDetector = new GestureDetector(getContext(),new TapGestureListener());
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    public void setOnViewPagerClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    private class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(mOnClickListener != null) {
                Log.e(TAG, "onSingleTapConfirmed: /////////");
                mOnClickListener.onViewPagerClick(MyViewPager.this);
            }

            return true;
        }
    }
}

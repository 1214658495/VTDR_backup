package com.byd.vtdr;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.byd.vtdr.view.MyDialog;
import com.byd.vtdr.view.MyViewPager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 *
 * @author byd_tw
 * @date 2017/11/28
 */

public class ActivityImagesViewPager extends AppCompatActivity {
    //类名若要更改，需提交给5部，不然无法实现全屏
    private static final String TAG = "ActivityImagesViewPager";
    @BindView(R.id.vp_viewPager)
    MyViewPager vpViewPager;
    @BindView(R.id.tv_vpIndex)
    TextView tvVpIndex;
    @BindView(R.id.btn_back_to_gridview)
    ImageButton btnBackToGridview;
    @BindView(R.id.tv_title_photo)
    TextView tvTitlePhoto;
    @BindView(R.id.rl_bar_showTitle)
    RelativeLayout rlBarShowTitle;
    @BindView(R.id.btn_share_preview)
    ImageButton btnSharePreview;
    @BindView(R.id.btn_delete_preview)
    ImageButton btnDeletePreview;
    @BindView(R.id.btn_zoom)
    ImageButton btnZoom;
    @BindView(R.id.ll_bar_editPhoto)
    LinearLayout llBarEditPhoto;

    private MyImagesPagerAdapter myImagesPagerAdapter;

    private ArrayList<Model> photoLists;

    private int currentItem;

    private static final int FADE_OUT = 1;
    private MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private WeakReference<ActivityImagesViewPager> mActivityViewPager;

        MyHandler(ActivityImagesViewPager activityImagesViewPager) {
            mActivityViewPager = new WeakReference<>(activityImagesViewPager);
        }

        @Override
        public void handleMessage(Message msg) {
            ActivityImagesViewPager activityViewPager = mActivityViewPager.get();
            if (activityViewPager != null) {
                switch (msg.what) {
                    case FADE_OUT:
                        if (activityViewPager.rlBarShowTitle.getVisibility() == View.VISIBLE) {
                            activityViewPager.rlBarShowTitle.setVisibility(View.INVISIBLE);
                        }

                        if (activityViewPager.llBarEditPhoto.getVisibility() == View.VISIBLE) {
                            activityViewPager.llBarEditPhoto.setVisibility(View.INVISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.layout_images_viewpager);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        photoLists = new ArrayList<>();
        Intent intent = getIntent();
//        urlList = intent.getStringArrayListExtra("mUrlsList");
        photoLists = (ArrayList<Model>) intent.getSerializableExtra("mPhotoList");
        currentItem = intent.getIntExtra("position", 0);
        myImagesPagerAdapter = new MyImagesPagerAdapter(photoLists, this);
        vpViewPager.setAdapter(myImagesPagerAdapter);
        vpViewPager.setCurrentItem(currentItem, false);
        vpViewPager.setOffscreenPageLimit(0);
//        tvVpIndex.setText(currentItem + 1 + "/" + urlList.size());
        tvTitlePhoto.setText(photoLists.get(currentItem).getName());
        tvVpIndex.setText(currentItem + 1 + "/" + photoLists.size());
        vpViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentItem = position;
                tvVpIndex.setText(currentItem + 1 + "/" + photoLists.size());
                tvTitlePhoto.setText(photoLists.get(currentItem).getName());
            }
        });


        Message msg = mHandler.obtainMessage(FADE_OUT);
        mHandler.removeMessages(FADE_OUT);
        mHandler.sendMessageDelayed(msg, 3000);
    }

//    如下把最下面虚拟键也隐藏了
   /* @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }*/

    @OnClick({R.id.btn_back_to_gridview, R.id.btn_share_preview, R.id.btn_delete_preview, R.id.btn_zoom})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back_to_gridview:
                this.finish();
                // TODO: 2017/11/29 删除完成了，需要去更新gridview
                break;
            case R.id.btn_share_preview:
                break;
            case R.id.btn_delete_preview:
                MyDialog myDialog = MyDialog.newInstance(0, getString(R.string.confirm_delete));
                myDialog.show(getFragmentManager(), "delete");
                myDialog.setOnDialogButtonClickListener(new MyDialog.OnDialogButtonClickListener() {
                    @Override
                    public void okButtonClick() {
                        // TODO: 2017/11/29  删除照片
//                        myImagesPagerAdapter.destroyItem(vpViewPager,currentItem,vpViewPager.);
//                        vpViewPager.removeViewAt(vpViewPager.getCurrentItem());
                        photoLists.remove(currentItem);
                        myImagesPagerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void cancelButtonClick() {
                    }
                });
                break;
            case R.id.btn_zoom:
                finish();
                break;
            default:
                break;
        }
    }

    public class MyImagesPagerAdapter extends PagerAdapter {

        //        private ArrayList<String> imageUrls;
        private ArrayList<Model> mPhotoLists;
        private AppCompatActivity activity;

        MyImagesPagerAdapter(ArrayList<Model> mPhotoLists, AppCompatActivity activity) {
//            this.imageUrls = imageUrls;
            this.mPhotoLists = mPhotoLists;
            this.activity = activity;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            String url = imageUrls.get(position);
//            String url = "http://" + ServerConfig.VTDRIP + "/SD0/NORMAL/" +
//            model.getName();
            String url = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" + mPhotoLists.get(position).getName();
            PhotoView photoView = new PhotoView(activity);
            Glide.with(activity)
                    .load(url)
                    .into(photoView);
            container.addView(photoView);
//        photoView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                activity.finish();
////                Log.e(TAG, "onClick: photoView.setOnClickListener");
//            }
//        });
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v1) {
                    showTitleBar();
                }

                @Override
                public void onOutsidePhotoTap() {
                    showTitleBar();
                }
            });
            return photoView;
        }

        void showTitleBar() {
            //                    如下考虑算法优化
            if (rlBarShowTitle.getVisibility() == View.VISIBLE) {
                rlBarShowTitle.setVisibility(View.INVISIBLE);
            } else {
                rlBarShowTitle.setVisibility(View.VISIBLE);
            }

            if (llBarEditPhoto.getVisibility() == View.VISIBLE) {
                llBarEditPhoto.setVisibility(View.INVISIBLE);
            } else {
                llBarEditPhoto.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getCount() {
//            return imageUrls != null ? imageUrls.size() : 0;
            return mPhotoLists != null ? mPhotoLists.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(ServerConfig.BORADCAST_ACTION_TAKEPHOTO);
      /*  Intent intent = new Intent(ServerConfig.BORADCAST_ACTION_EXIT);
//        intent.setAction(ServerConfig.BORADCAST_ACTION_EXIT);
//        intent.setAction(ServerConfig.BORADCAST_ACTION_TAKEPHOTO);
        intent.setPackage("com.byd.vtdr2");
//        intent.putExtra("extra_key_command", "extra_command_takephoto");
        intent.putExtra("extra_key_command", "extra_command_exit");
        sendBroadcast(intent);*/
        super.onBackPressed();
    }
}

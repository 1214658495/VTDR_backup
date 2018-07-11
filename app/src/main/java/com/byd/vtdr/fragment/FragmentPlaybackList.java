package com.byd.vtdr.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.byd.vtdr.MainActivity;
import com.byd.vtdr.MessageEvent;
import com.byd.vtdr.Model;
import com.byd.vtdr.MyApplication;
import com.byd.vtdr.R;
import com.byd.vtdr.RemoteCam;
import com.byd.vtdr.ServerConfig;
import com.byd.vtdr.adapter.MyFragmentPagerAdapter;
import com.byd.vtdr.connectivity.IFragmentListener;
import com.byd.vtdr.utils.DownloadUtil;
import com.byd.vtdr.view.MyDialog;
import com.byd.vtdr.view.ProgressDialogFragment;
import com.byd.vtdr.widget.LightTextView;
import com.byd.vtdr.widget.Theme;
import com.byd.vtdr.widget.ThemeCheckBox;
import com.byd.vtdr.widget.ThemeLightButton;
import com.byd.vtdr.widget.ThemeLightRadioButton;
import com.byd.vtdr.widget.ThemeManager;
import com.jakewharton.disklrucache.DiskLruCache;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * Created by byd_tw on 2017/11/1.
 */

public class FragmentPlaybackList extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "FragmentPlaybackList";
    @BindView(R.id.rg_groupDetail)
    public RadioGroup rgGroupDetail;
    //    @BindView(R.id.vp_itemPreview)
//    NoScrollViewPager vpItemPreview;

    Unbinder unbinder;
    @BindView(R.id.rb_recordvideo)
    ThemeLightRadioButton rbRecordvideo;
    @BindView(R.id.rb_lockvideo)
    ThemeLightRadioButton rbLockvideo;
    @BindView(R.id.rb_capturephoto)
    ThemeLightRadioButton rbCapturephoto;
    @BindView(R.id.gv_dataList)
    GridView mGridViewList;

    @BindView(R.id.btn_cancel)
    ThemeLightButton btnCancel;
    @BindView(R.id.btn_share)
    ThemeLightButton btnShare;
    @BindView(R.id.btn_export)
    ThemeLightButton btnExport;
    @BindView(R.id.btn_delete)
    ThemeLightButton btnDelete;
    @BindView(R.id.btn_selectall)
    ThemeCheckBox btnSelectall;
    @BindView(R.id.ll_editItemBar)
    LinearLayout llEditItemBar;
    @BindView(R.id.tv_editNav)
    LightTextView tvEditNav;
    //    @BindView(R.id.rl_menu_edit)
//    RelativeLayout rlMenuEdit;
    @BindView(R.id.iv_line_blowMenuEdit)
    ImageView ivLineBlowMenuEdit;
    @BindView(R.id.ib_search)
    ImageButton ibSearch;
    @BindView(R.id.fl_videoPlayPreview)
    FrameLayout flVideoPlayPreview;

    //    FragmentVideoPlay fragmentVideoPreview;
    public static FragmentVideoPreview fragmentVideoPreview;
    public static FragmentPhotoPreview fragmentPhotoPreview;

    private ProgressBar listLoadingView;

    private List<Fragment> fragments;
    private MyFragmentPagerAdapter myFragmentPagerAdapter;
    public PhotoWallAdapter mAdapter;
    public boolean isMultiChoose = false;

    private int screenHeight;
    private int screenWidth;

    private ArrayList<String> urlsList;
    private ArrayList<String> urlVideosList;

    private RemoteCam mRemoteCam;
    private String mPWD;
    private IFragmentListener mListener;

    private ArrayList<Model> mPlayLists;
    private ArrayList<Model> mSelectedLists = new ArrayList<>();
    public int currentRadioButton = ServerConfig.RB_RECORD_VIDEO;

    public volatile boolean isYuvDownload = false;
    public volatile boolean isThumbGetFail = false;
    private MyDialog myDialogTest;
    private ProgressDialogFragment progressDialogFragment;
    private Fragment listFragment;
    private MyDialog myDialog;
    private int doingDownFileCounts;
    private Intent shareIntent;
    private int lastPosition = -1;
//    public static MyThread resush;
    MyApplication myApplication;
    public static boolean isLockVideo;
    private static final int NOTIFY_LIST_UPDATE = 1;
    private UpdateListHandler updateListHandler = new UpdateListHandler(this);
    private RefreshListThread refreshListThread;

    public static FragmentPlaybackList newInstance() {
        FragmentPlaybackList fragmentPlaybackList = new FragmentPlaybackList();

        return fragmentPlaybackList;
    }

    public void setRemoteCam(RemoteCam mRemoteCam) {
        this.mRemoteCam = mRemoteCam;
    }

    //存放计算后的单元格相关信息
    @Override
    public void onAttach(Activity activity) {
        Log.e(TAG, "onAttach: ");
        super.onAttach(activity);
        try {
            mListener = (IFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication = (MyApplication) (getActivity().getApplicationContext());
        Log.e(TAG, "onCreate: ");
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

     /*   int mode = ThemeManager.getInstance().getTheme();
        if (mode == Theme.SPORT) {
//            //运动模式
            loadSkinTheme(Theme.SPORT);
        } else {
//            //经济模式
            loadSkinTheme(Theme.NORMAL);
        }*/
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_playback, container, false);
        listLoadingView = view.findViewById(R.id.listLoadingView);
        unbinder = ButterKnife.bind(this, view);

        int mode = ThemeManager.getInstance().getTheme();
        if (mode == Theme.SPORT) {
//            //运动模式
            loadSkinTheme(Theme.SPORT);
        } else {
//            //经济模式
            loadSkinTheme(Theme.NORMAL);
        }

        initData();
//        if (resush == null) {
        if (refreshListThread == null) {
            rueshcontrol = true;
//            resush = new MyThread();
            refreshListThread = new RefreshListThread(this);
            refreshListThread.start();
//            resush.start();
        }

        return view;

    }

    public void loadSkinTheme(int theme) {
        if (theme == Theme.NORMAL) {
            rbRecordvideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_recordvideo_selector),
                    null, null, null);
            rbLockvideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_lockvideo_selector),
                    null, null, null);
            rbCapturephoto.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_images_selector),
                    null, null, null);
            btnCancel.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_cancel_selector),
                    null, null, null);
            btnExport.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_export_selector),
                    null, null, null);
            btnDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_delete_selector),
                    null, null, null);
            btnShare.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_share_selector),
                    null, null, null);
            btnSelectall.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_selectall_selector),
                    null, null, null);
            tvEditNav.setLight(getResources().getColor(R.color.lightone), 22);


        } else if (theme == Theme.SPORT) {
            rbRecordvideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_recordvideo_selector_sport),
                    null, null, null);
            rbLockvideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_lockvideo_selector_sport),
                    null, null, null);
            rbCapturephoto.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_images_selector_sport),
                    null, null, null);
            btnCancel.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_cancel_selector_sport),
                    null, null, null);
            btnExport.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_export_selector_sport),
                    null, null, null);
            btnDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_delete_selector_sport),
                    null, null, null);
            btnShare.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_share_selector_sport),
                    null, null, null);
            btnSelectall.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit_selectall_selector_sport),
                    null, null, null);
            tvEditNav.setLight(getResources().getColor(R.color.sport_color), 22);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        int mode = ThemeManager.getInstance().getTheme();
        if (mode == Theme.SPORT) {
//            //运动模式
            loadSkinTheme(Theme.SPORT);
        } else {
//            //经济模式
            loadSkinTheme(Theme.NORMAL);
        }
    }

    private void initData() {
        ((MainActivity) getActivity()).isDialogShow = false;
        if (mAdapter == null) {
//            mPWD = mRemoteCam.videoFolder();
            mPWD = "/tmp/SD0/NORMAL";
            listDirContents(mPWD);
        } else {
            if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                mPWD = "/tmp/SD0/NORMAL";
                listDirContents(mPWD);
            } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                mGridViewList.setAdapter(mAdapter);
            } else if (currentRadioButton == ServerConfig.RB_CAPTURE_PHOTO) {
                mGridViewList.setAdapter(mAdapter);

            }
//            mGridViewList.setAdapter(mAdapter);
        }

//        mPlayLists = new ArrayList<>();
//        mSelectedLists = new ArrayList<>();

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //屏幕高度
            screenHeight = dm.heightPixels;
            //屏幕宽度
            screenWidth = dm.widthPixels;
        } else {
            screenHeight = dm.heightPixels;
            screenWidth = (int) (dm.widthPixels * 0.52);
        }
//        ColumnInfo colInfo = calculateColumnWidthAndCountInRow(screenWidth, 90,8);

        rgGroupDetail.check(R.id.rb_recordvideo);

        rgGroupDetail.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_recordvideo:
                        mGridViewList.setFastScrollAlwaysVisible(true);
                        mGridViewList.setFastScrollEnabled(true);
                        mGridViewList.setScrollbarFadingEnabled(true);

                        if (rbRecordvideo.isChecked()) {
                            currentRadioButton = ServerConfig.RB_RECORD_VIDEO;
                            mGridViewList.setNumColumns(1);
                            showRecordList();
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    case R.id.rb_lockvideo:
                        mGridViewList.setFastScrollAlwaysVisible(true);
                        mGridViewList.setFastScrollEnabled(true);
                        mGridViewList.setScrollbarFadingEnabled(true);

                        currentRadioButton = ServerConfig.RB_LOCK_VIDEO;
                        mGridViewList.setNumColumns(1);
                        showLockVideoList();
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    case R.id.rb_capturephoto:
                        mGridViewList.setFastScrollAlwaysVisible(false);
                        mGridViewList.setFastScrollEnabled(false);
                        mGridViewList.setScrollbarFadingEnabled(false);
                        currentRadioButton = ServerConfig.RB_CAPTURE_PHOTO;

                        ColumnInfo colInfo = calculateColumnWidthAndCountInRow(screenWidth, 300, 12);
                        int rowNum = mGridViewList.getCount() % colInfo.countInRow == 0 ? mGridViewList.getCount() / colInfo.countInRow : mGridViewList.getCount() / colInfo.countInRow + 1;
                        mGridViewList.setNumColumns(colInfo.countInRow);
                        showCapturePhotoList();
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }

                disableRadioGroup(rgGroupDetail);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableRadioGroup(rgGroupDetail);
                    }
                }, 500);

//                mGridViewList.setAdapter(mAdapter);

            }
        });

//        mAdapter = new PhotoWallAdapter(getContext(), 0, Images.imageThumbUrls, mGridViewList);
//        mGridViewList.setAdapter(mAdapter);

        mGridViewList.setNumColumns(1);
        mGridViewList.setOnItemClickListener(this);
        mGridViewList.setOnItemLongClickListener(this);
    }

    private void listDirContents(String psd) {
        if (psd != null) {
            if (mListener != null) {
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_LS, psd);
            }
        }
    }

    public void updateDirContents(JSONObject parser) {
        ArrayList<Model> models = new ArrayList<Model>();

        try {
            JSONArray contents = parser.getJSONArray("listing");

            for (int i = 0; i < contents.length(); i++) {
                models.add(new Model(contents.getJSONObject(i).toString()));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        Collections.sort(models, new order());
        if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO && models.size() > 0) {
            if (myApplication.getisRescod()) {
                models.remove(0);
            }
        } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO && models.size() > 0) {
           /* if (isLockVideo) {
                models.remove(0);
            }*/
        }
        mPlayLists = models;
        if (getActivity() != null) {
            mAdapter = new PhotoWallAdapter(getActivity(), 0, mPlayLists, mGridViewList);
            mGridViewList.setAdapter(mAdapter);
        }
//        mAdapter = new DentryAdapter(models);
//        showDirContents();
    }

    public void showRecordList() {
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.cancelAllTasks();
        }
        mPWD = "/tmp/SD0/NORMAL";
        listDirContents(mPWD);
        if (isMultiChoose) {
            cancelMultiChoose();
        }
    }

    public void showLockVideoList() {
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.cancelAllTasks();
        }
        mPWD = "/tmp/SD0/EVENT";
        listDirContents(mPWD);
        if (isMultiChoose) {
            cancelMultiChoose();
        }
    }

    public void showCapturePhotoList() {
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.cancelAllTasks();
        }
        mPWD = "/tmp/SD0/PHOTO";
        listDirContents(mPWD);
        if (isMultiChoose) {
            cancelMultiChoose();
        }
    }

    public void cancelMultiChoose() {
        isMultiChoose = false;
        llEditItemBar.setVisibility(View.INVISIBLE);
        rgGroupDetail.setVisibility(View.VISIBLE);
//        ibSearch.setVisibility(View.VISIBLE);
        btnSelectall.setChecked(false);
        mSelectedLists.clear();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            mAdapter.isSelectedMap.clear();

        }
    }

    public void showLoadView(boolean isShow) {
        if (isShow) {
            if (listLoadingView != null) {
                listLoadingView.setVisibility(View.VISIBLE);
            }
        } else {
            if (listLoadingView != null) {
                listLoadingView.setVisibility(View.INVISIBLE);
            }
        }
    }


    public void showSD() {

        listDirContents(mPWD);
    }

    class ColumnInfo {
        //单元格宽度。
        public int width = 0;
        //每行所能容纳的单元格数量。
        public int countInRow = 0;

    }

    /**
     * 根据手机屏幕宽度，计算gridview每个单元格的宽度
     *
     * @param screenWidth 屏幕宽度
     * @param width       单元格预设宽度
     * @param padding     单元格间距
     * @return
     */
    private ColumnInfo calculateColumnWidthAndCountInRow(int screenWidth, int width,
                                                         int padding) {
        ColumnInfo colInfo = new ColumnInfo();
        int colCount = 0;
        //判断屏幕是否刚好能容纳下整数个单元格，若不能，则将多出的宽度保存到space中
        int space = screenWidth % width;

        if (space == 0) { //正好容纳下
            colCount = screenWidth / width;
        } else if (space >= (width / 2)) { //多出的宽度大于单元格宽度的一半时，则去除最后一个单元格，将其所占的宽度平分并增加到其他每个单元格中
            colCount = screenWidth / width;
            space = width - space;
            width = width + space / colCount;
        } else {  //多出的宽度小于单元格宽度的一半时，则将多出的宽度平分，并让每个单元格减去平分后的宽度
            colCount = screenWidth / width + 1;
            width = width - space / colCount;
        }

        colInfo.countInRow = colCount;
        //计算出每行的间距总宽度，并根据单元格的数量重新调整单元格的宽度
        colInfo.width = width - ((colCount + 1) * padding) / colCount;
        return colInfo;
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause: ");
        super.onPause();
        if (mAdapter != null) {
            mAdapter.fluchCache();
        }
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop: ");
        super.onStop();
        EventBus.getDefault().unregister(this);
        cancelMultiChoose();
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: ");
        rueshcontrol = false;
//        resush.interrupt();
//        resush = null;
        refreshListThread.interrupt();
        refreshListThread = null;
        unbinder.unbind();
        // 退出程序时结束所有的下载任务
        if (mAdapter != null) {
            mAdapter.cancelAllTasks();
//            添加
//            mAdapter.isClickedMap.clear();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*RefWatcher refWatcher = MyApplication.getRefWatcher(getContext());
        refWatcher.watch(this);*/
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach: ");
        super.onDetach();
//        if (mAdapter != null) {
//            mAdapter.cancelAllTasks();
//            mAdapter.clear();
//        }
        mListener = null;
    }

    public Fragment getListFragment() {
        return listFragment;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvTime = view.findViewById(R.id.tv_time);
        if (!isMultiChoose) {
            Intent intent;
            if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                if (lastPosition >= 0) {
                    mAdapter.setItemIsClickedMap(lastPosition, false);
                }
                Boolean isClicked = mAdapter.getIsSelectedAt(i);
                mAdapter.setItemIsClickedMap(i, !isClicked);
                lastPosition = i;
               /* if (lastPosition >= 0) {
                    View view1 = adapterView.getChildAt(lastPosition);
                    TextView tvTitle1 = view1.findViewById(R.id.tv_title);
                    TextView tvTime1 = view1.findViewById(R.id.tv_time);
                    tvTitle1.setTextColor(Color.WHITE);
                    tvTime1.setTextColor(Color.WHITE);
                }
                tvTitle.setTextColor(Color.BLUE);
                tvTime.setTextColor(Color.BLUE);
                lastPosition = i;*/
                Model model = (Model) adapterView.getItemAtPosition(i);
                String url = "http://" + ServerConfig.VTDRIP + "/SD0/NORMAL/" +
                        model.getName();
//                fragmentVideoPreview = FragmentVideoPlay.newInstance(urlVideosList,urlVideosList.get(i));
                fragmentVideoPreview = FragmentVideoPreview.newInstance(url);
//                fragmentVideoPreview.show(getFragmentManager(),"videoPlay");

                fragmentTransaction.add(flVideoPlayPreview.getId(), fragmentVideoPreview, "fragmentVideoPreview").addToBackStack("fragmentVideoPreview").commitAllowingStateLoss();
//                getFragmentManager().beginTransaction().hide(this).add(flVideoPlayPreview.getId(), fragmentVideoPreview).commitAllowingStateLoss();
//                flVideoPlayPreview.setClickable(true);
//                intent = new Intent(view.getContext(), ActivityVideoViewPager.class);
//                intent.putStringArrayListExtra("mUrlsList", urlVideosList);

            } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                if (lastPosition >= 0) {
                    mAdapter.setItemIsClickedMap(lastPosition, false);
                }
                Boolean isClicked = mAdapter.getIsSelectedAt(i);
                mAdapter.setItemIsClickedMap(i, !isClicked);
                lastPosition = i;

                Model model = (Model) adapterView.getItemAtPosition(i);
//                http://192.169.42.1/SD0/EVENT/
                String url = "http://" + ServerConfig.VTDRIP + "/SD0/EVENT/" +
                        model.getName();
                fragmentVideoPreview = FragmentVideoPreview.newInstance(url);
                fragmentTransaction.add(flVideoPlayPreview.getId(), fragmentVideoPreview).addToBackStack(null).commitAllowingStateLoss();
            } else {
//                intent = new Intent(view.getContext(), ActivityImagesViewPager.class);
//                intent.putExtra("mPhotoList", mPlayLists);
//                intent.putExtra("position", i);
//                startActivity(intent);

                fragmentPhotoPreview = FragmentPhotoPreview.newInstance();
                fragmentPhotoPreview.setRemoteCam(mRemoteCam);
                Bundle bundle = new Bundle();
                bundle.putSerializable("mPhotoList", mPlayLists);
                bundle.putInt("position", i);
                fragmentPhotoPreview.setArguments(bundle);

                fragmentTransaction.add(flVideoPlayPreview.getId(), fragmentPhotoPreview,
                        "fragmentPhotoPreview")
                        .addToBackStack(null).commitAllowingStateLoss();
            }
        } else {
//            checkbox初始状态默认为false。
            boolean isSelected = mAdapter.getIsSelectedAt(i);
//            如下判断后续使用
            if (!isSelected) {
                mSelectedLists.add(mPlayLists.get(i));

            } else {
                mSelectedLists.remove(mPlayLists.get(i));
            }
            Log.e(TAG, "onItemClick: isSelected = " + isSelected + ";i = " + i);
//            此处把指定位置变为true，并通知item更新。
            mAdapter.setItemIsSelectedMap(i, !isSelected);

        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (isMultiChoose) {

        } else {
            isMultiChoose = true;
            llEditItemBar.setVisibility(View.VISIBLE);
            rgGroupDetail.setVisibility(View.INVISIBLE);
//            ibSearch.setVisibility(View.INVISIBLE);
//            如下可能不需要
            mSelectedLists.clear();
//            view.setBackgroundColor(Color.parseColor("#1CC9FE"));
            switch (currentRadioButton) {
                case ServerConfig.RB_RECORD_VIDEO:
                    tvEditNav.setText(R.string.record_video);
                    btnShare.setVisibility(View.GONE);
                    break;
                case ServerConfig.RB_LOCK_VIDEO:
                    tvEditNav.setText(R.string.lock_video);
                    btnShare.setVisibility(View.GONE);
                    break;
                case ServerConfig.RB_CAPTURE_PHOTO:
                    tvEditNav.setText(R.string.photo);
                    btnShare.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

        }
        return false;
    }

    @OnClick({R.id.btn_cancel, R.id.btn_share, R.id.btn_export, R.id.btn_delete, R.id.btn_selectall})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                llEditItemBar.setVisibility(View.INVISIBLE);
                rgGroupDetail.setVisibility(View.VISIBLE);
//                ibSearch.setVisibility(View.VISIBLE);
                isMultiChoose = false;
                mAdapter.isSelectedMap.clear();
                btnSelectall.setChecked(false);
                break;
            case R.id.btn_share:
                final int selectedCount = mSelectedLists.size();
                if (selectedCount == 0) {
                    Toast.makeText(getContext(), R.string.select_file, Toast.LENGTH_SHORT).show();
                    break;
                }
                if (selectedCount > 9 || selectedCount == 0) {
                    Toast.makeText(getContext(), R.string.download_num, Toast.LENGTH_SHORT).show();
                    break;
                }
                shareIntent = null;

                final ArrayList<Uri> localUriList = new ArrayList<>();
                for (int i = 0; i < selectedCount; i++) {
                    String mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" +
                            mSelectedLists.get(i).getName();
                    final String fileName = Environment.getExternalStorageDirectory() + "/行车记录仪"
                            + mGetFileName.substring(mGetFileName.lastIndexOf('/'));
                    final File file = new File(fileName);
                    localUriList.add(Uri.fromFile(file));

                    if (!file.exists()) {
                        final DownloadUtil downloadUtil = DownloadUtil.get();
                        downloadUtil.download(mGetFileName, "行车记录仪", new DownloadUtil
                                .OnDownloadListener() {
                            @Override
                            public void onDownloadSuccess() {
                                synchronized (this) {
                                }
                            }

                            @Override
                            public void onDownloading(final int progress) {

                            }

                            @Override
                            public void onDownloadFailed() {

                            }

                            @Override
                            public void onDownloadStart() {
                            }
                        });
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (shareIntent == null) {
                            shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                                    localUriList);
                            shareIntent.setType("image/*");
                            startActivity(Intent.createChooser(shareIntent, getString(R.string
                                    .share_to)));
                        }
                    }
                }, 600);//600毫秒秒后执行Runnable中的run方法
                break;
            case R.id.btn_export:
                if (mSelectedLists.size() > 0) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE_MULTI, mSelectedLists);
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_DOWNLOAD, null);
                } else {
                    // TODO: 2018/1/12 后续调用主函数的showdialog方法
                    myDialogTest = MyDialog.newInstance(1, getString(R.string.select_to_download));
                    myDialogTest.show(getActivity().getFragmentManager(), "selected_delete");
                }

                isMultiChoose = false;
                llEditItemBar.setVisibility(View.INVISIBLE);
                rgGroupDetail.setVisibility(View.VISIBLE);
//                ibSearch.setVisibility(View.VISIBLE);
                btnSelectall.setChecked(false);
                mSelectedLists.clear();
                mAdapter.isSelectedMap.clear();
                break;
            case R.id.btn_delete:
                if (mSelectedLists.size() > 0) {
                    myDialogTest = MyDialog.newInstance(0, getString(R.string.confirm_delete));
                    myDialogTest.show(getActivity().getFragmentManager(), "delete");
                    myDialogTest.setOnDialogButtonClickListener(new MyDialog.OnDialogButtonClickListener() {
                        @Override
                        public void okButtonClick() {
                            mListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE_MULTI, mSelectedLists);
                            mListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE_WAITING_TIP, null);
                            for (Model model : mSelectedLists) {
                                String fileHead;
                                if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                                    fileHead = "/tmp/SD0/NORMAL/";
                                } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                                    fileHead = "/tmp/SD0/EVENT/";
                                } else {
                                    fileHead = "/tmp/SD0/PHOTO/";
                                }
                                mListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE, fileHead + model.getName());
                            }

                        }

                        @Override
                        public void cancelButtonClick() {

                        }
                    });
                } else {
                    myDialogTest = MyDialog.newInstance(1, getString(R.string.select_file));
                    myDialogTest.show(getActivity().getFragmentManager(), "selected_delete");
                }
                break;
            case R.id.btn_selectall:
                mSelectedLists.clear();
                mAdapter.isSelectedMap.clear();
                if (btnSelectall.isChecked()) {
                    for (int i = 0; i < mGridViewList.getAdapter().getCount(); i++) {
                        mAdapter.setItemIsSelectedMap(i, true);
                        mSelectedLists.add(mPlayLists.get(i));
                    }
                } else {
                    for (int i = 0; i < mGridViewList.getAdapter().getCount(); i++) {
                        mAdapter.setItemIsSelectedMap(i, false);
                    }
                }
                break;
            default:
                break;
        }
//        更新checkbox，隐藏
        mAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.ib_search)
    public void onViewClicked() {
    }

    private void share(String content, Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (uri != null) {
            //uri 是图片的地址
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            //当用户选择短信时使用sms_body取得文字
            shareIntent.putExtra("sms_body", content);
        } else {
            shareIntent.setType("text/plain");
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        //自定义选择框的标题
        //startActivity(Intent.createChooser(shareIntent, "邀请好友"));
        //系统默认标题
        startActivity(shareIntent);
    }

    private class order implements Comparator<Model> {

        @Override
        public int compare(Model lhs, Model rhs) {
            return rhs.getName().compareTo(lhs.getName());
        }

    }


    /**
     * GridView的适配器，负责异步从网络上下载图片展示在照片墙上。
     *
     * @author guolin
     */
    public class PhotoWallAdapter extends ArrayAdapter<Model> {
        private static final String TAG = "PhotoWallAdapter";

        /**
         * 记录所有正在下载或等待下载的任务。
         */
        /*  private Set<BitmapWorkerTask> taskCollection;*/
        private Set<YuvBitmapWorkerTaskCashe> taskCollection1;

        /**
         * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
         */
        private LruCache<String, Bitmap> mMemoryCache;

        /**
         * 图片硬盘缓存核心类。
         */
        private DiskLruCache mDiskLruCache;

        /**
         * GridView的实例
         */
        private GridView mPhotoWall;

        /**
         * 记录每个子项的高度。
         */
        private int mItemHeight = 0;

//        public int currentRadioButton = ServerConfig.RB_RECORD_VIDEO;

        private SparseBooleanArray isSelectedMap;
        private SparseBooleanArray isClickedMap;
        private ArrayList<Model> mArrayList;
        private Context mContext;

        public PhotoWallAdapter(Context context, int textViewResourceId, ArrayList<Model> arrayList,
                                GridView photoWall) {
            super(context, textViewResourceId, arrayList);
            mContext = context;
            mArrayList = arrayList;
            mPhotoWall = photoWall;
            isSelectedMap = new SparseBooleanArray();
            isClickedMap = new SparseBooleanArray();
            /*taskCollection = new HashSet<>();*/
            taskCollection1 = new HashSet<>();
            // 获取应用程序最大可用内存
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = maxMemory / 8;
            // 设置图片缓存大小为程序最大可用内存的1/8
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount();
                }
            };
            try {
                // 获取图片缓存路径
                File cacheDir = getDiskCacheDir(context, "thumb");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                // 创建DiskLruCache实例，初始化缓存数据
                mDiskLruCache = DiskLruCache
                        .open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }

//            if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO || currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
//                loadBitmaps(0, mArrayList.size());
//            }
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            String url = getItem(position);
            Model model = getItem(position);
//            Log.e(TAG, "getView: url" + url);
            View view;
//        ImageView imageView = null;
            if (convertView == null) {
                if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.layout_record_video_item, null);
                } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.layout_lock_video_item, null);
                } else {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.layout_photo_item, null);
                }
            } else {
                view = convertView;
            }
            TextView nameView = view.findViewById(R.id.tv_title);
            TextView timeView = view.findViewById(R.id.tv_time);
            ImageView playView = view.findViewById(R.id.iv_play);
            nameView.setText(model.getName());


            CheckBox cbMuliChoose = view.findViewById(R.id.cb_cbx);
            if (isMultiChoose) {
                cbMuliChoose.setVisibility(View.VISIBLE);
                cbMuliChoose.setChecked(getIsSelectedAt(position));
//                如下怎么实现还是没理解
                // TODO: 2017/11/29  如下怎么实现还是没理解
                if (currentRadioButton == ServerConfig.RB_CAPTURE_PHOTO) {
                    if (getIsSelectedAt(position)) {
                        int mode = ThemeManager.getInstance().getTheme();
                        if (mode == Theme.SPORT) {
                            view.setBackgroundColor(Color.parseColor("#fb8218"));
                        } else {
                            view.setBackgroundColor(Color.parseColor("#1CC9FE"));
                        }
                    } else {
                        view.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            } else {
                cbMuliChoose.setVisibility(View.INVISIBLE);
//                当按下取消后，isMultiChoose为falae了，所以执行下面。
                if (currentRadioButton == ServerConfig.RB_CAPTURE_PHOTO) {
                    view.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    if (lastPosition >= 0) {
                        if (getIsClickedAt(lastPosition)) {
                            nameView.setTextColor(Color.WHITE);
                            timeView.setTextColor(Color.WHITE);
                            playView.setVisibility(View.INVISIBLE);
                        }
                    }
                    if (getIsClickedAt(position)) {
                        int mode = ThemeManager.getInstance().getTheme();
                        if (mode == Theme.SPORT) {
                            nameView.setTextColor(Color.parseColor("#fb8218"));
                            timeView.setTextColor(Color.parseColor("#fb8218"));
                        } else {
                            nameView.setTextColor(Color.parseColor("#1CC9FE"));
                            timeView.setTextColor(Color.parseColor("#1CC9FE"));
                        }
                        playView.setVisibility(View.VISIBLE);
                    }
                }

            }

            ImageView imageView;
            imageView = view.findViewById(R.id.iv_videoPhoto);

            String url = null;
            if (currentRadioButton == ServerConfig.RB_CAPTURE_PHOTO) {
//                "http://192.168.42.1/SD0/PHOTO/2017-12-20-23-14-0100.JPG"
                url = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" +
                        model.getName();
                //                快速切换避免发送文件名错误
                if (!url.contains("MP4")) {
                    Glide.with(mContext).load(url).thumbnail(0.1f).into(imageView);
                }

            } else {
                if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
//                /tmp/SD0/NORMAL/2018-01-03-17-58-13.MP4
                    url = "/tmp/SD0/NORMAL/" + model.getName();
//                    url = "http://" + ServerConfig.VTDRIP + "/SD0/NORMAL/" +
//                            model.getName();
                } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                    url = "/tmp/SD0/EVENT/" + model.getName();
//                    url = "http://" + ServerConfig.VTDRIP + "/SD0/EVENT/" +
//                            model.getName();
                }
                imageView.setTag(url);
                setImageView(url, imageView);
//                快速切换避免发送文件名错误
                if (url.contains("jpg") || (url.contains("NORMAL") && url.contains("LOCK")) || (url
                        .contains("EVENT") && !url.contains("LOCK"))) {
                    return view;
                }
                loadBitmaps(imageView, url);
//                long interval = 5000 * 1000;
//                RequestOptions options = new RequestOptions().frame(interval);
//                Glide.with(mContext).asBitmap().load(url).apply(options).into(imageView);
            }

            return view;
        }


        private void setImageView(String imageUrl, ImageView imageView) {
            Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
//                防止换肤时被自动加载一张空图
                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo));
//                imageView.setImageResource(R.drawable.empty_photo);
            }
        }

      /*  private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
            try {
                for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                    Model model = mArrayList.get(i);
                    String imageUrl = null;

                    if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                        imageUrl = "/tmp/SD0/NORMAL/" + model.getName();
                    } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                        imageUrl = "/tmp/SD0/EVENT/" + model.getName();
                    }
                    Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                    if (bitmap == null) {
                        YuvBitmapWorkerTaskCashe task1 = new YuvBitmapWorkerTaskCashe();
                        taskCollection1.add(task1);
                        task1.execute(imageUrl);
                    } else {
                        ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
                        if (imageView != null && bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        /**
         * 将一张图片存储到LruCache中。
         *
         * @param key    LruCache的键，这里传入图片的URL地址。
         * @param bitmap LruCache的键，这里传入从网络上下载的Bitmap对象。
         */
        public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
            if (getBitmapFromMemoryCache(key) == null) {
                mMemoryCache.put(key, bitmap);
            }
        }

        /**
         * 从LruCache中获取一张图片，如果不存在就返回null。
         *
         * @param key LruCache的键，这里传入图片的URL地址。
         * @return 对应传入键的Bitmap对象，或者null。
         */
        public Bitmap getBitmapFromMemoryCache(String key) {
            return mMemoryCache.get(key);
        }

        /**
         * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象，
         * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会开启异步线程去下载图片。
         */
        public void loadBitmaps(ImageView imageView, String imageUrl) {
            try {
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
//                Bitmap bitmap = createVideoThumbnail(imageUrl,100,100);
                if (bitmap == null) {
                    YuvBitmapWorkerTaskCashe task1 = new YuvBitmapWorkerTaskCashe();
                    taskCollection1.add(task1);
                    task1.execute(imageUrl);
                } else {
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // TODO: 2018/3/19 优化
        private Bitmap createVideoThumbnail(String url, int width, int height) {
            Bitmap bitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            int kind = MediaStore.Video.Thumbnails.MINI_KIND;
            try {
                if (Build.VERSION.SDK_INT >= 14) {
                    retriever.setDataSource(url, new HashMap<String, String>());
                } else {
                    retriever.setDataSource(url);
                }
                bitmap = retriever.getFrameAtTime();
            } catch (IllegalArgumentException ex) {
                // Assume this is a corrupt video file
            } catch (RuntimeException ex) {
                // Assume this is a corrupt video file.
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException ex) {
                    // Ignore failures while cleaning up.
                }
            }
            if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }
            return bitmap;
        }

        /**
         * 取消所有正在下载或等待下载的任务。
         */
        public void cancelAllTasks() {
           /* if (taskCollection != null) {
                for (BitmapWorkerTask task : taskCollection) {
                    task.cancel(false);
                }
            }*/

            if (taskCollection1 != null) {
                for (YuvBitmapWorkerTaskCashe task1 : taskCollection1) {
                    task1.cancel(false);
                }
            }
        }

        /**
         * 根据传入的uniqueName获取硬盘缓存的路径地址。
         */
        public File getDiskCacheDir(Context context, String uniqueName) {
            String cachePath;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
                cachePath = context.getExternalCacheDir().getPath();
            } else {
                cachePath = context.getCacheDir().getPath();
            }
            return new File(cachePath + File.separator + uniqueName);
        }

        /**
         * 获取当前应用程序的版本号。
         */
        public int getAppVersion(Context context) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                        0);
                return info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return 1;
        }

        /**
         * 设置item子项的高度。
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            notifyDataSetChanged();
        }

        /**
         * 使用MD5算法对传入的key进行加密并返回。
         */
        public String hashKeyForDisk(String key) {
            String cacheKey;
            try {
                final MessageDigest mDigest = MessageDigest.getInstance("MD5");
                mDigest.update(key.getBytes());
                cacheKey = bytesToHexString(mDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                cacheKey = String.valueOf(key.hashCode());
            }
            return cacheKey;
        }

        /**
         * 将缓存记录同步到journal文件中。
         */
        public void fluchCache() {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String bytesToHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        }

        public boolean getIsSelectedAt(int i) {
            if (isSelectedMap.get(i)) {
                return isSelectedMap.get(i);
            }
            return false;
        }

        public void setItemIsSelectedMap(int position, boolean isSelected) {
            this.isSelectedMap.put(position, isSelected);
            notifyDataSetChanged();
        }

        public boolean getIsClickedAt(int i) {
            if (isClickedMap.get(i)) {
                return isClickedMap.get(i);
            }
            return false;
        }

        public void setItemIsClickedMap(int position, boolean isSelected) {
            this.isClickedMap.put(position, isSelected);
            notifyDataSetChanged();
        }

        class YuvBitmapWorkerTaskCashe extends AsyncTask<String, Void, Bitmap> {

            private String imageUrl;

            @Override
            protected Bitmap doInBackground(String... params) {
                imageUrl = params[0];
                FileDescriptor fileDescriptor = null;
                FileInputStream fileInputStream = null;
                DiskLruCache.Snapshot snapShot = null;
                try {
                    // 生成图片URL对应的key
                    final String key = hashKeyForDisk(imageUrl);
                    // 查找key对应的缓存
                    snapShot = mDiskLruCache.get(key);
                    if (snapShot == null) {
                        // 如果没有找到对应的缓存，则准备从网络上请求数据，并写入缓存
                        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            OutputStream outputStream = editor.newOutputStream(0);
                            if (downloadUrlToStreamCashe(imageUrl, outputStream)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }
                        // 缓存被写入后，再次查找key对应的缓存
                        snapShot = mDiskLruCache.get(key);
                    }
                    if (snapShot != null) {
                        fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                        fileDescriptor = fileInputStream.getFD();
                    }
                    // 将缓存数据解析成Bitmap对象
                    Bitmap bitmap = null;
                    if (fileDescriptor != null) {
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    }
                    if (bitmap != null) {
                        // 将Bitmap对象添加到内存缓存当中
                        addBitmapToMemoryCache(params[0], bitmap);
                    }
                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
                taskCollection1.remove(this);
            }

            private boolean downloadUrlToStreamCashe(String param, OutputStream outputStream) {
                Log.e(TAG, "downloadYuvBitmap: 开始");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedOutputStream out = null;
                BufferedInputStream in = null;
                Bitmap bitmap = null;
                int timecount = 0;
                boolean result = false;
                mRemoteCam.getThumb(param);
                while (!isYuvDownload) {
                    if (isThumbGetFail) {
                        // TODO: 2018/1/9 此时bitmap如何处理
//                        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defualt_thm);
                        isThumbGetFail = false;
                        return false;
                    }

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timecount++;
                    if (timecount == 3) {
//                        break;
                        isYuvDownload = false;
                        return false;
                    }
                }
                isYuvDownload = false;
                Log.e(TAG, "downloadYuvBitmap: 接收到数据");
                if (mRemoteCam != null && mRemoteCam.getDataChannel() != null) {
                    bitmap = mRemoteCam.getDataChannel().rxYuvStreamUpdate();
                    result = true;
                } else {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
                    result = false;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                InputStream inputimage = new ByteArrayInputStream(baos.toByteArray());
                in = new BufferedInputStream(inputimage, 8 * 1024);
                out = new BufferedOutputStream(outputStream, 8 * 1024);
                int b;
                try {
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                } catch (IOException e) {
                    result = false;
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                return result;
            }
        }

        /**
         * 异步下载图片的任务。
         *
         * @author guolin
         */
        /* class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

         *//**
         * 图片的URL地址
         *//*
            private String imageUrl;

            @Override
            protected Bitmap doInBackground(String... params) {
                imageUrl = params[0];
                FileDescriptor fileDescriptor = null;
                FileInputStream fileInputStream = null;
                DiskLruCache.Snapshot snapShot = null;
                try {
                    // 生成图片URL对应的key
                    final String key = hashKeyForDisk(imageUrl);
                    // 查找key对应的缓存
                    snapShot = mDiskLruCache.get(key);
                    if (snapShot == null) {
                        // 如果没有找到对应的缓存，则准备从网络上请求数据，并写入缓存
                        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            OutputStream outputStream = editor.newOutputStream(0);
                            if (downloadUrlToStream(imageUrl, outputStream)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }
                        // 缓存被写入后，再次查找key对应的缓存
                        snapShot = mDiskLruCache.get(key);
                    }
                    if (snapShot != null) {
                        fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                        fileDescriptor = fileInputStream.getFD();
                    }
                    // 将缓存数据解析成Bitmap对象
                    Bitmap bitmap = null;
                    if (fileDescriptor != null) {
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    }
                    if (bitmap != null) {
                        // 将Bitmap对象添加到内存缓存当中
                        addBitmapToMemoryCache(params[0], bitmap);
                    }
                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。
                ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
                taskCollection.remove(this);
            }

            *//**
         * 建立HTTP请求，并获取Bitmap对象。
         *
         * @param urlString 图片的URL地址
         * @return 解析后的Bitmap对象
         *//*
            private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
                HttpURLConnection urlConnection = null;
                BufferedOutputStream out = null;
                BufferedInputStream in = null;
                Bitmap bitmap;
                try {
                    final URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();

//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 18;  //16-free 2.3M
//                    bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream(), null, options);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                    InputStream inputimage = new ByteArrayInputStream(baos.toByteArray());
//                    in = new BufferedInputStream(inputimage, 8 * 1024);

                    in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
                    out = new BufferedOutputStream(outputStream, 8 * 1024);
                    int b;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                    return true;
                } catch (final IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

        }
*/
    }


    /*
     * 定时刷新列表，更新数据
     * 解决debug，长时间停留不更新，定时十分钟
     *
     * rueshcontrol 控制线程停止
     *
     * */


    /* private Handler handlerUpdate = new Handler();*/
    public boolean rueshcontrol = false;

  /*  // TODO: 2018/4/8 后台去刷还有问题！！！！！！！！！！！！！！！！！！！！！！
    public class MyThread extends Thread {
        @Override
        public void run() {
            while (rueshcontrol) {
                try {
                    MyThread.sleep(1000 * 180);
                    if (currentRadioButton == ServerConfig.RB_CAPTURE_PHOTO) {
                        mPWD = "/tmp/SD0/PHOTO";
                        if (rueshcontrol && !isMultiChoose) {
                            listDirContents(mPWD);
                        }
                    } else if (currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                        mPWD = "/tmp/SD0/NORMAL";
                        if (rueshcontrol && !isMultiChoose) {
                            listDirContents(mPWD);
                        }
                    } else if (currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                        mPWD = "/tmp/SD0/EVENT";
                        if (rueshcontrol && !isMultiChoose) {
                            listDirContents(mPWD);
                        }
                    }
                    if (mAdapter != null && rueshcontrol) {
                        handlerUpdate.post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    private static class RefreshListThread extends Thread {
        private WeakReference<FragmentPlaybackList> fragmentPlaybackListWeakReference;

        RefreshListThread(FragmentPlaybackList fragmentPlaybackList) {
            fragmentPlaybackListWeakReference = new WeakReference<>(fragmentPlaybackList);
        }

        @Override
        public void run() {
            FragmentPlaybackList fragmentPlaybackListReference = fragmentPlaybackListWeakReference.get();
            while (fragmentPlaybackListReference.rueshcontrol) {
                try {
                    RefreshListThread.sleep(1000 * 180);
                    if (fragmentPlaybackListReference.currentRadioButton == ServerConfig.RB_CAPTURE_PHOTO) {
                        fragmentPlaybackListReference.mPWD = "/tmp/SD0/PHOTO";
                        if (fragmentPlaybackListReference.rueshcontrol && !fragmentPlaybackListReference.isMultiChoose) {
                            fragmentPlaybackListReference.listDirContents(fragmentPlaybackListReference.mPWD);
                        }
                    } else if (fragmentPlaybackListReference.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                        fragmentPlaybackListReference.mPWD = "/tmp/SD0/NORMAL";
                        if (fragmentPlaybackListReference.rueshcontrol && !fragmentPlaybackListReference.isMultiChoose) {
                            fragmentPlaybackListReference.listDirContents(fragmentPlaybackListReference.mPWD);
                        }
                    } else if (fragmentPlaybackListReference.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                        fragmentPlaybackListReference.mPWD = "/tmp/SD0/EVENT";
                        if (fragmentPlaybackListReference.rueshcontrol && !fragmentPlaybackListReference.isMultiChoose) {
                            fragmentPlaybackListReference.listDirContents(fragmentPlaybackListReference.mPWD);
                        }
                    }
                    if (fragmentPlaybackListReference.mAdapter != null && fragmentPlaybackListReference.rueshcontrol) {
                        fragmentPlaybackListReference.updateListHandler.sendEmptyMessage(NOTIFY_LIST_UPDATE);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class UpdateListHandler extends Handler {
        private WeakReference<FragmentPlaybackList> fragmentPlaybackListWeakReference;

        UpdateListHandler(FragmentPlaybackList fragmentPlaybackList) {
            fragmentPlaybackListWeakReference = new WeakReference<>(fragmentPlaybackList);
        }

        @Override
        public void handleMessage(Message msg) {
            FragmentPlaybackList fragmentPlaybackListUpdateReference = fragmentPlaybackListWeakReference.get();

            if (fragmentPlaybackListUpdateReference != null) {
                switch (msg.what) {
                    case NOTIFY_LIST_UPDATE:
                        fragmentPlaybackListUpdateReference.mAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public void disableRadioGroup(RadioGroup testRadioGroup) {
        if (testRadioGroup != null) {
            for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
                testRadioGroup.getChildAt(i).setEnabled(false);
            }
        }
    }

    public void enableRadioGroup(RadioGroup testRadioGroup) {
        if (testRadioGroup != null) {
            for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
                testRadioGroup.getChildAt(i).setEnabled(true);
            }
        }
    }
}


package com.byd.vtdr;

//import android.app.FragmentLoading;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byd.lighttextview.LightRadioButton;
import com.byd.vtdr.connectivity.IChannelListener;
import com.byd.vtdr.connectivity.IFragmentListener;
import com.byd.vtdr.fragment.FragmentPlaybackList;
import com.byd.vtdr.fragment.FragmentRTVideo;
import com.byd.vtdr.fragment.FragmentSetting;
import com.byd.vtdr.utils.LogcatHelper;
import com.byd.vtdr.view.CustomDialog;
import com.byd.vtdr.view.MyDialog;
import com.byd.vtdr.view.ProgressDialogFragment;
import com.byd.vtdr.widget.ThemeLightButton;
import com.byd.vtdr.widget.ThemeLightRadioButton;
import com.byd.vtdr.widget.ThemeManager;

import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import skin.support.annotation.Skinable;

/**
 * @author byd_tw 若要实现所以activity的换肤，把@Skinable放到BaseActivity中
 */

@Skinable
public class MainActivity extends AppCompatActivity implements IChannelListener, IFragmentListener {
    private static final String TAG = "MainActivity";
    private final static String KEY_CONNECTIVITY_TYPE = "connectivity_type";
    @BindView(R.id.fl_main)
    FrameLayout flMain;
    private int mConnectivityType;
    public SharedPreferences mPref;
    public RemoteCam mRemoteCam;

    @BindView(R.id.rb_realTimeVideo)
    ThemeLightRadioButton rbRealTimeVideo;
    @BindView(R.id.rb_playbackList)
    ThemeLightRadioButton rbPlaybackList;
    @BindView(R.id.rb_setting)
    ThemeLightRadioButton rbSetting;
    @BindView(R.id.rg_group)
    RadioGroup rgGroup;
    @BindView(R.id.btn_back)
    ThemeLightButton btnBack;

    private static FragmentRTVideo fragmentRTVideo = FragmentRTVideo.newInstance();
    private static FragmentPlaybackList fragmentPlaybackList = FragmentPlaybackList.newInstance();
    private static FragmentSetting fragmentSetting = FragmentSetting.newInstance();
    //    当时静态时才能旋转保持fragmentvideo
    private static Fragment fragment;
    private String appStateStr;
    private MyDialog myDialog;
    private ArrayList<Model> selectedLists;
    private static ArrayList<Model> selectedListsA = new ArrayList<Model>();

    private static int selectedCounts;
    private int hadDelete;
    private String mGetFileName;
    private ProgressDialogFragment progressDialogFragment;
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;
    private boolean isNetworkConnected;
    private static CustomDialog customDialog = null;

    //控制弹出框的显示，页面切换网络错误时，弹出一次控制
    public static boolean isDialogShow = false;
    private static Toast toast;
    private final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> mScheduledTask;
    private boolean isReconnecting;
    private boolean isCardNoExist;
    public static int isSensormessage = 0;
    private boolean isMicOn;
    private ThemeManager themeManager;
    MyApplication myApplication;
    private static boolean hasCard;
    private int valueEventRecord;
    private boolean isLocking;
    private boolean isCardInsert;
    private int valueSdcardInit;
    private int valueRecordInit;
    /* private UpdateHandler updateHandler = new UpdateHandler(this);*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogcatHelper.getInstance(getApplicationContext()).start();
//        requestWindowFeature();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initSkinView();
        myApplication = (MyApplication) this.getApplicationContext();

        requestPermission();
        initConnect();
    }

    private void initSkinView() {
        int bydTheme = getResources().getConfiguration().byd_theme;
        changeSkin(bydTheme);
    }

    @Override
    protected void onResume() {
        super.onResume();
//       receiverNetworkBroadcast();
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); //网络连接消息
//        filter.addAction(EthernetManager.ETHERNET_STATE_CHANGED_ACTION); //以太网消息
//        this.registerReceiver(receiver, filter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int bydTheme = newConfig.byd_theme;
        changeSkin(bydTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void initConnect() {
        isNetworkConnected = true;
//        rbRealTimeVideo.setOnClickListener(null);
//        rbPlaybackList.setOnClickListener(null);
//        rbSetting.setOnClickListener(null);
        mPref = getPreferences(MODE_PRIVATE);
        getPrefs(mPref);
        mRemoteCam = myApplication.getRemoteCam();
        if (!myApplication.isRemoteCreate) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            mRemoteCam.setChannelListener(this).setConnectivity(mConnectivityType)
                    .setWifiInfo(wifiManager.getConnectionInfo().getSSID().replace("\"", ""), getWifiIpAddr());
            mRemoteCam.startSession();
            myApplication.isRemoteCreate = true;
        }

        isDialogShow = false;
        mScheduledTask = worker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mRemoteCam.socketTest();
            }
        }, 0, 3, TimeUnit.SECONDS);
        fragmentPlaybackList.setRemoteCam(mRemoteCam);
        fragmentRTVideo.setRemoteCam(mRemoteCam);
        if (fragment == null) {
            fragment = fragmentRTVideo;
            getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment)
                    .commitAllowingStateLoss();
            rgGroup.check(R.id.rb_realTimeVideo);
        }


        rbRealTimeVideo.setOnCheckedChangeListener(new LightRadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (fragment != fragmentRTVideo) {
                        if (fragmentPlaybackList.fragmentVideoPreview != null) {
                            if (fragmentPlaybackList.fragmentVideoPreview.isVisible() ||
                                    fragmentPlaybackList.fragmentVideoPreview.reload) {
                                //getSupportFragmentManager().popBackStack();
                                fragmentPlaybackList.fragmentVideoPreview.reload = false;
                                fragmentPlaybackList.getFragmentManager().popBackStack();
                            }
                        } else if (fragmentPlaybackList.fragmentPhotoPreview != null) {
                            if (fragmentPlaybackList.fragmentPhotoPreview.isVisible()
                                    || fragmentPlaybackList.fragmentPhotoPreview.reload) {
                                fragmentPlaybackList.fragmentPhotoPreview.reload = false;
                                fragmentPlaybackList.getFragmentManager().popBackStack();
                            }
                        }
                        fragment = fragmentRTVideo;
                        getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment).commitAllowingStateLoss();
                    }
                }
            }
        });
        rbPlaybackList.setOnCheckedChangeListener(new LightRadioButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (fragment != fragmentPlaybackList) {
                        fragment = fragmentPlaybackList;
                        isSensormessage = 0;//控制对话框，弹出一次
                        getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment).commitAllowingStateLoss();
                    }

                }
            }
        });
        rbSetting.setOnCheckedChangeListener(new LightRadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (fragment != fragmentSetting) {
                        if (fragmentPlaybackList.fragmentVideoPreview != null) {
                            if (fragmentPlaybackList.fragmentVideoPreview.isVisible() ||
                                    fragmentPlaybackList.fragmentVideoPreview.reload) {
                                // getSupportFragmentManager().popBackStack();
                                fragmentPlaybackList.fragmentVideoPreview.reload = false;
                                fragmentPlaybackList.getFragmentManager().popBackStack();
                            }

                        } else if (fragmentPlaybackList.fragmentPhotoPreview != null) {
                            if (fragmentPlaybackList.fragmentPhotoPreview.isVisible()
                                    || fragmentPlaybackList.fragmentPhotoPreview.reload) {
                                fragmentPlaybackList.fragmentPhotoPreview.reload = false;
                                fragmentPlaybackList.getFragmentManager().popBackStack();
                            }
                        }
                        fragment = fragmentSetting;
                        isSensormessage = 0;//控制对话框，弹出一次

                        getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment).commitAllowingStateLoss();
                    }
                }
            }
        });

    }

    private void getPrefs(SharedPreferences preferences) {
        mConnectivityType = preferences.getInt(KEY_CONNECTIVITY_TYPE, RemoteCam
                .CAM_CONNECTIVITY_WIFI_WIFI);
//        neverShow = mPref.getBoolean(KEY_NEVER_SHOW, false);
    }

    public void putPrefs(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_CONNECTIVITY_TYPE, mConnectivityType);
//        editor.putBoolean(KEY_NEVER_SHOW, neverShow);
        editor.commit();
    }

    private String getWifiIpAddr() {
//        int type = NetworkUtils.getAPNType(getApplicationContext());
//        if (type == ConnectivityManager.TYPE_WIFI) {
//            WifiManager mgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            int ip = mgr.getConnectionInfo().getIpAddress();
//            return String.format("%d.%d.%d.%d", (ip & 0xFF), (ip >> 8 & 0xFF), (ip >> 16 & 0xFF), ip
//                    >> 24);
//        } else if (type == ConnectivityManager.TYPE_ETHERNET) {
////            得到自己的ip
//            return ServerConfig.PADIP;
////            return Settings.System.getString(getContentResolver(),Settings.System.);
//        }
//        return null;
        return ServerConfig.PADIP;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: 2018/2/3 如下的作用不知
//        putPrefs(mPref);
//        此处解注册因为app从后台快速切换回来
//        unregisterReceiver(mBroadcastReceiver);
//        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogcatHelper.getInstance(getApplicationContext()).stop();
        if (customDialog != null) {
            customDialog.dismiss();
            customDialog = null;
        }
        if (mScheduledTask != null) {
            mScheduledTask.cancel(false);
        }
//        mRemoteCam.stopSession();
     /*   if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
        updateHandler = null;*/
    }

    private void showToastTips(String tips) {
        if (toast == null) {
            toast = Toast.makeText(this, tips, Toast.LENGTH_SHORT);
        } else {
            toast.setText(tips);
        }
        toast.show();
    }

    private void showConfirmDialog(String tips) {
        final String temp = tips;
        if (customDialog != null && !isFinishing()) {
            customDialog.dismiss();
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        customDialog = builder.cancelTouchOut(false)
                .view(R.layout.fragment_custom_dialog)
                .style(R.style.CustomDialog)
                .setTitle(tips)
                .addViewOnclick(R.id.btn_dialogSure, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog.dismiss();
                        isDialogShow = false;
                        if (temp == (getString(R.string.format_finished))) {
                            if (fragmentPlaybackList.mAdapter != null) {
                                fragmentPlaybackList.mAdapter.clear();
                                fragmentPlaybackList.mAdapter.cancelAllTasks();
                            }
                        }
                    }
                })
                .build();
        customDialog.show();
    }

    private void showCrossDialog(String tips) {
        if (customDialog != null && !isFinishing()) {
            customDialog.dismiss();
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        customDialog = builder.cancelTouchOut(true)
                .view(R.layout.fragment_cross_dialog)
                .style(R.style.CustomDialog)
                .setTitle(tips)
                .addViewOnclick(R.id.btn_closeDialog, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog.dismiss();
                    }
                })
                .build();
        customDialog.show();
    }

    private void showDoubleButtonDialog(String tips) {
        if (customDialog != null && !isFinishing()) {
            customDialog.dismiss();
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        customDialog = builder.cancelTouchOut(false)
                .view(R.layout.fragment_doublebutton_dialog)
                .style(R.style.CustomDialog)
                .setTitle(tips)
                .addViewOnclick(R.id.btn_dialogSure, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRemoteCam.formatSD("C:");
                        customDialog.dismiss();
                        showWaitingDialog(getString(R.string.storage_card_formatting));
                    }
                })
                .addViewOnclick(R.id.btn_dialogCancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog.dismiss();
                    }
                })
                .build();
        customDialog.show();
    }

    private void showWaitingDialog(String tips) {
        if (customDialog != null && !isFinishing()) {
            customDialog.dismiss();
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        customDialog = builder.cancelTouchOut(false)
                .view(R.layout.fragment_waiting_dialog)
                .style(R.style.CustomDialog)
                .setTitle(tips)
                .build();
        customDialog.show();
    }

    private void showDoubleFormatDialog(String tips) {
        if (customDialog != null && !isFinishing()) {
            customDialog.dismiss();
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        customDialog = builder.cancelTouchOut(false)
                .view(R.layout.fragment_doublebutton_reformat__dialog)
                .style(R.style.CustomDialog)
                .setTitle(tips)
                .addViewOnclick(R.id.btn_dialogSure, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRemoteCam.formatSD("C:");
                        customDialog.dismiss();
                        showWaitingDialog(getString(R.string.storage_card_formatting));
                    }
                })
                .addViewOnclick(R.id.btn_dialogCancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog.dismiss();
                    }
                })
                .build();
        customDialog.show();
    }

    private void showDoubleImmeFormatDialog(String tips) {
        if (customDialog != null && !isFinishing()) {
            customDialog.dismiss();
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        customDialog = builder.cancelTouchOut(false)
                .view(R.layout.fragment_doublebutton_immeformatdetail_dialog)
                .style(R.style.CustomDialog)
                .setTitle(tips)
                .addViewOnclick(R.id.btn_dialogSure, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRemoteCam.formatSD("C:");
                        customDialog.dismiss();
                        showWaitingDialog(getString(R.string.storage_card_formatting));
                    }
                })
                .addViewOnclick(R.id.btn_dialogCancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog.dismiss();
                    }
                })
                .build();
        customDialog.show();
    }

//    @Override
//    public void onConfigurationChanged(Configuration config) {
//        super.onConfigurationChanged(config);
////        setContentView(R.layout.activity_main);
//    }

    @OnClick(R.id.btn_back)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        如下只有为getSupportFragmentManager时才能弹出
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
/*            if (!isNetworkConnected) {
                finish();
                return;
            }*/
            if (fragment == fragmentPlaybackList && fragmentPlaybackList.isMultiChoose) {
                fragmentPlaybackList.cancelMultiChoose();
            } else {
                super.onBackPressed();
                mRemoteCam.stopSession();
                finish();
                Log.e(TAG, "kill the process to force fresh launch next time");
                Process.killProcess(Process.myPid());
                System.exit(0);
            }
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onChannelEvent(final int type, final Object param, final String... array) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type & IChannelListener.MSG_MASK) {
                    case IChannelListener.CMD_CHANNEL_MSG:
                        handleCmdChannelEvent(type, param, array);
                        return;
                    case IChannelListener.DATA_CHANNEL_MSG:
                        handleDataChannelEvent(type, param);
                        return;
//                    case IChannelListener.STREAM_CHANNEL_MSG:
//                        handleStreamChannelEvent(type, param);
//                        return;
                    default:
                        break;
                }
            }
        });
    }

    private void handleCmdChannelEvent(int type, Object param, String... array) {
        if (type >= 80) {
            handleCmdChannelError(type, param);
            return;
        }

        switch (type) {
            case IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT:
                String str = (String) param;
                if ("CONNECT_FAIL".equals(str)) {
                    str = getString(R.string.connect_fail);
                    if (!isDialogShow) {
                        showConfirmDialog(str);
                        isDialogShow = true;
                    }
                } else {
//                    showAddSingleButtonDialog(str);
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_BYDSDCARD_ALERT:
                int value = (int) param;
                if (valueSdcardInit != value) {
                    valueSdcardInit = value;
                    switch (valueSdcardInit) {
                        case ServerConfig.BYD_CARD_STATE_OK:
                            hasCard = true;
                            showWaitingDialog(getString(R.string.card_readying));
                            fragmentRTVideo.showCheckSdCordTag(true);
                            if (fragment == fragmentRTVideo) {
                                if (!isReconnecting) {
                                    fragmentRTVideo.setImagerAple_SD(false);
                                }
                            }
                            break;
                        case ServerConfig.BYD_CARD_STATE_NOCARD:
                            hasCard = false;
                            fragmentRTVideo.showCheckSdCordTag(false);
                            showConfirmDialog(getString(R.string.card_removed));
                            if (fragment == fragmentPlaybackList) {
                                fragmentPlaybackList.setRemoteCam(mRemoteCam);
                                if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                                    fragmentPlaybackList.showRecordList();
                                } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                                    fragmentPlaybackList.showLockVideoList();
                                } else {
                                    fragmentPlaybackList.showCapturePhotoList();
                                }
                            }
                            if (fragment == fragmentRTVideo) {
                                fragmentRTVideo.setImagerAple_SD(true);
                            }
                            break;
                        case ServerConfig.BYD_CARD_STATE_SMALL_NAND:
                        case ServerConfig.BYD_CARD_STATE_NOT_MEM:
                        case ServerConfig.BYD_CARD_STATE_SETROOT_FAIL:
                        case ServerConfig.BYD_CARD_STATE_UNINIT:
                            hasCard = true;
                            showConfirmDialog(getString(R.string.card_issue));
                            break;
                        case ServerConfig.BYD_CARD_STATE_NEED_FORMAT:
                            hasCard = true;
                            showDoubleImmeFormatDialog(getString(R.string.card_need_format));
                            fragmentRTVideo.showCheckSdCordState();
                            if (fragment == fragmentRTVideo) {
                                fragmentRTVideo.setImagerAple_SD(true);
                            }
                            break;
                        case ServerConfig.BYD_CARD_STATE_NOT_ENOUGH:
                            hasCard = true;
                            showConfirmDialog(getString(R.string.card_not_enough));
                            break;
                        case ServerConfig.BYD_CARD_STATE_WP:
                            hasCard = true;
                            showConfirmDialog(getString(R.string.card_write_protect));
                            break;
                        default:
                            break;
                    }
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_BYDRECORD_ALERT:
                int valueRecord = (int) param;
                if (valueRecordInit != valueRecord) {
                    valueRecordInit = valueRecord;
                    switch (valueRecordInit) {
                        case ServerConfig.REC_CAP_STATE_PREVIEW:
                            break;
                        case ServerConfig.REC_CAP_STATE_RECORD:
                            if (fragment == fragmentRTVideo) {
                                if (customDialog != null && !isFinishing()) {
                                    customDialog.dismiss();
                                }
                                rgGroup.check(R.id.rb_realTimeVideo);
                                fragmentRTVideo = FragmentRTVideo.newInstance();
                                fragment = fragmentRTVideo;
                                getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment).commitAllowingStateLoss();
                            } else if (fragment == fragmentPlaybackList) {
                                if (customDialog != null && !isFinishing()) {
                                    customDialog.dismiss();
                                }
                                fragmentPlaybackList.setRemoteCam(mRemoteCam);
                                if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                                    fragmentPlaybackList.showRecordList();
                                } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                                    fragmentPlaybackList.showLockVideoList();
                                } else {
                                    fragmentPlaybackList.showCapturePhotoList();
                                }
                                mRemoteCam.getSystemState();
                            } else {
                                if (customDialog != null && !isFinishing()) {
                                    customDialog.dismiss();
                                }
                                mRemoteCam.getSystemState();
//                                    防止格式化时，收到录像状态弹窗被隐藏
                            }
//                        }
                            myApplication.setisRescod(true);
                            // TODO: 2018/4/13 先屏蔽
//                        mRemoteCam.appStatus();
                            break;
                        case ServerConfig.REC_CAP_STATE_PRE_RECORD:
                            break;
                        case ServerConfig.REC_CAP_STATE_FOCUS:
                            break;
                        case ServerConfig.REC_CAP_STATE_CAPTURE:
                            break;
                        case ServerConfig.REC_CAP_STATE_VF:
                            if (fragment == fragmentRTVideo) {
                                if (customDialog != null && !isFinishing()) {
                                    customDialog.dismiss();
                                }
                                rgGroup.check(R.id.rb_realTimeVideo);
                                fragmentRTVideo = FragmentRTVideo.newInstance();
                                fragment = fragmentRTVideo;
                                getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment).commitAllowingStateLoss();
                            } else if (fragment == fragmentPlaybackList) {
                                if (customDialog != null && !isFinishing()) {
                                    customDialog.dismiss();
                                }
                                fragmentPlaybackList.setRemoteCam(mRemoteCam);
                                if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                                    fragmentPlaybackList.showRecordList();
                                } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                                    fragmentPlaybackList.showLockVideoList();
                                } else {
                                    fragmentPlaybackList.showCapturePhotoList();
                                }
                                mRemoteCam.getSystemState();
                            } else {
                                if (customDialog != null && !isFinishing()) {
                                    customDialog.dismiss();
                                }
                                mRemoteCam.getSystemState();
//                                    防止格式化时，收到录像状态弹窗被隐藏
                            }
//                        mRemoteCam.appStatus();
                            myApplication.setisRescod(false);
//
                            break;
                        case ServerConfig.REC_CAP_STATE_TRANSIT_TO_VF:

                            break;
                        case ServerConfig.REC_CAP_STATE_RESET:
                            break;
                        default:
                            break;
                    }
                }

                break;
            case IChannelListener.CMD_CHANNEL_EVENT_BYDSENSOR_ALERT:
                int valueSensor = (int) param;
                switch (valueSensor) {
                    case 0:
                        isSensormessage = 0;//控制对话框弹出一次
//                        if (customDialog != null && !isFinishing()) {
//                            customDialog.dismiss();
//                        }
                        break;
                    case 1:
                        if (fragment == fragmentRTVideo && !isDialogShow && isSensormessage == 0) {
                            showConfirmDialog(getString(R.string.sensor_issue));
                            isDialogShow = true;
                            isSensormessage++;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_BYDPHOTO_ALERT:
                int valuePhoto = (int) param;
                switch (valuePhoto) {
                    case 0:
                        showToastTips(getString(R.string.Pictures_success));
                        break;
                    case -2:
                        showToastTips(getString(R.string.Pictures_fail));
                        break;
                    case -1:
                        showToastTips(getString(R.string.image_max));
                        break;
                    default:
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_BYDEVENTRECORD_ALERT:
                valueEventRecord = (int) param;
                switch (valueEventRecord) {
                    case 0:
//                        isLocking = false;
//                        showToastTips(getString(R.string.LockVideo_start));
                        break;
                    case 1:
                        // TODO: 2018/5/31 补充通知
//                        isLocking = true;
//                        if (!isLocking) {
//                            showToastTips(getString(R.string.LockVideo_start));
//                        }
                        isLocking = false;
                        break;
                    case 2:
//                        isLocking = false;
                        showToastTips(getString(R.string.LockVideo_end));
                        break;
                    default:
//                        isLocking = false;
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_GET_SPACE:
                fragmentRTVideo.showCheckSdCordTag(false);
                // 如下操作不会闪退
//                showAddSingleButtonDialog(getString(R.string.card_removed));
                showConfirmDialog(getString(R.string.card_removed));
                isCardNoExist = true;
                /*if (myDialog != null) {
                    myDialog.dismiss();
                }
                myDialog = MyDialog.newInstance(1, "请插入存储卡！");
                myDialog.show(getFragmentManager(), "SHOW_ALERT1");*/
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_START_SESSION:
//                mRemoteCam.getAllSettings();
//                mRemoteCam.appStatus();
//                mRemoteCam.micStatus();
//                mRemoteCam.getSystemState();
//                mRemoteCam.actionQuerySessionHolder();
//                mRemoteCam.getTotalFreeSpace();
//                mRemoteCam.getTotalFileCount();
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_TAKE_PHOTO:
                int capturePhotoFlag = (int) param;
                switch (capturePhotoFlag) {
                    case 1:
                        showToastTips(getString(R.string.Pictures_success));
                        break;
                    case -1:
                        showToastTips(getString(R.string.Pictures_fail));
                        break;
                    case -30:
                        showToastTips(getString(R.string.image_max));
                        break;
                    default:
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_LOCK_VIDEO:
                int isLockVideoFlag = (int) param;
                switch (isLockVideoFlag) {
                    case 0:
//                        showToastTips(getString(R.string.LockVideo_start));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (customDialog != null && !isFinishing()) {
                                    customDialog.dismiss();
                                }
                            }
                        }, 3000);
//                        isLocking = true;
                        break;
                    case 1:
                        if (customDialog != null && !isFinishing()) {
                            customDialog.dismiss();
                        }
                        showToastTips(getString(R.string.video_locking));
//                        isLocking = true;
                        break;
                    case -1:
                        if (customDialog != null && !isFinishing()) {
                            customDialog.dismiss();
                        }
                        showToastTips(getString(R.string.LockVideo_fail));
                        if (fragmentRTVideo != null) {
                            fragmentRTVideo.lastClickTime2 = 0;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_FRIMWORK_VERSION:
                String str1 = (String) param;
                if ("null".equals(str1)) {
                    showToastTips(getString(R.string.get_firmware_version_fail));
                } else {
                    showConfirmDialog(getString(R.string.firmware_version) + " " + str1);
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_APP_STATE:
                appStateStr = (String) param;
                if (Objects.equals(appStateStr, "record")) {
                    myApplication.setisRescod(true);
                    fragmentRTVideo.setRecordState(true);
                } else if (Objects.equals(appStateStr, "vf")) {
                    myApplication.setisRescod(false);
                    fragmentRTVideo.setRecordState(false);
                } else if (Objects.equals(appStateStr, "idle")) {
//                    showToastTips(getString(R.string.reboot_drivingReorder));
//                    Toast.makeText(getApplicationContext(), "请重启记录仪！", Toast.LENGTH_LONG).show();
//                    showAddSingleButtonDialog(getString(R.string.reboot_drivingReorder));
                    // TODO: 2018/1/4 如下显示弹窗，旋转就闪退。
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_MIC_STATE:
                // TODO: 2018/6/6 录音如何逻辑
                isMicOn = (boolean) param;
                fragmentRTVideo.setMicState(isMicOn);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_RECORD_TIME:
                // TODO: 2017/12/25
                fragmentRTVideo.updateRecordTime((String) param);
//                seconds = Integer.parseInt((String) param);
//                mHandler.postDelayed(runnable,1000);
//                Timer timer = new Timer();
//                timer.schedule(new RecordTimeTask(), 1000);
            case IChannelListener.CMD_CHANNEL_EVENT_START_LS:
                // TODO: 2017/12/27 开始发送获取视频的列表，需做刷新或提示
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_LS:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragmentPlaybackList.showLoadView(false);
                    }
                });
                fragmentPlaybackList.updateDirContents((JSONObject) param);
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB_TEST:
                if ((boolean) param) {
                    fragmentPlaybackList.isYuvDownload = true;
                } else {
                    fragmentPlaybackList.isYuvDownload = false;
                }
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB_FAIL:
                fragmentPlaybackList.isThumbGetFail = (boolean) param;
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_FORMAT_SD:
                int isFormatSD = (int) param;
                switch (isFormatSD) {
                    case 0:
                        if (customDialog != null && !isFinishing()) {
                            customDialog.dismiss();
                        }
                        showToastTips(getString(R.string.format_finished));
                        if (fragment == fragmentRTVideo) {
                            fragmentRTVideo.setImagerAple_SD(false);
                        }
                        break;
                    case -31:
                        if (customDialog != null && !isFinishing()) {
                            customDialog.dismiss();
                        }
                        showToastTips(getString(R.string.video_locking_format_later));
                        break;
                    case -1:
                        if (customDialog != null && !isFinishing()) {
                            customDialog.dismiss();
                        }
                        showToastTips(getString(R.string.format_fail));
                        if (fragment == fragmentRTVideo) {
                            fragmentRTVideo.setImagerAple_SD(true);
                        }
                        break;
                    default:
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_DEL:
                hadDelete++;
                if (hadDelete == selectedCounts) {
                    if (customDialog != null && !isFinishing()) {
                        customDialog.dismiss();
                    }
                    if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                        fragmentPlaybackList.showRecordList();
                    } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                        fragmentPlaybackList.showLockVideoList();
                    } else {
                        fragmentPlaybackList.showCapturePhotoList();
                    }
                    hadDelete = 0;
                    showToastTips(getString(R.string.delete_success));
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_DEL_FAIL:
                hadDelete++;
//                当删除失败为最后一个时
                if (hadDelete == selectedCounts) {
                    if (customDialog != null && !isFinishing()) {
                        customDialog.dismiss();
                    }
                    if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                        fragmentPlaybackList.showRecordList();
                    } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                        fragmentPlaybackList.showLockVideoList();
                    } else {
                        fragmentPlaybackList.showCapturePhotoList();
                    }
                    hadDelete = 0;
                    showToastTips(getString(R.string.delete_fail));
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_RECORD_START_FAIL:
                boolean isRecordStartFail = (boolean) param;
                if (isRecordStartFail) {
                    showToastTips(getString(R.string.openVideo_fail));
                    fragmentRTVideo.setRecordState(false);
                } else {
                    fragmentRTVideo.setRecordState(true);
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_RECORD_STOP_FAIL:
                boolean isRecordStopFail = (boolean) param;
                if (isRecordStopFail) {
                    showToastTips(getString(R.string.closeVideo_fail));
                    fragmentRTVideo.setRecordState(true);
                } else {
                    fragmentRTVideo.setRecordState(false);
                }
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_WAKEUP_START:
                Log.e(TAG, "handleCmdChannelEvent: Waking up the Remote Camera START");
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_CONNECTED:
            case IChannelListener.CMD_CHANNEL_EVENT_WAKEUP_OK:
                if (isReconnecting) {
                    if (customDialog != null && !isFinishing()) {
                        customDialog.dismiss();
                    }
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    mRemoteCam = new RemoteCam(getApplicationContext());
                    myApplication.setRemoteCam(mRemoteCam);
                    mRemoteCam.setChannelListener(this).setConnectivity(RemoteCam
                            .CAM_CONNECTIVITY_WIFI_WIFI)
                            .setWifiInfo(wifiManager.getConnectionInfo().getSSID().replace("\"", ""), getWifiIpAddr());
                    mRemoteCam.startSession();
                    fragmentPlaybackList.setRemoteCam(mRemoteCam);
                    fragmentRTVideo.setRemoteCam(mRemoteCam);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rgGroup.check(R.id.rb_realTimeVideo);
                            fragmentRTVideo = FragmentRTVideo.newInstance();
                            fragment = fragmentRTVideo;
                            getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment).commitAllowingStateLoss();
                        }
                    }, 800);
                    if (fragment == fragmentRTVideo) {
                        if (hasCard) {
                            fragmentRTVideo.setImagerAple(false);
                        }
                    }
                }
                isReconnecting = false;
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_QUERY_SESSION_HOLDER:
                mRemoteCam.actionQuerySessionHolder();
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_SET_SETTING:
//                fragmentRTVideo.setMicState(isMicOn);
                showToastTips(getString(R.string.voice_settingfail));
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_APP_STATE_INIT:
                valueRecordInit = (int) param;
                switch (valueRecordInit) {
                    case ServerConfig.REC_CAP_STATE_PREVIEW:
                        break;
                    case ServerConfig.REC_CAP_STATE_RECORD:
                        myApplication.setisRescod(true);
                        fragmentRTVideo.setRecordState(true);
                        break;
                    case ServerConfig.REC_CAP_STATE_PRE_RECORD:
                        break;
                    case ServerConfig.REC_CAP_STATE_FOCUS:
                        break;
                    case ServerConfig.REC_CAP_STATE_CAPTURE:
                        break;
                    case ServerConfig.REC_CAP_STATE_VF:
                        myApplication.setisRescod(false);
                        fragmentRTVideo.setRecordState(false);
                        break;
                    case ServerConfig.REC_CAP_STATE_TRANSIT_TO_VF:
                        break;
                    case ServerConfig.REC_CAP_STATE_RESET:
                        break;
                    default:
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_SDCARD_STATE_INIT:
                valueSdcardInit = (int) param;
                switch (valueSdcardInit) {
                    case ServerConfig.BYD_CARD_STATE_OK:
                        hasCard = true;
                        if (fragment == fragmentRTVideo) {
                            if (!isReconnecting) {
                                fragmentRTVideo.setImagerAple_SD(false);
                            }
                        }
                        break;
                    case ServerConfig.BYD_CARD_STATE_NOCARD:
                        hasCard = false;
                        fragmentRTVideo.showCheckSdCordTag(false);
                        showConfirmDialog(getString(R.string.card_removed));
                        if (fragment == fragmentRTVideo) {
                            fragmentRTVideo.setImagerAple_SD(true);
                        }
                        break;
                    case ServerConfig.BYD_CARD_STATE_SMALL_NAND:
                    case ServerConfig.BYD_CARD_STATE_NOT_MEM:
                    case ServerConfig.BYD_CARD_STATE_SETROOT_FAIL:
                    case ServerConfig.BYD_CARD_STATE_UNINIT:
                        hasCard = true;
                        showConfirmDialog(getString(R.string.card_issue));
                        break;
                    case ServerConfig.BYD_CARD_STATE_NEED_FORMAT:
                        hasCard = true;
                        showDoubleImmeFormatDialog(getString(R.string.card_need_format));
                        fragmentRTVideo.showCheckSdCordState();
                        if (fragment == fragmentRTVideo) {
                            fragmentRTVideo.setImagerAple_SD(true);
                        }
                        break;
                    case ServerConfig.BYD_CARD_STATE_NOT_ENOUGH:
                        hasCard = true;
                        showConfirmDialog(getString(R.string.card_not_enough));
                        break;
                    case ServerConfig.BYD_CARD_STATE_WP:
                        hasCard = true;
                        showConfirmDialog(getString(R.string.card_write_protect));
                        break;
                    default:
                        break;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_EVENTRECORD_STATE_INIT:
                valueEventRecord = (int) param;
                switch (valueEventRecord) {
                    case 0:
//                        showToastTips(getString(R.string.LockVideo_success));
                        break;
                    case 1:
//                        showToastTips(getString(R.string.video_locking));
                        break;
                    case 2:
//                        showToastTips(getString(R.string.LockVideo_fail));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void handleDataChannelEvent(int type, Object param) {
        switch (type) {
            case IChannelListener.DATA_CHANNEL_EVENT_GET_START:
                myDialog = MyDialog.newInstance(2, getString(R.string.downloading));
                myDialog.show(getFragmentManager(), "doingDownload");
                myDialog.setOnDialogButtonClickListener(new MyDialog.OnDialogButtonClickListener() {
                    @Override
                    public void okButtonClick() {
                    }

                    @Override
                    public void cancelButtonClick() {
                    }
                });
                break;
            default:
                break;
        }
    }

    private void handleCmdChannelError(int type, Object param) {
        switch (type) {
            case IChannelListener.CMD_CHANNEL_ERROR_INVALID_TOKEN:
//                showToastTips(getString(R.string.invalid_token));
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_TIMEOUT:
                //showToastTips(getString(R.string.time_out));
//                mRemoteCam.stopSession();
//                对应管道破裂
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                mRemoteCam = new RemoteCam(getApplicationContext());
                myApplication.setRemoteCam(mRemoteCam);
                mRemoteCam.setChannelListener(this).setConnectivity(RemoteCam
                        .CAM_CONNECTIVITY_WIFI_WIFI)
                        .setWifiInfo(wifiManager.getConnectionInfo().getSSID().replace("\"", ""), getWifiIpAddr());
                mRemoteCam.startSession();
                fragmentPlaybackList.setRemoteCam(mRemoteCam);
                fragmentRTVideo.setRemoteCam(mRemoteCam);
                break;

            case IChannelListener.CMD_CHANNEL_ERROR_BLE_INVALID_ADDR:
//                showAlertDialog("Error", "Invalid bluetooth device");
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_BLE_DISABLED:
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_BROKEN_CHANNEL:
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_CONNECT:
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_WAKEUP:

                // TODO: 2018/4/14 此处socket会断，闪退
                if (!isDialogShow) {
                    showConfirmDialog(getString(R.string.connect_fail));
                    isDialogShow = true;
                    if (fragment == fragmentRTVideo) {
                        fragmentRTVideo.setImagerAple(true);
                    }
                }
                isReconnecting = true;

                Log.e(TAG, "handleCmdChannelEvent: Waking up the Remote Camera ERROR");
                break;
            default:
                break;
        }
    }


    @Override
    public void onFragmentAction(int type, Object param, Integer... array) {
        switch (type) {
            case IFragmentListener.ACTION_PHOTO_START:
                mRemoteCam.takePhoto();
                break;
            case IFragmentListener.ACTION_LOCK_VIDEO_START:
                showWaitingDialog(getString(R.string.video_locking));
                mRemoteCam.lockVideo();
                break;
            case IFragmentListener.ACTION_FRIMWORK_VERSION:
                mRemoteCam.frimworkVersion();
                break;
            case IFragmentListener.ACTION_APP_VERSION:
                String ver = getAppVersion(getApplicationContext());
                showConfirmDialog("App" + getString(R.string.version) + " " + ver);
//                checkUpdateThread();
                break;
            case IFragmentListener.ACTION_RECORD_START:
                boolean isRecord = (boolean) param;
                if (isRecord) {
                    mRemoteCam.startRecord();
                } else {
                    mRemoteCam.stopRecord();
                }
                break;
            case IFragmentListener.ACTION_MIC_ON:
//                若使用旧协议使用如下
//                isMicOn = (boolean) param;
//                if (isMicOn) {
//                    mRemoteCam.startMic();
//                    showToastTips(getString(R.string.open_voice));
//                } else {
//                    mRemoteCam.stopMic();
//                    showToastTips(getString(R.string.close_voice));
//                }
//                fragmentRTVideo.setMicState(isMicOn);
                // TODO: 2018/4/25 后续收vil
                break;
            case IFragmentListener.ACTION_RECORD_TIME:
                // TODO: 2018/4/3
//                mRemoteCam.getRecordTime();
                break;
            case IFragmentListener.ACTION_FS_LS:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragmentPlaybackList.showLoadView(true);
                    }
                });
                mRemoteCam.listDir((String) param);
                break;
            case IFragmentListener.ACTION_DEFAULT_SETTING:
                mRemoteCam.defaultSetting();
                break;

            case IFragmentListener.ACTION_FS_FORMAT_SD:
//                if (isCardNoExist) {
                if (!hasCard) {
                    showConfirmDialog(getString(R.string.card_removed));
                } else {
                    showDoubleButtonDialog(getString(R.string.confirm_format_memory_card));
                }
                break;
            case IFragmentListener.ACTION_FS_DELETE_MULTI:
                selectedLists = (ArrayList<Model>) param;
                selectedCounts = selectedLists.size();
                break;
            case IFragmentListener.ACTION_FS_DELETE:
                mRemoteCam.deleteFile((String) param);
                break;
            case IFragmentListener.ACTION_FS_DOWNLOAD:
                if (param != null) {

                } else {
                    countsDownload();
                    downloadManager = (DownloadManager) getApplicationContext().getSystemService
                            (Context.DOWNLOAD_SERVICE);
                    ContentObserver mObserver;
                    mObserver = new DownloadChangeObserver(null);
                    getContentResolver().registerContentObserver(CONTENT_URI, true, mObserver);
                    query = new DownloadManager.Query();

                    new MyTheardDownLoad().start();
                }
                break;
            case IFragmentListener.ACTION_FS_DELETE_WAITING_TIP:
                showWaitingDialog(getString(R.string.deleting));
                break;
            default:
                break;
        }
    }

    public String getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            return info.versionCode;
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void updateCardData() {

        if (customDialog != null && !isFinishing()) {
            customDialog.dismiss();
        }
        if (getSupportFragmentManager().isStateSaved()) {
            showConfirmDialog(getString(R.string.reboot_drivingReorder));
        } else {
            // TODO: 2018/4/14 当不在这个界面时如何处理
            if (fragment == fragmentRTVideo) {
                rgGroup.check(R.id.rb_realTimeVideo);
                fragmentRTVideo = FragmentRTVideo.newInstance();
                fragment = fragmentRTVideo;
                getSupportFragmentManager().beginTransaction().replace(flMain.getId(), fragment).commitAllowingStateLoss();
            } else if (fragment == fragmentPlaybackList) {
                fragmentPlaybackList.setRemoteCam(mRemoteCam);
                if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                    fragmentPlaybackList.showRecordList();
                } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                    fragmentPlaybackList.showLockVideoList();
                } else {
                    fragmentPlaybackList.showCapturePhotoList();
                }
            }
        }
    }

    /*
     *
     *
     *3.30 add
     * */
// 查询下载进度，文件总大小多少，已经下载多少？
    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    private int newsize = 0, totalsize = 0;
    private static int IDcount = 0;
    //获取下载管理器
    private DownloadManager downloadManager;
    DownloadManager.Query query;

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onChange(boolean selfChange) {
            queryDownloadStatus();
        }
    }

    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private void queryDownloadStatus() {
        try {
            query.setFilterById(IdA);
            Cursor c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                int reasonIdx = c.getColumnIndex(DownloadManager.COLUMN_REASON);
                int titleIdx = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
                int fileSizeIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                int bytesDLIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                String title = c.getString(titleIdx);
                int fileSize = c.getInt(fileSizeIdx);
                int bytesDL = c.getInt(bytesDLIdx);
                newsize = bytesDL;
                totalsize = fileSize;
                // Translate the pause reason to friendly text.
                int reason = c.getInt(reasonIdx);
                StringBuilder sb = new StringBuilder();
                sb.append(title).append("\n");
                sb.append("Downloaded ").append(bytesDL).append(" / ").append(fileSize);

                // Display the status
                Log.d("tag", sb.toString());
                switch (status) {
                    case DownloadManager.STATUS_PAUSED:
                        Log.v("tag", "STATUS_PAUSED");
                        // progressDialogFragment.dismissAllowingStateLoss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (IDcount != 0) {
                                    //更新UI
                                    Toast.makeText(MainActivity.this, getString(R.string
                                            .Download_fail), Toast.LENGTH_SHORT).show();
                                }
                                showdialogA = false;
                                countsOKdownload = 0;
                                IDcount = 0;
                                downloading = false;
                                downloadManager.remove(IdA);
                            }
                        });
                        break;

                    case DownloadManager.STATUS_PENDING:
                        Log.v("tag", "STATUS_PENDING");
                        break;

                    case DownloadManager.STATUS_RUNNING:
                        // 正在下载，不做任何事情
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //更新UI
                                double temp = div((double) newsize, (double) totalsize, 3);
                                if (progressDialogFragment != null) {
                                    progressDialogFragment.setProgressText((int) (temp * 100.00));
                                    progressDialogFragment.setMessageText(getString(R.string
                                            .downloading) + (IDcount) + "/" + countsOKdownload);
                                }
                            }
                        });
                        downloading = true;

                        Log.v("tag", "STATUS_RUNNING");
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        // 完成
                        Log.v("tag", "下载完成");
                        if (countsOKdownload == IDcount) {
                            progressDialogFragment.dismissAllowingStateLoss();
                            showdialogA = false;
                            countsOKdownload = 0;
                            IDcount = 0;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToastTips(getString(R.string
                                            .Download_completed));
                                }
                            });
                        }
                        downloading = false;


                        break;
                    case DownloadManager.STATUS_FAILED:
                        // 清除已下载的内容，重新下载
                        Log.v("tag", "STATUS_FAILED");
                        countsOKdownload = 0;
                        IDcount = 0;
                        downloading = false;
                        downloadManager.remove(IdA);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readSDCard() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            //String count2 = Formatter.formatFileSize(getApplicationContext(), sf.getFreeBytes());
            int free = (int) (sf.getFreeBytes() / 1024 / 1024 / 1024);
            if (free < 2) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.download_free_space, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }
    }

    private long IdA;
    boolean showdialogA = false;//
    boolean downloading = false;//是否在下载，其他下载等待
    private int countsOKdownload = 0;

    private void countsDownload() {
        boolean downloadImage = false;
        countsOKdownload = 0;
        IDcount = 0;
        downloading = false;
        selectedListsA.clear();
        readSDCard();

        for (int i = 0; i < selectedCounts; i++) {
            if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/NORMAL/" + selectedLists
                        .get(i).getName();

            } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/EVENT/" + selectedLists
                        .get(i).getName();

            } else {
                mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" + selectedLists
                        .get(i).getName();
                downloadImage = true;
            }
            String fileName = Environment.getExternalStorageDirectory() + "/行车记录仪" + mGetFileName
                    .substring(mGetFileName.lastIndexOf('/'));
            File file = new File(fileName);
            if (!file.exists()) {
                selectedListsA.add(selectedLists.get(i));
                countsOKdownload++;
            }
        }

        if (downloadImage) {
            if (selectedCounts > 9) {
                countsOKdownload = 0;
                IDcount = 0;
                downloading = false;
                selectedListsA.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.download_num, Toast.LENGTH_SHORT).show();

                    }
                });
                return;
            }
        } else {
            if (selectedCounts > 5) {
                countsOKdownload = 0;
                IDcount = 0;
                downloading = false;
                selectedListsA.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.download_num_tv, Toast.LENGTH_SHORT).show();

                    }
                });
                return;
            }
        }

        if (countsOKdownload == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, R.string.File_downloaded, Toast
                            .LENGTH_SHORT).show();
                }
            });
        }
    }

    private void downloadfilesA() {

        if (countsOKdownload > IDcount) {

            if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_RECORD_VIDEO) {
                mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/NORMAL/" + selectedListsA
                        .get(IDcount).getName();
            } else if (fragmentPlaybackList.currentRadioButton == ServerConfig.RB_LOCK_VIDEO) {
                mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/EVENT/" + selectedListsA
                        .get(IDcount).getName();
            } else {
                mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" + selectedListsA
                        .get(IDcount).getName();
            }
            String fileName = Environment.getExternalStorageDirectory() + "/行车记录仪" + mGetFileName
                    .substring(mGetFileName.lastIndexOf('/'));
            //创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mGetFileName));
            //指定下载路径和下载文件名
            request.setDestinationInExternalPublicDir("/行车记录仪/", selectedListsA.get(IDcount)
                    .getName());
            //不显示下载界面
            request.setVisibleInDownloadsUi(true);
            //将下载任务加入下载队列，否则不会进行下载
            IdA = downloadManager.enqueue(request);
            IDcount++;//注意关键

            if (!showdialogA) {

                progressDialogFragment = ProgressDialogFragment.newInstance(getString(R.string
                        .downloading) + (IDcount) + "/" + countsOKdownload);

                progressDialogFragment.show(getFragmentManager(), "text");
                progressDialogFragment.setOnDialogButtonClickListener(new ProgressDialogFragment
                        .OnDialogButtonClickListener() {
                    @Override

                    public void okButtonClick() {

                    }

                    @Override
                    public void cancelButtonClick() {
                        downloadManager.remove(IdA);
                        countsOKdownload = 0;
                        downloading = false;
                        showdialogA = false;
                        mRemoteCam.restartHttp();//重置记录仪Http
                    }
                });
                showdialogA = true;
            }
        }
    }

    public class MyTheardDownLoad extends Thread {
        @Override
        public void run() {
            while (countsOKdownload > IDcount) {
                try {
                    if (!downloading) {
                        downloadfilesA();
                    }
                    MyTheardDownLoad.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showTipDialog(String msg) {
        if (myDialog != null) {
            myDialog.dismiss();
        }
        if (progressDialogFragment != null) {
            progressDialogFragment.dismiss();
        }
        myDialog = MyDialog.newInstance(1, msg);
        myDialog.show(getFragmentManager(), "showTipDialog");
    }

    public void requestPermission() {
        //判断当前Activity是否已经获得了该权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                Toast.makeText(this,"please give me the permission",Toast.LENGTH_SHORT).show();
                showTipDialog(getString(R.string.open_permissions));
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);
            }
        }
    }
/*
    private static class UpdateHandler extends Handler {
        private WeakReference<Context> reference;

        UpdateHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = (MainActivity) reference.get();
            if (mainActivity != null) {
                switch (msg.what) {
                }
            }
        }
    }*/

    private void changeSkin(int bydTheme) {
        if (bydTheme == 1) {
            //经济模式
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_back_selector), null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector), null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector), null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_setting_selector), null, null);
            } else {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_back_selector),
                        null, null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector),
                        null, null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector),
                        null, null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_setting_selector),
                        null, null, null);
            }
        } else if (bydTheme == 2) {
            //运动模式
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_back_selector_sport), null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector_sport), null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector_sport), null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_setting_selector_sport), null, null);
            } else {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_back_selector_sport),
                        null, null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector_sport),
                        null, null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector_sport),
                        null, null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_setting_selector_sport),
                        null, null, null);
            }
        } else if (bydTheme == 101) {
            //hadeco模式
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_back_selector_hadeco), null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector_hadeco), null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector_hadeco), null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_setting_selector_hadeco), null, null);
            } else {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_back_selector_hadeco),
                        null, null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector_hadeco),
                        null, null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector_hadeco),
                        null, null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_setting_selector_hadeco),
                        null, null, null);
            }
        } else if (bydTheme == 102) {
            //had运动模式
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_back_selector_hadsport), null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector_hadsport), null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector_hadsport), null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.btn_tab_setting_selector_hadsport), null, null);
            } else {
                btnBack.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_back_selector_hadsport),
                        null, null, null);
                rbRealTimeVideo.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_realtimevideo_selector_hadsport),
                        null, null, null);
                rbPlaybackList.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_playbacklist_selector_hadsport),
                        null, null, null);
                rbSetting.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_tab_setting_selector_hadsport),
                        null, null, null);
            }
        }
    }

    private static class TestConnectRunnable implements Runnable {

        @Override
        public void run() {

        }
    }

}

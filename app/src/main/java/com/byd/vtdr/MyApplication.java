package com.byd.vtdr;

import android.app.Application;
import android.hardware.bydauto.energy.AbsBYDAutoEnergyListener;
import android.hardware.bydauto.energy.BYDAutoEnergyDevice;
import android.os.Handler;
import android.os.Message;

import com.byd.vtdr.widget.Theme;
import com.byd.vtdr.widget.ThemeManager;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;

import skin.support.SkinCompatManager;
import skin.support.constraint.app.SkinConstraintViewInflater;

import static android.hardware.bydauto.energy.BYDAutoEnergyDevice.ENERGY_OPERATION_SPORT;

/**
 * Created by ximsfei on 2017/1/10.
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private BYDAutoEnergyDevice mBYDAutoEnergyDevice;
    private ThemeManager themeManager;
    public boolean isRescod;
    private RemoteCam mRemoteCam;
    public boolean isRemoteCreate;
    private RefWatcher refWatcher;

    private Handler modelChange = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == ENERGY_OPERATION_SPORT) {
                themeManager.updateTheme(Theme.SPORT);
                SkinCompatManager.getInstance().loadSkin("sport", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                EventBus.getDefault().post(new MessageEvent());
            } else {
                themeManager.updateTheme(Theme.NORMAL);
                SkinCompatManager.getInstance().restoreDefaultTheme();
                EventBus.getDefault().post(new MessageEvent());
            }
        }
    };

    AbsBYDAutoEnergyListener absBYDAutoEnergyListener = new AbsBYDAutoEnergyListener() {
        @Override
        public void onOperationModeChanged(int type) {
            // TODO Auto-generated method stub
            super.onOperationModeChanged(type);
            modelChange.sendEmptyMessage(type);
        }

        @Override
        public void onRoadSurfaceChanged(int type) {
            super.onRoadSurfaceChanged(type);
            modelChange.sendEmptyMessage(type);
        }
    };
    private int mode;

    @Override
    public void onCreate() {
        super.onCreate();
        SkinCompatManager.withoutActivity(this)
//                .addStrategy(new CustomSDCardLoader())          // 自定义加载策略，指定SDCard路径
//                .addInflater(new SkinMaterialViewInflater())    // material design
                .addInflater(new SkinConstraintViewInflater())  // ConstraintLayout
//                .addInflater(new SkinCardViewInflater())        // CardView v7
//                .addInflater(new SkinCircleImageViewInflater()) // hdodenhof/CircleImageView
//                .addInflater(new SkinFlycoTabLayoutInflater())  // H07000223/FlycoTabLayout
//                .setSkinStatusBarColorEnable(false)             // 关闭状态栏换肤
//                .setSkinWindowBackgroundEnable(false)           // 关闭windowBackground换肤
//                .setSkinAllActivityEnable(false)                // true: 默认所有的Activity都换肤; false: 只有实现SkinCompatSupportable接口的Activity换肤
                .loadSkin();
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        themeManager = ThemeManager.getInstance();

        //如下为运动模式功能开启代码
        mBYDAutoEnergyDevice = BYDAutoEnergyDevice.getInstance(getApplicationContext());
        mBYDAutoEnergyDevice.registerListener(absBYDAutoEnergyListener);
        mode = mBYDAutoEnergyDevice.getOperationMode();
        if (mode == ENERGY_OPERATION_SPORT) {
            //运动模式
            themeManager.updateTheme(Theme.SPORT);
            SkinCompatManager.getInstance().loadSkin("sport", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
        } else {
            //其他模式
            themeManager.updateTheme(Theme.NORMAL);
            SkinCompatManager.getInstance().restoreDefaultTheme();
        }

        mRemoteCam = new RemoteCam(this);

        /*refWatcher = setupLeakCanary();*/

    }

   /* private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        MyApplication myApplication = (MyApplication) context.getApplicationContext();
        return myApplication.refWatcher;
    }*/

    public void setisRescod(boolean is) {
        isRescod = is;
    }

    public boolean getisRescod() {
        return isRescod;
    }

    public void setRemoteCam(RemoteCam remoteCam) {
        mRemoteCam = remoteCam;
    }

    public RemoteCam getRemoteCam() {
        return mRemoteCam;
    }

    public int getMode() {
        return mode;
    }
}

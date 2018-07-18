package com.byd.vtdr;

import android.app.Application;
import android.content.res.Configuration;
import android.hardware.bydauto.energy.BYDAutoEnergyDevice;

import com.byd.vtdr.widget.Theme;
import com.byd.vtdr.widget.ThemeManager;
import com.squareup.leakcanary.RefWatcher;

import skin.support.SkinCompatManager;
import skin.support.constraint.app.SkinConstraintViewInflater;

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
        initView();
     /*   //如下为运动模式功能开启代码
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

        */
        mRemoteCam = new RemoteCam(this);

        /*refWatcher = setupLeakCanary();*/

    }

    private void initView() {
        int bydTheme = getResources().getConfiguration().byd_theme;
        changeSkin(bydTheme);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int bydTheme = newConfig.byd_theme;
        changeSkin(bydTheme);
    }

    private void changeSkin(int bydTheme) {
        if (bydTheme == 1) {
            //经济模式
            themeManager.updateTheme(Theme.NORMAL);
            SkinCompatManager.getInstance().restoreDefaultTheme();
        } else if (bydTheme == 2) {
            //运动模式
            themeManager.updateTheme(Theme.SPORT);
            SkinCompatManager.getInstance().loadSkin("sport", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
        } else if (bydTheme == 101) {
            //运动模式
            themeManager.updateTheme(Theme.HAD_NORMAL);
            SkinCompatManager.getInstance().loadSkin("hadeco", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
        } else if (bydTheme == 102) {
            //运动模式
            themeManager.updateTheme(Theme.HAD_SPORT);
            SkinCompatManager.getInstance().loadSkin("hadsport", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
        }
    }
}

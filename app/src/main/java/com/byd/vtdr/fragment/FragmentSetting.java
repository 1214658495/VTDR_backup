package com.byd.vtdr.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byd.lighttextview.LightButton;
import com.byd.vtdr.R;
import com.byd.vtdr.connectivity.IFragmentListener;
import com.byd.vtdr.view.MyDialog;
import com.byd.vtdr.widget.ThemeLightButton;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by byd_tw on 2017/11/1.
 */

public class FragmentSetting extends Fragment {
    @BindView(R.id.iv_line_settingtext)
    ImageView ivLineSettingtext;
    @BindView(R.id.iv_bg_settingtext)
    ImageView ivBgSettingtext;

    @BindView(R.id.btn_memoryCard_format)
    ThemeLightButton btnMemoryCardFormat;

    @BindView(R.id.btn_firmwareVersion)
    ThemeLightButton btnFirmwareVersion;
    @BindView(R.id.btn_appVersion)
    ThemeLightButton btnAppVersion;
    @BindView(R.id.btn_default_setting)
    LightButton btnDefaultSetting;
    Unbinder unbinder;
    @BindView(R.id.update_test)
    LightButton btnUpdateTest;
    @BindView(R.id.tv_appVersionDetail)
    TextView tvAppVersionDetail;
    @BindView(R.id.tv_textSetting)
    TextView tvTextSetting;
    @BindView(R.id.tv_version)
    TextView tvVersion;

    private IFragmentListener mListener;
    private MyDialog myDialog;
    public boolean reload = false;
    private int clickNum;

    public static FragmentSetting newInstance() {
        FragmentSetting fragmentSetting = new FragmentSetting();

        return fragmentSetting;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        tvAppVersionDetail.setText(getAppVersion(Objects.requireNonNull(getContext())));
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.e("CAM_Commands:", "onAttach");
        super.onAttach(activity);
        try {
            mListener = (IFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
      /*  RefWatcher refWatcher = MyApplication.getRefWatcher(getContext());
        refWatcher.watch(this);*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_default_setting, R.id.btn_memoryCard_format, R.id.btn_firmwareVersion, R.id.btn_appVersion, R.id.update_test,R.id.tv_textSetting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_default_setting:
                myDialog = MyDialog.newInstance(0, getString(R.string.restore_settings));
                myDialog.show(getActivity().getFragmentManager(), "default_setting");
                myDialog.setOnDialogButtonClickListener(new MyDialog.OnDialogButtonClickListener() {
                    @Override
                    public void okButtonClick() {
                        mListener.onFragmentAction(IFragmentListener.ACTION_DEFAULT_SETTING, null);
                    }

                    @Override
                    public void cancelButtonClick() {
                        // TODO: 2018/1/4 点击取消有空指针异常
                    }
                });
                break;
            case R.id.btn_memoryCard_format:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_FORMAT_SD, "C:");
                break;
            case R.id.btn_firmwareVersion:
                mListener.onFragmentAction(IFragmentListener.ACTION_FRIMWORK_VERSION, null);
                break;
            case R.id.btn_appVersion:
                mListener.onFragmentAction(IFragmentListener.ACTION_APP_VERSION, null);
                break;
            case R.id.update_test:
//                mListener.onFragmentAction(IFragmentListener.ACTION_APP_VERSION,null);
                break;
            case R.id.tv_textSetting:
                clickNum++;
//                mListener.onFragmentAction(IFragmentListener.ACTION_APP_VERSION,null);
                if (clickNum == 3) {
                    if (mListener != null) {
                        mListener.onFragmentAction(IFragmentListener.ACTION_FRIMWORK_VERSION, null);
                    }
                    clickNum = 0;
                }
                break;
            default:
                break;
        }
    }

    private String getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            return info.versionCode;
            return "V" + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

}

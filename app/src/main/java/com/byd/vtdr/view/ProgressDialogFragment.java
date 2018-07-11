package com.byd.vtdr.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.byd.vtdr.R;
import com.byd.vtdr.widget.ThemeLightButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by byd_tw on 2018/1/9.
 */

public class ProgressDialogFragment extends DialogFragment {
//    @BindView(R.id.btn_closeDialog)
//    ImageButton btnCloseDialog;

    @BindView(R.id.tv_dialogContent)
    TextView tvDialogContent;
    @BindView(R.id.btn_dialogSure)
    ThemeLightButton btnDialogSure;
    @BindView(R.id.btn_dialogCancel)
    ThemeLightButton btnDialogCancel;
    Unbinder unbinder;
    @BindView(R.id.tv_progressPercent)
    TextView tvProgressPercent;
    @BindView(R.id.pb_downloadProgressBar)
    ProgressBar pbDownloadProgressBar;
    private int mHeight;
    public static String pReccsstext;

    private ProgressDialogFragment.OnDialogButtonClickListener buttonClickListener;

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment newDialog = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        newDialog.setArguments(bundle);
        return newDialog;
    }

    public interface OnDialogButtonClickListener {
        void okButtonClick();

        void cancelButtonClick();
    }

    public void setOnDialogButtonClickListener(ProgressDialogFragment.OnDialogButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(false);
        View view;
        String message = getArguments().getString("message");
        view = inflater.inflate(R.layout.fragment_progress_dialog, container);
        unbinder = ButterKnife.bind(this, view);

        tvDialogContent.setText(message);

        btnDialogSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClickListener.okButtonClick();
                getDialog().cancel();
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClickListener.cancelButtonClick();
                pReccsstext = "   ";
                getDialog().cancel();
            }
        });
        if (tvProgressPercent != null) {
            tvProgressPercent.setText(pReccsstext);
        }
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog myDialog = getDialog();
        if (myDialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.8), (int) (dm.heightPixels * 0.25));
                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.8), (int) (dm.heightPixels * 0.2));
            } else {
//                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.6), (int) (dm.heightPixels * 0.5));
                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.4));
            }
            WindowManager.LayoutParams wl = myDialog.getWindow().getAttributes();
            wl.y = -50;
            myDialog.getWindow().setAttributes(wl);
        }
    }

//    @OnClick(R.id.btn_closeDialog)
//    public void onViewClicked(View view) {
//        dismiss();
//    }

    public void setProgressText(int progress) {
        if (tvProgressPercent != null) {
            tvProgressPercent.setText(progress + "%");
        }
    }

    public void setMessageText(String msg) {
        if (tvDialogContent != null) {
            tvDialogContent.setText(msg);
        }
    }
    @Override
    public void onDestroyView() {
        // 如下在旋转后重建了
        pReccsstext = (String) tvProgressPercent.getText();
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
        unbinder.unbind();
    }

}

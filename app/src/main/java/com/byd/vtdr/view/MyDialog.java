package com.byd.vtdr.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.byd.vtdr.R;
import com.byd.vtdr.widget.ThemeLightButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author byd_tw
 * @date 2017/11/13
 */

public class MyDialog extends DialogFragment {
    @BindView(R.id.btn_closeDialog)
    ImageButton btnCloseDialog;

    @BindView(R.id.tv_dialogContent)
    TextView tvDialogContent;
    @BindView(R.id.btn_dialogSure)
    ThemeLightButton btnDialogSure;
    @BindView(R.id.btn_dialogCancel)
    ThemeLightButton btnDialogCancel;
    Unbinder unbinder;
    private int mHeight;

    private OnDialogButtonClickListener buttonClickListener;

    public static MyDialog newInstance(int style,String message) {
        MyDialog newDialog = new MyDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("myStyle", style);
        bundle.putString("message",message);
        newDialog.setArguments(bundle);
        return newDialog;
    }

    public interface OnDialogButtonClickListener {
        void okButtonClick();
        void cancelButtonClick();
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        int myStyleNum = getArguments().getInt("myStyle", 0);
//        int style = 0;
//        switch (myStyleNum) {
//            case 0:
//                style = DialogFragment.STYLE_NORMAL;
//                break;
//            case 1:
//                style = DialogFragment.STYLE_NO_TITLE;
//                break;
//            case 2:
//                style = DialogFragment.STYLE_NO_FRAME;
//                break;
//            case 3:
//                style = DialogFragment.STYLE_NO_INPUT;
//                break;
//            default:
//                break;
//        }
//        setStyle(style,0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//        //        dialogFragment  setting size
//        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        if (mHeight == 0) {
//            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        } else {
//            lp.height = mHeight;
//        }
//        getDialog().getWindow().setAttributes(lp);


        View view;
        int myStyleNum = getArguments().getInt("myStyle", 0);
        String message = getArguments().getString("message");
        if (myStyleNum == 0) {
            view = inflater.inflate(R.layout.fragment_dialog, container);
        } else if (myStyleNum == 1) {
            view = inflater.inflate(R.layout.fragment_simple_dialog, container);
        } else {
            view = inflater.inflate(R.layout.fragment_progress_dialog,container);
        }
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
                getDialog().cancel();
            }
        });
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog myDialog = getDialog();
        if (myDialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.8), (int) (dm.heightPixels * 0.25));
//            } else {
//                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.6), (int) (dm.heightPixels * 0.5));
//            }

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

    @OnClick(R.id.btn_closeDialog)
    public void onViewClicked(View view) {
                dismiss();
    }


    public void show(FragmentManager fragmentManager, String delete) {
    }

    @Override
    public void onDestroyView() {
        // 如下在旋转后重建了
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
        unbinder.unbind();
    }

//    @Override
//    public void show(FragmentManager manager, String tag) {
////        super.show(manager, tag);
//        FragmentTransaction ft = manager.beginTransaction();
//        ft.add(this, tag);
//        // 这里吧原来的commit()方法换成了commitAllowingStateLoss()
//        ft.commitAllowingStateLoss();
//
//    }
}

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
import android.widget.TextView;

import com.byd.lighttextview.LightButton;
import com.byd.vtdr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author byd_tw
 * @date 2017/11/13
 */

public class AddSingleButtonDialog extends DialogFragment {

    @BindView(R.id.tv_dialogContent)
    TextView tvDialogContent;
    @BindView(R.id.btn_dialogSure)
    LightButton btnDialogSure;
    @BindView(R.id.btn_dialogCancel)
    LightButton btnDialogCancel;
    Unbinder unbinder;

    private OnDialogButtonClickListener buttonClickListener;
    private static AddSingleButtonDialog addSingleButtonDialog;

    public static AddSingleButtonDialog newInstance(String message) {
        if (addSingleButtonDialog == null) {
            synchronized (AddSingleButtonDialog.class) {
                if (addSingleButtonDialog == null) {
                    addSingleButtonDialog = new AddSingleButtonDialog();
                }
            }
        }
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        if (!addSingleButtonDialog.isVisible()) {
            addSingleButtonDialog.setArguments(bundle);
        }
        return addSingleButtonDialog;
    }

    public interface OnDialogButtonClickListener {
        void okButtonClick();

        void cancelButtonClick();
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        buttonClickListener = (OnDialogButtonClickListener) context;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        添加如下则dialog旋转可消失
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
        view = inflater.inflate(R.layout.fragment_addsinglebutton_dialog, container);
        unbinder = ButterKnife.bind(this, view);

        tvDialogContent.setText(message);

        btnDialogSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {
                    buttonClickListener.okButtonClick();
                }
                getDialog().cancel();
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {
                    buttonClickListener.cancelButtonClick();
                }
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
            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.8), (int) (dm.heightPixels * 0.25));
            } else {
                myDialog.getWindow().setLayout((int) (dm.widthPixels * 0.6), (int) (dm.heightPixels * 0.5));
            }
        }
    }


    @Override
    public void onDestroyView() {
        // 如下在旋转后重建了
       /* Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }*/
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

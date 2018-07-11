package com.byd.vtdr.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.byd.vtdr.R;

/**
 * Created by byd_tw on 2018/3/22.
 */

public class CustomDialog extends Dialog {
    private Context context;
    private boolean cancelTouchOut;
    private View view;
    private TextView tvDialogContent;
    private Button btnDialogSure;
    private Button btnCancel;
    private String title;
//    private String buttonText;


    private CustomDialog(Builder builder) {
        super(builder.context);
        context = builder.context;
        cancelTouchOut = builder.cancelTouchOut;
        view = builder.view;
        title = builder.title;
//        buttonText = builder.buttonText;
    }

    private CustomDialog(Builder builder, int resStyle) {
        super(builder.context, resStyle);
        context = builder.context;
        cancelTouchOut = builder.cancelTouchOut;
        view = builder.view;
        title = builder.title;
//        buttonText = builder.buttonText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvDialogContent = view.findViewById(R.id.tv_dialogContent);
//        btnDialogSure = view.findViewById(R.id.btn_dialogSure);
//        btnCancel = view.findViewById(R.id.btn_cancel);
        setContentView(view);
        setCanceledOnTouchOutside(cancelTouchOut);
        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            getWindow().setLayout((int) (dm.widthPixels * 0.8), (int) (dm.heightPixels * 0.25));
            getWindow().setLayout((int) (dm.widthPixels * 0.8), (int) (dm.heightPixels * 0.2));
        } else {
//            getWindow().setLayout((int) (dm.widthPixels * 0.6), (int) (dm.heightPixels * 0.5));
            getWindow().setLayout((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.4));
        }
        WindowManager.LayoutParams wl = getWindow().getAttributes();
        wl.y = -50;
        getWindow().setAttributes(wl);
        tvDialogContent.setText(title);
//        if (ok) {
//        }
//        btnDialogSure.setText(ok);
//        btnCancel.setText(cancel);
    }


    public static final class Builder {
        private Context context;
        private boolean cancelTouchOut;
        private View view;
        private int resStyle = -1;
        private String title;
        private String buttonText;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder view(int resView) {
            view = LayoutInflater.from(context).inflate(resView, null);
            return this;
        }

        public Builder style(int resStyle) {
            this.resStyle = resStyle;
            return this;
        }

        public Builder cancelTouchOut(boolean val) {
            cancelTouchOut = val;
            return this;
        }

        public Builder addViewOnclick(int viewRes, View.OnClickListener listener) {
            view.findViewById(viewRes).setOnClickListener(listener);
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public CustomDialog build() {
            if (resStyle != -1) {
                return new CustomDialog(this, resStyle);
            } else {
                return new CustomDialog(this);
            }
        }
    }

}

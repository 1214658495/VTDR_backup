package com.byd.vtdr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.Objects;

/**
 * @author byd_tw
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ServerConfig.BORADCAST_ACTION_EXIT);
//        filter.addAction(ServerConfig.BORADCAST_ACTION_TAKEPHOTO);
//        filter.addAction(ServerConfig.BORADCAST_ACTION_LOCKVIDEO);
//        filter.addAction(ServerConfig.BORADCAST_ACTION_OPENVOICE);
//        filter.addAction(ServerConfig.BORADCAST_ACTION_CLOSEVOICE);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //发来关闭action的广播
            if (Objects.equals(intent.getAction(), ServerConfig.BORADCAST_ACTION_EXIT)) {
                String extraCommand = intent.getStringExtra("extra_key_command");
                if ("extra_command_exit".equals(extraCommand)) {
//                    unregisterReceiver(mBroadcastReceiver);
                    finish();
                    Process.killProcess(Process.myPid());
                    System.exit(0);
                }
            }
            /*else if (Objects.equals(intent.getAction(), ServerConfig.BORADCAST_ACTION_TAKEPHOTO)) {
                String extraCommand = intent.getStringExtra("extra_key_command");
                if ("extra_command_takephoto".equals(extraCommand)) {
//                    mRemoteCam.takePhoto();
                }
            } else if (Objects.equals(intent.getAction(), ServerConfig.BORADCAST_ACTION_LOCKVIDEO)) {
                String extraCommand = intent.getStringExtra("extra_key_command");
                if ("extra_command_lockvideo".equals(extraCommand)) {
//                    mRemoteCam.lockVideo();
                }
            }else if (Objects.equals(intent.getAction(), ServerConfig.BORADCAST_ACTION_OPENVOICE)) {
                String extraCommand = intent.getStringExtra("extra_key_command");
                if ("extra_command_openvoice".equals(extraCommand)) {
//                    mRemoteCam.startMic();
                }
            }else if (Objects.equals(intent.getAction(), ServerConfig.BORADCAST_ACTION_CLOSEVOICE)) {
                String extraCommand = intent.getStringExtra("extra_key_command");
                if ("extra_command_closevoice".equals(extraCommand)) {
//                    mRemoteCam.stopMic();
                }
            }*/
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}

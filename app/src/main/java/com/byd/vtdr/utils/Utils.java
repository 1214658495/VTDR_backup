package com.byd.vtdr.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.byd.vtdr.CommonUtility;
import com.byd.vtdr.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author byd_tw
 */
public class Utils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    /**
     * Is the live streaming still available
     * @return is the live streaming is available
     */
    public static boolean isLiveStreamingAvailable() {
        // Todo: Please ask your app server, is the live streaming still available
        return true;
    }

    public static boolean isSocketAvailable(Context context) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ServerConfig.VTDRIP, 7878), 2000);
            return true;
        } catch (IOException e) {
            Log.e(CommonUtility.LOG_TAG, e.getMessage());
//            String message = "Can't connect to " + mHostName + "/" + mPortNum;
//            String message = "无法连接到记录仪，请检查网络";
            String message = "CONNECT_FAIL";
//            Resources res = getResources();
//            String message = getString(R.string.connect_fail);
        }
        return false;
    }
}

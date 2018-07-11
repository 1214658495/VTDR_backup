package com.byd.vtdr;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

//import com.ambarella.streamview.AmbaStreamListener;
//import com.ambarella.streamview.AmbaStreamSource;
import com.byd.vtdr.connectivity.CmdChannel;
import com.byd.vtdr.connectivity.CmdChannelBLE;
import com.byd.vtdr.connectivity.CmdChannelWIFI;
import com.byd.vtdr.connectivity.DataChannel;
import com.byd.vtdr.connectivity.DataChannelWIFI;
import com.byd.vtdr.connectivity.IChannelListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.ambarella.remotecamera.connectivity.DataChannelWIFI;

//import com.ambarella.remotecamera.connectivity.DataChannel;

//import com.ambarella.remotecamera.connectivity.IChannelListener;

//import com.ambarella.remotecamera.connectivity.CmdChannelBLE;
//import com.ambarella.remotecamera.connectivity.CmdChannelWIFI;

//import com.ambarella.remotecamera.connectivity.CmdChannel;


/**
 * Created by jli on 9/8/14.
 */
public class RemoteCam
        implements IChannelListener {
    //        implements IChannelListener, AmbaStreamListener {
    private final static String TAG = "RemoteCam::";

    public int ble_connection_timeout;
    public boolean gatt_autoConnect_flag;

    public Boolean uiStatusSessionFlag;
    public static final int CAM_CONNECTIVITY_INVALID = 0;
    public static final int CAM_CONNECTIVITY_BLE_WIFI = 2;
    public static final int CAM_CONNECTIVITY_WIFI_WIFI = 3;
    public static final int CAM_CONNECTIVITY_BLE = 5;

    public String connectionMode;
    public String lastCommandResponse;

    private int mConnectivityType;
    private String mBlueAddrRequested;
    private String mWifiSSIDRequested;
    private String mBlueAddrConnected;
    private String mWifiSSIDConnected;
    private String mGetFileName;
    private String mPutFileName;
    private String mZoomInfoType;

    private Boolean mfDataChannelConnected;
    private String mWifiIpAddr;
    private Context mContext;
    private CmdChannel mCmdChannel;
    private DataChannel mDataChannel;
    private IChannelListener mListener;

    private String mWifiHostURL;
    private String mSDCardDirectory;

    private String mVideoFolder;
    private String mEventFolder;
    private String mPhotoFolder;

    private int mMediaInfoStep;
    private String mMediaInfoReply;

    private boolean querySessionHolder_flag;

    //    static private CmdChannelBLE mCmdChannelBLE;
//    static private CmdChannelWIFI mCmdChannelWIFI;
//    static private DataChannelWIFI mDataChannelWIFI;
    private CmdChannelBLE mCmdChannelBLE;
    private CmdChannelWIFI mCmdChannelWIFI;
    private DataChannelWIFI mDataChannelWIFI;

    private static final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    public RemoteCam(Context context) {
        mContext = context;
        mConnectivityType = CAM_CONNECTIVITY_INVALID;
//        AmbaStreamSource.setListener(this);
        // if (mCmdChannelWIFI == null) {
        mCmdChannelWIFI = new CmdChannelWIFI(this);
        mDataChannelWIFI = new DataChannelWIFI(this);
        setWifiIP(ServerConfig.VTDRIP, ServerConfig.cmdPort, ServerConfig.dataPort);

        if (mContext.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mCmdChannelBLE = new CmdChannelBLE(this);
        }
        // }
    }

    //我自己加的
    public DataChannel getDataChannel() {
        return mDataChannel;
    }

    public void reset() {
        mBlueAddrConnected = null;
        mWifiSSIDConnected = null;
        mSDCardDirectory = null;
        mfDataChannelConnected = false;
        if (mCmdChannel != null) {
            mCmdChannel.reset();
        }
    }

    public RemoteCam setWifiIP(String host, int cmdPort, int dataPort) {
        mWifiHostURL = host;
        mCmdChannelWIFI.setIP(host, cmdPort);
        mDataChannelWIFI.setIP(host, dataPort);
        return this;
    }

    public RemoteCam setConnectivity(int type) {
        if (mConnectivityType != type) {
            reset();
            mConnectivityType = type;
        }
        return this;
    }


    public RemoteCam setBtDeviceAddr(String addr) {
        mBlueAddrRequested = addr;
        return this;
    }

    public RemoteCam setWifiInfo(String ssid, String ipAddr) {
        mWifiSSIDRequested = ssid;
        mWifiIpAddr = ipAddr;
        return this;
    }

    public RemoteCam debugLastCmdResponse(String resp) {
        lastCommandResponse = resp;
        return this;
    }

    public RemoteCam setChannelListener(IChannelListener listener) {
        mListener = listener;
        return this;
    }

    public void wakeUp() {
        worker.execute(new Runnable() {
            public void run() {
                String cmd = "amba discovery";
                switch (mConnectivityType) {
                    case CAM_CONNECTIVITY_WIFI_WIFI:
                        WifiManager mgr = (WifiManager) mContext
                                .getSystemService(Context.WIFI_SERVICE);
                        CmdChannelWIFI.wakeup(mgr, cmd, 7877, 7877);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //madd
    public void socketTest() {
        CmdChannelWIFI.isSocketAvailable();
    }

    public void standBy() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.standBy();
            }
        });
    }

    public void startSession() {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                if (!connectToCmdChannel()) {
                    return;
                }
                //getram set the connection status for UI update
                uiStatusSessionFlag = mCmdChannel.startSession();
                if (mSDCardDirectory == null && (uiStatusSessionFlag == true)) {
                   /* new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    mCmdChannel.getDevInfo();
                                }
                            }, 500);*/
                    //mCmdChannel.getDevInfo();
                }
            }
        });
    }

    public void stopSession() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                //getram: set connection status for UI update
                mCmdChannel.stopSession();
//                if (mCmdChannel.stopSession())
//                    uiStatusSessionFlag = false;
            }
        });
    }

    public void getAllSettings() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getAllSettings();
            }
        });
    }

    public void getSettingOptions(final String setting) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getSettingOptions(setting);
            }
        });
    }

    public void setSetting(final String setting) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.setSetting(setting);
            }
        });
    }

    public void listDir(final String path) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.listDir(path);
            }
        });
    }


    public void changeFolder(final String path) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.changeDirectory(path);
            }
        });
    }

    public void deleteFile(final String path) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.deleteFile(path);
            }
        });
    }

    public void burnFW(final String path) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.burnFW(path);
            }
        });
    }

    public void setZoom(final String type, final int level) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.setZoom(type, level);
            }
        });
    }

    public void getZoomInfo(final String type) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mZoomInfoType = type;
                mCmdChannel.getZoomInfo(type);
            }
        });
    }

    public void setBitRate(final int bitRate) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.setBitRate(bitRate);
            }
        });
    }

    public void getThumb(final String path) {
        int pos = path.lastIndexOf('/');
        mGetFileName = path.substring(pos + 1, path.length()) + ".thumb";
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToDataChannel() || !connectToCmdChannel())
                    return;
                /*int len = path.length();
                String surfix = path.substring(len-3, len).toLowerCase();
                String type = surfix.equals("jpg") ? "thumb" : "IDR";*/
                /* mCmdChannel.getThumb(path, type);*/
                mCmdChannel.getThumb(path, "thumb");
            }
        });
    }

    /*public void burnInFw(final String path){
        worker.execute(new Runnable () {
            public void run() {
                if (!connectToDataChannel() || !connectToCmdChannel())
                    return;
                mCmdChannel.burnFW(path);
            }
        });
    }*/

    public void getFile(final String path) {
        int pos = path.lastIndexOf('/');
        mGetFileName = path.substring(pos + 1, path.length());
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel() || !connectToDataChannel())
                    return;
                mCmdChannel.getFile(path);
            }
        });
    }

    public void putFile(final String srcFile, final String dstFile) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel() || !connectToDataChannel())
                    return;

                mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_PUT_MD5, null);
                File file = new File(srcFile);
                String md5;
                try {
                    FileInputStream in = new FileInputStream(file);
                    byte[] buf = new byte[4096];
                    int bytes;
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    while ((bytes = in.read(buf)) > 0) {
                        md.update(buf, 0, bytes);
                    }
                    byte[] hash = md.digest();
                    StringBuilder sb = new StringBuilder();
                    for (byte b : hash)
                        sb.append(String.format("%02x", b & 0xff));
                    md5 = sb.toString();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                mPutFileName = srcFile;
                mCmdChannel.putFile(dstFile, md5, file.length());
            }
        });
    }

    public void getInfo(final String path) {
        int pos = path.lastIndexOf('/');
        mGetFileName = path.substring(pos + 1, path.length());
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getInfo(path);
            }
        });
    }

    public void setMediaAttribute(final String path, final int flag) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.setMediaAttribute(path, flag);
            }
        });
    }

    public void cancelGetFile(final String path) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.cancelGetFile(path);
                mDataChannel.cancelGetFile();
            }
        });
    }

    public void cancelPutFile(final String path) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                int xfer_size = mDataChannel.cancelPutFile();
                mCmdChannel.cancelPutFile(path, xfer_size);
            }
        });
    }

    public void startVF() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.resetViewfinder();
            }
        });
    }

    public void stopVF() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.stopViewfinder();
            }
        });
    }

    public void getRecordTime() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getRecordTime();
            }
        });
    }

    public void getBatteryLevel() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getBatteryLevel();
            }
        });
    }

    public void takePhoto() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.takePhoto();
            }
        });
    }

    public void stopPhoto() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.stopPhoto();
            }
        });
    }

    //    madd
    public void lockVideo() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.lockVideo();
            }
        });
    }

    //    madd
    public void frimworkVersion() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.frimworkVersion();
            }
        });
    }

    public void startRecord() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.startRecord();
            }
        });
    }

    public void stopRecord() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.stopRecord();
            }
        });
    }

    //add
    public void startMic() {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                if (!connectToCmdChannel()) {
                    return;
                }
                mCmdChannel.startMic();
            }
        });
    }

    //add
    public void stopMic() {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                if (!connectToCmdChannel()) {
                    return;
                }
                mCmdChannel.stopMic();
            }
        });
    }

    public void forceSplit() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.forceSplit();
            }
        });
    }

    public void formatSD(final String slot) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.formatSD(slot);
            }
        });
    }

    //madd;
    public void defaultSetting() {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                if (!connectToCmdChannel()) {
                    return;
                }
                mCmdChannel.defaultSetting();
            }
        });
    }

    //madd;
    public void appStatus() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.appStatus();
            }
        });
    }

    //madd;
    public void micStatus() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.micStatus();
            }
        });
    }

    public void getMediaInfo() {
        mMediaInfoStep = 0;
        mMediaInfoReply = "";
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                if (!mCmdChannel.getNumFiles("photo"))
                    return;
                mCmdChannel.getNumFiles("video");
                mCmdChannel.getNumFiles("total");
                mCmdChannel.getSpace("free");
                mCmdChannel.getSpace("total");
                mCmdChannel.getDevInfo();
            }
        });
    }

    public void getTotalFileCount() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getNumFiles("total");
            }
        });
    }

    public void getAllVideoFilesCount() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getNumFiles("video");
            }
        });
    }

    public void getAllPhotoFilesCount() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getNumFiles("photo");
            }
        });
    }

    public void getTotalDiskSpace() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getSpace("total");
            }
        });
    }

    public void getTotalFreeSpace() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getSpace("free");
            }
        });
    }

    //    madd
    public void getSystemState() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel()) {
                    return;
                }
                mCmdChannel.getSystemState();
            }
        });
    }


    public void sendCommand(final String command) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.sendRequest(command);
            }
        });
    }

    public String streamFile(String path) {
        return "rtsp://" + mWifiHostURL + path;
    }

    public String sdCardDirectory() {
        return mSDCardDirectory;
    }

    public void startLiveStream() {
//        AmbaStreamSource.startWifi("rtsp://" + mWifiHostURL + "/live");
    }

    public void stopLiveStream() {
//        AmbaStreamSource.stopWifi();
    }

    public String videoFolder() {
        return mVideoFolder;
    }

    public String eventFolder() {
        return mEventFolder;
    }

    public String photoFolder() {
        return mPhotoFolder;
    }

    //--getram
    public void setClientInfo() {
        mCmdChannel.setClntInfo("TCP", mWifiIpAddr);
    }

    public void getDeviceInfo() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getDevInfo();
            }
        });
    }

    public void getPWD() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getCurrentWorkingDir();
            }
        });
    }

    public void getWifiSettings() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.getAmbaWifiSettings();
            }
        });
    }

    public void setWifiSettings(final String param) {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.setWifiSettings(param);
            }
        });
    }

    public void stopWifi() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.stopWifi();
            }
        });
    }

    public void startWifi() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.startWifi();
            }
        });
    }

    public void restartWifi() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.restartWifi();
            }
        });
    }

    public void restartHttp() {
        worker.execute(new Runnable() {
            public void run() {
                if (!connectToCmdChannel())
                    return;
                mCmdChannel.restartHttp();
            }
        });
    }

    public void actionQuerySessionHolder() {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                //
                //if (!connectToCmdChannel())
                //    return;
                /* if (querySessionHolder_flag)我注销的*/
                mCmdChannel.replyQuerySessionHolder();
            }
        });
    }

    public void setQuerySessionFlag(boolean Flag) {
        querySessionHolder_flag = Flag;
    }

/*//    @Override
//    public void onStreamViewEvent(int event) {
//        int type;
//        switch (event) {
//            case AmbaStreamListener.BUFFERING:
//                type = IChannelListener.STREAM_CHANNEL_EVENT_BUFFERING;
//                break;
//            case AmbaStreamListener.PLAYING:
//                type = IChannelListener.STREAM_CHANNEL_EVENT_PLAYING;
//                break;
//            default:
//                type = IChannelListener.STREAM_CHANNEL_ERROR_PLAYING;
//                break;
//        }
//        mListener.onChannelEvent(type, null);
//    }*/

    @Override
    public void onChannelEvent(int type, Object param, String... array) {
        JSONObject parser;
        int size;
        String path;

        switch (type) {
            case IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB:
                parser = (JSONObject) param;

                try {
                    if (parser.getInt("rval") != 0) {
                        /*mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT,
                                "GET_THUMB failed");*/
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB_FAIL, true);

                        break;
                    } else {
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB_TEST, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

               /* try {
                    if (parser.getInt("rval") != 0) {
                        mListener.onChannelEvent(
                                IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT,
                                "GET_THUMB failed");
                        break;
                    }
                    size = parser.getInt("size");
                    path = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS) + "/" + mGetFileName;
                    mDataChannel.getFile(path, size);
                } catch (JSONException e) {e.printStackTrace();}*/
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_GET_FILE:
                size = Integer.parseInt((String) param);
                /*path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" + mGetFileName;*/
                String dirName = Environment.getExternalStorageDirectory() + "/" + "行车记录仪";
                File dir = new File(dirName);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                path = dirName + "/" + mGetFileName;
                mDataChannel.getFile(path, size);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_PUT_FILE:
                mDataChannel.putFile(mPutFileName);
                break;
//                mremove
//            case IChannelListener.CMD_CHANNEL_EVENT_GET_SPACE:
//                mMediaInfoStep++;
//                mMediaInfoReply += "\n" + (mMediaInfoStep == 4 ? "free space: " : "total space: ");
//                mMediaInfoReply += (String) param;
//                mMediaInfoReply += "KB";
//                break;
            case IChannelListener.CMD_CHANNEL_EVENT_GET_NUM_FILES:
                mMediaInfoStep++;
                if (mMediaInfoStep == 1)
                    mMediaInfoReply += "\nPhoto Files: ";
                else if (mMediaInfoStep == 2)
                    mMediaInfoReply += "\nVideo Files: ";
                else
                    mMediaInfoReply += "\nTotal Files: ";
                mMediaInfoReply += (String) param;
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_GET_DEVINFO:
                try {
                    parser = (JSONObject) param;
                    /*if (mSDCardDirectory == null) {
                        if (parser.has("media_folder")) {
                            String val = parser.getString("media_folder");
                            val = val.substring(0, val.length()-1);
                            mSDCardDirectory = val.substring(0, val.lastIndexOf('/')+1);
                        } else {
                            //mSDCardDirectory = "/tmp/fuse_d/";
                            mSDCardDirectory = "/tmp";
                        }
                        //Log.e(TAG, "SD directory: " + mSDCardDirectory);
                        break;
                    }*/

                    if (mVideoFolder == null) {
//                        if (parser.has("video_folder")) {
                        if (parser.has("media_folder")) {
                            String val = parser.getString("media_folder");
                            mVideoFolder = val;
                        }
                        // break;
                    }
                    // ������Ƶ·��
                    if (mEventFolder == null) {
                        if (parser.has("event_folder")) {
                            String val = parser.getString("event_folder");
                            mEventFolder = val;
                        }
                        // break;
                    }
                    // ���·��
                    if (mPhotoFolder == null) {
                        if (parser.has("photo_folder")) {
                            String val = parser.getString("photo_folder");
                            mPhotoFolder = val;
//                            mPhotoFolder = "/tmp/SD0/PHOTO";
                        }
                        // break;
                    }

                    Iterator<?> keys = parser.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (key.equals("rval") || key.equals("msg_id"))
                            continue;
                        mMediaInfoReply += "\n" + key + ": " + parser.getString(key);
                    }
                    mListener.onChannelEvent(type, mMediaInfoReply);
                    mMediaInfoReply = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_GET_ZOOM_INFO:
                mListener.onChannelEvent(type, mZoomInfoType, (String) param);
                break;
            default:
                if (mListener != null)
                    mListener.onChannelEvent(type, param);
        }
    }

    private boolean connectToCmdBLE() {
        // check if we are connected already
        if (mBlueAddrRequested.equals(mBlueAddrConnected))
            return true;

        // try to connect
        if (mCmdChannelBLE.connectTo(mBlueAddrRequested, ble_connection_timeout, gatt_autoConnect_flag)) {
            mBlueAddrConnected = mBlueAddrRequested;
            mCmdChannel = mCmdChannelBLE;
            return true;
        }

        mBlueAddrConnected = null;
        return false;
    }

    private boolean connectToCmdWIFI() {
        // check if we are connected already
        if (mWifiSSIDRequested.equals(mWifiSSIDConnected))
            return true;
        mWifiSSIDConnected = null;

        // check if we can connect to cmd channel
        if (mCmdChannelWIFI.connect()) {
            mCmdChannel = mCmdChannelWIFI;
            mWifiSSIDConnected = mWifiSSIDRequested;
            return true;
        }
        return false;
    }

    private boolean connectToDataWIFI() {

        if (mfDataChannelConnected) {
            Log.e(TAG, "already Connected to data Channel");
            return true;
        }


        // check if we can connect to data channel
        mCmdChannel.setClntInfo("TCP", mWifiIpAddr);
        if (mDataChannelWIFI.connect()) {
            Log.e(TAG, "Connecting  to data Channel");
            mDataChannel = mDataChannelWIFI;
            mfDataChannelConnected = true;
            return true;
        }

        return false;
    }

    private boolean connectToDataChannel() {
        switch (mConnectivityType) {
            case CAM_CONNECTIVITY_BLE_WIFI:
            case CAM_CONNECTIVITY_WIFI_WIFI:
                return connectToDataWIFI();

            default:
                if (mListener != null) {
//                    String msg = mContext.getString(R.string.invalid_connect_error);
                    String msg = "The selected connectivity type is not supported";
                    mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT, msg);
                }
        }
        return false;
    }

    private boolean connectToCmdChannel() {
        switch (mConnectivityType) {
            case CAM_CONNECTIVITY_BLE_WIFI:
                return connectToCmdBLE();

            case CAM_CONNECTIVITY_WIFI_WIFI:
                return connectToCmdWIFI();

            default:
                if (mListener != null) {
//                    String msg = mContext.getString(R.string.invalid_connect_error);
                    String msg = "The selected connectivity type is not supported";
                    mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT, msg);
                }
        }
        return false;
    }

    public void closeBLEConnection() {
        switch (mConnectivityType) {
            case CAM_CONNECTIVITY_BLE_WIFI:
                if (mCmdChannelBLE.disconnectBLE()) {
                    Log.e(TAG, "Disconnecting BLE connection");
                }
        }
    }
}

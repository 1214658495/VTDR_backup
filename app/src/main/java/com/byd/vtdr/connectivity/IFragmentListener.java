package com.byd.vtdr.connectivity;

/**
 * Created by jli on 8/29/14.
 */
public interface IFragmentListener {

    public void onFragmentAction(int type, Object param, Integer... array);

    /**
     * Action: Connectivity setup
     */
    final static int ACTION_CONNECTIVITY_SELECTED = 0x01;
    final static int ACTION_BT_LIST = 0x02;
    final static int ACTION_BT_CANCEL = 0x03;
    final static int ACTION_BT_SELECTED = 0x04;
    final static int ACTION_WIFI_LIST = 0x05;
    final static int ACTION_BLE_LIST = 0x06;
    final static int ACTION_BT_ENABLE = 0x07;
    //final static int ACTION_APP_SETTINGS = 0x08;

    final static int ACTION_BC_WAKEUP = 0x10;
    final static int ACTION_BC_STANDBY = 0x11;

    /**
     * Action: Various Button Clicks
     */
    final static int ACTION_BATTERY_INFO = 0x10;
    final static int ACTION_BC_GET_CURRENT_SETTING = 0x11;
    final static int ACTION_BC_START_SESSION = 0x12;
    final static int ACTION_BC_STOP_SESSION = 0x13;
    final static int ACTION_BC_SEND_COMMAND = 0x14;
    final static int ACTION_BC_GET_ALL_SETTINGS = 0x15;
    final static int ACTION_BC_GET_SETTING_OPTIONS = 0x16;
    final static int ACTION_BC_SET_SETTING = 0x17;
    final static int ACTION_BC_GET_ALL_SETTINGS_DONE = 0x18;
    final static int ACTION_BC_SET_BITRATE = 0x19;

    //    madd
    final static int ACTION_MIC_ON = 0x60;
    final static int ACTION_FS_DELETE_MULTI = 0x61;
    final static int ACTION_FS_DELETE_WAITING_TIP = 0x62;


    //getram
    final static int ACTION_BC_SET_CLIENT_INFO = 0x1A;
    final static int ACTION_DISC_SPACE = 0x1B;
    final static int ACTION_DISC_FREE_SPACE = 0x1C;
    final static int ACTION_APP_STATUS = 0x1D;
    final static int ACTION_DEVICE_INFO = 0x1E;
    final static int ACTION_CLOSE_BLE = 0x1F;


    /**
     * file-system related
     */
    final static int ACTION_FS_CD = 0x20;
    final static int ACTION_FS_LS = 0x21;
    final static int ACTION_FS_DELETE = 0x22;
    final static int ACTION_FS_DOWNLOAD = 0x23;
    final static int ACTION_FS_VIEW = 0x24;
    final static int ACTION_FS_INFO = 0x25;
    final static int ACTION_FS_FORMAT_SD = 0x26;
    final static int ACTION_FS_GET_FILE_INFO = 0x27;
    final static int ACTION_FS_BURN_FW = 0x28;
    final static int ACTION_FS_SET_RO = 0x29;
    final static int ACTION_FS_SET_WR = 0x2A;
    final static int ACTION_FS_GET_THUMB = 0x2B;
    final static int ACTION_FS_GET_ALL_FILE_COUNT = 0x2C;
    final static int ACTION_FS_GET_ALL_PHOTO_FILES = 0x2D;
    final static int ACTION_FS_GET_ALL_VIDEO_FILES = 0x2E;
    final static int ACTION_FS_GET_PWD = 0x2F;


    /**
     * Viewfinder related
     */
    final static int ACTION_VF_START = 0x30;
    final static int ACTION_VF_STOP = 0x31;
    final static int ACTION_PHOTO_START = 0x32;
    final static int ACTION_PHOTO_STOP = 0x38;
    final static int ACTION_RECORD_START = 0x33;
    final static int ACTION_RECORD_STOP = 0x34;
    final static int ACTION_RECORD_TIME = 0x35;
    final static int ACTION_PLAYER_START = 0x36;
    final static int ACTION_PLAYER_STOP = 0x37;
    final static int ACTION_FORCE_SPLIT = 0x39;
    final static int ACTION_SET_ZOOM = 0x3A;
    final static int ACTION_GET_ZOOM_INFO = 0x3B;
//madd
    final static int ACTION_DEFAULT_SETTING = 0x3C;
    final static int ACTION_LOCK_VIDEO_START = 0x3D;
    final static int ACTION_FRIMWORK_VERSION = 0x3E;
    final static int ACTION_APP_VERSION = 0x3F;
    /* MISC --getram -- UI related
     */
    final static int ACTION_OPEN_CAMERA_LIVEVIEW = 0x40;
    final static int ACTION_OPEN_CONTROLPANEL = 0x41;
    final static int ACTION_OPEN_CAMERA_FILE_CMDS = 0x42;
    final static int ACTION_OPEN_CAMERA_COMMANDS = 0x43;
    final static int ACTION_OPEN_CAMERA_SETTINGS = 0x44;
    final static int ACTION_OPEN_CAMERA_WIFI_SETTINGS = 0x45;
    final static int ACTION_OPEN_LOG_VIEW = 0x46;
    final static int ACTION_SHOW_LAST_CMD_RESP = 0x47;
    final static int ACTION_OPEN_CAMERA_LIVEVIEW_WITH_AUDIO = 0x48;
    final static int ACTION_SET_QUERY_SESSION_HOLDER = 0x49;
    final static int ACTION_UNSET_QUERY_SESSION_HOLDER = 0x4A;
    final static int ACTION_CLOSE_EXTERNAL_LOG_FILE = 0x4B;


    //Wifi
    final static int ACTION_SET_CAMERA_WIFI_IP = 0x50;
    final static int ACTION_GET_WIFI_SETTINGS = 0x51;
    final static int ACTION_SET_WIFI_SETTINGS = 0x52;
    final static int ACTION_WIFI_STOP = 0x53;
    final static int ACTION_WIFI_START = 0x54;
    final static int ACTION_WIFI_RESTART = 0x55;

}

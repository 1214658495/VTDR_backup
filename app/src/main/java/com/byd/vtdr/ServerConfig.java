package com.byd.vtdr;

public class ServerConfig {
    public static final String VTDRIP = "192.168.195.6";
    public static final String PADIP = "192.168.195.2";

    //	public static final int cmdPort = 7878;
//	public static final int dataPort = 8787;
    public static final int cmdPort = 50000;
    public static final int dataPort = 50008;

    public static final int RB_RECORD_VIDEO = 0;
    public static final int RB_LOCK_VIDEO = 1;
    public static final int RB_CAPTURE_PHOTO = 2;

    public static final int BYD_CARD_STATE_OK = 0;
    public static final int BYD_CARD_STATE_NOCARD = -1;
    public static final int BYD_CARD_STATE_SMALL_NAND = -2;
    public static final int BYD_CARD_STATE_NOT_MEM = -3;
    public static final int BYD_CARD_STATE_UNINIT = -4;
    public static final int BYD_CARD_STATE_NEED_FORMAT = -5;
    public static final int BYD_CARD_STATE_SETROOT_FAIL = -6;
    public static final int BYD_CARD_STATE_NOT_ENOUGH = -7;
    public static final int BYD_CARD_STATE_WP = -8;

    public static final int REC_CAP_STATE_PREVIEW = 0;
    public static final int REC_CAP_STATE_RECORD = 1;
    public static final int REC_CAP_STATE_PRE_RECORD = 2;
    public static final int REC_CAP_STATE_FOCUS = 3;
    public static final int REC_CAP_STATE_CAPTURE = 4;
    public static final int REC_CAP_STATE_VF = 5;
    public static final int REC_CAP_STATE_TRANSIT_TO_VF = 6;
    public static final int REC_CAP_STATE_RESET = 255;

    public static final String BORADCAST_ACTION_EXIT = "com.byd.vtdr.exit";//关闭活动的广播action名称
    public static final String BORADCAST_ACTION_TAKEPHOTO = "com.byd.vtdr.takephoto";//拍照的广播action名称
    public static final String BORADCAST_ACTION_LOCKVIDEO = "com.byd.vtdr.lockvideo";//锁定视频的广播action名称
    public static final String BORADCAST_ACTION_OPENVOICE = "com.byd.vtdr.openvoice";//打开录音的广播action名称
    public static final String BORADCAST_ACTION_CLOSEVOICE = "com.byd.vtdr.closevoice";//关闭录音的广播action名称

    public static final int BYDMODE_NORMAL = 1;
    public static final int BYDMODE_SPORT = 2;

}

package com.byd.vtdr.connectivity;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.byd.vtdr.CommonUtility;
import com.byd.vtdr.ServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

//import com.ambarella.remotecamera.CommonUtility;

/**
 * Created by jli on 9/10/14.
 */
public class CmdChannelWIFI extends CmdChannel {
    private static final String TAG="CmdChannelWIFI";
    private static final int CONN_TIME_OUT = 3000;
    private static final int READ_TIME_OUT = 3000;
    private static final int WAKEUP_MAX_TRY = 1;

    private Socket mSocket;
    private String mHostName;
    private int mPortNum;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private byte[] mBuffer = new byte[2048];

    public CmdChannelWIFI(IChannelListener listener) {
        super(listener);
    }

    public CmdChannelWIFI setIP(String host, int port) {
        mHostName = host;
        mPortNum = port;
        return this;
    }

    public boolean connect() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }

        Log.e(TAG, "Connecting...");
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(mHostName, mPortNum), CONN_TIME_OUT);
            mSocket = socket;
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            startIO();
            return true;
        } catch (IOException e) {
            Log.e(CommonUtility.LOG_TAG, e.getMessage());
//            String message = "Can't connect to " + mHostName + "/" + mPortNum;
//            String message = "无法连接到记录仪，请检查网络";
            String message = "CONNECT_FAIL";
//            Resources res = getResources();
//            String message = getString(R.string.connect_fail);
            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT, message);
        }
        return false;
    }

    static public boolean wakeup(WifiManager mgr, String cmd,
                                 int srcPort, int dstPort) {

        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_WAKEUP_START, null);
        InetAddress bcAddr;
        try {
//            如下俩行被我反向注释了
//            bcAddr = getBroadcastAddress(mgr);
//            bcAddr = InetAddress.getByName("192.168.42.1");
            bcAddr = InetAddress.getByName("192.168.195.6");
        } catch (IOException e) {
            Log.e(TAG, "Can't get broadcast address!!!");
            e.printStackTrace();
            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_WAKEUP, null);
            return false;
        }

        for (int i = 0;  i < WAKEUP_MAX_TRY; i++) {
            try {
                Log.e(TAG, "bcAddr is " + bcAddr.toString());
                DatagramSocket socket = new DatagramSocket(srcPort);
                Log.e(TAG, "created socket " + socket.toString());
                DatagramPacket packet = new DatagramPacket(cmd.getBytes(),
                        cmd.length(), bcAddr, dstPort);
                Log.e(TAG, "created packet " + packet.toString());
                socket.send(packet);
                Log.e(TAG, "Sent the wakeup message ");

                byte[] buf = new byte[1024];
                socket.setSoTimeout(READ_TIME_OUT);
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] reply = Arrays.copyOf(buf, packet.getLength());
                Log.e(TAG, "Received message " + (new String(reply, "UTF-8")));
                socket.close();
                mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_WAKEUP_OK, null);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_WAKEUP, null);
        return false;
    }

    public static boolean isSocketAvailable() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ServerConfig.VTDRIP, ServerConfig.cmdPort), 2000);
            socket.close();
            socket = null;
            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_WAKEUP_OK, null);
            return true;
        } catch (IOException e) {
            Log.e(CommonUtility.LOG_TAG, e.getMessage());
            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_WAKEUP, null);
            Log.e(TAG, "isSocketAvailable: Can't connect to socket");
            String message = "CONNECT_FAIL";
        }
        return false;
    }

    static private InetAddress getBroadcastAddress(WifiManager mgr) throws IOException {
        DhcpInfo dhcp = mgr.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    @Override
    protected void writeToChannel(byte[] buffer) {
        try {
            if (mOutputStream != null) {
                mOutputStream.write(buffer);
            }
        } catch (IOException e) {
            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_TIMEOUT, null);
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected String readFromChannel() {
        try {
            if (mInputStream != null) {
                int size = mInputStream.read(mBuffer);
                return new String(mBuffer, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_BROKEN_CHANNEL, 0);
        }
        return null;
    }
}
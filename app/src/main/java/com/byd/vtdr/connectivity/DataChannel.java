package com.byd.vtdr.connectivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jli on 9/19/14.
 */
public class DataChannel {
    private final static String TAG = "DataChannel";
    private final static int PROGRESS_MIN_STEP = 1;

    protected IChannelListener mListener;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    protected boolean mContinueRx;

    protected boolean mContinueTx;
    protected int mTxBytes;
    protected final Object mTxLock = new Object();

    private static final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    public DataChannel(IChannelListener listener) {
        mListener = listener;
    }

    public DataChannel setStream(InputStream input, OutputStream output) {
        mInputStream = input;
        mOutputStream = output;
        return this;
    }

    public void getFile(final String dstPath, final int size) {
        mContinueRx = true;
        worker.execute(new Runnable() {
            public void run() {
                rxStream(dstPath, size);
            }
        });
    }

    public void cancelGetFile() {
        mContinueRx = false;
    }

    public void putFile(final String srcPath) {
        mContinueTx = true;
        worker.execute(new Runnable() {
            public void run() {
                txStream(srcPath);
            }
        });
    }

    public int cancelPutFile() {
        mContinueTx = false;
        synchronized (mTxLock) {
            try {
                mTxLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mTxBytes;
    }

    private void txStream(String srcPath) {
        int total = 0;
        int prev = 0;

        try {
            byte[] buffer = new byte[1024];
            File file = new File(srcPath);
            FileInputStream in = new FileInputStream(file);
            final int size = (int) file.length();

            mTxBytes = 0;
            mListener.onChannelEvent(
                    IChannelListener.DATA_CHANNEL_EVENT_PUT_START, srcPath);
            while (mContinueTx) {
                int read = in.read(buffer);
                if (read <= 0)
                    break;
                mOutputStream.write(buffer, 0, read);
                mTxBytes += read;

                total += read;
                int curr = (int) (((long) total * 100) / size);
                if (curr - prev >= PROGRESS_MIN_STEP) {
                    mListener.onChannelEvent(
                            IChannelListener.DATA_CHANNEL_EVENT_PUT_PROGRESS, curr);
                    prev = curr;
                }
            }
            in.close();

            if (mContinueTx) {
                mListener.onChannelEvent(
                        IChannelListener.DATA_CHANNEL_EVENT_PUT_FINISH, srcPath);
            } else {
                synchronized (mTxLock) {
                    mTxLock.notify();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: 2018/1/26 研究流程
    private void rxStream(String dstPath, int size) {
        int total = 0;
        int prev = 0;
        try {
            byte[] buffer = new byte[1024];
            FileOutputStream out = new FileOutputStream(dstPath);
            int bytes;

            mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_GET_START, dstPath);
            while (total < size) {
                try {
                    bytes = mInputStream.read(buffer);
                    //Log.e(TAG, "read bytes " + bytes);
                    out.write(buffer, 0, bytes);
                } catch (SocketTimeoutException e) {
                    if (!mContinueRx) {
                        Log.e(TAG, "RX canceled");
                        out.close();
                        return;
                    }
                    continue;
                }

                total += bytes;
                int curr = (int) (((long) total * 100) / size);
                if (curr - prev >= PROGRESS_MIN_STEP) {
                    mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_GET_PROGRESS,
                            curr);
                    prev = curr;
                }
            }
            out.close();
            mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_GET_FINISH, dstPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap rxYuvStreamUpdate() {
        int total = 0;
        int width = 160;
        int height = 90;
        int size = 34560;
        Bitmap bitmap = null;
        byte[] yuvArray = new byte[size];
        byte[] yuvArray1 = new byte[160 * 90 * 2];
        byte[] yuvArray2 = new byte[160 * 90 * 2];
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int bytes;
           /* mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_GET_START, dstPath);*/
            while (total < size) {
                try {
                    bytes = mInputStream.read(yuvArray);
                    //Log.e(TAG, "read bytes " + bytes);
                    /*out.write(buffer, 0, bytes);*/
                } catch (SocketTimeoutException e) {
                    /*if (!mContinueRx) {
                        Log.e(TAG, "RX canceled");
                        out.close();
                        return;
                    }*/
                    continue;
                }

                total += bytes;
                if (total == size) {
                    for (int i = 0; i < 90 * 2; i++) {
                        System.arraycopy(yuvArray, 192 * i, yuvArray1, 160 * i, 160);
                    }
                    for (int j = 0; j < 160 * 90; j++) {
                        yuvArray2[2 * j] = yuvArray1[j];
                        yuvArray2[2 * j + 1] = yuvArray1[160 * 90 + j];
                    }
                    YuvImage yuvImage = new YuvImage(yuvArray2, ImageFormat.YUY2, width, height, null);
                    yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
                    bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                    stream.close();
                    return bitmap;
                }
            }
//            stream.close();
            /*mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_GET_FINISH, dstPath);*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Bitmap rxYuvStream2() {
        int total = 0;
        int width = 160;
        int height = 90;
        int size = 34560;
        Bitmap bitmap = null;
        MessageDigest digest = null;
        try {
            byte[] yuvArray = new byte[size];
            byte[] yuvArray1 = new byte[160 * 90 * 2];
            byte[] yuvArray2 = new byte[160 * 90 * 2];
//			FileOutputStream out = new FileOutputStream(dstPath);
            int bytes;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


//			mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_GET_START, dstPath);
            while (total < size) {
                try {
                    bytes = mInputStream.read(yuvArray, total, size - total);
                    Log.e(TAG, "rxYuvStream2: bytes = " + bytes);
                    if (bytes > 0) {
                        total += bytes;
                    } else {
                        Log.e(TAG, "rxYuvStream2: bytes <= 0");
                        break;
                    }
                    Log.e(TAG, "rxYuvStream2: mInputStream.read");
                    if (total == size) {
                        digest.update(yuvArray, 0, bytes);
                        BigInteger bigInt = new BigInteger(1, digest.digest());
                        String bitIntString = "00" + bigInt.toString(16);
                        String bitIntString1 = bitIntString.substring(bitIntString.length() - 32);
                        Log.e(TAG, "rxYUY2Stream: bigInt.toString(16) = " + bigInt.toString(16));
                        Log.e(TAG, "rxYUY2Stream: bitIntString1       = " + bitIntString1);
//                        Log.e(TAG, "rxYUY2Stream: 原md5 =               " + md5);
                        for (int i = 0; i < 90 * 2; i++) {
                            System.arraycopy(yuvArray, 192 * i, yuvArray1, 160 * i, 160);
                        }

                        for (int j = 0; j < 160 * 90; j++) {
                            yuvArray2[2 * j] = yuvArray1[j];
                            yuvArray2[2 * j + 1] = yuvArray1[160 * 90 + j];
                        }

                        YuvImage yuvImage = new YuvImage(yuvArray2, ImageFormat.YUY2, width, height, null);

                        if (yuvArray2 != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
                            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            Log.e(TAG, "rxYuvStream2: BitmapFactory.decodeByteArray stream.size()" + stream.size() + bitmap.getByteCount());
                            try {
                                stream.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                }
            }
//			out.close();
//            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB_TEST, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    public ByteArrayOutputStream rxYuvStreamCashe() {
        int total = 0;
        int width = 160;
        int height = 90;
        int size = 34560;
        Bitmap bitmap = null;
        MessageDigest digest = null;
        try {
            byte[] yuvArray = new byte[size];
            byte[] yuvArray1 = new byte[160 * 90 * 2];
            byte[] yuvArray2 = new byte[160 * 90 * 2];
//			FileOutputStream out = new FileOutputStream(dstPath);
            int bytes;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


//			mListener.onChannelEvent(IChannelListener.DATA_CHANNEL_EVENT_GET_START, dstPath);
            while (total < size) {
                try {
                    bytes = mInputStream.read(yuvArray, total, size - total);
                    Log.e(TAG, "rxYuvStream2: bytes = " + bytes);
                    if (bytes > 0) {
                        total += bytes;
                    } else {
                        Log.e(TAG, "rxYuvStream2: bytes <= 0");
                        break;
                    }
                    Log.e(TAG, "rxYuvStream2: mInputStream.read");
                    if (total == size) {
                        digest.update(yuvArray, 0, bytes);
                        BigInteger bigInt = new BigInteger(1, digest.digest());
                        String bitIntString = "00" + bigInt.toString(16);
                        String bitIntString1 = bitIntString.substring(bitIntString.length() - 32);
                        Log.e(TAG, "rxYUY2Stream: bigInt.toString(16) = " + bigInt.toString(16));
                        Log.e(TAG, "rxYUY2Stream: bitIntString1       = " + bitIntString1);
//                        Log.e(TAG, "rxYUY2Stream: 原md5 =               " + md5);
                        for (int i = 0; i < 90 * 2; i++) {
                            System.arraycopy(yuvArray, 192 * i, yuvArray1, 160 * i, 160);
                        }

                        for (int j = 0; j < 160 * 90; j++) {
                            yuvArray2[2 * j] = yuvArray1[j];
                            yuvArray2[2 * j + 1] = yuvArray1[160 * 90 + j];
                        }

                        YuvImage yuvImage = new YuvImage(yuvArray2, ImageFormat.YUY2, width, height, null);

                        if (yuvArray2 != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
//                            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            return stream;
//                            try {
//                                stream.close();
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                }
            }
//			out.close();
//            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB_TEST, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

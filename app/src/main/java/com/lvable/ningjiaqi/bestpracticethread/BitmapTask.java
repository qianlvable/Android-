package com.lvable.ningjiaqi.bestpracticethread;

import android.graphics.Bitmap;

import java.lang.ref.WeakReference;
import java.net.URL;


/**
 * Created by ningjiaqi on 16/4/19.
 */
public class BitmapTask {
    private WeakReference<NetworkImageView> mImageWeakRef;

    private URL mImageUrl;
    private int mTargetHeight;
    private int mTargetWidth;
    private byte[] mByteBuffer;
    private Bitmap mDecodeImage;

    private Runnable mDownloadRunnable;
    private Runnable mDecodeRunnable;
    private Thread mCurrentThread;

    private static BitmapManager sPhotoManager;

    BitmapTask() {
        mDownloadRunnable = new BitmapDownloadRunnable(this);
        mDecodeRunnable = new BitmapDecodeRunnable(this);
        sPhotoManager = BitmapManager.getInstance();
    }

    public NetworkImageView getImageView() {
        if (mImageWeakRef != null){
            return mImageWeakRef.get();
        }
        return null;
    }

    public URL getImageUrl() {
        return mImageUrl;
    }

    public void initDownloadTask(BitmapManager instance, NetworkImageView imgView) {
        sPhotoManager = instance;
        mImageUrl = imgView.getUrl();
        mImageWeakRef = new WeakReference<NetworkImageView>(imgView);
        mTargetHeight = imgView.getHeight();
        mTargetWidth = imgView.getWidth();
    }

    public int getTargetWidth() {
        return mTargetWidth;
    }

    public int getTargetHeight() {
        return mTargetHeight;
    }

    public void setByteBuffer(byte[] byteBuffer) {
        mByteBuffer = byteBuffer;
    }

    public byte[] getByteBuffer() {
        return mByteBuffer;
    }

    public Runnable getDownloadRunnable() {
        return mDownloadRunnable;
    }
    // 在放入池子之前，先调用改方法避免内存系列
    public void recycle() {
        if (null != mImageWeakRef) {
            mImageWeakRef.clear();
            mImageWeakRef = null;
        }

        mByteBuffer = null;
        mDecodeImage = null;
    }

    public Runnable getDecodeRunnable() {
        return mDecodeRunnable;
    }

    public Thread getCurrentThread() {
        return mCurrentThread;
    }

    public void setCurrentThread(Thread thread) {
        synchronized (sPhotoManager) {
            mCurrentThread = thread;
        }
    }

    public void handleDownloadState(int state) {
        int outState;

        // Converts the download state to the overall state
        switch(state) {
            case BitmapDownloadRunnable.HTTP_STATE_COMPLETED:
                outState = BitmapManager.DOWNLOAD_COMPLETE;
                break;
            case BitmapDownloadRunnable.HTTP_STATE_FAILED:
                outState = BitmapManager.DOWNLOAD_FAILED;
                break;
            case BitmapDecodeRunnable.DECODE_STATE_COMPLETED:
                outState = BitmapManager.TASK_COMPLETE;
                break;
            default:
                outState = BitmapManager.DOWNLOAD_STARTED;
                break;
        }
        // Passes the state to the ThreadPool object.
        sPhotoManager.handleState(this,outState);
    }

    public void setDecodeImage(Bitmap mDecodeImage) {
        this.mDecodeImage = mDecodeImage;
    }

    public Bitmap getBitmap(){
        return mDecodeImage;
    }
}

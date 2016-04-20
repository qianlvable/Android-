package com.lvable.ningjiaqi.bestpracticethread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by ningjiaqi on 16/4/20.
 */
public class BitmapDecodeRunnable implements Runnable {
    private static final int NUMBER_OF_DECODE_TRIES = 2;

    static final int DECODE_STATE_FAILED = -1;
    static final int DECODE_STATE_STARTED = 0;
    static final int DECODE_STATE_COMPLETED = 5;

    final BitmapTask mPhotoTask;

    public BitmapDecodeRunnable(BitmapTask photoTask) {
        mPhotoTask = photoTask;
    }

    @Override
    public void run() {
        mPhotoTask.setCurrentThread(Thread.currentThread());

        byte[] imgBuffer = mPhotoTask.getByteBuffer();
        Bitmap bitmap = null;
        mPhotoTask.handleDownloadState(DECODE_STATE_STARTED);
        try {


            BitmapFactory.Options options = new BitmapFactory.Options();
            int targetWidth = mPhotoTask.getTargetWidth();
            int targetHeight = mPhotoTask.getTargetHeight();
            options.inJustDecodeBounds = true;

            if (Thread.interrupted()) {
                return;
            }
            // 为了得到bitmap 大小
            BitmapFactory.decodeByteArray(imgBuffer, 0, imgBuffer.length, options);
            int hScale = options.outHeight / targetHeight;
            int wScale = options.outWidth / targetWidth;
            int sampleSize = Math.max(hScale, wScale);

            if (sampleSize > 1) {
                options.inSampleSize = sampleSize;
            }

            if (Thread.interrupted()) {
                return;
            }

            options.inJustDecodeBounds = false;
            for (int i = 0; i < NUMBER_OF_DECODE_TRIES; i++) {
                try {
                    bitmap = BitmapFactory.decodeByteArray(imgBuffer, 0, imgBuffer.length, options);
                } catch (Throwable e) {
                    // out of memeory error
                    java.lang.System.gc();

                    if (Thread.interrupted()) {
                        return;

                    }
                }

            }
        } finally {
            if (null == bitmap) {
                mPhotoTask.handleDownloadState(DECODE_STATE_FAILED);
            } else {
                mPhotoTask.setDecodeImage(bitmap);
                mPhotoTask.handleDownloadState(DECODE_STATE_COMPLETED);
            }
            mPhotoTask.setCurrentThread(null);
            Thread.interrupted();
        }

    }
}

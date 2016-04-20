package com.lvable.ningjiaqi.bestpracticethread;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;

/**
 * Created by ningjiaqi on 16/4/19.
 */
public class BitmapDownloadRunnable implements Runnable {
    private static final int READ_SIZE = 1024 * 2;
    static final int HTTP_STATE_FAILED = -1;
    static final int HTTP_STATE_STARTED = 0;
    static final int HTTP_STATE_COMPLETED = 1;

    private BitmapTask mBitmapTask;

    public BitmapDownloadRunnable(BitmapTask photoTask) {
        mBitmapTask = photoTask;
    }

    @Override
    public void run() {
        mBitmapTask.setCurrentThread(Thread.currentThread());
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        byte[] byteBuffer = mBitmapTask.getByteBuffer();
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            // no cache
            if (null == byteBuffer) {
                mBitmapTask.handleDownloadState(HTTP_STATE_STARTED);
                InputStream byteStream = null;
                try {
                    HttpURLConnection conn = (HttpURLConnection) mBitmapTask
                            .getImageUrl().openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    byteStream = conn.getInputStream();
                    int contentSize = conn.getContentLength();
                    if (-1 == contentSize) {
                        byte[] tmp = new byte[READ_SIZE];
                        int readResult = 0;
                        int bufferLeft = tmp.length;
                        int bufferOffset = 0;
                        /*
                         * outer:读取完所有数据，inner的读取数据知道buffer存满了
                         */
                        outer:do {
                            while (bufferLeft > 0) {
                                readResult = byteStream.read(tmp, bufferOffset,
                                        bufferLeft);
                                if (readResult < 0) {
                                    break outer;
                                }
                                bufferOffset += readResult;
                                bufferLeft -= readResult;
                                if (Thread.interrupted()) {
                                    throw new InterruptedIOException();
                                }

                            }
                            /*
                             * buffer用完了，新建新的buffer
                             */
                            bufferLeft = READ_SIZE;
                            int newSize = tmp.length + READ_SIZE;
                            byte[] expandedBuffer = new byte[newSize];
                            System.arraycopy(tmp, 0, expandedBuffer, 0,
                                    tmp.length);
                            tmp = expandedBuffer;
                        }while (true);
                    } else {
                        byteBuffer = new byte[contentSize];
                        int remainingLength = contentSize;

                        int bufferOffset = 0;
                        while (remainingLength > 0) {
                            int readResult = byteStream.read(
                                    byteBuffer,
                                    bufferOffset,
                                    remainingLength);
                            if (readResult < 0) {
                                throw new EOFException();
                            }

                            bufferOffset += readResult;
                            remainingLength -= readResult;

                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != byteStream) {
                        try {
                            byteStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            mBitmapTask.setByteBuffer(byteBuffer);
            mBitmapTask.handleDownloadState(HTTP_STATE_COMPLETED);
        } catch (InterruptedException e) {
           // do nothing
        } finally {
            if (null == byteBuffer) {
                mBitmapTask.handleDownloadState(HTTP_STATE_FAILED);
            }
            mBitmapTask.setCurrentThread(null);
            Thread.interrupted();
        }
    }
}

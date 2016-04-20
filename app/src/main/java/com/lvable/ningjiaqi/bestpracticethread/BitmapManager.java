package com.lvable.ningjiaqi.bestpracticethread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LruCache;

import java.net.URL;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ningjiaqi on 16/4/19.
 * 用线程池实现下载照片的缓存还有decode bitmap的任务
 */
public class BitmapManager {

    static final int DOWNLOAD_FAILED = -1;
    static final int DOWNLOAD_STARTED = 1;
    static final int DOWNLOAD_COMPLETE = 2;
    static final int DECODE_STARTED = 3;
    static final int TASK_COMPLETE = 4;

    private static final int IMAGE_CACHE_SIZE = 120 * 120 * 10;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final LruCache<URL,byte[]> mPhotoCache;
    private final BlockingDeque<Runnable> mDownloadWorkQueue;
    private final BlockingDeque<Runnable> mDecodeWorkQueue;
    private final BlockingDeque<BitmapTask> mPhotoTaskWorkQueue;

    private final ThreadPoolExecutor mDownloadThreadPool;
    private final ThreadPoolExecutor mDecodeThreadPool;

    // 用于和UI thread的回调
    private Handler mHandler;

    private static BitmapManager sInstance = null;
    private BitmapManager() {
        mDownloadWorkQueue = new LinkedBlockingDeque<>();
        mDecodeWorkQueue = new LinkedBlockingDeque<>();
        mPhotoTaskWorkQueue = new LinkedBlockingDeque<>();

        mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE
                ,MAXIMUM_POOL_SIZE,KEEP_ALIVE_TIME,KEEP_ALIVE_TIME_UNIT,mDownloadWorkQueue);
        mDecodeThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE
                ,MAXIMUM_POOL_SIZE,KEEP_ALIVE_TIME,KEEP_ALIVE_TIME_UNIT,mDecodeWorkQueue);

        mPhotoCache = new LruCache<URL, byte[]>(IMAGE_CACHE_SIZE){
            @Override
            protected int sizeOf(URL key, byte[] value) {
                return value.length;
            }
        };

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                BitmapTask photoTask = (BitmapTask) msg.obj;
                NetworkImageView localView = photoTask.getImageView();
                if (localView != null) {
                    URL localUrl = localView.getUrl();
                    if (photoTask.getImageUrl() == localUrl) {
                        switch (msg.what) {
                            case DOWNLOAD_STARTED:
                                localView.setImageResource(R.drawable.imagedownloading);
                                break;
                            case TASK_COMPLETE:
                                localView.setImageBitmap(photoTask.getBitmap());
                                break;
                            case DOWNLOAD_FAILED:
                                localView.setImageResource(R.drawable.imagedownloadfailed);
                                recycleTask(photoTask);
                                break;
                            default:
                                super.handleMessage(msg);
                        }
                    }
                }
            }
        };
    }
    // Double check 方式实现的单例模式
    public static BitmapManager getInstance() {
        if (sInstance == null) {
            synchronized (BitmapManager.class) {
                if (sInstance == null) {
                    sInstance = new BitmapManager();
                }
            }
        }
        return sInstance;
    }

     public static BitmapTask startDownLoad(NetworkImageView imgView) {
         BitmapTask downloadTask = getInstance().mPhotoTaskWorkQueue.poll();
         if (null == downloadTask) {
             downloadTask = new BitmapTask();
         }

         downloadTask.initDownloadTask(getInstance(),imgView);
         downloadTask.setByteBuffer(getInstance().mPhotoCache.get(downloadTask.getImageUrl()));
         if (null == downloadTask.getByteBuffer()) {
             getInstance().mDownloadThreadPool.execute(downloadTask.getDownloadRunnable());
         } else {
             getInstance().handleState(downloadTask, DOWNLOAD_COMPLETE);
         }
         return downloadTask;
     }

    public static void cancelAll() {
        BitmapTask[] taskArray = new BitmapTask[getInstance().mDownloadWorkQueue.size()];
        getInstance().mDownloadWorkQueue.toArray(taskArray);
        synchronized (getInstance()) {
            for (int i = 0; i < taskArray.length;i++){
                Thread thread = taskArray[i].getCurrentThread();
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    public static void removeDownload(BitmapTask task, URL url) {
        if (task != null && task.getImageUrl().equals(url)) {
            synchronized (getInstance()) {
                Thread thread = task.getCurrentThread();
                if (thread != null)
                    thread.interrupt();
            }
            getInstance().mDownloadThreadPool.remove(task.getDownloadRunnable());
        }
    }

    void recycleTask(BitmapTask downloadTask) {

        // Frees up memory in the task
        downloadTask.recycle();

        // Puts the task object back into the queue for re-use.
        mPhotoTaskWorkQueue.offer(downloadTask);
    }

    public void handleState(BitmapTask task, int state) {
        switch (state) {
            case TASK_COMPLETE:
                mPhotoCache.put(task.getImageUrl(), task.getByteBuffer());
                Message msg = mHandler.obtainMessage(state,task);
                msg.sendToTarget();
                break;
            case DOWNLOAD_COMPLETE:
                mDecodeThreadPool.execute(task.getDecodeRunnable());
                break;
            default:
                mHandler.obtainMessage(state,task).sendToTarget();
        }
    }

}

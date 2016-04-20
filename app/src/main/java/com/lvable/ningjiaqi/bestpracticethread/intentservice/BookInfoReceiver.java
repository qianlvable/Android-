package com.lvable.ningjiaqi.bestpracticethread.intentservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ningjiaqi on 16/4/20.
 */
public class BookInfoReceiver extends BroadcastReceiver {
    private OnDataReceiveListener mListener;

    public void setListener(OnDataReceiveListener mListener) {
        this.mListener = mListener;
    }

    public interface OnDataReceiveListener {
        void onDataGet(BookInfo data);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListener != null) {
            int code = intent.getIntExtra(Constants.KEY_MSG, Constants.FAILD);
            if (code == Constants.SUCCESS) {
                mListener.onDataGet((BookInfo) intent.getParcelableExtra(Constants.KEY_BOOK));
            } else {
                mListener.onDataGet(null);
            }
        }
    }
}
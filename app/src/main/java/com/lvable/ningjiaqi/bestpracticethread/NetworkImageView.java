package com.lvable.ningjiaqi.bestpracticethread;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * Created by ningjiaqi on 16/4/19.
 */
public class NetworkImageView extends ImageView{

    private URL mUrl;
    private boolean mIsDrawn;
    private WeakReference<View> mThisView;
    private BitmapTask mPhotoTask;

    public NetworkImageView(Context context) {
        super(context);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        if (mThisView != null) {
            View localView = mThisView.get();
            if (localView != null){
                localView.setVisibility(visibility);
            }
        }
    }
    @Override
    protected void onDetachedFromWindow() {

        setImageUrl(null);

        Drawable localDrawable = getDrawable();
        if (localDrawable != null) {
            localDrawable.setCallback(null);
        }

        if (mThisView != null) {
            mThisView.clear();
            mThisView = null;
        }

        this.mPhotoTask = null;

        super.onDetachedFromWindow();
    }

    public void setImageUrl(URL pictureURL) {
        if (mUrl != null) {
            // 如果新来的连接与之前不同，取消之前任务，相同则退出
            if (!mUrl.equals(pictureURL)) {
                BitmapManager.removeDownload(mPhotoTask, mUrl);
            } else {
                return;
            }
        }

        mUrl = pictureURL;
        if (mUrl != null) {
            // 如果绘制过了，就开始下载（不然targetWidth会为0）
            if ((mIsDrawn)) {
                mPhotoTask = BitmapManager.startDownLoad(this);
            } else {
                invalidate();
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if ((!mIsDrawn) && (mUrl != null)) {

            mPhotoTask = BitmapManager.startDownLoad(this);

            mIsDrawn = true;
        }
        super.onDraw(canvas);
    }

    @Override
    public void setImageResource(int resId) {
        if (mThisView != null){
            setImageResource(resId);
        }
    }

    public URL getUrl() {
        return mUrl;
    }
}

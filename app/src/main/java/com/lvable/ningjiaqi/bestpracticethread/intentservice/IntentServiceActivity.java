package com.lvable.ningjiaqi.bestpracticethread.intentservice;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lvable.ningjiaqi.bestpracticethread.NetworkImageView;
import com.lvable.ningjiaqi.bestpracticethread.R;

import java.net.MalformedURLException;
import java.net.URL;

public class IntentServiceActivity extends Activity
        implements BookInfoReceiver.OnDataReceiveListener {

    private BookInfoReceiver mRecevier;
    private String mBaseUrl = "https://api.douban.com/v2/book/";
    private String[] bookId = {"26606521","6548683","1220562"};

    private NetworkImageView mCoverIv;
    private TextView mTitleTv;
    private TextView mAuthorTv;
    private TextView mSummaryTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_service);

        mCoverIv = (NetworkImageView) findViewById(R.id.cover_img);
        mTitleTv = (TextView) findViewById(R.id.tile);
        mAuthorTv = (TextView) findViewById(R.id.author);
        mSummaryTv = (TextView) findViewById(R.id.summary);

        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mRecevier = new BookInfoReceiver();
        mRecevier.setListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRecevier,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRecevier);
    }

    @Override
    public void onDataGet(BookInfo data) {
        if (data != null) {
            mTitleTv.setText(data.title);
            mAuthorTv.setText(data.author);
            mSummaryTv.setText(data.summary);
            try {
                mCoverIv.setImageUrl(new URL(data.imgUrl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,"get book error",Toast.LENGTH_SHORT).show();
        }
    }

    public void selectBook(View view) {
        int id = view.getId();
        Intent intent = new Intent(this,DownloadBookService.class);
        String url;
        switch (id){
            case R.id.book_1:
                url = mBaseUrl + bookId[0];
                break;
            case R.id.book_2:
                url = mBaseUrl + bookId[1];
                break;
            case R.id.book_3:
                url = mBaseUrl + bookId[2];
                break;
            default:
                return;
        }
        intent.putExtra(Constants.KEY_URL,url);
        startService(intent);
    }
}

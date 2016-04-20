package com.lvable.ningjiaqi.bestpracticethread.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lvable.ningjiaqi.bestpracticethread.intentservice.BookInfo;
import com.lvable.ningjiaqi.bestpracticethread.intentservice.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ningjiaqi on 16/4/20.
 */
public class DownloadBookService extends IntentService {
    private LocalBroadcastManager mBroadcaster;

    public DownloadBookService(){
        super("DownloadBookService");
        mBroadcaster = LocalBroadcastManager.getInstance(getBaseContext());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(Constants.KEY_URL);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET
                , url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                BookInfo bookInfo = json2BookInfo(response);
                Intent result = new Intent();
                result.setAction(Constants.BROADCAST_ACTION);
                result.putExtra(Constants.KEY_BOOK, bookInfo);
                result.putExtra(Constants.KEY_MSG,Constants.SUCCESS);
                result.addCategory(Intent.CATEGORY_DEFAULT);
                mBroadcaster.sendBroadcast(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent result = new Intent();
                result.setAction(Constants.BROADCAST_ACTION);
                result.addCategory(Intent.CATEGORY_DEFAULT);
                result.putExtra(Constants.KEY_MSG,Constants.FAILD);
                mBroadcaster.sendBroadcast(result);
            }
        });
        requestQueue.add(request);
    }


    public BookInfo json2BookInfo(JSONObject json) {
        BookInfo bookInfo = new BookInfo();
        try {
            JSONArray authors = json.getJSONArray("author");
            String author = authors.getString(0);
            String title = json.getString("title");
            String imgUrl = json.getString("image");
            String summary = json.getString("summary");

            bookInfo.author = author;
            bookInfo.title = title;
            bookInfo.imgUrl = imgUrl;
            bookInfo.summary = summary;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bookInfo;
    }
}

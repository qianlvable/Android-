package com.lvable.ningjiaqi.bestpracticethread;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends Activity {
    String url = "http://api-app.meizu.com/apps/public/mime/recommend?os=18";
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applist_layout);


        recyclerView = (RecyclerView) findViewById(R.id.app_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                recyclerView.setAdapter(new AppListAdapter(parseJson(response)));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Network Error",Toast.LENGTH_LONG).show();
            }
        });

        requestQueue.add(request);
    }


    public List<AppData> parseJson(JSONObject json){
        List<AppData> result = new ArrayList<>();
        try {
            JSONObject value = json.getJSONObject("value");
            JSONArray jsonArray = value.getJSONArray("data");
            for (int i =0;i<jsonArray.length();i++){
                JSONObject item = jsonArray.getJSONObject(i);
                String iconUrl = item.getString("appIcon");
                String pkg = item.getString("packageName");
                String appName = item.optString("appNameZhCn");
                result.add(new AppData(iconUrl,pkg,appName));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}

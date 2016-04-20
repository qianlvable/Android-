package com.lvable.ningjiaqi.bestpracticethread;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by ningjiaqi on 16/4/11.
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.MyViewHolder> {
    private List<AppData> mData;

    public AppListAdapter(List dataSet){
        mData = dataSet;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_layout,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, final int i) {
        myViewHolder.mAppNameTv.setText(mData.get(i).appName);
        myViewHolder.mPkgNameTv.setText(mData.get(i).packageName);
        try {
            myViewHolder.mAppIconIv.setImageUrl(new URL(mData.get(i).iconUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        myViewHolder.mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView mAppNameTv;
        TextView mPkgNameTv;
        NetworkImageView mAppIconIv;
        View mRoot;
        MyViewHolder(View itemView) {
            super(itemView);

            mRoot = itemView;
            mAppIconIv = (NetworkImageView) itemView.findViewById(R.id.app_icon);
            mPkgNameTv = (TextView) itemView.findViewById(R.id.app_pkg);
            mAppNameTv = (TextView) itemView.findViewById(R.id.app_name);
        }
    }

}

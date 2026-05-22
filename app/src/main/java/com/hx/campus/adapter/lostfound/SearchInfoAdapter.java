package com.hx.campus.adapter.lostfound;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.hx.campus.adapter.entity.SearchInfo;
import com.hx.campus.databinding.SearchInfoBinding;

import java.util.ArrayList;
import java.util.List;


public class SearchInfoAdapter extends BaseAdapter {
    
    private final Context context;
    
    private final List<SearchInfo> searchInfoList = new ArrayList<>();

    
    public void setData(List<SearchInfo> data, int pageIndex) {
        if (pageIndex == 0) {
            data.clear();
        }
        searchInfoList.addAll(data);
        notifyDataSetChanged();
    }

    
    public SearchInfoAdapter(Context context) {
        this.context = context;
    }


    @Override
    public int getCount() {
        return searchInfoList.size();
    }


    @Override
    public SearchInfo getItem(int position) {
        return searchInfoList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        com.hx.campus.databinding.SearchInfoBinding searchInfoBinding;
        if (view == null) {
            searchInfoBinding = SearchInfoBinding.inflate(LayoutInflater.from(context));
            view = searchInfoBinding.getRoot();
            view.setTag(searchInfoBinding);
        } else {
            searchInfoBinding = (SearchInfoBinding) view.getTag();
        }
        SearchInfo searchInfo = getItem(position);
        searchInfoBinding.lostTitle.setText(searchInfo.getTitle());
        searchInfoBinding.authorName.setText(searchInfo.getNickname());
        searchInfoBinding.tvLostContent.setText(searchInfo.getContent());

        if (TextUtils.isEmpty(searchInfo.getImg())) {
            searchInfoBinding.lostImg.setVisibility(View.GONE);
        } else {
            searchInfoBinding.lostImg.setVisibility(View.VISIBLE);
            Glide.with(context).load(searchInfo.getImg()).into(searchInfoBinding.lostImg);
        }
        return view;
    }
}

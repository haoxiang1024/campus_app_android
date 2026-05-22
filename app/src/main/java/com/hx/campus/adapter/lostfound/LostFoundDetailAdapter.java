package com.hx.campus.adapter.lostfound;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.databinding.FoundItemsBinding;
import com.hx.campus.databinding.LostItemsBinding;

import java.util.ArrayList;
import java.util.List;


public class LostFoundDetailAdapter extends BaseAdapter {
    
    private final Context context;
    
    private final List<LostFound> dataList = new ArrayList<>();

    
    public LostFoundDetailAdapter(Context context) {
        this.context = context;
    }

    
    public void setData(List<LostFound> data, int pageIndex) {
        if (pageIndex == 0) {
            dataList.clear();
        }
        dataList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public LostFound getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemViewType(int position) {

        return "0".equals(getItem(position).getType()) ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LostFound item = getItem(position);
        int type = getItemViewType(position);

        if (type == 0) {

            LostItemsBinding lostBinding;
            if (view == null) {
                lostBinding = LostItemsBinding.inflate(LayoutInflater.from(context), viewGroup, false);
                view = lostBinding.getRoot();
                view.setTag(lostBinding);
            } else {
                lostBinding = (LostItemsBinding) view.getTag();
            }

            lostBinding.lostTitle.setText(item.getTitle());
            lostBinding.authorName.setText(item.getNickname());
            lostBinding.tvLostContent.setText(item.getContent());
            loadImage(item.getImg(), lostBinding.lostImg);

        } else {

            FoundItemsBinding foundBinding;
            if (view == null) {
                foundBinding = FoundItemsBinding.inflate(LayoutInflater.from(context), viewGroup, false);
                view = foundBinding.getRoot();
                view.setTag(foundBinding);
            } else {
                foundBinding = (FoundItemsBinding) view.getTag();
            }

            foundBinding.lostTitle.setText(item.getTitle());
            foundBinding.authorName.setText(item.getNickname());
            foundBinding.tvLostContent.setText(item.getContent());
            loadImage(item.getImg(), foundBinding.lostImg);
        }

        return view;
    }


    
    private void loadImage(String imgUrl, android.widget.ImageView imageView) {
        if (TextUtils.isEmpty(imgUrl)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(imgUrl).into(imageView);
        }
    }
}

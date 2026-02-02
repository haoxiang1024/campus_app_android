package com.hx.campus.adapter.lostandfound;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.hx.campus.adapter.entity.LostFound; // 使用合并后的实体类
import com.hx.campus.databinding.FoundItemsBinding;
import com.hx.campus.databinding.LostItemsBinding;

import java.util.ArrayList;
import java.util.List;

public class LostFoundDetailAdapter extends BaseAdapter {
    private final Context context;
    private final List<LostFound> dataList = new ArrayList<>(); // 统一数据源

    public LostFoundDetailAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<LostFound> data, int pageIndex) {
        if (pageIndex == 0) {
            dataList.clear();
        }
        dataList.addAll(data);
        notifyDataSetChanged(); // 刷新界面
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

    // 定义两种视图类型：0 代表失物，1 代表招领
    @Override
    public int getItemViewType(int position) {
        // 根据实体类中的 type 字段判断（假设 "0" 是失物，"1" 是招领）
        return "0".equals(getItem(position).getType()) ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // 总共有两种布局
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LostFound item = getItem(position);
        int type = getItemViewType(position);

        if (type == 0) {
            // 处理失物布局 (Lost)
            LostItemsBinding lostBinding;
            if (view == null) {
                lostBinding = LostItemsBinding.inflate(LayoutInflater.from(context), viewGroup, false);
                view = lostBinding.getRoot();
                view.setTag(lostBinding);
            } else {
                lostBinding = (LostItemsBinding) view.getTag();
            }
            // 绑定数据
            lostBinding.lostTitle.setText(item.getTitle());
            lostBinding.authorName.setText(item.getNickname());
            lostBinding.tvLostContent.setText(item.getContent());
            loadImage(item.getImg(), lostBinding.lostImg);

        } else {
            // 处理招领布局 (Found)
            FoundItemsBinding foundBinding;
            if (view == null) {
                foundBinding = FoundItemsBinding.inflate(LayoutInflater.from(context), viewGroup, false);
                view = foundBinding.getRoot();
                view.setTag(foundBinding);
            } else {
                foundBinding = (FoundItemsBinding) view.getTag();
            }
            // 绑定数据
            foundBinding.lostTitle.setText(item.getTitle());
            foundBinding.authorName.setText(item.getNickname());
            foundBinding.tvLostContent.setText(item.getContent());
            loadImage(item.getImg(), foundBinding.lostImg);
        }

        return view;
    }

    // 提取公共的图片加载逻辑
    private void loadImage(String imgUrl, android.widget.ImageView imageView) {
        if (TextUtils.isEmpty(imgUrl)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(imgUrl).into(imageView);
        }
    }
}

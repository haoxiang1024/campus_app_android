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

/**
 * 失物招领详情适配器
 * 支持失物和招领两种不同布局类型的列表展示
 * 使用BaseAdapter实现，通过ViewType区分不同的布局
 */
public class LostFoundDetailAdapter extends BaseAdapter {
    /** 上下文环境 */
    private final Context context;
    /** 统一数据源，存储失物和招领信息 */
    private final List<LostFound> dataList = new ArrayList<>();

    /**
     * 构造函数
     * 
     * @param context 上下文环境
     */
    public LostFoundDetailAdapter(Context context) {
        this.context = context;
    }

    /**
     * 设置数据源
     * 
     * @param data 数据列表
     * @param pageIndex 页面索引，0表示第一页需要清空旧数据
     */
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
        // 根据实体类中的 type 字段判断
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
            // 处理失物布局
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
            // 处理招领布局
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
    /**
     * 加载图片到ImageView
     * 
     * @param imgUrl 图片URL地址
     * @param imageView 目标ImageView控件
     */
    private void loadImage(String imgUrl, android.widget.ImageView imageView) {
        if (TextUtils.isEmpty(imgUrl)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(imgUrl).into(imageView);
        }
    }
}

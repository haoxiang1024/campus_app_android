

package com.hx.campus.adapter.base.broccoli;

import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.android.vlayout.LayoutHelper;
import com.hx.campus.adapter.base.delegate.SimpleDelegateAdapter;
import com.hx.campus.adapter.base.delegate.XDelegateAdapter;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import me.samlss.broccoli.Broccoli;


/**
 * Broccoli骨架屏简单委托适配器
 * 结合SimpleDelegateAdapter和Broccoli骨架屏功能
 * 提供统一的布局ID和骨架屏加载效果
 * 
 * @param <T> 数据项类型
 */
public abstract class BroccoliSimpleDelegateAdapter<T> extends SimpleDelegateAdapter<T> {

    /**
     * Broccoli实例映射表
     * Key为View，Value为对应的Broccoli实例
     */
    private final Map<View, Broccoli> mBroccoliMap = new HashMap<>();
    /**
     * 数据加载状态标记
     * false表示正在加载中，true表示加载完成
     */
    private boolean mHasLoad = false;

    /**
     * 构造函数
     * 
     * @param layoutId 布局资源ID
     * @param layoutHelper 布局助手
     */
    public BroccoliSimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper) {
        super(layoutId, layoutHelper);
    }

    /**
     * 带集合数据的构造函数
     * 
     * @param layoutId 布局资源ID
     * @param layoutHelper 布局助手
     * @param list 初始化数据集合
     */
    public BroccoliSimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper, Collection<T> list) {
        super(layoutId, layoutHelper, list);
    }

    /**
     * 带数组数据的构造函数
     * 
     * @param layoutId 布局资源ID
     * @param layoutHelper 布局助手
     * @param data 初始化数据数组
     */
    public BroccoliSimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper, T[] data) {
        super(layoutId, layoutHelper, data);
    }

    @Override
    protected void bindData(@NonNull RecyclerViewHolder holder, int position, T item) {
        // 获取当前View对应的Broccoli实例
        Broccoli broccoli = mBroccoliMap.get(holder.itemView);
        if (broccoli == null) {
            // 如果不存在则创建新的Broccoli实例
            broccoli = new Broccoli();
            mBroccoliMap.put(holder.itemView, broccoli);
        }
        
        if (mHasLoad) {
            // 数据加载完成，移除占位符并绑定真实数据
            broccoli.removeAllPlaceholders();
            onBindData(holder, item, position);
        } else {
            // 数据加载中，绑定占位符并显示骨架屏
            onBindBroccoli(holder, broccoli);
            broccoli.show();
        }
    }


    /**
     * 绑定控件
     *
     * @param holder
     * @param model
     * @param position
     */
    protected abstract void onBindData(RecyclerViewHolder holder, T model, int position);

    /**
     * 绑定占位控件
     *
     * @param holder
     * @param broccoli
     */
    protected abstract void onBindBroccoli(RecyclerViewHolder holder, Broccoli broccoli);

    /**
     * 刷新数据并标记加载完成
     * 
     * @param collection 新的数据集合
     * @return 当前适配器实例
     */
    @Override
    public XDelegateAdapter refresh(Collection<T> collection) {
        mHasLoad = true;
        return super.refresh(collection);
    }

    /**
     * 资源释放，防止内存泄漏
     */
    public void recycle() {
        for (Broccoli broccoli : mBroccoliMap.values()) {
            broccoli.removeAllPlaceholders();
        }
        mBroccoliMap.clear();
        clear();
    }
}

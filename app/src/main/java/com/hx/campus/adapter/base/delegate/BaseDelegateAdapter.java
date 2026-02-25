

package com.hx.campus.adapter.base.delegate;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;

import java.util.Collection;


/**
 * 基础委托适配器抽象类
 * 继承自XDelegateAdapter，提供RecyclerViewHolder支持
 * 子类需要实现getItemLayoutId方法来指定布局资源
 * 
 * @param <T> 数据项类型
 */
public abstract class BaseDelegateAdapter<T> extends XDelegateAdapter<T, RecyclerViewHolder> {

    /**
     * 默认构造函数
     * 调用父类无参构造函数初始化空数据源
     */
    public BaseDelegateAdapter() {
        super();
    }

    /**
     * 带集合数据的构造函数
     * 
     * @param list 初始化数据集合，可为null
     */
    public BaseDelegateAdapter(Collection<T> list) {
        super(list);
    }

    /**
     * 带数组数据的构造函数
     * 
     * @param data 初始化数据数组，可为null或空数组
     */
    public BaseDelegateAdapter(T[] data) {
        super(data);
    }

    /**
     * 适配的布局
     *
     * @param viewType
     * @return
     */
    protected abstract int getItemLayoutId(int viewType);

    @NonNull
    @Override
    protected RecyclerViewHolder getViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(inflateView(parent, getItemLayoutId(viewType)));
    }
}

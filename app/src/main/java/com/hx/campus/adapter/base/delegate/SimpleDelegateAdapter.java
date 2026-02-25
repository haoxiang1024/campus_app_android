

package com.hx.campus.adapter.base.delegate;

import com.alibaba.android.vlayout.LayoutHelper;

import java.util.Collection;


/**
 * 简单委托适配器抽象类
 * 继承自BaseDelegateAdapter，提供固定的布局ID和布局助手
 * 适用于单一布局类型的列表项
 * 
 * @param <T> 数据项类型
 */
public abstract class SimpleDelegateAdapter<T> extends BaseDelegateAdapter<T> {

    /** 布局资源ID */
    private final int mLayoutId;

    /** 布局助手，用于控制RecyclerView的布局行为 */
    private final LayoutHelper mLayoutHelper;

    /**
     * 构造函数
     * 
     * @param layoutId 布局资源ID
     * @param layoutHelper 布局助手
     */
    public SimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper) {
        super();
        mLayoutId = layoutId;
        mLayoutHelper = layoutHelper;
    }

    /**
     * 带集合数据的构造函数
     * 
     * @param layoutId 布局资源ID
     * @param layoutHelper 布局助手
     * @param list 初始化数据集合
     */
    public SimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper, Collection<T> list) {
        super(list);
        mLayoutId = layoutId;
        mLayoutHelper = layoutHelper;
    }

    /**
     * 带数组数据的构造函数
     * 
     * @param layoutId 布局资源ID
     * @param layoutHelper 布局助手
     * @param data 初始化数据数组
     */
    public SimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper, T[] data) {
        super(data);
        mLayoutId = layoutId;
        mLayoutHelper = layoutHelper;
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        return mLayoutId;
    }


    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return mLayoutHelper;
    }
}



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



public abstract class BroccoliSimpleDelegateAdapter<T> extends SimpleDelegateAdapter<T> {

    
    private final Map<View, Broccoli> mBroccoliMap = new HashMap<>();
    
    private boolean mHasLoad = false;

    
    public BroccoliSimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper) {
        super(layoutId, layoutHelper);
    }

    
    public BroccoliSimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper, Collection<T> list) {
        super(layoutId, layoutHelper, list);
    }

    
    public BroccoliSimpleDelegateAdapter(int layoutId, LayoutHelper layoutHelper, T[] data) {
        super(layoutId, layoutHelper, data);
    }

    @Override
    protected void bindData(@NonNull RecyclerViewHolder holder, int position, T item) {

        Broccoli broccoli = mBroccoliMap.get(holder.itemView);
        if (broccoli == null) {

            broccoli = new Broccoli();
            mBroccoliMap.put(holder.itemView, broccoli);
        }
        
        if (mHasLoad) {

            broccoli.removeAllPlaceholders();
            onBindData(holder, item, position);
        } else {

            onBindBroccoli(holder, broccoli);
            broccoli.show();
        }
    }


    
    protected abstract void onBindData(RecyclerViewHolder holder, T model, int position);

    
    protected abstract void onBindBroccoli(RecyclerViewHolder holder, Broccoli broccoli);

    
    @Override
    public XDelegateAdapter refresh(Collection<T> collection) {
        mHasLoad = true;
        return super.refresh(collection);
    }

    
    public void recycle() {
        for (Broccoli broccoli : mBroccoliMap.values()) {
            broccoli.removeAllPlaceholders();
        }
        mBroccoliMap.clear();
        clear();
    }
}



package com.hx.campus.adapter.base.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;



public abstract class XDelegateAdapter<T, V extends RecyclerView.ViewHolder> extends DelegateAdapter.Adapter<V> {
    
    protected final List<T> mData = new ArrayList<>();
    
    protected int mSelectPosition = -1;

    
    public XDelegateAdapter() {

    }

    
    public XDelegateAdapter(Collection<T> list) {
        if (list != null) {
            mData.addAll(list);
        }
    }

    
    public XDelegateAdapter(T[] data) {
        if (data != null && data.length > 0) {
            mData.addAll(Arrays.asList(data));
        }
    }

    
    @NonNull
    protected abstract V getViewHolder(@NonNull ViewGroup parent, int viewType);

    
    protected abstract void bindData(@NonNull V holder, int position, T item);

    
    protected View inflateView(ViewGroup parent, @LayoutRes int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        bindData(holder, position, mData.get(position));
    }

    
    public T getItem(int position) {
        return checkPosition(position) ? mData.get(position) : null;
    }

    
    private boolean checkPosition(int position) {
        return position >= 0 && position <= mData.size() - 1;
    }

    
    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    
    public List<T> getData() {
        return mData;
    }

    
    public XDelegateAdapter add(int pos, T item) {
        mData.add(pos, item);
        notifyItemInserted(pos);
        return this;
    }

    
    public XDelegateAdapter add(T item) {
        mData.add(item);
        notifyItemInserted(mData.size() - 1);
        return this;
    }

    
    public XDelegateAdapter delete(int pos) {
        mData.remove(pos);
        notifyItemRemoved(pos);
        return this;
    }

    
    public XDelegateAdapter refresh(int pos, T item) {
        mData.set(pos, item);
        notifyItemChanged(pos);
        return this;
    }

    
    public XDelegateAdapter refresh(Collection<T> collection) {
        if (collection != null) {
            mData.clear();
            mData.addAll(collection);
            mSelectPosition = -1;
            notifyDataSetChanged();
        }
        return this;
    }

    
    public XDelegateAdapter refresh(T[] array) {
        if (array != null && array.length > 0) {
            mData.clear();
            mData.addAll(Arrays.asList(array));
            mSelectPosition = -1;
            notifyDataSetChanged();
        }
        return this;
    }

    
    public XDelegateAdapter loadMore(Collection<T> collection) {
        if (collection != null) {
            mData.addAll(collection);
            notifyDataSetChanged();
        }
        return this;
    }

    
    public XDelegateAdapter loadMore(T[] array) {
        if (array != null && array.length > 0) {
            mData.addAll(Arrays.asList(array));
            notifyDataSetChanged();
        }
        return this;
    }

    
    public XDelegateAdapter load(T item) {
        if (item != null) {
            mData.add(item);
            notifyDataSetChanged();
        }
        return this;
    }

    
    public int getSelectPosition() {
        return mSelectPosition;
    }

    
    public XDelegateAdapter setSelectPosition(int selectPosition) {
        mSelectPosition = selectPosition;
        notifyDataSetChanged();
        return this;
    }

    
    public T getSelectItem() {
        return getItem(mSelectPosition);
    }

    
    public void clear() {
        if (!isEmpty()) {
            mData.clear();
            mSelectPosition = -1;
            notifyDataSetChanged();
        }
    }
}

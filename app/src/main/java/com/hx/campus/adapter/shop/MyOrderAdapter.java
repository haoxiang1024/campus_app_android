package com.hx.campus.adapter.shop;

import static com.hx.campus.fragment.shop.MyOrderFragment.formatTime;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.ExchangeOrder;
import com.hx.campus.fragment.shop.MyOrderFragment;

import java.util.List;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder> {

    private Context context;
    private List<ExchangeOrder> list;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    public interface OnItemClickListener {
        void onItemClick(ExchangeOrder order);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(ExchangeOrder order);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(ExchangeOrder order);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public MyOrderAdapter(Context context, List<ExchangeOrder> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_my_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExchangeOrder order = list.get(position);
        holder.tvItemName.setText("商品名称：" + order.getItem_name());
        holder.tvOrderNo.setText("订单号：" + order.getOrder_no());
        holder.tvPointsCost.setText("消耗积分：" + order.getPoints_cost());
        holder.tvCreateTime.setText("兑换时间：" + formatTime(order.getCreate_time()));
        int status = order.getStatus();
        if (status == 0) {
            holder.tvStatus.setText("待核销");
            holder.tvStatus.setTextColor(Color.parseColor("#FFA500"));
            holder.tvDelete.setVisibility(View.GONE);
        } else if (status == 1) {
            holder.tvStatus.setText("已核销");
            holder.tvStatus.setTextColor(Color.parseColor("#008000"));
            holder.tvDelete.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setText("已取消");
            holder.tvStatus.setTextColor(Color.parseColor("#FF0000"));
            holder.tvDelete.setVisibility(View.VISIBLE);
        }
        holder.tvDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(order);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(order);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(order);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvOrderNo, tvPointsCost, tvCreateTime, tvStatus,tvDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvPointsCost = itemView.findViewById(R.id.tv_points_cost);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDelete = itemView.findViewById(R.id.tv_delete);
        }
    }

}
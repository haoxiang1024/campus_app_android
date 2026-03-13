package com.hx.campus.fragment.shop;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.ShopItem;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.shop.ShopAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentShopBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.MMKVUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class ShopFragment extends BaseFragment<FragmentShopBinding> {

    RecyclerView recyclerView;

    SmartRefreshLayout refreshLayout;

    private ShopAdapter mAdapter;



    @Override
    protected void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        // 设置网格布局，2列
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new ShopAdapter();
        recyclerView.setAdapter(mAdapter);

        // 设置点击兑换的回调
        mAdapter.setOnExchangeClickListener(item -> showExchangeConfirmDialog(item));

        // 下拉刷新逻辑
        refreshLayout.setOnRefreshListener(refreshLayout -> loadShopItems());

        // 自动触发第一次加载
        refreshLayout.autoRefresh();
    }

    /**
     * 调用 ApiService 获取商品列表
     */
    private void loadShopItems() {
        RetrofitClient.getInstance().getApi().getShopItems().enqueue(new Callback<Result<List<ShopItem>>>() {
            @Override
            public void onResponse(Call<Result<List<ShopItem>>> call, Response<Result<List<ShopItem>>> response) {
                refreshLayout.finishRefresh();
                if (response.body() != null && response.body().getStatus() == 0) {
                    mAdapter.setData(response.body().getData());
                } else {
                    XToastUtils.error("加载失败：" + response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<Result<List<ShopItem>>> call, Throwable t) {
                refreshLayout.finishRefresh(false);
                XToastUtils.error("网络异常，请稍后再试");
            }
        });
    }

    /**
     * 优雅的 XUI 确认弹窗
     */
    private void showExchangeConfirmDialog(ShopItem item) {
        new MaterialDialog.Builder(getContext())
                .title("兑换确认")
                .content(String.format("您确定要消耗 %d 积分兑换【%s】吗？", item.getRequiredPoints(), item.getName()))
                .positiveText("确认兑换")
                .negativeText("取消")
                .onPositive((dialog, which) -> requestExchange(item.getId()))
                .show();
    }

    /**
     * 发起兑换的网络请求
     */
    private void requestExchange(int itemId) {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        int userId = user.getId();
        if (userId == -1) {
            XToastUtils.warning("请先登录！");
            return;
        }
        Utils.showResponse("正在兑换");
        RetrofitClient.getInstance().getApi().exchangeItem(userId, itemId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().getStatus() == 0) {
                    XToastUtils.success("🎉 兑换成功！");
                    refreshLayout.autoRefresh();
                } else {
                    XToastUtils.error("兑换失败：" + response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                XToastUtils.error("网络异常，兑换请求发送失败");
            }
        });
    }

    @NonNull
    @Override
    protected FragmentShopBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return FragmentShopBinding.inflate(inflater, container, attachToRoot);
    }
}
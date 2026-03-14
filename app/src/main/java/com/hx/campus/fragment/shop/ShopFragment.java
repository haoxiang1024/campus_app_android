package com.hx.campus.fragment.shop;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.ShopItem;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.shop.ShopAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentShopBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class ShopFragment extends BaseFragment<FragmentShopBinding> {

    RecyclerView recyclerView;
    User user;
    SmartRefreshLayout refreshLayout;
    TextView tvUserPoints;

    private ShopAdapter mAdapter;

    @Override
    protected void initViews() {
        user = Utils.getBeanFromSp(getContext(), "User", "user");
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        tvUserPoints = findViewById(R.id.tv_user_points);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new ShopAdapter();
        recyclerView.setAdapter(mAdapter);

        if (user != null) {
            tvUserPoints.setText(String.valueOf(user.getPoints()));
            mAdapter.setUserPoints(user.getPoints());
        }

        loadShopItems();

        // 绑定兑换按钮点击事件
        mAdapter.setOnExchangeClickListener(item -> showExchangeConfirmDialog(item));

        // 绑定商品卡片点击事件 弹出详情
        mAdapter.setOnItemClickListener(item -> showItemDetailDialog(item));

        refreshLayout.setOnRefreshListener(refreshLayout -> loadShopItems());
    }

    @Override
    protected String getPageTitle() {
        return "积分商城";
    }

    private void loadShopItems() {
        RetrofitClient.getInstance().getApi().getShopItems().enqueue(new Callback<Result<List<ShopItem>>>() {
            @Override
            public void onResponse(Call<Result<List<ShopItem>>> call, Response<Result<List<ShopItem>>> response) {
                refreshLayout.finishRefresh();
                if (response.body() != null && response.body().getStatus() == 0) {
                    mAdapter.setData(response.body().getData());
                } else {
                    XToastUtils.error("加载失败：" + (response.body() != null ? response.body().getMsg() : "未知错误"));
                }
            }

            @Override
            public void onFailure(Call<Result<List<ShopItem>>> call, Throwable t) {
                refreshLayout.finishRefresh(false);
                XToastUtils.error("网络异常，请稍后再试");
            }
        });
    }

    private void showItemDetailDialog(ShopItem item) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 20);
        // 创建 ImageView 展示商品图片
        ImageView imageView = new ImageView(getContext());
        // 将图片高度设置为约 200dp (通过转换公式)
        int height = (int) (200 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        imgParams.bottomMargin = 40; // 设置图片与下方文字的间距
        imageView.setLayoutParams(imgParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // 使用 Glide 加载网络图片
        Glide.with(this).load(item.getImage_url()).into(imageView);

        // 创建 TextView 展示商品详情文本
        TextView textView = new TextView(getContext());
        String detailContent = "商品名称：" + item.getName() + "\n\n" +
                "所需积分：" + item.getRequired_points() + " 积分\n\n" +
                "商品说明：" + item.getDescription();
        textView.setText(detailContent);
        textView.setTextSize(15);
        textView.setTextColor(Color.parseColor("#333333")); // 设置较深的文字颜色方便阅读

        // 依次将图片和文字添加到布局中
        layout.addView(imageView);
        layout.addView(textView);

        new MaterialDialog.Builder(getContext())
                .title("商品详情")
                .customView(layout, true)
                .positiveText("关闭")
                .show();
    }

    private void showExchangeConfirmDialog(ShopItem item) {
        new MaterialDialog.Builder(getContext())
                .title("兑换确认")
                .content(String.format("您确定要消耗 %d 积分兑换【%s】吗？", item.getRequired_points(), item.getName()))
                .positiveText("确认兑换")
                .negativeText("取消")
                .onPositive((dialog, which) -> requestExchange(item.getId()))
                .show();
    }

    private void requestExchange(int itemId) {
        User currentUser = Utils.getBeanFromSp(getContext(), "User", "user");
        if (currentUser == null || currentUser.getId() == -1) {
            XToastUtils.warning("请先登录！");
            return;
        }

        int userId = currentUser.getId();
        Utils.showResponse("正在兑换...");

        RetrofitClient.getInstance().getApi().exchangeItem(userId, itemId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().getStatus() == 0) {
                    XToastUtils.success("兑换成功！");

                    fetchLatestUserInfo(userId);

                    refreshLayout.autoRefresh();
                } else {
                    XToastUtils.error("兑换失败：" + (response.body() != null ? response.body().getMsg() : "未知错误"));
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                XToastUtils.error("网络异常，兑换请求发送失败");
            }
        });
    }

    private void fetchLatestUserInfo(int userId) {
        RetrofitClient.getInstance().getApi().getUserInfo(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User latestUser = response.body();
                    tvUserPoints.setText(String.valueOf(latestUser.getPoints()));
                    Utils.doUserData(latestUser);
                    user = latestUser;
                    mAdapter.setUserPoints(latestUser.getPoints());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                XToastUtils.info("积分显示刷新延迟，请稍后留意");
            }
        });
    }

    @NonNull
    @Override
    protected FragmentShopBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return FragmentShopBinding.inflate(inflater, container, attachToRoot);
    }
}
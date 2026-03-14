package com.hx.campus.fragment.shop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
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

import java.util.Hashtable;
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

        ImageView imageView = new ImageView(getContext());
        int height = (int) (200 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        imgParams.bottomMargin = 40;
        imageView.setLayoutParams(imgParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this).load(item.getImage_url()).into(imageView);

        TextView textView = new TextView(getContext());
        String detailContent = "商品名称：" + item.getName() + "\n\n" +
                "所需积分：" + item.getRequired_points() + " 积分\n\n" +
                "商品说明：" + item.getDescription();
        textView.setText(detailContent);
        textView.setTextSize(15);
        textView.setTextColor(Color.parseColor("#333333"));

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
                .onPositive((dialog, which) -> requestExchange(item)) // 注意这里传的是 item 对象
                .show();
    }

    /**
     * 请求兑换商品
     */
    private void requestExchange(ShopItem item) {
        User currentUser = Utils.getBeanFromSp(getContext(), "User", "user");
        if (currentUser == null || currentUser.getId() == -1) {
            XToastUtils.warning("请先登录！");
            return;
        }

        int userId = currentUser.getId();
        Utils.showResponse("正在处理兑换请求...");

        RetrofitClient.getInstance().getApi().exchangeItem(userId, item.getId()).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().getStatus() == 0) {
                    // 获取后端生成的 8位 核销码
                    String verifyCode = response.body().getData();

                    // 兑换成功，展示核销码和二维码弹窗
                    showVerifyCodeDialog(verifyCode, item);

                    // 刷新最新积分和列表
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

    /**
     * 弹出包含二维码和核销码的核销凭证弹窗
     */
    private void showVerifyCodeDialog(String verifyCode, ShopItem item) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER); // 整体居中
        layout.setPadding(50, 40, 50, 40);

        //  顶部提示文字
        TextView tvTip = new TextView(getContext());
        tvTip.setText("请向管理员出示此二维码或核销码\n领取【" + item.getName() + "】");
        tvTip.setTextSize(16);
        tvTip.setGravity(Gravity.CENTER);
        tvTip.setTextColor(Color.parseColor("#333333"));
        layout.addView(tvTip);

        // 二维码 ImageView
        ImageView ivQrCode = new ImageView(getContext());
        // 转换 dp 为 px
        int qrSize = (int) (220 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(qrSize, qrSize);
        qrParams.setMargins(0, 60, 0, 60); // 上下间距
        ivQrCode.setLayoutParams(qrParams);

        Bitmap qrBitmap = createQRCode(verifyCode, qrSize, qrSize);
        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
        }
        layout.addView(ivQrCode);

        //  底部红色显眼核销码
        TextView tvCode = new TextView(getContext());
        tvCode.setText("核销码：" + verifyCode);
        tvCode.setTextSize(22);
        tvCode.setTypeface(null, Typeface.BOLD); // 加粗
        tvCode.setGravity(Gravity.CENTER);
        tvCode.setTextColor(Color.parseColor("#FF5722")); // 醒目红色
        layout.addView(tvCode);

        // 显示弹窗
        new MaterialDialog.Builder(getContext())
                .customView(layout, false)
                .cancelable(false)
                .positiveText("我已截图保存")
                .show();
    }

    /**
     * 使用 ZXing 生成二维码 Bitmap
     */
    private Bitmap createQRCode(String content, int width, int height) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1); // 二维码边距

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            }
        });
    }

    @NonNull
    @Override
    protected FragmentShopBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return FragmentShopBinding.inflate(inflater, container, attachToRoot);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            fetchLatestUserInfo(user.getId());
        }
    }
}
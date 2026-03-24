package com.hx.campus.fragment.shop;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.OutputStream;
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
    TextView tvPointHistory;

    private ShopAdapter mAdapter;



    @Override
    protected void initViews() {
        user = Utils.getBeanFromSp(getContext(), "User", "user");
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        tvUserPoints = findViewById(R.id.tv_user_points);
        tvPointHistory = findViewById(R.id.tv_point_history);

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

        // 绑定商品卡片点击事件弹出详情
        mAdapter.setOnItemClickListener(item -> showItemDetailDialog(item));

        // 绑定下拉刷新事件
        refreshLayout.setOnRefreshListener(refreshLayout -> loadShopItems());

        // 绑定积分明细点击事件
        tvPointHistory.setOnClickListener(v -> requestPointHistory());
        //我的订单
        TextView tvMyOrders = findViewById(R.id.tv_my_orders);
        tvMyOrders.setOnClickListener(v -> {
            openNewPage(MyOrderFragment.class);
        });
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
                   List<ShopItem> shopItems= response.body().getData();
                   if (shopItems.isEmpty()){
                       Utils.showResponse("没有商品");
                   }
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

    // 请求积分明细列表数据
    private void requestPointHistory() {
        if (user == null || user.getId() == -1) {
            XToastUtils.warning("请先登录");
            return;
        }
        openNewPage(PointHistoryFragment.class);
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
                .onPositive((dialog, which) -> requestExchange(item))
                .show();
    }

    // 请求兑换商品
    private void requestExchange(ShopItem item) {
        User currentUser = Utils.getBeanFromSp(getContext(), "User", "user");
        if (currentUser == null || currentUser.getId() == -1) {
            XToastUtils.warning("请先登录！");
            return;
        }

        int userId = currentUser.getId();

        RetrofitClient.getInstance().getApi().exchangeItem(userId, item.getId()).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().getStatus() == 0) {
                    // 获取后端生成的核验码
                    String verifyCode = response.body().getData();

                    // 兑换成功展示核验码和二维码弹窗
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

    // 弹出包含二维码和核验码的核验凭证弹窗
    private void showVerifyCodeDialog(String verifyCode, ShopItem item) {
        Utils.showResponse("兑换成功");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 40, 50, 40);

        // 顶部提示文字
        TextView tvTip = new TextView(getContext());
        tvTip.setText("请向管理员出示此二维码或核验码\n领取【" + item.getName() + "】");
        tvTip.setTextSize(16);
        tvTip.setGravity(Gravity.CENTER);
        tvTip.setTextColor(Color.parseColor("#333333"));
        layout.addView(tvTip);

        // 二维码 ImageView
        ImageView ivQrCode = new ImageView(getContext());
        // 转换尺寸单位
        int qrSize = (int) (220 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(qrSize, qrSize);
        qrParams.setMargins(0, 60, 0, 60);
        ivQrCode.setLayoutParams(qrParams);

        Bitmap qrBitmap = createQRCode(verifyCode, qrSize, qrSize);
        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
        }
        layout.addView(ivQrCode);

        // 底部显眼核验码
        TextView tvCode = new TextView(getContext());
        tvCode.setText("核验码：" + verifyCode);
        tvCode.setTextSize(22);
        tvCode.setTypeface(null, Typeface.BOLD);
        tvCode.setGravity(Gravity.CENTER);
        tvCode.setTextColor(Color.parseColor("#FF5722"));
        layout.addView(tvCode);

        // 显示弹窗
        new MaterialDialog.Builder(getContext())
                .customView(layout, false)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .positiveText("保存二维码")
                .onPositive((dialog, which) -> {
                    Bitmap viewBitmap = createBitmapFromView(layout);
                    if (viewBitmap != null) {
                        // 保存到相册
                        saveBitmapToGallery(viewBitmap);
                    } else {
                        Toast.makeText(getContext(), "生成图片失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * 将 Bitmap 保存到手机相册
     */
    private void saveBitmapToGallery(Bitmap bitmap) {
        // 准备图片的元数据
        String fileName = "VerifyCode_" + System.currentTimeMillis() + ".png";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        // 适配 Android 10 及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES); // 保存到 Pictures 目录
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        // 插入图片信息并获取 Uri
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try {
                // 打开输出流并压缩图片
                OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                }

                // 适配 Android 10：解除 PENDING 状态
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContext().getContentResolver().update(uri, values, null, null);
                }

                Toast.makeText(getContext(), "核验码已保存到相册", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "无法访问相册", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 将 View 转换为 Bitmap
     */
    private Bitmap createBitmapFromView(View view) {
        // 获取 View 的宽和高
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        // 创建一个和 View 大小一样的 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 绘制白色背景，防止保存后透明部分变黑
        canvas.drawColor(Color.WHITE);

        // 将 View 的内容绘制到 Canvas 上
        view.draw(canvas);

        return bitmap;
    }
    // 使用 ZXing 生成二维码图片数据
    private Bitmap createQRCode(String content, int width, int height) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

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
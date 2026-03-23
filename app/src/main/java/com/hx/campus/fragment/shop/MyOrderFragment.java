package com.hx.campus.fragment.shop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.ExchangeOrder;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.shop.MyOrderAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentMyOrderBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.ApiService;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page(name = "我的订单")
public class MyOrderFragment extends BaseFragment<FragmentMyOrderBinding> {

    private RecyclerView recyclerView;
    private MyOrderAdapter adapter;
    private EditText etSearch;
    private ImageView ivSearch;
    private List<ExchangeOrder> orderList = new ArrayList<>();

    @Override
    protected void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        etSearch = findViewById(R.id.et_search);
        ivSearch = findViewById(R.id.iv_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyOrderAdapter(getContext(), orderList);
        recyclerView.setAdapter(adapter);

        loadOrders("");
    }

    @NonNull
    @Override
    protected FragmentMyOrderBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return FragmentMyOrderBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initListeners() {
        ivSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            loadOrders(keyword);
        });

        adapter.setOnItemClickListener(this::showOrderDetailDialog);

        adapter.setOnDeleteClickListener(order -> {
            if (order.getStatus() == 0) {
                Utils.showResponse("待核验的订单不能删除哦");
                return; // 提前结束，不弹出删除框
            }
            new MaterialDialog.Builder(getContext())
                    .title("提示")
                    .content("确定要删除该订单吗？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive((dialog, which) -> deleteOrder(order))
                    .show();
        });
    }

    private void showOrderDetailDialog(ExchangeOrder order) {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .customView(R.layout.dialog_order_detail, true)
                .title("订单详情")
                .positiveText("关闭")
                .build();

        View view = dialog.getCustomView();
        if (view != null) {
            ImageView ivImage = view.findViewById(R.id.iv_item_image);
            TextView tvItemName = view.findViewById(R.id.tv_item_name);
            TextView tvOrderNo = view.findViewById(R.id.tv_order_no);
            TextView tvPointsCost = view.findViewById(R.id.tv_points_cost);
            TextView tvCreateTime = view.findViewById(R.id.tv_create_time);
            TextView tvStatus = view.findViewById(R.id.tv_status);
            TextView tvGetQrCode = view.findViewById(R.id.tv_get_qrcode);

            tvItemName.setText("商品名称：" + order.getItem_name());
            tvOrderNo.setText("订单编号：" + order.getOrder_no());
            tvPointsCost.setText("消耗积分：" + order.getPoints_cost());

            // 优化时间格式
            tvCreateTime.setText("兑换时间：" + formatTime(order.getCreate_time()));

            // 处理状态显示和二维码按钮逻辑
            int status = order.getStatus();
            if (status == 0) {
                tvStatus.setText("订单状态：待核验");
                tvStatus.setTextColor(Color.parseColor("#FFA500"));

                // 只有待核验状态才显示获取二维码按钮
                tvGetQrCode.setVisibility(View.VISIBLE);
                tvGetQrCode.setOnClickListener(v -> {
                    String verifyCode = order.getVerify_code();
                    if (verifyCode != null && !verifyCode.isEmpty()) {
                        showVerifyCodeDialog(verifyCode, order.getItem_name());
                    } else {
                        Utils.showResponse("未找到核验码数据");
                    }
                });

            } else if (status == 1) {
                tvStatus.setText("订单状态：已核验");
                tvStatus.setTextColor(Color.parseColor("#008000"));
                tvGetQrCode.setVisibility(View.GONE);
            } else {
                tvStatus.setText("订单状态：已取消");
                tvStatus.setTextColor(Color.parseColor("#FF0000"));
                tvGetQrCode.setVisibility(View.GONE);
            }

            // 加载图片
            Glide.with(this)
                    .load(order.getItem_image())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivImage);
        }

        dialog.show();
    }


    // 弹出包含二维码和核验码的核验凭证弹窗
    private void showVerifyCodeDialog(String verifyCode, String itemName) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 40, 50, 40);

        // 顶部提示文字
        TextView tvTip = new TextView(getContext());
        tvTip.setText("请向管理员出示此二维码或核验码\n领取【" + itemName + "】");
        tvTip.setTextSize(16);
        tvTip.setGravity(Gravity.CENTER);
        tvTip.setTextColor(Color.parseColor("#333333"));
        layout.addView(tvTip);

        // 二维码 ImageView
        ImageView ivQrCode = new ImageView(getContext());
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
                .positiveText("关闭")
                .show();
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


    public static String formatTime(Object timeObj) {
        if (timeObj == null) return "";

        // 如果后端直接传的是 String
        if (timeObj instanceof String) {
            String timeStr = (String) timeObj;
            if (timeStr.contains("T")) {
                // 处理 2026-03-14T12:51:50
                return timeStr.replace("T", " ").substring(0, 16);
            }
            // 兼容可能已经是格式化好的字符串
            if (timeStr.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                if (timeStr.length() >= 16) {
                    return timeStr.substring(0, 16);
                }
            }
            return timeStr;
        }

        if (timeObj instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
            return sdf.format((Date) timeObj);
        }

        return String.valueOf(timeObj);
    }

    private void loadOrders(String keyword) {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;
        ApiService apiService = RetrofitClient.getInstance().getApi();
        apiService.getMyOrders(user.getId() ,keyword).enqueue(new Callback<Result<List<ExchangeOrder>>>() {
            @Override
            public void onResponse(Call<Result<List<ExchangeOrder>>> call, Response<Result<List<ExchangeOrder>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 0) {
                    orderList.clear();
                    orderList.addAll(response.body().getData());
                    if(orderList.isEmpty()){
                        Utils.showResponse("没有订单");
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Utils.showResponse("获取订单失败");
                }
            }

            @Override
            public void onFailure(Call<Result<List<ExchangeOrder>>> call, Throwable t) {
                Utils.showResponse("网络错误");
            }
        });
    }

    private void deleteOrder(ExchangeOrder order) {
        ApiService apiService = RetrofitClient.getInstance().getApi();
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;
        apiService.deleteOrder(order.getId(), user.getId()).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 0) {
                    Utils.showResponse("删除成功");
                    orderList.remove(order);
                    adapter.notifyDataSetChanged();
                } else {
                    Utils.showResponse("删除失败");
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Utils.showResponse("网络错误");
            }
        });
    }
}
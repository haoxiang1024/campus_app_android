package com.hx.campus.fragment.navigation;

import static com.xuexiang.xutil.XUtil.runOnUiThread;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.LostFoundType;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentAddFoundBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.LoadingDialog;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.imageview.ImageLoader;
import com.xuexiang.xui.widget.toast.XToast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;

/**
 * AddFoundFragment 类用于处理用户发布招领信息的功能。
 */
@Page
public class AddFoundFragment extends BaseFragment<FragmentAddFoundBinding> {
    public static final int CHOOSE_PHOTO = 1;
    public static final int STORAGE_PERMISSION = 1;
    private static final int LOCATION_PERMISSION = 100; // 新增定位权限码

    int id = 0; // 分类id
    private File file = null;
    private String fileName = "";
    private String foundJson;
    private String foundTitleEditValue;
    private String contentEditValue;
    private String locationEditValue;
    private String result;
    LoadingDialog loadingDialog;
    //经纬度
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private List<LostFoundType> categoryList = new ArrayList<>();
    private ArrayAdapter<LostFoundType> categoryAdapter;

    private LocationClient mLocationClient;
    private final MyLocationListener mListener = new MyLocationListener();


    @NonNull
    @Override
    protected FragmentAddFoundBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentAddFoundBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        initData();
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.send_found_info);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // 选择图片按钮点击事件
        binding.chooseImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            } else {
                chooseImage();
            }
        });

        // 提交按钮点击事件
        binding.btnSubmitLost.setOnClickListener(v -> {
            if (id == 0) {
                XToast.warning(getContext(), "请稍后，正在获取分类信息...").show();
                return;
            }
            showLoadingDialog();

            User user = Utils.getBeanFromSp(getContext(), "User", "user");
            Date date = new Date();
            String state = "待审核";
            int stick = 0;
            Integer userId = user.getId();
            String phone = user.getPhone();

            foundTitleEditValue = binding.etLostTitle.getEditValue();
            contentEditValue = binding.addContent.getEditValue();
            locationEditValue = binding.etLocation.getEditValue();

            LostFound lostFound=new LostFound(foundTitleEditValue, "", date, contentEditValue, locationEditValue, phone, state, stick, id, userId);
            lostFound.setType("招领");
            //添加经纬度
            lostFound.setLatitude(currentLat);
            lostFound.setLongitude(currentLng);
            foundJson = JSON.toJSONString(lostFound);

            if (file == null) {
                hideLoadingDialog();
                result = Utils.getString(getContext(),R.string.no_image_selected_yet);
                showResponse(result);
            } else if (TextUtils.isEmpty(foundTitleEditValue.trim())) {
                hideLoadingDialog();
                result = Utils.getString(getContext(),R.string.title_not_empty);
                showResponse(result);
            } else if (TextUtils.isEmpty(contentEditValue.trim())) {
                hideLoadingDialog();
                result = Utils.getString(getContext(),R.string.content_not_empty);
                showResponse(result);
            } else if (TextUtils.isEmpty(locationEditValue.trim())) {
                hideLoadingDialog();
                result = Utils.getString(getContext(),R.string.location_not_empty);
                showResponse(result);
            } else {
                addLostFound(foundJson);
            }
        });

        binding.btnGetLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_PHONE_STATE
                        },
                        LOCATION_PERMISSION);
            } else {
                startLocation();
            }
        });
    }

    private void startLocation() {
        showLoadingDialog();
        try {
            mLocationClient = new LocationClient(requireContext().getApplicationContext());
            mLocationClient.registerLocationListener(mListener);

            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll");
            option.setScanSpan(0);         // 只定位一次
            option.setIsNeedAddress(true); // 需要地址
            option.setIsNeedLocationPoiList(true); // 允许获取周边兴趣点
            option.setOpenGps(true);
            option.setIgnoreKillProcess(false);

            mLocationClient.setLocOption(option);
            mLocationClient.start();

        } catch (Exception e) {
            hideLoadingDialog();
            e.printStackTrace();
            XToast.error(getContext(), "定位初始化失败：" + e.getMessage()).show();
        }
    }

    // 百度定位回调
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            hideLoadingDialog();

            if (bdLocation == null) {
                XToast.error(getContext(), "定位失败：返回数据为空").show();
                return;
            }

            int code = bdLocation.getLocType();
            if (code == BDLocation.TypeGpsLocation || code == BDLocation.TypeNetWorkLocation) {
                // 获取并保存经纬度
                currentLat = bdLocation.getLatitude();
                currentLng = bdLocation.getLongitude();
                // 定位成功
                String addr = bdLocation.getAddrStr();
                // 获取周边 POI 列表
                List<com.baidu.location.Poi> poiList = bdLocation.getPoiList();
                if (poiList != null && !poiList.isEmpty()) {
                    // 如果有周边建筑，弹出列表让用户选一个更准的
                    List<String> poiNames = new ArrayList<>();
                    for (com.baidu.location.Poi p : poiList) {
                        poiNames.add(p.getName());
                    }
                    new MaterialDialog.Builder(getContext())
                            .title("请选择具体位置")
                            .items(poiNames)
                            .itemsCallback((dialog, itemView, position, text) -> {
                                // 将“大地址 + 具体建筑”合并填入
                                binding.etLocation.setText(addr + "（" + text + "）");
                            })
                            .positiveText("就用当前位置")
                            .onPositive((dialog, which) -> binding.etLocation.setText(addr))
                            .show();
                } else {
                    // 没有周边信息时直接填入基础地址
                    binding.etLocation.setText(addr);
                }
            } else {
                XToast.error(getContext(), "定位失败，错误码：" + code).show();
                Log.e("百度定位", "错误码：" + code + " 信息：" + bdLocation.getLocTypeDescription());
            }

            // 定位一次就停止
            if (mLocationClient != null) {
                mLocationClient.stop();
            }
        }
    }

    /**
     * 启动系统相册以选择图片。
     */
    private void chooseImage() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /**
     * 上传图片和招领信息到服务器。
     *
     * @param foundJson 招领信息的 JSON 字符串
     */
    private void addLostFound(String foundJson) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("upload_file", file.getName(), requestFile);
        RetrofitClient.getInstance()
                .getApi()
                .addLostFound(filePart,foundJson)
                .enqueue(new retrofit2.Callback<Result<List<LostFound>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<Result<List<LostFound>>> call, retrofit2.Response<Result<List<LostFound>>> response) {
                        hideLoadingDialog();
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 0) {
                                showResponse(response.body().getMsg());

                                List<LostFound> matchData = response.body().getData();
                                if (matchData != null && !matchData.toString().equals("[]")) {
                                    List<LostFound> matchList = JSON.parseArray(JSON.toJSONString(matchData), LostFound.class);
                                    if (matchList != null && !matchList.isEmpty()) {
                                        runOnUiThread(() -> showMatchDialog(matchList, response.body().getMsg()));
                                    } else {
                                        runOnUiThread(() -> clearUI());
                                    }
                                } else {
                                    runOnUiThread(() -> clearUI());
                                }
                            } else {
                                // 状态码不为0时的错误提示
                                showResponse(response.body().getMsg());
                                runOnUiThread(() -> clearUI());
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Result<List<LostFound>>> call, Throwable t) {
                        hideLoadingDialog();
                        t.printStackTrace();
                        showResponse("网络异常，请稍后再试");
                    }
                });
    }

    private void showMatchDialog(List<LostFound> matchList, String msg) {
        if (getContext() == null) return;

        MatchAggregateAdapter adapter = new MatchAggregateAdapter(matchList);

        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title("🤖 智能匹配助手")
                .content("发布成功！系统为您匹配到了以下疑似物品，点击即可查看详情：")
                .adapter(adapter, new LinearLayoutManager(getContext()))
                .positiveText("暂不需要")
                .onPositive((d, which) -> clearUI())
                .cancelable(false)
                .show();

        adapter.setDialog(dialog);

        // 限制弹窗高度，防止内容过多撑满全屏
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
            window.setAttributes(lp);
        }
    }

    /**
     * 在主线程中显示响应结果。
     *
     * @param response 响应结果字符串
     */
    private void showResponse(final String response) {
        runOnUiThread(() -> XToast.info(getContext(), response).show());
    }

    /**
     * 初始化数据，包括设置用户信息、分类适配器和获取分类列表。
     */
    private void initData() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        binding.authorName.setText(user.getNickname());
        binding.phone.setText(user.getPhone());
        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryList);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                LostFoundType selectedType = categoryAdapter.getItem(position);
                if (selectedType != null) {
                    id = selectedType.getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        fetchCategoriesFromNet();
    }

    /**
     * 从网络获取分类数据并更新 UI。
     */
    private void fetchCategoriesFromNet() {
        RetrofitClient.getInstance().getApi().getAllType().enqueue(new Callback<Result<List<LostFoundType>>>() {
            @Override
            public void onResponse(retrofit2.Call<Result<List<LostFoundType>>> call, retrofit2.Response<Result<List<LostFoundType>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<LostFoundType> data = response.body().getData();
                    if (data != null && !data.isEmpty()) {
                        categoryList.clear();
                        categoryList.addAll(data);
                        categoryAdapter.notifyDataSetChanged();
                        id = categoryList.get(0).getId();
                    }
                } else {
                    XToast.error(getContext(), "获取分类数据失败").show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Result<List<LostFoundType>>> call, Throwable t) {
                XToast.error(getContext(), "网络异常，无法获取分类").show();
            }
        });
    }

    /**
     * 处理权限请求的结果。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImage();
            } else {
                XToast.error(getContext(), "存储权限被拒绝").show();
            }
        } else if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation();
            } else {
                XToast.error(getContext(), "定位权限被拒绝").show();
            }
        }
    }

    /**
     * 处理活动结果，主要用于获取选择的图片路径。
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO && data != null) {
            binding.ivImage.setImageURI(data.getData());
            String realPath = Utils.getRealPath(getContext(), data);
            String[] temp = realPath.replaceAll("\\\\", "/").split("/");
            if (temp.length > 1) {
                fileName = temp[temp.length - 1];
            }
            file = new File(realPath);
        }
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void clearUI() {
        binding.etLostTitle.setText("");
        binding.addContent.setText("");
        binding.etLocation.setText("");

        if (!categoryList.isEmpty()) {
            binding.spinnerCategory.setSelection(0);
        }

        binding.ivImage.setImageDrawable(null);
        this.file = null;
        this.fileName = "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stop();
            mLocationClient.unRegisterLocationListener(mListener);
        }
    }

    // 弹窗相关辅助方法和适配器


    private int dpToPx(Context context, float dp) {
        if (context == null) return 0;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void showImagePreviewDialog(String imageUrl) {
        if (getContext() == null || imageUrl == null || imageUrl.isEmpty()) return;

        android.app.Dialog previewDialog = new android.app.Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        android.widget.ImageView fullImageView = new android.widget.ImageView(getContext());
        fullImageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        fullImageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        fullImageView.setBackgroundColor(0xFF000000);

        ImageLoader.get().loadImage(fullImageView, imageUrl);
        fullImageView.setOnClickListener(v -> previewDialog.dismiss());

        previewDialog.setContentView(fullImageView);

        Window window = previewDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        previewDialog.show();
    }

    private class MatchAggregateAdapter extends RecyclerView.Adapter<MatchAggregateAdapter.ViewHolder> {
        private List<LostFound> mData;
        private MaterialDialog mDialog;

        public MatchAggregateAdapter(List<LostFound> data) {
            this.mData = data;
        }

        public void setDialog(MaterialDialog dialog) {
            this.mDialog = dialog;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
            layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            int padding = dpToPx(context, 16);
            layout.setPadding(padding, padding, padding, padding);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvDesc = new TextView(context);
            tvDesc.setTextSize(15);
            tvDesc.setTextColor(0xFF333333);
            tvDesc.setLineSpacing(0, 1.2f);
            android.widget.LinearLayout.LayoutParams tvParams = new android.widget.LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            tvDesc.setLayoutParams(tvParams);

            com.xuexiang.xui.widget.imageview.RadiusImageView ivThumb = new com.xuexiang.xui.widget.imageview.RadiusImageView(context);
            int imgSize = dpToPx(context, 60);
            android.widget.LinearLayout.LayoutParams ivParams = new android.widget.LinearLayout.LayoutParams(imgSize, imgSize);
            ivParams.leftMargin = dpToPx(context, 12);
            ivThumb.setLayoutParams(ivParams);
            ivThumb.setCornerRadius(dpToPx(context, 6));
            ivThumb.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

            android.util.TypedValue outValue = new android.util.TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            ivThumb.setBackgroundResource(outValue.resourceId);

            layout.addView(tvDesc);
            layout.addView(ivThumb);

            return new ViewHolder(layout, tvDesc, ivThumb);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LostFound item = mData.get(position);
            String typeIcon = "招领".equals(item.getType()) ? "🎁" : "🔍";
            String formattedText = String.format("%s [%s] %s \n📍 %s",
                    typeIcon, item.getType(), item.getTitle(), item.getPlace());

            holder.tvDesc.setText(formattedText);

            if (item.getImg() != null && !item.getImg().isEmpty()) {
                holder.ivThumb.setVisibility(View.VISIBLE);
                ImageLoader.get().loadImage(holder.ivThumb, item.getImg());
                holder.ivThumb.setOnClickListener(v -> showImagePreviewDialog(item.getImg()));
            } else {
                holder.ivThumb.setVisibility(View.GONE);
                holder.ivThumb.setOnClickListener(null);
            }

            holder.itemView.setOnClickListener(v -> {
                if (mDialog != null) mDialog.dismiss();
                if ("招领".equals(item.getType())) {
                    openPage(FoundDetailFragment.class, FoundDetailFragment.KEY_FOUND, item);
                } else {
                    openPage(LostDetailFragment.class, LostDetailFragment.KEY_LOST, item);
                }
                clearUI();
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDesc;
            com.xuexiang.xui.widget.imageview.RadiusImageView ivThumb;

            public ViewHolder(@NonNull View itemView, TextView tvDesc, com.xuexiang.xui.widget.imageview.RadiusImageView ivThumb) {
                super(itemView);
                this.tvDesc = tvDesc;
                this.ivThumb = ivThumb;
            }
        }
    }
}
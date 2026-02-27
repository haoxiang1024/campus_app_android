package com.hx.campus.fragment.navigation;

import static com.xuexiang.xutil.XUtil.runOnUiThread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
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
 * 该类继承自 BaseFragment，负责初始化界面、监听事件、上传图片和提交招领信息。
 */
@Page
public class AddFoundFragment extends BaseFragment<FragmentAddFoundBinding> {
    public static final int CHOOSE_PHOTO = 1;
    public static final int STORAGE_PERMISSION = 1;

    int id = 0; // 分类id
    private File file = null;
    private String fileName = "";
    private String foundJson;
    private String foundTitleEditValue;
    private String contentEditValue;
    private String locationEditValue;
    private String result;
    LoadingDialog loadingDialog;

    private List<LostFoundType> categoryList = new ArrayList<>();
    private ArrayAdapter<LostFoundType> categoryAdapter;

    /**
     * 创建并返回 FragmentAddFoundBinding 实例。
     *
     * @param inflater     LayoutInflater 对象，用于解析布局文件
     * @param container    ViewGroup 容器，用于承载视图
     * @param attachToRoot 是否将视图附加到根容器
     * @return FragmentAddFoundBinding 绑定对象
     */
    @NonNull
    @Override
    protected FragmentAddFoundBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentAddFoundBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化视图组件。
     */
    @Override
    protected void initViews() {
        initData();
    }

    /**
     * 获取页面标题。
     *
     * @return 页面标题字符串
     */
    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.send_found_info);
    }

    /**
     * 初始化事件监听器。
     * 包括选择图片按钮点击事件和提交按钮点击事件。
     */
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
                upload(foundJson);
            }
        });
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
    private void upload(String foundJson) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("upload_file", file.getName(), requestFile);
        RequestBody opPart = RequestBody.create(MediaType.parse("text/plain"), "招领");
        RequestBody foundJsonPart = RequestBody.create(MediaType.parse("text/plain"), foundJson);
        RequestBody lostJsonPart = RequestBody.create(MediaType.parse("text/plain"), "");

        RetrofitClient.getInstance()
                .getApi()
                .addLostFound(filePart, lostJsonPart, foundJsonPart, opPart)
                .enqueue(new retrofit2.Callback<Result<String>>() {
                    @Override
                    public void onResponse(retrofit2.Call<Result<String>> call, retrofit2.Response<Result<String>> response) {
                        hideLoadingDialog();
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 0) {
                                showResponse(response.body().getMsg());
                                runOnUiThread(() -> clearUI());
                            } else {
                                showResponse( response.body().getMsg());
                                runOnUiThread(() -> clearUI());
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Result<String>> call, Throwable t) {
                        hideLoadingDialog();
                        t.printStackTrace();
                        showResponse("网络异常，请稍后再试");
                    }
                });
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
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 授权结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImage();
            } else {
                XToast.error(getContext(), "你还没有申请权限");
            }
        }
    }

    /**
     * 处理活动结果，主要用于获取选择的图片路径。
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的数据
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

    /**
     * 显示加载对话框。
     */
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        loadingDialog.show();
    }

    /**
     * 隐藏加载对话框。
     */
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * 清除界面中的输入内容和图片。
     */
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
}

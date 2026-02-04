package com.hx.campus.fragment.navigation;

import static com.xuexiang.xutil.XUtil.runOnUiThread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentAddLostBinding;
import com.hx.campus.utils.LoadingDialog;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.toast.XToast;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class AddLostFragment extends BaseFragment<FragmentAddLostBinding> {

    public static final int CHOOSE_PHOTO = 1;
    public static final int STORAGE_PERMISSION = 1;

    int id = 0; // 分类id
    private File file = null; // 用于上传图片文件
    private String fileName = "";
    private String lostJson; // 丢失信息json
    private String lostTitleEditValue; // 标题
    private String contentEditValue; // 内容
    private String locationEditValue; // 地点
    private String result;
    LoadingDialog loadingDialog; // 加放动画

    @NonNull
    @Override
    protected FragmentAddLostBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentAddLostBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        initData();
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.send_lost_info);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // 图片选择
        binding.chooseImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            } else {
                chooseImage();
            }
        });

        // 提交逻辑
        binding.btnSubmitLost.setOnClickListener(v -> {
            // 校验分类 ID 是否获取成功
            if (id == 0) {
                XToast.warning(getContext(), "请稍后，分类信息正在加载...").show();
                return;
            }

            showLoadingDialog();
            User user = Utils.getBeanFromSp(getContext(), "User", "user");

            Date date = new Date();
            String state = "待审核"; // 丢失状态默认为寻找中
            int stick = 0;
            Integer userId = user.getId();
            String phone = user.getPhone();

            lostTitleEditValue = binding.etLostTitle.getEditValue();
            contentEditValue = binding.addContent.getEditValue();
            locationEditValue = binding.etLocation.getEditValue();

            // 构造对象 (复用 LostFound 实体类)
            LostFound lostFound = new LostFound(lostTitleEditValue, "", date, contentEditValue, locationEditValue, phone, state, stick, id, userId);
            lostFound.setType("失物");

            lostJson = JSON.toJSONString(lostFound);

            // 输入校验
            if (file == null) {
                hideLoadingDialog();
                showResponse(Utils.getString(getContext(), R.string.no_image_selected_yet));
            } else if (TextUtils.isEmpty(lostTitleEditValue.trim())) {
                hideLoadingDialog();
                showResponse(Utils.getString(getContext(), R.string.title_not_empty));
            } else if (TextUtils.isEmpty(contentEditValue.trim())) {
                hideLoadingDialog();
                showResponse(Utils.getString(getContext(), R.string.content_not_empty));
            } else if (TextUtils.isEmpty(locationEditValue.trim())) {
                hideLoadingDialog();
                showResponse(Utils.getString(getContext(), R.string.location_not_empty));
            } else if (binding.radioGroup.getCheckedRadioButtonId() == -1) {
                hideLoadingDialog();
                showResponse(Utils.getString(getContext(), R.string.category_not_selected));
            } else {
                upload(lostJson);
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    // 使用 Retrofit 上传
    private void upload(String lostJson) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("upload_file", file.getName(), requestFile);

        RequestBody opPart = RequestBody.create(MediaType.parse("text/plain"), "失物");
        RequestBody lostJsonPart = RequestBody.create(MediaType.parse("text/plain"), lostJson);
        RequestBody foundJsonPart = RequestBody.create(MediaType.parse("text/plain"), "");

        RetrofitClient.getInstance()
                .getApi()
                .addLostFound(filePart, lostJsonPart, foundJsonPart, opPart)
                .enqueue(new retrofit2.Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        hideLoadingDialog();
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isSuccess()) {
                                showResponse(Utils.getString(getContext(), R.string.send_su));
                                runOnUiThread(() -> clearUI());
                            } else {
                                showResponse("提交失败：" + response.body().getMsg());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        hideLoadingDialog();
                        Log.e("AddLost", "网络异常", t);
                        showResponse("网络异常，请稍后再试");
                    }
                });
    }

    private void showResponse(final String response) {
        runOnUiThread(() -> XToast.info(getContext(), response).show());
    }

    private void initData() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        binding.authorName.setText(user.getNickname());
        binding.phone.setText(user.getPhone());

        String[] types = getResources().getStringArray(R.array.type_titles);
        setRadioBtn(Arrays.asList(types));

        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = group.findViewById(checkedId);
            if (radioButton != null) {
                String name = radioButton.getText().toString();
                getIdByName(name);
            }
        });
    }

    private void getIdByName(String name) {
        RetrofitClient.getInstance().getApi().getTypeid(name).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    String data = response.body().getData();
                    id = Integer.parseInt(data);
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Log.e("AddLost", "获取分类ID失败", t);
            }
        });
    }

    private void setRadioBtn(List<String> list) {
        binding.radioGroup.removeAllViews();
        for (String typeName : list) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(typeName);
            binding.radioGroup.addView(radioButton);
        }
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO && data != null) {
            binding.ivImage.setImageURI(data.getData());
            String realPath = Utils.getRealPath(getContext(), data);
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
        //  清空所有输入框
        binding.etLostTitle.setText("");    // 标题
        binding.addContent.setText("");     // 内容
        binding.etLocation.setText("");    // 地点

        //  重置单选按钮（分类）
        binding.radioGroup.clearCheck();
        this.id = 0; // 记得重置保存的分类ID变量

        // 重置图片预览和文件对象
        binding.ivImage.setImageDrawable(null);// 替换为你项目的默认占位图
        this.file = null;
        this.fileName = "";
    }
}
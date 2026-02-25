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
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;

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

        binding.chooseImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            } else {
                chooseImage();
            }
        });

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

    private void chooseImage() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

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
                            if (response.body().isSuccess()) {
                                showResponse(Utils.getString(getContext(), R.string.send_su));
                                runOnUiThread(() -> clearUI());
                            } else {
                                showResponse("提交失败：" + response.body().getMsg());
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

    private void showResponse(final String response) {
        runOnUiThread(() -> XToast.info(getContext(), response).show());
    }

    private void initData() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        binding.authorName.setText(user.getNickname());
        binding.phone.setText(user.getPhone());

        String[] types = getResources().getStringArray(R.array.type_titles);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, types);
        binding.spinnerCategory.setAdapter(adapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = types[position];
                getIdByName(selectedName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void getIdByName(String name) {
        RetrofitClient.getInstance().getApi().getTypeid(name).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(retrofit2.Call<Result<String>> call, retrofit2.Response<Result<String>> response) {
                if(response.body() != null && response.body().isSuccess()){
                    String data=response.body().getData();
                    id = Integer.parseInt(data);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Result<String>> call, Throwable t) {
                Utils.showResponse("网络异常");
            }
        });
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

        binding.spinnerCategory.setSelection(0);

        binding.ivImage.setImageDrawable(null);
        this.file = null;
        this.fileName = "";
    }
}
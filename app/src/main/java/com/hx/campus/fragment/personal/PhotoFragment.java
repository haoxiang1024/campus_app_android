package com.hx.campus.fragment.personal;

import static com.hx.campus.core.webview.AgentWebFragment.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.hx.campus.R;
import com.hx.campus.activity.MainActivity;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentPhotoBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.toast.XToast;

import java.io.File;

import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.UserInfo;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class PhotoFragment extends BaseFragment<FragmentPhotoBinding> {
    public static final int CHOOSE_PHOTO = 1;
    public static final int STORAGE_PERMISSION = 1;
    private File mFile = null;
    private String mFileName = "";

    @NonNull
    @Override
    protected FragmentPhotoBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentPhotoBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.update_photo);
    }

    @Override
    protected void initViews() {
        initAc();
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        // 选择图片
        binding.chooseimg.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            } else {
                chooseImage();
            }
        });

        // 上传图片
        binding.uploadimg.setOnClickListener(v -> {
            if (mFile == null) {
                XToast.info(getContext(), Utils.getString(getContext(), R.string.no_image_selected_yet)).show();
            } else {
                upload();
            }
        });
    }

    private void initAc() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user != null && !TextUtils.isEmpty(user.getPhoto())) {
            binding.rivHeadPic.setVisibility(View.VISIBLE);
            Glide.with(this).load(user.getPhoto()).into(binding.rivHeadPic);
        } else {
            binding.rivHeadPic.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImage();
            } else {
                XToast.error(getContext(), "请授予存储权限以选择图片").show();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private void upload() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), mFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("upload_file", mFileName, requestFile);

        RetrofitClient.getInstance().getApi().updatePhoto(body, user.getId()).enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(@NonNull Call<Result<User>> call, @NonNull Response<Result<User>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    Result<User> result = response.body();
                    if (result.isSuccess()) {
                        // 更新本地缓存
                        Utils.doUserData(result.getData());
                        User user = Utils.getBeanFromSp(getContext(), "User", "user");
                        //IM刷新
                        UserInfo userInfo = new UserInfo(
                                String.valueOf(user.getId()),
                                user.getNickname(),
                                Uri.parse(user.getPhoto())
                        );
                        RongUserInfoManager.getInstance().refreshUserInfoCache(userInfo);
                        // 显示成功提示
                        XToast.success(getContext(), "修改头像成功！").show();

                        // 延迟跳转，确保提示可见
                        binding.getRoot().postDelayed(() -> {
                            if (isAdded()) {
                                startActivity(new Intent(getContext(), MainActivity.class));
                            }
                        }, 800);
                    } else {
                        XToast.error(getContext(), result.getMsg()).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<User>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "上传失败: " + t.getMessage());
                    XToast.error(getContext(), "网络异常，上传失败").show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO && resultCode == getActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();
            binding.rivHeadPic.setImageURI(uri);

            String realPath = Utils.getRealPath(getContext(), data);
            if (realPath != null) {
                mFile = new File(realPath);
                mFileName = mFile.getName();
            }
        }
    }
}
package com.hx.campus.fragment.navigation;

import static com.xuexiang.xutil.XUtil.runOnUiThread;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
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
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.LostFoundType;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentAddLostBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.LoadingDialog;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.toast.XToast;

import java.io.File;
import java.util.ArrayList;
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
    private static final int LOCATION_PERMISSION = 100;

    int id = 0;
    private File file = null;
    private String fileName = "";
    private String lostJson;
    private String lostTitleEditValue;
    private String contentEditValue;
    private String locationEditValue;
    private String result;
    LoadingDialog loadingDialog;

    private double currentLat = 0.0;
    private double currentLng = 0.0;

    private List<LostFoundType> categoryList = new ArrayList<>();
    private ArrayAdapter<LostFoundType> categoryAdapter;

    private LocationClient mLocationClient;
    private final MyLocationListener mListener = new MyLocationListener();

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

        binding.chooseImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
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

            lostTitleEditValue = binding.etLostTitle.getEditValue();
            contentEditValue = binding.addContent.getEditValue();
            locationEditValue = binding.etLocation.getEditValue();

            LostFound lostFound = new LostFound(lostTitleEditValue, "", date, contentEditValue,
                    locationEditValue, phone, state, stick, id, userId);
            lostFound.setType("失物");
            lostFound.setLatitude(currentLat);
            lostFound.setLongitude(currentLng);
            lostJson = JSON.toJSONString(lostFound);

            if (file == null) {
                hideLoadingDialog();
                result = Utils.getString(getContext(), R.string.no_image_selected_yet);
                showResponse(result);
            } else if (TextUtils.isEmpty(lostTitleEditValue.trim())) {
                hideLoadingDialog();
                result = Utils.getString(getContext(), R.string.title_not_empty);
                showResponse(result);
            } else if (TextUtils.isEmpty(contentEditValue.trim())) {
                hideLoadingDialog();
                result = Utils.getString(getContext(), R.string.content_not_empty);
                showResponse(result);
            } else if (TextUtils.isEmpty(locationEditValue.trim())) {
                hideLoadingDialog();
                result = Utils.getString(getContext(), R.string.location_not_empty);
                showResponse(result);
            } else {
                upload(lostJson);
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
            option.setScanSpan(0);
            option.setIsNeedAddress(true);
            option.setIsNeedLocationPoiList(true);
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
                currentLat = bdLocation.getLatitude();
                currentLng = bdLocation.getLongitude();
                String addr = bdLocation.getAddrStr();
                List<com.baidu.location.Poi> poiList = bdLocation.getPoiList();
                if (poiList != null && !poiList.isEmpty()) {
                    List<String> poiNames = new ArrayList<>();
                    for (com.baidu.location.Poi p : poiList) {
                        poiNames.add(p.getName());
                    }
                    new MaterialDialog.Builder(getContext())
                            .title("请选择具体位置")
                            .items(poiNames)
                            .itemsCallback((dialog, itemView, position, text) -> {
                                binding.etLocation.setText(addr + "（" + text + "）");
                            })
                            .positiveText("就用当前位置")
                            .onPositive((dialog, which) -> binding.etLocation.setText(addr))
                            .show();
                } else {
                    binding.etLocation.setText(addr);
                }
            } else {
                XToast.error(getContext(), "定位失败，错误码：" + code).show();
                Log.e("百度定位", "错误码：" + code + " 信息：" + bdLocation.getLocTypeDescription());
            }

            if (mLocationClient != null) {
                mLocationClient.stop();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private void upload(String lostJson) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("upload_file", file.getName(), requestFile);

        RequestBody opPart = RequestBody.create(MediaType.parse("text/plain"), "失物");
        RequestBody lostJsonPart = RequestBody.create(MediaType.parse("text/plain"), lostJson);
        RequestBody foundJsonPart = RequestBody.create(MediaType.parse("text/plain"), "");

        RetrofitClient.getInstance().getApi().addLostFound(filePart, lostJsonPart, foundJsonPart, opPart)
                .enqueue(new Callback<Result<List<LostFound>>>() {
                    @Override
                    public void onResponse(Call<Result<List<LostFound>>> call, Response<Result<List<LostFound>>> response) {
                        hideLoadingDialog();
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 0) {
                                // 统一在这里提示一次即可
                                showResponse(response.body().getMsg());

                                List<LostFound> matchData = response.body().getData();
                                if (matchData != null && !matchData.toString().equals("[]")) {
                                    List<LostFound> matchList = JSON.parseArray(JSON.toJSONString(matchData), LostFound.class);
                                    if (matchList != null && !matchList.isEmpty()) {
                                        runOnUiThread(() -> showMatchDialog(matchList, response.body().getMsg()));
                                    } else {
                                        runOnUiThread(AddLostFragment.this::clearUI);
                                    }
                                } else {
                                    runOnUiThread(AddLostFragment.this::clearUI);
                                }
                            } else {
                                showResponse(response.body().getMsg());
                                runOnUiThread(AddLostFragment.this::clearUI);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<List<LostFound>>> call, Throwable t) {
                        hideLoadingDialog();
                        showResponse("网络异常，请稍后再试");
                    }
                });
    }

    private void showMatchDialog(List<LostFound> matchList, String msg) {
        List<String> displayItems = new ArrayList<>();
        for (LostFound item : matchList) {
            String typeIcon = "招领".equals(item.getType()) ? "🎁" : "🔍";
            String formattedText = String.format("%s [%s] %s    📍 %s",
                    typeIcon, item.getType(), item.getTitle(), item.getPlace());
            displayItems.add(formattedText);
        }
        new MaterialDialog.Builder(getContext())
                .title("🤖 智能匹配助手")
                .content("发布成功！系统为您匹配到了以下疑似物品，点击即可查看详情：")
                .items(displayItems)
                .itemsCallback((dialog, itemView, position, text) -> {
                    LostFound selected = matchList.get(position);
                    if ("招领".equals(selected.getType())) {
                        openPage(FoundDetailFragment.class, FoundDetailFragment.KEY_FOUND, selected);
                    } else {
                        openPage(LostDetailFragment.class, LostDetailFragment.KEY_LOST, selected);
                    }
                    clearUI();
                })
                .positiveText("暂不需要")
                .onPositive((dialog, which) -> clearUI())
                .cancelable(false)
                .show();
    }

    private void showResponse(final String response) {
        runOnUiThread(() -> XToast.info(getContext(), response).show());
    }

    private void initData() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        binding.authorName.setText(user.getNickname());
        binding.phone.setText(user.getPhone());

        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryList);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LostFoundType type = categoryAdapter.getItem(position);
                if (type != null) {
                    AddLostFragment.this.id = type.getId();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fetchCategoriesFromNet();
    }

    private void fetchCategoriesFromNet() {
        RetrofitClient.getInstance().getApi().getAllType().enqueue(new Callback<Result<List<LostFoundType>>>() {
            @Override
            public void onResponse(Call<Result<List<LostFoundType>>> call, Response<Result<List<LostFoundType>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<LostFoundType> data = response.body().getData();
                    if (data != null && !data.isEmpty()) {
                        categoryList.clear();
                        categoryList.addAll(data);
                        categoryAdapter.notifyDataSetChanged();
                        id = categoryList.get(0).getId();
                    }
                } else {
                    XToast.error(getContext(), "获取分类失败").show();
                }
            }
            @Override
            public void onFailure(Call<Result<List<LostFoundType>>> call, Throwable t) {
                XToast.error(getContext(), "网络异常").show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO && data != null) {
            binding.ivImage.setImageURI(data.getData());
            String path = Utils.getRealPath(getContext(), data);
            file = new File(path);
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
        file = null;
        fileName = "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stop();
            mLocationClient.unRegisterLocationListener(mListener);
        }
    }
}
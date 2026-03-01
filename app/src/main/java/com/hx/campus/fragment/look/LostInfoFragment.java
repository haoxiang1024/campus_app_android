package com.hx.campus.fragment.look;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.lostfound.LostFoundDetailAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentLostInfoBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 显示失物信息页面 - 展示用户发布的失物信息列表
@Page()
public class LostInfoFragment extends BaseFragment<FragmentLostInfoBinding> {
    // 失物招领详情适配器，用于显示失物信息列表
    private LostFoundDetailAdapter adapter;

    /**
     * 创建视图绑定对象
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentLostInfoBinding 视图绑定实例
     */
    @NonNull
    @Override
    protected FragmentLostInfoBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        // 使用FragmentLostInfoBinding inflate方法创建绑定对象
        return FragmentLostInfoBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化视图组件
     * 启动加载动画并初始化数据
     */
    @Override
    protected void initViews() {
        // 显示加载动画，提升用户体验
        startAnim();
        // 初始化列表数据和适配器
        initData();
    }

    /**
     * 获取页面标题
     * @return String 页面标题字符串
     */
    @Override
    protected String getPageTitle() {
        // 从资源文件获取失物信息标题
        return getResources().getString(R.string.lost_info);
    }

    /**
     * 初始化事件监听器
     * 为列表项设置点击事件监听
     */
    @Override
    protected void initListeners() {
        // 调用父类监听器初始化方法
        super.initListeners();
        // 为ListView设置项点击监听器，跳转到失物详情页面
        binding.listview.setOnItemClickListener((parent, view, position, id) -> {
            // 获取被点击项的失物数据
            LostFound lost = adapter.getItem(position);
            // 跳转到失物详情页面并传递数据
            openPage(LostInfoDetailFragment.class, LostInfoDetailFragment.KEY_LOST, lost);
        });
    }

    /**
     * 初始化数据和适配器
     * 创建适配器并关联到ListView
     */
    private void initData() {
        // 创建失物招领详情适配器
        adapter = new LostFoundDetailAdapter(getContext());
        // 为ListView设置适配器
        binding.listview.setAdapter(adapter);
        // 发起网络请求获取数据
        getData();
    }

    /**
     * 获取失物数据
     * 通过Retrofit请求用户发布的失物信息
     */
    private void getData() {
        // 从SharedPreferences获取当前登录用户信息
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        // 用户未登录则停止加载动画并返回
        if (user == null) {
            stopAnim();
            return;
        }

        // 使用Retrofit发起网络请求获取该用户发布的所有失物招领数据
        RetrofitClient.getInstance().getApi()
                .getLostFoundListByUserId(user.getId())
                .enqueue(new Callback<Result<List<LostFound>>>() {
                    /**
                     * 网络请求成功响应处理
                     * @param call 请求调用对象
                     * @param response 响应结果
                     */
                    @Override
                    public void onResponse(Call<Result<List<LostFound>>> call, Response<Result<List<LostFound>>> response) {
                        // 停止加载动画
                        stopAnim();
                        // 检查响应是否成功且数据有效
                        if (response.isSuccessful() && response.body() != null) {
                            Result<List<LostFound>> result = response.body();
                            // 检查业务逻辑是否成功
                            if (result.isSuccess()) {
                                // 获取全部失物招领数据
                                List<LostFound> allData = result.getData();
                                // 手动过滤出类型为"失物"的数据
                                List<LostFound> lostList = new ArrayList<>();
                                if (allData != null) {
                                    // 遍历所有数据进行类型筛选
                                    for (LostFound item : allData) {
                                        if ("失物".equals(item.getType())) {
                                            lostList.add(item);
                                        }
                                    }
                                }
                                // 设置适配器显示筛选后的数据
                                setAdapter(lostList);
                            } else {
                                // 显示错误信息
                                Utils.showResponse(result.getMsg());
                            }
                        }
                    }

                    /**
                     * 网络请求失败处理
                     * @param call 请求调用对象
                     * @param t 异常信息
                     */
                    @Override
                    public void onFailure(Call<Result<List<LostFound>>> call, Throwable t) {
                        // 停止加载动画
                        stopAnim();
                        // 显示网络异常信息
                        Utils.showResponse(t.getMessage());
                    }
                });
    }

    /**
     * 设置适配器数据
     * @param list 失物数据列表
     */
    private void setAdapter(List<LostFound> list) {
        // 检查数据是否为空
        if (list == null || list.isEmpty()) {
            // 显示暂无信息发布提示
            Utils.showResponse(Utils.getString(getContext(), R.string.no_info_posted_yet));
            return;
        }
        // 为适配器设置数据，参数1表示失物类型
        adapter.setData(list, 1);
    }

    /**
     * 启动加载动画
     * 显示正在加载的视觉反馈
     */
    private void startAnim() {
        // 显示加载动画控件
        binding.avLoad.show();
    }

    /**
     * 停止加载动画
     * 隐藏加载动画控件
     */
    private void stopAnim() {
        // 隐藏加载动画控件
        binding.avLoad.hide();
    }
}
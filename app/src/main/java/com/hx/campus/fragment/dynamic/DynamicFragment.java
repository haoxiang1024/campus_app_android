package com.hx.campus.fragment.dynamic;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.hx.campus.R;
import com.hx.campus.adapter.base.broccoli.BroccoliSimpleDelegateAdapter;
import com.hx.campus.adapter.base.delegate.SimpleDelegateAdapter;
import com.hx.campus.adapter.base.delegate.SingleDelegateAdapter;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.NewInfo;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.core.webview.AgentWebActivity;
import com.hx.campus.databinding.FragmentNewsBinding;
import com.hx.campus.fragment.navigation.FoundDetailFragment;
import com.hx.campus.fragment.navigation.FoundFragment;
import com.hx.campus.fragment.navigation.LostDetailFragment;
import com.hx.campus.fragment.navigation.LostFragment;
import com.hx.campus.fragment.other.SearchFragment;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.DemoDataProvider;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;
import com.xuexiang.xui.adapter.simple.AdapterItem;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.imageview.ImageLoader;
import com.xuexiang.xui.widget.imageview.RadiusImageView;

import java.util.ArrayList;
import java.util.List;

import me.samlss.broccoli.Broccoli;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page(anim = CoreAnim.fade)
public class DynamicFragment extends BaseFragment<FragmentNewsBinding> {

    // 新闻信息适配器，用于显示推荐内容列表
    private SimpleDelegateAdapter<NewInfo> newInfoSimpleDelegateAdapter;
    // 新闻信息数据列表，存储从服务器获取的数据
    private List<NewInfo> list = new ArrayList<>();
    // 地图相关变量
    private com.baidu.mapapi.map.MapView mMapView;
    private BaiduMap mBaiduMap;
    private boolean isMapMode = false; // 当前是否处于地图模式
    // 新闻信息数据列表，存储从服务器获取的数据
   // private List<NewInfo> list = new ArrayList<>();
    // 新增：保存当前地图上的所有原始数据，用于点击 Marker 时进行位置聚合筛选
    private List<LostFound> currentMapDataList = new ArrayList<>();
    /**
     * 创建视图绑定对象
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentNewsBinding 视图绑定实例
     */
    @NonNull
    @Override
    protected FragmentNewsBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        // 使用视图绑定inflate方法创建绑定对象
        return FragmentNewsBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected TitleBar initTitle() {
        return null;
    }

    @Override
    protected void initViews() {
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(virtualLayoutManager);
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        binding.recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 10);

        // 轮播条适配器
        SingleDelegateAdapter bannerAdapter = createBannerAdapter();

        // 九宫格菜单适配器
        SimpleDelegateAdapter<AdapterItem> commonAdapter = createGridAdapter();

        // 标题适配器
        SingleDelegateAdapter titleAdapter = new SingleDelegateAdapter(R.layout.adapter_title_item) {
            @Override
            public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
                holder.text(R.id.tv_title, getResources().getString(R.string.recommendation));
            }
        };

        // 推荐内容适配器
        newInfoSimpleDelegateAdapter = new BroccoliSimpleDelegateAdapter<NewInfo>(R.layout.adapter_news_card_view_list_item, new LinearLayoutHelper(), DemoDataProvider.getEmptyNewInfo()) {
            @Override
            protected void onBindData(RecyclerViewHolder holder, NewInfo model, int position) {
                if (model != null) {
                    holder.text(R.id.tv_user_name, model.getUserName());
                    holder.text(R.id.tv_tag, model.getTag());
                    holder.text(R.id.tv_title, model.getTitle());
                    holder.text(R.id.tv_summary, model.getSummary());
                    holder.image(R.id.iv_image, model.getImageUrl());
                    holder.click(R.id.card_view, v -> {
                        NewInfo newInfo = getItem(position);
                        if (newInfo == null) return;
                        // 跳转详情逻辑
                        handleItemClick(newInfo);
                    });
                }
            }

            @Override
            public NewInfo getItem(int position) {
                return (position >= 0 && position < list.size()) ? list.get(position) : null;
            }

            @Override
            protected void onBindBroccoli(RecyclerViewHolder holder, Broccoli broccoli) {
                broccoli.addPlaceholders(holder.findView(R.id.tv_user_name), holder.findView(R.id.tv_tag),
                        holder.findView(R.id.tv_title), holder.findView(R.id.tv_summary), holder.findView(R.id.iv_image));
            }
        };

        DelegateAdapter delegateAdapter = new DelegateAdapter(virtualLayoutManager);
        delegateAdapter.addAdapter(bannerAdapter);
        delegateAdapter.addAdapter(commonAdapter);
        delegateAdapter.addAdapter(titleAdapter);
        delegateAdapter.addAdapter(newInfoSimpleDelegateAdapter);
        binding.recyclerView.setAdapter(delegateAdapter);

        mMapView = binding.bmapView;
        mBaiduMap = mMapView.getMap();
        // 点击右下角按钮：进入地图模式
        binding.fabSwitchMode.setOnClickListener(v -> {
            isMapMode = true;
            binding.refreshLayout.setVisibility(View.GONE);
            mMapView.setVisibility(View.VISIBLE);

            // 显示左上角返回按钮，隐藏右下角按钮
            binding.fabMapBack.setVisibility(View.VISIBLE);
            binding.llMapControls.setVisibility(View.VISIBLE);
            binding.fabSwitchMode.setVisibility(View.GONE);
        });

        // 点击左上角返回按钮：退出地图模式
        binding.fabMapBack.setOnClickListener(v -> {
            isMapMode = false;
            binding.refreshLayout.setVisibility(View.VISIBLE);
            mMapView.setVisibility(View.GONE);

            // 隐藏返回按钮，恢复显示右下角按钮
            binding.fabMapBack.setVisibility(View.GONE);
            binding.llMapControls.setVisibility(View.GONE);
            binding.fabSwitchMode.setVisibility(View.VISIBLE);
        });

        // 初始自动刷新数据
        binding.refreshLayout.autoRefresh();
    }

    /**
     * DP 转 PX 工具方法
     */
    private int dpToPx(Context context, float dp) {
        if (context == null) return 0;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 在地图上绘制标记点
     */
    private void showMarkersOnMap(List<LostFound> dataList) {
        if (mBaiduMap == null) return;
        mBaiduMap.clear(); // 刷新时清除旧点
        BitmapDescriptor lostIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker_lost);
        BitmapDescriptor foundIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker_found);

        boolean isFirstPoint = true; // 用于记录是否是第一个有效点，用来移动地图视角
        for (LostFound item : dataList) {
            double lat = item.getLatitude();
            double lng = item.getLongitude();
            // 获取经纬度
            if (item.getLatitude() == 0 && item.getLongitude() == 0) {
                continue;
            }
            if (lat > 90 || lat < -90) {
                Utils.showResponse("经纬度错误");
                continue; // 遇到非法坐标直接跳过，防止地图崩溃
            }
            LatLng point = new LatLng(item.getLatitude(), item.getLongitude());
            BitmapDescriptor currentIcon;
            if (item.getType().equals("失物")) {
                currentIcon = lostIcon;
            } else {
                currentIcon = foundIcon;
            }
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(currentIcon)
                    .title(item.getTitle());

            Marker marker = (Marker) mBaiduMap.addOverlay(option);

            Bundle bundle = new Bundle();
            bundle.putSerializable("info", item);
            marker.setExtraInfo(bundle);
            if (isFirstPoint) {
                // 15.0f 是缩放级别，越大看街道越清楚
                com.baidu.mapapi.map.MapStatusUpdate msu =
                        com.baidu.mapapi.map.MapStatusUpdateFactory.newLatLngZoom(point, 16.0f);
                mBaiduMap.animateMapStatus(msu); // 移动镜头
                isFirstPoint = false;
            }
        }

        // 设置地图 Marker 的点击事件
        mBaiduMap.setOnMarkerClickListener(marker -> {
            Bundle bundle = marker.getExtraInfo();
            if (bundle != null) {
                LostFound clickedItem = (LostFound) bundle.getSerializable("info");
                if (clickedItem != null) {
                    // 封装数据以便跳转
                    NewInfo tempInfo = new NewInfo(clickedItem.getType(), clickedItem.getTitle())
                            .setSummary(clickedItem.getContent())
                            .setUserName(clickedItem.getNickname())
                            .setState(clickedItem.getState())
                            .setPhone(clickedItem.getPhone())
                            .setPlace(clickedItem.getPlace())
                            .setPub_Date(clickedItem.getPubDate())
                            .setImageUrl(clickedItem.getImg())
                            .setUser_id(clickedItem.getUserId())
                            .setId(clickedItem.getId());

                    // 调用 XUI 风格的底部弹窗
                    showXUIBottomSheet(clickedItem, tempInfo);
                }
            }
            return true;
        });
    }
    private void showXUIBottomSheet(LostFound item, NewInfo newInfo) {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .customView(R.layout.dialog_bottom_marker_info, true)
                .build();

        View view = dialog.getCustomView();
        if (view != null) {
            // 绑定数据
            TextView tvTitle = view.findViewById(R.id.tv_title);
            TextView tvUserName = view.findViewById(R.id.tv_user_name);
            TextView tvSummary = view.findViewById(R.id.tv_summary);
            TextView tvPlace = view.findViewById(R.id.tv_place);
            RadiusImageView ivImage = view.findViewById(R.id.iv_marker_img);
            Button btnDetail = view.findViewById(R.id.btn_detail);
            tvTitle.setText(item.getTitle());
            tvUserName.setText("发布者：" + item.getNickname());
            tvSummary.setText(item.getContent());
            tvPlace.setText("📍 " + item.getPlace());
            ImageLoader.get().loadImage(ivImage, item.getImg());

            btnDetail.setOnClickListener(v -> {
                dialog.dismiss();
                handleItemClick(newInfo);
            });
        }
        Window window = dialog.getWindow();
        if (window != null) {
            // 设置从底部弹出
            window.setGravity(Gravity.BOTTOM);
            // 设置弹出动画
             window.setWindowAnimations(android.R.style.Animation_InputMethod);
            // 设置宽度全屏
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);

            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }
    @Override
    protected void initListeners() {
        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadTopListData();
        });

        // 上拉加载
        binding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            refreshLayout.finishLoadMore(1000);
        });
    }

    /**
     * 使用 Retrofit 加载置顶列表数据
     */
    private void loadTopListData() {
        RetrofitClient.getInstance().getApi().showTopList(1).enqueue(new Callback<Result<List<LostFound>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<List<LostFound>>> call, @NonNull Response<Result<List<LostFound>>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    List<LostFound> dataList = response.body().getData();
                    if (dataList != null) {
                        list.clear();
                        for (LostFound item : dataList) {
                            list.add(new NewInfo(item.getLostfoundtype().getName(), item.getTitle())
                                    .setSummary(item.getContent())
                                    .setUserName(item.getNickname())
                                    .setState(item.getState())
                                    .setPhone(item.getPhone())
                                    .setPlace(item.getPlace())
                                    .setPub_Date(item.getPubDate())
                                    .setImageUrl(item.getImg())
                                    .setUser_id(item.getUserId())
                                    .setId(item.getId())    );
                        }
                        // 刷新适配器数据
                        newInfoSimpleDelegateAdapter.refresh(list);
                        // 在地图上绘制标记点
                        showMarkersOnMap(dataList);
                    }
                } else {
                    Utils.showResponse("数据加载失败");
                }
                binding.refreshLayout.finishRefresh();
            }

            @Override
            public void onFailure(@NonNull Call<Result<List<LostFound>>> call, @NonNull Throwable t) {
                Utils.showResponse("网络异常");
                binding.refreshLayout.finishRefresh();
            }
        });
    }

    private void handleItemClick(NewInfo newInfo) {
        LostFound lostFound = new LostFound(newInfo.getTitle(), newInfo.getImageUrl(), newInfo.getPub_Date(), newInfo.getSummary(), newInfo.getPlace(), newInfo.getPhone(), newInfo.getState(), newInfo.getUserName());
        lostFound.setUserId(newInfo.getUser_id());
        lostFound.setId(newInfo.getId());
        if ("寻找中".equals(newInfo.getState())) {
            openNewPage(LostDetailFragment.class, LostDetailFragment.KEY_LOST, lostFound);
        } else {
            openNewPage(FoundDetailFragment.class, FoundDetailFragment.KEY_FOUND, lostFound);
        }
    }

    private SingleDelegateAdapter createBannerAdapter() {
        return new SingleDelegateAdapter(R.layout.include_head_view_banner) {

            @Override
            public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
                ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    holder.itemView.setLayoutParams(layoutParams);
                }

                SimpleImageBanner banner = holder.findViewById(R.id.sib_simple_usage);
                banner.setSource(DemoDataProvider.getBannerList())
                        .setOnItemClickListener((view, item, pos) -> {
                            String url="";
                            switch (pos){
                                case 0:
                                    url="/pages/notification.html";
                                    break;
                                case 1:
                                    url="/pages/contract.html";
                                    break;
                                case 2:
                                    url="/pages/appcrash.html";
                                    break;
                                case 3:
                                    url="/pages/privacy.html";
                                    break;
                                default:
                                    url="/pages/notification.html";
                                    break;
                            }
                            AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl(url, getContext()));
                        }).startScroll();
            }
        };
    }

    private SimpleDelegateAdapter<AdapterItem> createGridAdapter() {
        GridLayoutHelper helper = new GridLayoutHelper(4);
        helper.setPadding(0, 16, 0, 0);
        helper.setVGap(10);
        return new SimpleDelegateAdapter<AdapterItem>(R.layout.adapter_common_grid_item, helper, DemoDataProvider.getGridItems(getContext())) {
            @Override
            protected void bindData(@NonNull RecyclerViewHolder holder, int position, AdapterItem item) {
                RadiusImageView imageView = holder.findViewById(R.id.riv_item);
                imageView.setCircle(true);
                ImageLoader.get().loadImage(imageView, item.getIcon());
                String title = item.getTitle().toString();
                holder.text(R.id.tv_title, title.substring(0, 1));
                holder.text(R.id.tv_sub_title, title);
                holder.click(R.id.ll_container, v -> {
                    if (title.contains("失物")) openNewPage(LostFragment.class, LostFragment.KEY_TITLE_NAME, title);
                    else if (title.contains("招领")) openNewPage(FoundFragment.class, FoundFragment.KEY_TITLE_NAME, title);
                    else if (title.contains("搜索")) openNewPage(SearchFragment.class);
                    else if (title.contains("意见")) AgentWebActivity.goWeb(getContext(), Utils.rebuildUrl("/pages/contract.html", getContext()));
                });
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
        if (binding.refreshLayout != null) binding.refreshLayout.autoRefresh(50);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }
}
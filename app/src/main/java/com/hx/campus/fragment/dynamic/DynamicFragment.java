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
import androidx.recyclerview.widget.LinearLayoutManager;
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
import java.util.Locale;

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
    // 保存当前地图上的所有原始数据，用于点击 Marker 时进行位置聚合筛选
    private List<LostFound> currentMapDataList = new ArrayList<>();

    // 修复弹窗重叠：保存当前的弹窗实例
    private MaterialDialog mBottomSheetDialog;

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
        FragmentNewsBinding binding = FragmentNewsBinding.inflate(inflater, container, attachToRoot);
        if (binding != null && Utils.isEmulator()) {
            // 关键点：不仅要判断 bmapView，还要确保它有父容器才能 remove
            if (binding.bmapView != null) {
                View mapView = binding.bmapView;
                ViewGroup parent = (ViewGroup) mapView.getParent();
                if (parent != null) {
                    parent.removeView(mapView);
                    Log.d("HX_CAMPUS", "模拟器环境：已安全移除百度地图控件");
                }
            }
        }
        return binding;
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
        adapter(virtualLayoutManager);
        if (Utils.isEmulator()){
            binding.fabSwitchMode.setVisibility(View.GONE);
            binding.bmapView.setVisibility(View.GONE);
            Log.e("HX_CAMPUS", "模拟器环境");
        }else {
            baiduMap();
        }
        // 初始自动刷新数据
        binding.refreshLayout.autoRefresh();
    }

    private void adapter(VirtualLayoutManager virtualLayoutManager) {
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
    }

    private void baiduMap() {
        mMapView = binding.bmapView;
        mBaiduMap = mMapView.getMap();

        // 将 Marker 点击事件移到这里，生命周期内只绑定一次
        mBaiduMap.setOnMarkerClickListener(marker -> {
            Bundle bundle = marker.getExtraInfo();
            if (bundle != null) {
                LostFound clickedItem = (LostFound) bundle.getSerializable("info");
                if (clickedItem != null) {
                    showXUIBottomSheet(clickedItem);
                }
            }
            return true;
        });

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

        // 记录已经绘制过 Marker 的位置集合
        List<String> drawnPoints = new ArrayList<>();

        for (LostFound item : dataList) {
            double lat = item.getLatitude();
            double lng = item.getLongitude();

            // 获取经纬度验证
            if (lat == 0 && lng == 0) {
                continue;
            }
            if (lat > 90 || lat < -90) {
                Utils.showResponse("经纬度错误");
                continue;
            }

            // 保留 5 位小数精度（约1米左右），防止相同坐标重复添加 Marker
            String locKey = String.format(Locale.getDefault(), "%.5f,%.5f", lat, lng);
            if (drawnPoints.contains(locKey)) {
                continue; // 这个位置已经画过点了，直接跳过，反正弹窗时会聚合显示
            }
            drawnPoints.add(locKey);

            LatLng point = new LatLng(lat, lng);
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
                // 16.0f 是缩放级别，越大看街道越清楚
                com.baidu.mapapi.map.MapStatusUpdate msu =
                        com.baidu.mapapi.map.MapStatusUpdateFactory.newLatLngZoom(point, 16.0f);
                mBaiduMap.animateMapStatus(msu); // 移动镜头
                isFirstPoint = false;
            }
        }
    }

    /**
     * 弹出位置聚合列表信息
     */
    private void showXUIBottomSheet(LostFound clickedItem) {
        if (getContext() == null) return;

        // 如果当前已经有弹窗在显示，直接拦截
        if (mBottomSheetDialog != null && mBottomSheetDialog.isShowing()) {
            return;
        }

        // 聚合逻辑：找出地图上所有与当前点击位置相同（或极近）的物品
        List<LostFound> aggregatedList = new ArrayList<>();
        for (LostFound item : currentMapDataList) {
            if (Math.abs(item.getLatitude() - clickedItem.getLatitude()) < 0.00001 &&
                    Math.abs(item.getLongitude() - clickedItem.getLongitude()) < 0.00001) {
                aggregatedList.add(item);
            }
        }

        // 实例化自定义适配器
        MapAggregateAdapter adapter = new MapAggregateAdapter(aggregatedList);

        // 创建弹窗并设置 Adapter
        mBottomSheetDialog = new MaterialDialog.Builder(getContext())
                .title("📍 位置：详情列表")
                .content("该地点共有 " + aggregatedList.size() + " 件关联物品：")
                .adapter(adapter, new LinearLayoutManager(getContext()))
                .positiveText("关闭")
                .show();

        // 将弹窗实例传给 Adapter，方便点击条目时关闭弹窗
        adapter.setDialog(mBottomSheetDialog);

        // 设置底部弹出效果（保持 BottomSheet 体验）
        Window window = mBottomSheetDialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(android.R.style.Animation_InputMethod);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            // 限制弹窗最大高度，防止列表过长导致界面撑满
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
            window.setAttributes(lp);
        }
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
                        currentMapDataList.clear();
                        currentMapDataList.addAll(dataList); // 保存完整数据源给地图用

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
        if ("寻找中".equals(newInfo.getState()) ) {
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

    /**
     * 提取出的公共跳转方法，供 Adapter 使用
     */
    private void handleItemClickWrapper(LostFound selectedItem) {
        NewInfo tempInfo = new NewInfo(selectedItem.getType(), selectedItem.getTitle())
                .setSummary(selectedItem.getContent())
                .setUserName(selectedItem.getNickname())
                .setState(selectedItem.getState())
                .setPhone(selectedItem.getPhone())
                .setPlace(selectedItem.getPlace())
                .setPub_Date(selectedItem.getPubDate())
                .setImageUrl(selectedItem.getImg())
                .setUser_id(selectedItem.getUserId())
                .setId(selectedItem.getId());

        handleItemClick(tempInfo);
    }

    /**
     * 显示图片放大预览弹窗
     */

    private void showImagePreviewDialog(String imageUrl) {
        if (getContext() == null || imageUrl == null || imageUrl.isEmpty()) return;

        // 创建一个全屏无标题栏的 Dialog
        android.app.Dialog previewDialog = new android.app.Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        // 改用系统原生的 ImageView，完美支持 FIT_CENTER
        android.widget.ImageView fullImageView = new android.widget.ImageView(getContext());
        fullImageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // FIT_CENTER 会保证图片完整显示在屏幕内，且比例不变
        fullImageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        fullImageView.setBackgroundColor(0xFF000000); // 黑色背景

        // 使用框架的 ImageLoader 加载图片
        ImageLoader.get().loadImage(fullImageView, imageUrl);

        // 点击大图关闭预览
        fullImageView.setOnClickListener(v -> previewDialog.dismiss());

        previewDialog.setContentView(fullImageView);

        // 确保 Dialog 的 Window 也是全屏状态
        Window window = previewDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        previewDialog.show();
    }

    /**
     * 底部弹窗列表的自定义 Adapter
     */
    private class MapAggregateAdapter extends RecyclerView.Adapter<MapAggregateAdapter.ViewHolder> {
        private List<LostFound> mData;
        private MaterialDialog mDialog;

        public MapAggregateAdapter(List<LostFound> data) {
            this.mData = data;
        }

        public void setDialog(MaterialDialog dialog) {
            this.mDialog = dialog;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 纯代码动态构建子项布局：左侧文字，右侧图片
            Context context = parent.getContext();
            android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
            layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            int padding = dpToPx(context, 16);
            layout.setPadding(padding, padding, padding, padding);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            // 左侧文本描述
            TextView tvDesc = new TextView(context);
            tvDesc.setTextSize(15);
            tvDesc.setTextColor(0xFF333333); // 稍微深一点的灰色
            tvDesc.setLineSpacing(0, 1.2f);
            android.widget.LinearLayout.LayoutParams tvParams = new android.widget.LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            tvDesc.setLayoutParams(tvParams);

            // 右侧缩略图
            com.xuexiang.xui.widget.imageview.RadiusImageView ivThumb = new com.xuexiang.xui.widget.imageview.RadiusImageView(context);
            int imgSize = dpToPx(context, 60); // 设置图片宽高为 60dp
            android.widget.LinearLayout.LayoutParams ivParams = new android.widget.LinearLayout.LayoutParams(imgSize, imgSize);
            ivParams.leftMargin = dpToPx(context, 12); // 文字和图片的间距
            ivThumb.setLayoutParams(ivParams);
            ivThumb.setCornerRadius(dpToPx(context, 6)); // 圆角效果
            ivThumb.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            // 增加点击水波纹反馈
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

            // 如果有图片链接，加载并显示；否则隐藏 ImageView
            if (item.getImg() != null && !item.getImg().isEmpty()) {
                holder.ivThumb.setVisibility(View.VISIBLE);
                ImageLoader.get().loadImage(holder.ivThumb, item.getImg());

                // 点击图片触发放大效果
                holder.ivThumb.setOnClickListener(v -> {
                    showImagePreviewDialog(item.getImg());
                });
            } else {
                holder.ivThumb.setVisibility(View.GONE);
                holder.ivThumb.setOnClickListener(null);
            }

            // 点击除了图片以外的区域（整个 Item 行），跳转详情页
            holder.itemView.setOnClickListener(v -> {
                if (mDialog != null) mDialog.dismiss();
                handleItemClickWrapper(item);
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
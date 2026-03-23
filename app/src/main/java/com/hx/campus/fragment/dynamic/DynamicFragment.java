package com.hx.campus.fragment.dynamic;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
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
import com.hx.campus.adapter.entity.User;
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
    // 新闻信息数据列表
    private List<NewInfo> list = new ArrayList<>();
    // 地图相关变量
    private com.baidu.mapapi.map.MapView mMapView;
    private BaiduMap mBaiduMap;
    private boolean isMapMode = false;
    // 保存当前地图上的所有原始数据
    private List<LostFound> currentMapDataList = new ArrayList<>();

    // 保存当前的弹窗实例
    private MaterialDialog mBottomSheetDialog;

    @NonNull
    @Override
    protected FragmentNewsBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentNewsBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected TitleBar initTitle() {
        return null;
    }

    @Override
    protected void initViews() {
        // 初始化顶部固定的 Banner
        initFixedBanner();

        //  初始化顶部固定的 九宫格菜单
        initFixedGrid();

        //  初始化下方可滑动的列表
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(virtualLayoutManager);
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        binding.recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 10);
        adapter(virtualLayoutManager);

        baiduMap();
        binding.refreshLayout.autoRefresh();
    }

    /**
     * 初始化独立固定的 Banner
     */
    private void initFixedBanner() {
        SimpleImageBanner banner = findViewById(R.id.sib_simple_usage);
        if (banner != null) {
            banner.setSource(DemoDataProvider.getBannerList())
                    .setOnItemClickListener((view, item, pos) -> {
                        String url = "";
                        switch (pos) {
                            case 0:
                                url = "/pages/notification.html"; // 系统公告
                                break;
                            case 1:
                                url = "/pages/appcrash.html";    // 崩溃日志
                                break;
                            case 2:
                                url = "/pages/privacy.html";     // 隐私协议
                                break;
                            default:
                                url = "/pages/notification.html";
                                break;
                        }
                        String finalUrl = Utils.rebuildUrl(url, getContext());
                        AgentWebActivity.goWeb(getContext(), finalUrl);
                    }).startScroll();
        }
    }

    /**
     * 初始化独立固定的 九宫格菜单
     */
    private void initFixedGrid() {
        RecyclerView rvGrid = findViewById(R.id.rv_grid_menu);
        if (rvGrid != null) {
            rvGrid.setLayoutManager(new GridLayoutManager(getContext(), 4));
            List<AdapterItem> gridData = DemoDataProvider.getGridItems(getContext());

            rvGrid.setAdapter(new RecyclerView.Adapter<RecyclerViewHolder>() {
                @NonNull
                @Override
                public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_common_grid_item, parent, false);
                    return new RecyclerViewHolder(view);
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
                    AdapterItem item = gridData.get(position);
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
                        else if (title.contains("留言")) openMSGWeb();
                    });
                }

                @Override
                public int getItemCount() {
                    return gridData.size();
                }
            });
        }
    }

    /**
     * 下方新闻列表适配器
     */
    private void adapter(VirtualLayoutManager virtualLayoutManager) {
        // 仅保留标题适配器
        SingleDelegateAdapter titleAdapter = new SingleDelegateAdapter(R.layout.adapter_title_item) {
            @Override
            public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
                holder.text(R.id.tv_title, getResources().getString(R.string.recommendation));
            }
        };

        // 仅保留推荐内容适配器
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
        // 不再添加 Banner 和 Grid
        delegateAdapter.addAdapter(titleAdapter);
        delegateAdapter.addAdapter(newInfoSimpleDelegateAdapter);
        binding.recyclerView.setAdapter(delegateAdapter);
    }

    private void baiduMap() {
        mMapView = binding.bmapView;
        mBaiduMap = mMapView.getMap();

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

        // 切换到地图模式
        binding.fabSwitchMode.setOnClickListener(v -> {
            isMapMode = true;
            // 隐藏整个主界面内容(包括Banner, Grid和列表)
            findViewById(R.id.ll_main_content).setVisibility(View.GONE);
            mMapView.setVisibility(View.VISIBLE);

            binding.fabMapBack.setVisibility(View.VISIBLE);
            binding.llMapControls.setVisibility(View.VISIBLE);
            binding.fabSwitchMode.setVisibility(View.GONE);
        });

        // 退出地图模式
        binding.fabMapBack.setOnClickListener(v -> {
            isMapMode = false;
            // 恢复主界面内容
            findViewById(R.id.ll_main_content).setVisibility(View.VISIBLE);
            mMapView.setVisibility(View.GONE);

            binding.fabMapBack.setVisibility(View.GONE);
            binding.llMapControls.setVisibility(View.GONE);
            binding.fabSwitchMode.setVisibility(View.VISIBLE);
        });
    }

    private void openMSGWeb() {
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        Uri uri = Uri.parse("/pages/message_board.html")
                .buildUpon()
                .appendQueryParameter("userId", String.valueOf(user.getId()))
                .build();
        String url = Utils.rebuildUrl(uri.toString(), getContext());
        AgentWebActivity.goWeb(getContext(), url);
    }

    @Override
    protected void initListeners() {
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadTopListData();
        });

        binding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            refreshLayout.finishLoadMore(1000);
        });
    }

    private void loadTopListData() {
        RetrofitClient.getInstance().getApi().showTopList(1).enqueue(new Callback<Result<List<LostFound>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<List<LostFound>>> call, @NonNull Response<Result<List<LostFound>>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    List<LostFound> dataList = response.body().getData();
                    if (dataList != null) {
                        list.clear();
                        currentMapDataList.clear();
                        currentMapDataList.addAll(dataList);

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
                                    .setId(item.getId()));
                        }
                        newInfoSimpleDelegateAdapter.refresh(list);
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

    private void showMarkersOnMap(List<LostFound> dataList) {
        if (mBaiduMap == null) return;
        mBaiduMap.clear();
        BitmapDescriptor lostIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker_lost);
        BitmapDescriptor foundIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker_found);
        boolean isFirstPoint = true;
        List<String> drawnPoints = new ArrayList<>();

        for (LostFound item : dataList) {
            double lat = item.getLatitude();
            double lng = item.getLongitude();
            if (lat == 0 && lng == 0) continue;
            if (lat > 90 || lat < -90) continue;

            String locKey = String.format(Locale.getDefault(), "%.5f,%.5f", lat, lng);
            if (drawnPoints.contains(locKey)) continue;
            drawnPoints.add(locKey);

            LatLng point = new LatLng(lat, lng);
            BitmapDescriptor currentIcon = item.getType().equals("失物") ? lostIcon : foundIcon;
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(currentIcon)
                    .title(item.getTitle());

            Marker marker = (Marker) mBaiduMap.addOverlay(option);
            Bundle bundle = new Bundle();
            bundle.putSerializable("info", item);
            marker.setExtraInfo(bundle);
            if (isFirstPoint) {
                com.baidu.mapapi.map.MapStatusUpdate msu = com.baidu.mapapi.map.MapStatusUpdateFactory.newLatLngZoom(point, 16.0f);
                mBaiduMap.animateMapStatus(msu);
                isFirstPoint = false;
            }
        }
    }

    private void showXUIBottomSheet(LostFound clickedItem) {
        if (getContext() == null) return;
        if (mBottomSheetDialog != null && mBottomSheetDialog.isShowing()) return;

        List<LostFound> aggregatedList = new ArrayList<>();
        for (LostFound item : currentMapDataList) {
            if (Math.abs(item.getLatitude() - clickedItem.getLatitude()) < 0.00001 &&
                    Math.abs(item.getLongitude() - clickedItem.getLongitude()) < 0.00001) {
                aggregatedList.add(item);
            }
        }

        MapAggregateAdapter adapter = new MapAggregateAdapter(aggregatedList);
        mBottomSheetDialog = new MaterialDialog.Builder(getContext())
                .title("📍 位置：详情列表")
                .content("该地点共有 " + aggregatedList.size() + " 件关联物品：")
                .adapter(adapter, new LinearLayoutManager(getContext()))
                .positiveText("关闭")
                .show();
        adapter.setDialog(mBottomSheetDialog);

        Window window = mBottomSheetDialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(android.R.style.Animation_InputMethod);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
            window.setAttributes(lp);
        }
    }

    private int dpToPx(Context context, float dp) {
        if (context == null) return 0;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) mMapView.onResume();
        if (binding.refreshLayout != null) binding.refreshLayout.autoRefresh(50);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) mMapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMapView != null) mMapView.onDestroy();
    }

    private class MapAggregateAdapter extends RecyclerView.Adapter<MapAggregateAdapter.ViewHolder> {
        private List<LostFound> mData;
        private MaterialDialog mDialog;

        public MapAggregateAdapter(List<LostFound> data) { this.mData = data; }
        public void setDialog(MaterialDialog dialog) { this.mDialog = dialog; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
            layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int padding = dpToPx(context, 16);
            layout.setPadding(padding, padding, padding, padding);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvDesc = new TextView(context);
            tvDesc.setTextSize(15);
            tvDesc.setTextColor(0xFF333333);
            tvDesc.setLineSpacing(0, 1.2f);
            android.widget.LinearLayout.LayoutParams tvParams = new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
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
            String formattedText = String.format("%s [%s] %s \n📍 %s", typeIcon, item.getType(), item.getTitle(), item.getPlace());
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
                handleItemClickWrapper(item);
            });
        }

        @Override
        public int getItemCount() { return mData == null ? 0 : mData.size(); }

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
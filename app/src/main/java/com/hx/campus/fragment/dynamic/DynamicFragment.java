package com.hx.campus.fragment.dynamic;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
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

    private SimpleDelegateAdapter<NewInfo> mNewsAdapter;
    private List<NewInfo> list = new ArrayList<>();

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
        mNewsAdapter = new BroccoliSimpleDelegateAdapter<NewInfo>(R.layout.adapter_news_card_view_list_item, new LinearLayoutHelper(), DemoDataProvider.getEmptyNewInfo()) {
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
        delegateAdapter.addAdapter(mNewsAdapter);
        binding.recyclerView.setAdapter(delegateAdapter);

        // 初始自动刷新数据
        binding.refreshLayout.autoRefresh();
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
                                    .setImageUrl(item.getImg()));
                        }
                        // 刷新适配器数据
                        mNewsAdapter.refresh(list);
                    }
                } else {
                    Utils.showResponse("数据加载失败");
                }
                if (binding.refreshLayout != null) binding.refreshLayout.finishRefresh();
            }

            @Override
            public void onFailure(@NonNull Call<Result<List<LostFound>>> call, @NonNull Throwable t) {
                Utils.showResponse("网络异常");
                if (binding.refreshLayout != null) binding.refreshLayout.finishRefresh();
            }
        });
    }

    private void handleItemClick(NewInfo newInfo) {
        LostFound lostFound = new LostFound(newInfo.getTitle(), newInfo.getImageUrl(), newInfo.getPub_Date(), newInfo.getSummary(), newInfo.getPlace(), newInfo.getPhone(), newInfo.getState(), newInfo.getUserName());
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
        if (binding.refreshLayout != null) binding.refreshLayout.autoRefresh(50);
    }
}
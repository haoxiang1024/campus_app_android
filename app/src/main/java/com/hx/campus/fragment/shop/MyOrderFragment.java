package com.hx.campus.fragment.shop;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.ExchangeOrder;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.adapter.shop.MyOrderAdapter;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentMyOrderBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.ApiService;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

@Page(name = "我的订单")
public class MyOrderFragment extends BaseFragment<FragmentMyOrderBinding> {

    private RecyclerView recyclerView;
    private MyOrderAdapter adapter;
    private EditText etSearch;
    private ImageView ivSearch;
    private List<ExchangeOrder> orderList = new ArrayList<>();



    @Override
    protected void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        etSearch = findViewById(R.id.et_search);
        ivSearch = findViewById(R.id.iv_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyOrderAdapter(getContext(), orderList);
        recyclerView.setAdapter(adapter);

        loadOrders("");
    }

    @NonNull
    @Override
    protected FragmentMyOrderBinding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        return FragmentMyOrderBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initListeners() {
        ivSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            loadOrders(keyword);
        });

        adapter.setOnItemClickListener(order -> {
            popToBack("ShopFragment", null);
        });

        adapter.setOnItemLongClickListener(order -> {
            new MaterialDialog.Builder(getContext())
                    .title("提示")
                    .content("确定要删除该订单吗？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive((dialog, which) -> deleteOrder(order))
                    .show();
        });
    }

    private void loadOrders(String keyword) {
        ApiService apiService = RetrofitClient.getInstance().getApi();
        apiService.getMyOrders(keyword).enqueue(new Callback<Result<List<ExchangeOrder>>>() {
            @Override
            public void onResponse(Call<Result<List<ExchangeOrder>>> call, Response<Result<List<ExchangeOrder>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 0) {
                    orderList.clear();
                    orderList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Utils.showResponse("获取订单失败");
                }
            }

            @Override
            public void onFailure(Call<Result<List<ExchangeOrder>>> call, Throwable t) {
                Utils.showResponse("网络错误");
            }
        });
    }

    private void deleteOrder(ExchangeOrder order) {
        ApiService apiService = RetrofitClient.getInstance().getApi();
        //获取用户信息
        User user = Utils.getBeanFromSp(getContext(), "User", "user");
        if (user == null) return;
        apiService.deleteOrder(order.getId(), user.getId()).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 0) {
                    Utils.showResponse("删除成功");
                    orderList.remove(order);
                    adapter.notifyDataSetChanged();
                } else {
                    Utils.showResponse("删除失败");
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Utils.showResponse("网络错误");
            }
        });
    }
}
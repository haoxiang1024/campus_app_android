package com.hx.campus.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hx.campus.R;
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseActivity;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.ActivityMainBinding;
import com.hx.campus.fragment.dynamic.DynamicFragment;
import com.hx.campus.fragment.look.FoundInfoDetailFragment;
import com.hx.campus.fragment.look.LookFragment;
import com.hx.campus.fragment.look.LostInfoDetailFragment;
import com.hx.campus.fragment.message.MessageMainFragment;
import com.hx.campus.fragment.other.AboutFragment;
import com.hx.campus.fragment.other.LoginFragment;
import com.hx.campus.fragment.other.SearchFragment;
import com.hx.campus.fragment.personal.AccountFragment;
import com.hx.campus.fragment.personal.PersonalFragment;
import com.hx.campus.fragment.settings.SettingsFragment;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.TokenUtils;
import com.hx.campus.utils.sdkinit.XUpdateInit;
import com.xuexiang.xpage.core.CoreSwitchBean;
import com.xuexiang.xui.adapter.FragmentAdapter;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.common.ClickUtils;
import com.xuexiang.xutil.common.CollectionUtils;
import com.xuexiang.xutil.display.Colors;

import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener, ClickUtils.OnClick2ExitListener, Toolbar.OnMenuItemClickListener {

    private String[] mTitles;

    @Override
    protected ActivityMainBinding viewBindingInflate(LayoutInflater inflater) {
        return ActivityMainBinding.inflate(inflater);
    }

    @Override
    protected void onResume() {
        super.onResume();
        User user = Utils.getBeanFromSp(MainActivity.this, "User", "user");
        if (user != null) {
            fetchUserInfoFromServer(String.valueOf(user.getId()));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkLoginStatus()) {
            return;
        }
        initViews();
        initData();
        initListeners();
        checkIMStatus();

        checkNotificationPermission();

        handleDeepLinkIntent(getIntent());

    }

    private boolean checkLoginStatus() {
        String token = TokenUtils.getToken();
        User user = Utils.getBeanFromSp(this, "User", "user");


        if (TextUtils.isEmpty(token) || user == null) {

            openNewPage(LoginFragment.class);
            return false;
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLinkIntent(intent);
    }

    
    private void handleDeepLinkIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null && "hxcampus".equals(data.getScheme())) {
                String path = data.getPath();
                if ("/detail".equals(path)) {

                    String idStr = data.getQueryParameter("id");
                    if (!TextUtils.isEmpty(idStr)) {
                        int id = Integer.parseInt(idStr);

                        jumpToDetail(id);
                    }
                }
            }
        }
    }

    
    public void onScanResult(String resultUrl) {
        if (resultUrl.contains("share.html?id=")) {
            Uri uri = Uri.parse(resultUrl);
            String idStr = uri.getQueryParameter("id");
            if (!TextUtils.isEmpty(idStr)) {
                int id = Integer.parseInt(idStr);

                jumpToDetail(id);
            }
        } else {
            Utils.showResponse("无效的二维码");
        }
    }

    
    private void jumpToDetail(int foundId) {

        Utils.showResponse("正在加载详情...");
        RetrofitClient.getInstance().getApi().getLostFoundById(foundId).enqueue(new Callback<Result<LostFound>>() {
            @Override
            public void onResponse(Call<Result<LostFound>> call, Response<Result<LostFound>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess() && response.body().getData() != null) {
                        LostFound lostFound = response.body().getData();
                        lostFound.setImg(Utils.getImageUrl(lostFound.getImg(), MainActivity.this));
                        if ("失物".equals(lostFound.getType())) {

                            openLost(lostFound);
                        } else {

                            openFound(lostFound);
                        }
                    } else {
                        Utils.showResponse("该物品可能已被删除");
                    }
                } else {
                    Utils.showResponse("获取物品详情失败");
                }
            }

            @Override
            public void onFailure(Call<Result<LostFound>> call, Throwable t) {
                Utils.showResponse("网络异常，无法打开详情");
            }
        });
    }

    private void openFound(LostFound lostFound) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(FoundInfoDetailFragment.KEY_FOUND, lostFound);
        CoreSwitchBean page = new CoreSwitchBean(FoundInfoDetailFragment.class)
                .setBundle(bundle)
                .setNewActivity(true);
        openPage(page);
    }

    private void openLost(LostFound lostFound) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(LostInfoDetailFragment.KEY_LOST, lostFound);
        CoreSwitchBean page = new CoreSwitchBean(LostInfoDetailFragment.class)
                .setBundle(bundle)
                .setNewActivity(true);
        openPage(page);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this,
                        new String[]{"android.permission.POST_NOTIFICATIONS"}, 101);
            }
        }
    }

    private void checkIMStatus() {

        RongIMClient.ConnectionStatusListener.ConnectionStatus status =
                IMCenter.getInstance().getCurrentConnectionStatus();

        if (status != RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {

            String cachedToken = TokenUtils.getImToken();
            if (!TextUtils.isEmpty(cachedToken)) {
                RongIMClient.ConnectCallback connectCallback = new RongIMClient.ConnectCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Log.e("IM_LOG", "融云连接成功 用户id: " + userId);
                        User user = Utils.getBeanFromSp(MainActivity.this, "User", "user");
                        if (user != null) {
                            String fullAvatarUrl = "";
                            if (!TextUtils.isEmpty(user.getPhoto())) {
                                fullAvatarUrl = user.getPhoto().startsWith("http") ?
                                        user.getPhoto() :
                                        Utils.rebuildUrl("upload/" + user.getPhoto(), MainActivity.this);
                            }
                            UserInfo myInfo = new UserInfo(
                                    String.valueOf(user.getId()),
                                    user.getNickname(),
                                    Uri.parse(fullAvatarUrl)
                            );
                            RongIM.getInstance().setCurrentUserInfo(myInfo);
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ConnectionErrorCode e) {
                        Log.e("IM_LOG", "连接失败码: " + e.getValue());
                    }

                    @Override
                    public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus code) {
                    }
                };
                IMCenter.getInstance().connect(cachedToken, connectCallback);
            }
        }
    }

    @Override
    protected boolean isSupportSlideBack() {
        return false;
    }

    private void initViews() {
        WidgetUtils.clearActivityBackground(this);

        mTitles = getResources().getStringArray(R.array.home_titles);

        binding.includeMain.toolbar.setTitle(mTitles[0]);
        binding.includeMain.toolbar.inflateMenu(R.menu.menu_main);
        binding.includeMain.toolbar.setOnMenuItemClickListener(this);
        initHeader();

        BaseFragment[] fragments = new BaseFragment[]{
                new DynamicFragment(),
                new MessageMainFragment(),
                new LookFragment(),
                new PersonalFragment()
        };
        FragmentAdapter<BaseFragment> adapter = new FragmentAdapter<>(getSupportFragmentManager(), fragments);
        binding.includeMain.viewPager.setOffscreenPageLimit(mTitles.length - 1);
        binding.includeMain.viewPager.setAdapter(adapter);
    }

    private void initData() {

        TokenUtils.setToken(TokenUtils.getToken());
        XUpdateInit.checkUpdate(this, false);
    }

    private void initHeader() {
        binding.navView.setItemIconTintList(null);
        View headerView = binding.navView.getHeaderView(0);
        LinearLayout navHeader = headerView.findViewById(R.id.nav_header);
        RadiusImageView ivAvatar = headerView.findViewById(R.id.iv_avatar);
        TextView tvAvatar = headerView.findViewById(R.id.tv_avatar);
        TextView tvSign = headerView.findViewById(R.id.tv_sign);
        ImageView sexView = headerView.findViewById(R.id.sex_photo);

        if (Utils.isColorDark(ThemeUtils.resolveColor(this, R.attr.colorAccent))) {
            tvAvatar.setTextColor(Colors.WHITE);
            tvSign.setTextColor(Colors.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivAvatar.setImageTintList(ResUtils.getColors(R.color.xui_config_color_white));
            }
        } else {
            tvAvatar.setTextColor(ThemeUtils.resolveColor(this, R.attr.xui_config_color_title_text));
            tvSign.setTextColor(ThemeUtils.resolveColor(this, R.attr.xui_config_color_explain_text));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivAvatar.setImageTintList(ResUtils.getColors(R.color.xui_config_color_gray_3));
            }
        }


        User user = Utils.getBeanFromSp(this, "User", "user");

        if (user != null) {

            if (TextUtils.isEmpty(user.getPhoto())) {
                tvAvatar.setVisibility(View.GONE);
            } else {
                tvAvatar.setVisibility(View.VISIBLE);
                Glide.with(this).load(user.getPhoto()).into(ivAvatar);
            }

            tvAvatar.setText(user.getNickname());


            if ("男".equals(user.getSex())) {
                tvSign.setText("小哥哥");
                sexView.setVisibility(View.VISIBLE);
                Glide.with(this).load(R.drawable.man).into(sexView);
            } else {
                tvSign.setText("小姐姐");
                sexView.setVisibility(View.VISIBLE);
                Glide.with(this).load(R.drawable.women).into(sexView);
            }
        } else {

            tvAvatar.setVisibility(View.VISIBLE);
            tvAvatar.setText("未登录");
            tvSign.setText("点击登录/注册");
            sexView.setVisibility(View.GONE);
        }

        navHeader.setOnClickListener(this);
    }


    protected void initListeners() {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.includeMain.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.isCheckable()) {
                binding.drawerLayout.closeDrawers();
                return handleNavigationItemSelected(menuItem);
            } else {
                int id = menuItem.getItemId();
                if (id == R.id.nav_settings) {

                    openNewPage(SettingsFragment.class);
                } else if (id == R.id.nav_about) {

                    openNewPage(AboutFragment.class);
                } else if (id == R.id.nav_search) {

                    openNewPage(SearchFragment.class);
                }
            }
            return true;
        });

        binding.includeMain.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MenuItem item = binding.includeMain.bottomNavigation.getMenu().getItem(position);
                binding.includeMain.toolbar.setTitle(item.getTitle());
                item.setChecked(true);
                updateSideNavStatus(item);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        binding.includeMain.bottomNavigation.setOnNavigationItemSelectedListener(this);
    }

    
    private boolean handleNavigationItemSelected(@NonNull MenuItem menuItem) {
        int index = CollectionUtils.arrayIndexOf(mTitles, menuItem.getTitle());
        if (index != -1) {
            binding.includeMain.toolbar.setTitle(menuItem.getTitle());
            binding.includeMain.viewPager.setCurrentItem(index, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.nav_header) {
            openNewPage(AccountFragment.class);
        }
    }


    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int index = CollectionUtils.arrayIndexOf(mTitles, menuItem.getTitle());
        if (index != -1) {
            binding.includeMain.toolbar.setTitle(menuItem.getTitle());
            binding.includeMain.viewPager.setCurrentItem(index, false);
            updateSideNavStatus(menuItem);
            return true;
        }
        return false;
    }

    
    private void updateSideNavStatus(MenuItem menuItem) {
        MenuItem side = binding.navView.getMenu().findItem(menuItem.getItemId());
        if (side != null) {
            side.setChecked(true);
        }
    }
    
    private void fetchUserInfoFromServer(String userId) {
        RetrofitClient.getInstance().getApi().getUserInfo(Integer.parseInt(userId)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String fullAvatarUrl = "";
                    String photo = user.getPhoto();
                    if (!TextUtils.isEmpty(photo)) {
                        if (photo.startsWith("http")) {
                            fullAvatarUrl = photo;
                        } else {
                            fullAvatarUrl = Utils.rebuildUrl("upload/" + photo, getApplicationContext());
                        }
                    }

                    UserInfo userInfo = new UserInfo(
                            userId,
                            user.getNickname(),
                            Uri.parse(fullAvatarUrl)
                    );

                    RongUserInfoManager.getInstance().refreshUserInfoCache(userInfo);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.exitBy2Click(2000, this);
        }
        return true;
    }

    @Override
    public void onRetry() {
        XToastUtils.toast("再按一次退出程序");
    }

    @Override
    public void onExit() {
        XUtil.exitApp();
    }
}
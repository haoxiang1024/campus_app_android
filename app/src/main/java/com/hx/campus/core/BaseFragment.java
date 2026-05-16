


package com.hx.campus.core;

import android.content.res.Configuration;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.hx.campus.core.http.loader.ProgressLoader;
import com.umeng.analytics.MobclickAgent;
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader;
import com.xuexiang.xpage.base.XPageActivity;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.core.PageOption;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xpage.utils.Utils;
import com.xuexiang.xrouter.facade.service.SerializationService;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.actionbar.TitleUtils;

import java.io.Serializable;
import java.lang.reflect.Type;



public abstract class BaseFragment<Binding extends ViewBinding> extends XPageFragment {

    
    private IProgressLoader mIProgressLoader;

    
    protected Binding binding;

    
    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        binding = viewBindingInflate(inflater, container, attachToRoot);
        return binding.getRoot();
    }

    
    @NonNull
    protected abstract Binding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot);

    
    public Binding getBinding() {
        return binding;
    }

    
    @Override
    protected void initPage() {
        initTitle();
        initViews();
        initListeners();
    }

    
    protected TitleBar initTitle() {
        return TitleUtils.addTitleBarDynamic(getToolbarContainer(), getPageTitle(), v -> popToBack());
    }

    
    @Override
    protected void initListeners() {
        // 默认空实现，子类根据需要重写
    }

    
    public IProgressLoader getProgressLoader() {
        if (mIProgressLoader == null) {
            mIProgressLoader = ProgressLoader.create(getContext());
        }
        return mIProgressLoader;
    }

    
    public IProgressLoader getProgressLoader(String message) {
        if (mIProgressLoader == null) {
            mIProgressLoader = ProgressLoader.create(getContext(), message);
        } else {
            mIProgressLoader.updateMessage(message);
        }
        return mIProgressLoader;
    }

    
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        // 屏幕旋转时刷新标题栏
        super.onConfigurationChanged(newConfig);
        ViewGroup root = (ViewGroup) getRootView();
        if (root.getChildAt(0) instanceof TitleBar) {
            root.removeViewAt(0);
            initTitle();
        }
    }

    
    @Override
    public void onDestroyView() {
        if (mIProgressLoader != null) {
            mIProgressLoader.dismissLoading();
        }
        super.onDestroyView();
        binding = null;
    }

    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getPageName());
    }

    
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getPageName());
    }

    //==============================页面跳转API===================================//

    
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz) {
        return new PageOption(clazz)
                .setNewActivity(true)
                .open(this);
    }

    
    public <T extends XPageFragment> Fragment openNewPage(String pageName) {
        return new PageOption(pageName)
                .setAnim(CoreAnim.slide)
                .setNewActivity(true)
                .open(this);
    }


    
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz, @NonNull Class<? extends XPageActivity> containActivityClazz) {
        return new PageOption(clazz)
                .setNewActivity(true)
                .setContainActivityClazz(containActivityClazz)
                .open(this);
    }

    
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz, String key, Object value) {
        PageOption option = new PageOption(clazz).setNewActivity(true);
        return openPage(option, key, value);
    }

    
    public Fragment openPage(PageOption option, String key, Object value) {
        if (value instanceof Integer) {
            option.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            option.putFloat(key, (Float) value);
        } else if (value instanceof String) {
            option.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            option.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            option.putLong(key, (Long) value);
        } else if (value instanceof Double) {
            option.putDouble(key, (Double) value);
        } else if (value instanceof Parcelable) {
            option.putParcelable(key, (Parcelable) value);
        } else if (value instanceof Serializable) {
            option.putSerializable(key, (Serializable) value);
        } else {
            // 对于复杂对象，序列化为JSON字符串传递
            option.putString(key, serializeObject(value));
        }
        return option.open(this);
    }

    
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, boolean addToBackStack, String key, String value) {
        return new PageOption(clazz)
                .setAddToBackStack(addToBackStack)
                .putString(key, value)
                .open(this);
    }

    
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, String key, Object value) {
        return openPage(clazz, true, key, value);
    }

    
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, boolean addToBackStack, String key, Object value) {
        PageOption option = new PageOption(clazz).setAddToBackStack(addToBackStack);
        return openPage(option, key, value);
    }

    
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, String key, String value) {
        return new PageOption(clazz)
                .putString(key, value)
                .open(this);
    }

    
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, String key, Object value, int requestCode) {
        PageOption option = new PageOption(clazz).setRequestCode(requestCode);
        return openPage(option, key, value);
    }

    
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, String key, String value, int requestCode) {
        return new PageOption(clazz)
                .setRequestCode(requestCode)
                .putString(key, value)
                .open(this);
    }

    
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, int requestCode) {
        return new PageOption(clazz)
                .setRequestCode(requestCode)
                .open(this);
    }

    
    public String serializeObject(Object object) {
        return XRouter.getInstance().navigation(SerializationService.class).object2Json(object);
    }

    
    public <T> T deserializeObject(String input, Type clazz) {
        return XRouter.getInstance().navigation(SerializationService.class).parseObject(input, clazz);
    }


    
    @Override
    protected void hideCurrentPageSoftInput() {
        if (getActivity() == null) {
            return;
        }
        // 通过清除当前焦点来隐藏软键盘
        Utils.hideSoftInputClearFocus(getActivity().getCurrentFocus());
    }

}

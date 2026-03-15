


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


/**
 * 抽象基础Fragment类，继承自XPageFragment
 * 提供ViewBinding支持、统一的生命周期管理、页面导航等功能
 * 
 * @param <Binding> ViewBinding类型参数，确保类型安全
 */
public abstract class BaseFragment<Binding extends ViewBinding> extends XPageFragment {

    /** 进度加载器实例，用于显示加载状态 */
    private IProgressLoader mIProgressLoader;

    /**
     * ViewBinding实例，用于替代findViewById
     * 通过泛型确保类型安全，避免强制类型转换
     */
    protected Binding binding;

    /**
     * 创建Fragment的内容视图
     * 通过ViewBinding inflate布局文件，替代传统的findViewById方式
     * 
     * @param inflater 布局填充器
     * @param container 父容器视图
     * @param attachToRoot 是否附加到根布局
     * @return Fragment的根视图
     */
    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot) {
        binding = viewBindingInflate(inflater, container, attachToRoot);
        return binding.getRoot();
    }

    /**
     * 抽象方法：子类必须实现此方法来创建具体的ViewBinding实例
     * 通常通过调用对应Binding类的inflate方法实现
     * 
     * @param inflater 布局填充器
     * @param container 父容器视图
     * @param attachToRoot 是否附加到根布局
     * @return 具体的ViewBinding实例
     */
    @NonNull
    protected abstract Binding viewBindingInflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToRoot);

    /**
     * 获取当前Fragment的ViewBinding实例
     * 方便在Fragment外部访问绑定的视图组件
     * 
     * @return ViewBinding实例
     */
    public Binding getBinding() {
        return binding;
    }

    /**
     * 页面初始化入口方法
     * 按固定顺序初始化页面组件：标题栏 -> 视图 -> 监听器
     * 子类可以通过重写对应方法来自定义初始化行为
     */
    @Override
    protected void initPage() {
        initTitle();
        initViews();
        initListeners();
    }

    /**
     * 初始化页面标题栏
     * 自动创建并配置标题栏，包含返回按钮功能
     * 
     * @return 创建的TitleBar实例
     */
    protected TitleBar initTitle() {
        return TitleUtils.addTitleBarDynamic(getToolbarContainer(), getPageTitle(), v -> popToBack());
    }

    /**
     * 初始化事件监听器
     * 子类应该重写此方法来注册各种UI事件监听器
     * 默认实现为空，提供给子类扩展
     */
    @Override
    protected void initListeners() {
        // 默认空实现，子类根据需要重写
    }

    /**
     * 获取默认的进度加载器实例
     * 单例模式，首次调用时创建实例
     * 
     * @return 进度加载器实例
     */
    public IProgressLoader getProgressLoader() {
        if (mIProgressLoader == null) {
            mIProgressLoader = ProgressLoader.create(getContext());
        }
        return mIProgressLoader;
    }

    /**
     * 获取带自定义提示信息的进度加载器
     * 如果加载器已存在则更新提示信息，否则创建新的实例
     * 
     * @param message 显示在进度对话框中的提示文本
     * @return 配置好的进度加载器实例
     */
    public IProgressLoader getProgressLoader(String message) {
        if (mIProgressLoader == null) {
            mIProgressLoader = ProgressLoader.create(getContext(), message);
        } else {
            mIProgressLoader.updateMessage(message);
        }
        return mIProgressLoader;
    }

    /**
     * 处理配置变更事件（如屏幕旋转）
     * 当设备方向改变时重新初始化标题栏以适应新的布局
     * 
     * @param newConfig 新的配置信息
     */
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

    /**
     * Fragment视图销毁时的清理工作
     * 释放资源，隐藏可能正在显示的加载对话框，并清空ViewBinding引用
     */
    @Override
    public void onDestroyView() {
        if (mIProgressLoader != null) {
            mIProgressLoader.dismissLoading();
        }
        super.onDestroyView();
        binding = null;
    }

    /**
     * Fragment恢复可见状态时调用
     * 通知友盟统计开始页面统计
     */
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getPageName());
    }

    /**
     * Fragment暂停时调用
     * 通知友盟统计结束页面统计
     */
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getPageName());
    }

    //==============================页面跳转API===================================//

    /**
     * 在新Activity中打开指定页面
     * 适用于主Tab页面，避免在当前Activity中堆叠过多Fragment
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz) {
        return new PageOption(clazz)
                .setNewActivity(true)
                .open(this);
    }

    /**
     * 通过页面名称在新Activity中打开页面
     * 使用滑动动画效果，提升用户体验
     * 
     * @param pageName 目标页面的注册名称
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openNewPage(String pageName) {
        return new PageOption(pageName)
                .setAnim(CoreAnim.slide)
                .setNewActivity(true)
                .open(this);
    }


    /**
     * 在指定容器Activity中打开新页面
     * 用于需要特定Activity容器的场景
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param containActivityClazz 指定的容器Activity Class
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz, @NonNull Class<? extends XPageActivity> containActivityClazz) {
        return new PageOption(clazz)
                .setNewActivity(true)
                .setContainActivityClazz(containActivityClazz)
                .open(this);
    }

    /**
     * 在新Activity中打开页面并传递单个参数
     * 简化版的页面跳转方法，适用于只需要传递一个参数的场景
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param key 参数键名
     * @param value 参数值
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz, String key, Object value) {
        PageOption option = new PageOption(clazz).setNewActivity(true);
        return openPage(option, key, value);
    }

    /**
     * 通用页面跳转方法，支持多种数据类型的参数传递
     * 自动识别参数类型并使用对应的put方法
     * 
     * @param option 页面选项配置对象
     * @param key 参数键名
     * @param value 参数值（支持多种基本类型和对象）
     * @return 新创建的Fragment实例
     */
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

    /**
     * 打开页面并控制是否加入回退栈
     * 专门用于传递字符串参数的便捷方法
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param addToBackStack true表示加入回退栈，false表示不加入
     * @param key 字符串参数键名
     * @param value 字符串参数值
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, boolean addToBackStack, String key, String value) {
        return new PageOption(clazz)
                .setAddToBackStack(addToBackStack)
                .putString(key, value)
                .open(this);
    }

    /**
     * 打开页面（默认加入回退栈）
     * 简化版页面跳转方法，默认将页面加入回退栈
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param key 参数键名
     * @param value 参数值
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, String key, Object value) {
        return openPage(clazz, true, key, value);
    }

    /**
     * 打开页面并控制回退栈行为
     * 支持传递任意类型参数的完整版页面跳转方法
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param addToBackStack true表示加入回退栈，false表示不加入
     * @param key 参数键名
     * @param value 参数值
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, boolean addToBackStack, String key, Object value) {
        PageOption option = new PageOption(clazz).setAddToBackStack(addToBackStack);
        return openPage(option, key, value);
    }

    /**
     * 打开页面并传递字符串参数
     * 专门用于字符串参数传递的便捷方法
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param key 字符串参数键名
     * @param value 字符串参数值
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, String key, String value) {
        return new PageOption(clazz)
                .putString(key, value)
                .open(this);
    }

    /**
     * 打开页面并等待结果返回
     * 用于需要从目标页面获取返回数据的场景
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param key 参数键名
     * @param value 参数值
     * @param requestCode 请求码，用于区分不同的请求
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, String key, Object value, int requestCode) {
        PageOption option = new PageOption(clazz).setRequestCode(requestCode);
        return openPage(option, key, value);
    }

    /**
     * 打开页面等待结果返回（字符串参数版）
     * 专门用于传递字符串参数并等待结果的便捷方法
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param key 字符串参数键名
     * @param value 字符串参数值
     * @param requestCode 请求码
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, String key, String value, int requestCode) {
        return new PageOption(clazz)
                .setRequestCode(requestCode)
                .putString(key, value)
                .open(this);
    }

    /**
     * 打开页面等待结果返回（无参数版）
     * 用于不需要传递参数但需要获取返回结果的场景
     * 
     * @param clazz 目标页面Fragment的Class对象
     * @param requestCode 请求码
     * @param <T> 继承自XPageFragment的泛型参数
     * @return 新创建的Fragment实例
     */
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, int requestCode) {
        return new PageOption(clazz)
                .setRequestCode(requestCode)
                .open(this);
    }

    /**
     * 将对象序列化为JSON字符串
     * 使用XRouter的序列化服务处理复杂对象的传递
     * 
     * @param object 需要序列化的对象
     * @return 序列化后的JSON字符串
     */
    public String serializeObject(Object object) {
        return XRouter.getInstance().navigation(SerializationService.class).object2Json(object);
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象
     * 配合serializeObject方法使用，实现对象的完整传递
     * 
     * @param input 序列化的JSON字符串
     * @param clazz 目标对象的类型信息
     * @param <T> 返回对象的泛型类型
     * @return 反序列化后的对象实例
     */
    public <T> T deserializeObject(String input, Type clazz) {
        return XRouter.getInstance().navigation(SerializationService.class).parseObject(input, clazz);
    }


    /**
     * 隐藏当前页面的软键盘
     * 通过清除焦点的方式来隐藏输入法
     * 注意：XML布局的父容器需要设置android:focusable="true"和android:focusableInTouchMode="true"
     */
    @Override
    protected void hideCurrentPageSoftInput() {
        if (getActivity() == null) {
            return;
        }
        // 通过清除当前焦点来隐藏软键盘
        Utils.hideSoftInputClearFocus(getActivity().getCurrentFocus());
    }

}

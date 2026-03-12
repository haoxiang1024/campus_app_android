
package com.hx.campus.fragment.look;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentTrendingBinding;
import com.hx.campus.fragment.message.MyCommentsFragment;
import com.hx.campus.fragment.message.MyMessagesFragment;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;


// 查看信息页面 - 提供失物和招领信息浏览入口
@Page(anim = CoreAnim.none)
public class LookFragment extends BaseFragment<FragmentTrendingBinding> implements SuperTextView.OnSuperTextViewClickListener {

    /**
     * 创建视图绑定对象
     * @param inflater 布局填充器
     * @param container 父容器
     * @param attachToRoot 是否附加到根布局
     * @return FragmentTrendingBinding 视图绑定实例
     */
    @NonNull
    @Override
    protected FragmentTrendingBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        // 使用FragmentTrendingBinding inflate方法创建绑定对象
        return FragmentTrendingBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化标题栏
     * @return TitleBar 标题栏对象，返回null表示不使用默认标题栏
     */
    @Override
    protected TitleBar initTitle() {
        // 查看信息页面不需要显示标题栏
        return null;
    }

    /**
     * 初始化视图控件
     * 当前页面主要通过XML布局定义控件，无需额外初始化
     */
    @Override
    protected void initViews() {
    }

    /**
     * 初始化事件监听器
     * 为失物和招领按钮设置点击监听
     */
    @Override
    protected void initListeners() {
        // 调用父类监听器初始化
        super.initListeners();
        // 为失物按钮设置点击监听器
        binding.lost.setOnSuperTextViewClickListener(this);
        // 为招领按钮设置点击监听器
        binding.found.setOnSuperTextViewClickListener(this);
        //"我的评论"设置点击监听器
        binding.myComments.setOnSuperTextViewClickListener(this);
        binding.myMessage.setOnSuperTextViewClickListener(this);

    }

    /**
     * 处理SuperTextView点击事件
     * 根据点击的按钮跳转到相应的信息页面
     * @param view 被点击的SuperTextView控件
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(SuperTextView view) {
        // 判断点击的是失物按钮
        if (view.getId() == binding.lost.getId()) {
            // 跳转到失物信息页面
            openNewPage(LostInfoFragment.class);
        } 
        // 判断点击的是招领按钮
        else if (view.getId() == binding.found.getId()) {
            // 跳转到招领信息页面
            openNewPage(FoundInfoFragment.class);
        }else if (view.getId() == binding.myComments.getId()) {
            // 跳转到我的评论页面
            openNewPage(MyCommentsFragment.class);
        } else if (view.getId()==binding.myMessage.getId()) {
            //我的留言页面
            openNewPage(MyMessagesFragment.class);
        }
    }
}

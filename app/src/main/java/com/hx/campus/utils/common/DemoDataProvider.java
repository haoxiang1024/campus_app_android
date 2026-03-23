package com.hx.campus.utils.common;


import android.content.Context;
import android.graphics.drawable.Drawable;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.NewInfo;
import com.xuexiang.xui.adapter.simple.AdapterItem;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.widget.banner.widget.banner.BannerItem;

import java.util.ArrayList;
import java.util.List;


public class DemoDataProvider {

    /**
     * 定义Handler常量
     */
    private static final int MSG_GET_NEWS_LIST_SUCCESS = 1;

    public static String[] titles = new String[]{
            "紧急通知",
            "意见反馈",
            "app闪退",
            "隐私",
    };


    public static String[] urls = new String[]{
            "https://gitee.com/hx_a/pic/raw/master/%E4%B8%8B%E8%BD%BD.png",//紧急通知"
            "https://gitee.com/hx_a/pic/raw/master/2026%E5%B9%B4-03%E6%9C%88-23%E6%97%A5_11%E6%97%B6-30%E5%88%86-03%E7%A7%92.png",//app帮助
            "https://gitee.com/hx_a/pic/raw/master/2026%E5%B9%B4-03%E6%9C%88-23%E6%97%A5_11%E6%97%B6-36%E5%88%86-15%E7%A7%92.png",//隐私
    };


    public static List<BannerItem> getBannerList() {
        List<BannerItem> list = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            BannerItem item = new BannerItem();
            item.imgUrl = urls[i];
            item.title = titles[i];
            list.add(item);
        }
        return list;
    }



    public static List<AdapterItem> getGridItems(Context context) {
        return getGridItems(context, R.array.grid_titles_entry, R.array.grid_icons_entry);
    }


    private static List<AdapterItem> getGridItems(Context context, int titleArrayId, int iconArrayId) {
        List<AdapterItem> list = new ArrayList<>();
        String[] titles =context.getResources().getStringArray(titleArrayId);
        Drawable[] icons = ResUtils.getDrawableArray(context, iconArrayId);
        for (int i = 0; i < titles.length; i++) {
            list.add(new AdapterItem(titles[i], icons[i]));
        }
        return list;
    }

    /**
     * 用于占位的空信息
     *
     * @return
     */
    
    public static List<NewInfo> getEmptyNewInfo() {
        List<NewInfo> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(new NewInfo());
        }
        return list;
    }

}

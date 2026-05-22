
package com.hx.campus.utils.common;

import android.content.Context;

import com.hx.campus.R;
import com.hx.campus.activity.LoginActivity;
import com.hx.campus.utils.Utils;
import com.umeng.analytics.MobclickAgent;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xutil.app.ActivityUtils;
import com.xuexiang.xutil.common.StringUtils;


public final class TokenUtils {

    private static final String KEY_TOKEN = "com.hx.campus.utils.KEY_TOKEN";
    private static final String KEY_PROFILE_CHANNEL = "github";
    private static String sToken;
    private static Context context;

    private static final String KEY_IM_TOKEN = "com.hx.campus.utils.KEY_IM_TOKEN";
    
    public static String getImToken() {
        return MMKVUtils.getString(KEY_IM_TOKEN, "");
    }

    
    public static void setImToken(String imToken) {
        MMKVUtils.put(KEY_IM_TOKEN, imToken);
    }
    public TokenUtils(Context context) {
        TokenUtils.context = context;
    }

    public void setContext(Context context) {
        TokenUtils.context = context;
    }

    private TokenUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    
    public static void init(Context context) {
        MMKVUtils.init(context);
        sToken = MMKVUtils.getString(KEY_TOKEN, "");
        TokenUtils.context = context;
    }

    public static void clearToken() {
        sToken = null;
        MMKVUtils.remove(KEY_TOKEN);
        MMKVUtils.remove(KEY_IM_TOKEN);
    }

    public static String getToken() {
        return sToken;
    }

    public static void setToken(String token) {
        sToken = token;
        MMKVUtils.put(KEY_TOKEN, token);
    }

    public static boolean hasToken() {
        return MMKVUtils.containsKey(KEY_TOKEN);
    }

    
    public static boolean handleLoginSuccess(String token) {
        if (!StringUtils.isEmpty(token)) {
            XToastUtils.success(Utils.getString(context, R.string.login_su));
            MobclickAgent.onProfileSignIn(KEY_PROFILE_CHANNEL, token);
            setToken(token);
            return true;
        } else {
            XToastUtils.success(Utils.getString(context, R.string.logout_failed));
            return false;
        }
    }

    
    public static void handleLogoutSuccess() {
        MobclickAgent.onProfileSignOff();

        clearToken();

        XToastUtils.success(Utils.getString(context, R.string.logout_success));
        SettingUtils.setIsAgreePrivacy(false);

        ActivityUtils.startActivity(LoginActivity.class);
    }

}

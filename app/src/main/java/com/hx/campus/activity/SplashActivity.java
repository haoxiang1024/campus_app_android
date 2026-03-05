package com.hx.campus.activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.hx.campus.utils.common.LanguageUtil;
import com.hx.campus.utils.common.TokenUtils;
import com.xuexiang.xrouter.utils.TextUtils;

public class SplashActivity extends AppCompatActivity {
    public static final String LANG_KEY = "language";//语言设置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginOrGoMainPage();
        darkMOde();

    }

    private void darkMOde() {
        SharedPreferences sp = getSharedPreferences("config_settings", Context.MODE_PRIVATE);
        boolean isFollow = sp.getBoolean("is_follow_system", true);
        if (isFollow) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void loginOrGoMainPage() {
        //通过用户令牌的操作
        if (TokenUtils.hasToken()) {
            //用户已经登录过，读取语言设置
            String language = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this).getString(LANG_KEY, "");
            if (!TextUtils.isEmpty(language)) {
                LanguageUtil.changeAppLanguage(SplashActivity.this, language, MainActivity.class);//更改语言设置
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}



/**
 * 应用核心工具类
 * 提供各种常用的工具方法，包括隐私政策处理、页面跳转、颜色处理等
 * 采用单例模式设计，防止被实例化
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2024
 */
package com.hx.campus.utils;

import static com.hx.campus.core.webview.AgentWebFragment.KEY_URL;
import static com.hx.campus.fragment.other.ServiceProtocolFragment.KEY_IS_IMMERSIVE;
import static com.hx.campus.fragment.other.ServiceProtocolFragment.KEY_PROTOCOL_TITLE;
import static com.xuexiang.xutil.XUtil.getContext;
import static com.xuexiang.xutil.XUtil.runOnUiThread;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.hx.campus.R;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.webview.AgentWebActivity;
import com.hx.campus.fragment.other.ServiceProtocolFragment;
import com.hx.campus.utils.common.PropertiesUtil;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.core.PageOption;
import com.xuexiang.xui.utils.ColorUtils;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.toast.XToast;
import com.xuexiang.xutil.XUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 工具类集合，提供应用所需的各类辅助方法
 * 使用final修饰防止继承，构造函数私有化防止实例化
 */
public final class Utils {

    /** 应用隐私政策网页地址 */
    private static final String PRIVACY_URL = "https://gitee.com/hx_a/campus_app_android/blob/master/LICENSE";

    /**
     * 私有构造函数
     * 防止工具类被实例化
     */
    private Utils() {
        throw new UnsupportedOperationException("工具类不允许被实例化");
    }

    /**
     * 显示隐私政策确认对话框
     * 实现用户隐私政策同意流程，包含多次确认机制
     * 
     * @param context 上下文环境
     * @param submitListener 用户同意隐私政策的回调监听器
     * @return 创建的对话框实例
     */
    public static Dialog showPrivacyDialog(Context context, MaterialDialog.SingleButtonCallback submitListener) {
        // 构建隐私政策确认对话框
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.title_reminder)
                .autoDismiss(false)
                .cancelable(false)
                .positiveText(R.string.lab_agree)
                .onPositive((dialog1, which) -> {
                    if (submitListener != null) {
                        submitListener.onClick(dialog1, which);
                    } else {
                        dialog1.dismiss();
                    }
                })
                .negativeText(R.string.lab_disagree)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        // 用户拒绝后再次确认
                        DialogLoader.getInstance().showConfirmDialog(
                                context, 
                                ResUtils.getString(R.string.title_reminder), 
                                String.format(ResUtils.getString(R.string.content_privacy_explain_again), ResUtils.getString(R.string.app_name)), 
                                ResUtils.getString(R.string.lab_look_again),
                                (dialog3, which2) -> {
                                    dialog3.dismiss();
                                    // 重新显示隐私政策对话框
                                    showPrivacyDialog(context, submitListener);
                                },
                                ResUtils.getString(R.string.lab_still_disagree),
                                (dialog2, which1) -> {
                                    dialog2.dismiss();
                                    // 最后一次确认机会
                                    DialogLoader.getInstance().showConfirmDialog(
                                            context,
                                            ResUtils.getString(R.string.content_think_about_it_again),
                                            ResUtils.getString(R.string.lab_look_again),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog2, int which1) {
                                                    dialog2.dismiss();
                                                    showPrivacyDialog(context, submitListener);
                                                }
                                            },
                                            ResUtils.getString(R.string.lab_exit_app),
                                            (dialog4, which3) -> {
                                                dialog4.dismiss();
                                                // 用户最终拒绝，退出应用
                                                XUtil.exitApp();
                                            });
                                });
                    }
                }).build();
        // 设置隐私政策内容
        dialog.setContent(getPrivacyContent(context));
        // 启用链接点击功能
        dialog.getContentView().setMovementMethod(LinkMovementMethod.getInstance());
        dialog.show();
        return dialog;
    }

    /**
     * 构造隐私政策说明文本
     * 包含可点击的超链接，引导用户查看完整隐私政策
     * 
     * @param context 上下文环境
     * @return 格式化的隐私政策说明文本
     */
    private static SpannableStringBuilder getPrivacyContent(Context context) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder()
                .append("    欢迎来到").append(ResUtils.getString(R.string.app_name)).append("!\n")
                .append("    我们深知个人信息对你的重要性，也感谢你对我们的信任。\n")
                .append("    为了更好地保护你的权益，同时遵守相关监管的要求，我们将通过");
        
        // 添加可点击的隐私政策链接
        stringBuilder.append(getPrivacyLink(context, PRIVACY_URL))
                .append("向你说明我们会如何收集、存储、保护、使用及对外提供你的信息，并说明你享有的权利。\n")
                .append("    更多详情，敬请查阅")
                .append(getPrivacyLink(context, PRIVACY_URL))
                .append("全文。\n");
        
        return stringBuilder;
    }

    /**
     * 创建可点击的隐私政策链接文本
     * 
     * @param context 上下文环境
     * @param privacyUrl 隐私政策网页地址
     * @return 可点击的SpannableString对象
     */
    private static SpannableString getPrivacyLink(Context context, String privacyUrl) {
        String privacyName = String.format(ResUtils.getString(R.string.lab_privacy_name), ResUtils.getString(R.string.app_name));
        SpannableString spannableString = new SpannableString(privacyName);
        
        // 设置可点击的链接样式
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // 点击后跳转到隐私政策网页
                goWeb(context, privacyUrl);
            }
        }, 0, privacyName.length(), Spanned.SPAN_MARK_MARK);
        
        return spannableString;
    }


    /**
     * 在内置WebView中打开网页
     * 使用应用内浏览器而非系统浏览器，提供更好的用户体验
     * 
     * @param context 上下文环境
     * @param url 要打开的网页地址
     */
    public static void goWeb(Context context, final String url) {
        Intent intent = new Intent(context, AgentWebActivity.class);
        intent.putExtra(KEY_URL, url);
        context.startActivity(intent);
    }


    /**
     * 跳转到协议页面（用户协议或隐私协议）
     * 
     * @param fragment 当前Fragment实例
     * @param isPrivacy true表示隐私协议，false表示用户协议
     * @param isImmersive 是否使用沉浸式状态栏
     */
    public static void gotoProtocol(XPageFragment fragment, boolean isPrivacy, boolean isImmersive) {
        PageOption.to(ServiceProtocolFragment.class)
                .putString(KEY_PROTOCOL_TITLE, isPrivacy ? ResUtils.getString(R.string.title_privacy_protocol) : ResUtils.getString(R.string.title_user_protocol))
                .putBoolean(KEY_IS_IMMERSIVE, isImmersive)
                .open(fragment);
    }

    /**
     * 判断颜色是否为深色
     * 用于自动调整文字颜色以确保良好的对比度
     * 
     * @param color 要判断的颜色值
     * @return true表示深色，false表示浅色
     */
    public static boolean isColorDark(@ColorInt int color) {
        return ColorUtils.isColorDark(color, 0.382);
    }

    /**
     * @param reurl   需要构造的url
     * @param context 上下文
     * @return 返回url
     */


    public static String rebuildUrl(String reurl, Context context) {
        //读取url资源文件
        String endUrl = "";
        PropertiesUtil propertiesUtil = new PropertiesUtil();
        Properties properties = propertiesUtil.LoadProperties(context);
        String url = properties.getProperty("url");
        //改造url
        if (url != null) {
            endUrl = url + reurl;
        }
        return endUrl;
    }
    //获取key
    public static String getAppKey(Context context) {
        Properties properties = new Properties();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
            return properties.getProperty("IM_APP_KEY");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //时间格式转换
    public static String dateFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    //存储Javabean
    public static <T> void saveBean2Sp(Context context, T t, String fileName, String keyName) {
        SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        ByteArrayOutputStream bos;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(t);
            byte[] bytes = bos.toByteArray();
            String ObjStr = Base64.encodeToString(bytes, Base64.DEFAULT);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(keyName, ObjStr);
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.flush();
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //读取javabean
    public static <T extends Object> T getBeanFromSp(Context context, String fileName, String keyNme) {
        SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        byte[] bytes = Base64.decode(preferences.getString(keyNme, ""), Base64.DEFAULT);
        ByteArrayInputStream bis;
        ObjectInputStream ois = null;
        T obj = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    /**
     * 图片真实路径获取
     */
    public static String getRealPath(Context context, Intent data) {
        // 判断手机系统版本号
        if (Build.VERSION.SDK_INT >= 19) {
            // 4.4及以上系统使用这个方法处理图片
            return handleImageOnKitKat(context, data);
        } else {
            // 4.4以下系统使用这个方法处理图片
            return handleImageBeforeKitKat(context, data);
        }
    }

    @TargetApi(19)
    private static String handleImageOnKitKat(Context context, Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public downloads"), Long.valueOf(docId));
                imagePath = getImagePath(context, contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(context, uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        //displayImage(imagePath); // 根据图片路径显示图片
        return imagePath;
    }

    private static String handleImageBeforeKitKat(Context context, Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(context, uri, null);
        return imagePath;
    }


    @SuppressLint("Range")
    private static String getImagePath(Context context, Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //ui操作，提示框
    public static void showResponse(final String response) {
        runOnUiThread(() -> {
            // 在这里进行UI操作，将结果显示到界面上
            XToast.info(getContext(), response).show();
        });
    }
    //获取服务端返回的用户数据并存储
    public static void doUserData(User user) {
        if (user == null) return;
        String photoUrl = user.getPhoto();
        if (!photoUrl.startsWith("http")) {
            // 如果不是以 http 开头，说明是文件名，需要手动拼接
            photoUrl = Utils.rebuildUrl("upload/" + photoUrl, getContext());
            user.setPhoto(photoUrl);
        }
        //存储SharedPreferences以便之后调用
        Utils.saveBean2Sp(getContext(), user, "User", "user");
    }

    /**
     * 从 assets/config.properties 文件中读取指定的 key 值
     */
    public static String getPropertyFromAssets(Context context, String key) {
        String value = "";
        try {
            java.util.Properties properties = new java.util.Properties();
            java.io.InputStream inputStream = context.getAssets().open("config.properties");
            properties.load(inputStream);
            value = properties.getProperty(key);
            inputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Log.e("Utils", "读取 config.properties 失败");
        }
        return value;
    }

    //根据资源id读取字符串
    public static String getString(Context context, int id) {
        return context.getResources().getString(id);
    }
    public static String getUrlFromAssets(Context context) {
        Properties properties = new Properties();
        try {
            InputStream is = context.getAssets().open("url.properties");
            properties.load(is);
            String url = properties.getProperty("url");
            // 确保以 / 结尾，Retrofit 的 baseUrl 要求必须以斜杠结尾
            if (url != null && !url.endsWith("/")) {
                url += "/";
            }
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return "http://192.168.254.122:8081/school/"; // 备选默认值
        }
    }
    /**
     * 智能时间格式化
     *
     */
    public static String formatCommentTime(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            Date date = sdf.parse(timeStr);
            if (date == null) return timeStr;

            long time = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - time;

            if (diff < 60 * 1000) {
                return "刚刚";
            } else if (diff < 60 * 60 * 1000) {
                return (diff / (60 * 1000)) + "分钟前";
            } else if (diff < 24 * 60 * 60 * 1000) {
                return (diff / (60 * 60 * 1000)) + "小时前";
            } else {
                Calendar commentCal = Calendar.getInstance();
                commentCal.setTime(date);
                Calendar nowCal = Calendar.getInstance();

                // 判断是否是昨天
                nowCal.add(Calendar.DAY_OF_YEAR, -1);
                if (commentCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                        commentCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) {
                    SimpleDateFormat hourSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    return "昨天 " + hourSdf.format(date);
                }

                // 判断是否在3天内 (包含昨天的话算2天前，这里再看是否是前天)
                long days = diff / (24 * 60 * 60 * 1000);
                if (days < 3) {
                    return days + "天前";
                } else {
                    // 超过3天，显示 "MM-dd"
                    SimpleDateFormat monthDaySdf = new SimpleDateFormat("M-d", Locale.getDefault());
                    return monthDaySdf.format(date);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return timeStr;
        }
    }
    /**
     * @param oldPic 原图地址
     * @return 新图地址
     */
    public static String getImageUrl(String oldPic, Context context) {
        Pattern pattern = Pattern.compile(".*http.*");
        Matcher matcher = pattern.matcher(oldPic); // 将正则表达式应用到输入字符串上
        String savePath = getUrlFromAssets(context)+"upload/";
        //String savePath = "http://123.207.51.104:8081/school/upload/";
        if (!matcher.matches()) {
            return savePath+oldPic;
        }
        return oldPic;
    }



}



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



public final class Utils {

    
    private static final String PRIVACY_URL = "https://gitee.com/hx_a/campus_app_android/blob/master/LICENSE";

    
    private Utils() {
        throw new UnsupportedOperationException("工具类不允许被实例化");
    }

    
    public static Dialog showPrivacyDialog(Context context, MaterialDialog.SingleButtonCallback submitListener) {

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

                        DialogLoader.getInstance().showConfirmDialog(
                                context, 
                                ResUtils.getString(R.string.title_reminder), 
                                String.format(ResUtils.getString(R.string.content_privacy_explain_again), ResUtils.getString(R.string.app_name)), 
                                ResUtils.getString(R.string.lab_look_again),
                                (dialog3, which2) -> {
                                    dialog3.dismiss();

                                    showPrivacyDialog(context, submitListener);
                                },
                                ResUtils.getString(R.string.lab_still_disagree),
                                (dialog2, which1) -> {
                                    dialog2.dismiss();

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

                                                XUtil.exitApp();
                                            });
                                });
                    }
                }).build();

        dialog.setContent(getPrivacyContent(context));

        dialog.getContentView().setMovementMethod(LinkMovementMethod.getInstance());
        dialog.show();
        return dialog;
    }

    
    private static SpannableStringBuilder getPrivacyContent(Context context) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder()
                .append("    欢迎来到").append(ResUtils.getString(R.string.app_name)).append("!\n")
                .append("    我们深知个人信息对你的重要性，也感谢你对我们的信任。\n")
                .append("    为了更好地保护你的权益，同时遵守相关监管的要求，我们将通过");
        

        stringBuilder.append(getPrivacyLink(context, PRIVACY_URL))
                .append("向你说明我们会如何收集、存储、保护、使用及对外提供你的信息，并说明你享有的权利。\n")
                .append("    更多详情，敬请查阅")
                .append(getPrivacyLink(context, PRIVACY_URL))
                .append("全文。\n");
        
        return stringBuilder;
    }

    
    private static SpannableString getPrivacyLink(Context context, String privacyUrl) {
        String privacyName = String.format(ResUtils.getString(R.string.lab_privacy_name), ResUtils.getString(R.string.app_name));
        SpannableString spannableString = new SpannableString(privacyName);
        

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

                goWeb(context, privacyUrl);
            }
        }, 0, privacyName.length(), Spanned.SPAN_MARK_MARK);
        
        return spannableString;
    }


    
    public static void goWeb(Context context, final String url) {
        Intent intent = new Intent(context, AgentWebActivity.class);
        intent.putExtra(KEY_URL, url);
        context.startActivity(intent);
    }


    
    public static void gotoProtocol(XPageFragment fragment, boolean isPrivacy, boolean isImmersive) {
        PageOption.to(ServiceProtocolFragment.class)
                .putString(KEY_PROTOCOL_TITLE, isPrivacy ? ResUtils.getString(R.string.title_privacy_protocol) : ResUtils.getString(R.string.title_user_protocol))
                .putBoolean(KEY_IS_IMMERSIVE, isImmersive)
                .open(fragment);
    }

    
    public static boolean isColorDark(@ColorInt int color) {
        return ColorUtils.isColorDark(color, 0.382);
    }

    


    public static String rebuildUrl(String reurl, Context context) {

        String endUrl = "";
        PropertiesUtil propertiesUtil = new PropertiesUtil();
        Properties properties = propertiesUtil.LoadProperties(context);
        String url = properties.getProperty("url");

        if (url != null) {
            endUrl = url + reurl;
        }
        return endUrl;
    }

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


    public static String dateFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }


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

    
    public static String getRealPath(Context context, Intent data) {

        if (Build.VERSION.SDK_INT >= 19) {

            return handleImageOnKitKat(context, data);
        } else {

            return handleImageBeforeKitKat(context, data);
        }
    }

    @TargetApi(19)
    private static String handleImageOnKitKat(Context context, Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(context, uri)) {

            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public downloads"), Long.valueOf(docId));
                imagePath = getImagePath(context, contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            imagePath = getImagePath(context, uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {

            imagePath = uri.getPath();
        }

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

        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    public static void showResponse(final String response) {
        runOnUiThread(() -> {

            XToast.info(getContext(), response).show();
        });
    }

    public static void doUserData(User user) {
        if (user == null) return;
        String photoUrl = user.getPhoto();
        if (!photoUrl.startsWith("http")) {

            photoUrl = Utils.rebuildUrl("upload/" + photoUrl, getContext());
            user.setPhoto(photoUrl);
        }

        Utils.saveBean2Sp(getContext(), user, "User", "user");
    }

    
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


    public static String getString(Context context, int id) {
        return context.getResources().getString(id);
    }
    public static String getUrlFromAssets(Context context) {
        Properties properties = new Properties();
        try {
            InputStream is = context.getAssets().open("url.properties");
            properties.load(is);
            String url = properties.getProperty("url");

            if (url != null && !url.endsWith("/")) {
                url += "/";
            }
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return "http://192.168.254.122:8081/school/"; // 备选默认值
        }
    }
    
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


                nowCal.add(Calendar.DAY_OF_YEAR, -1);
                if (commentCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                        commentCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) {
                    SimpleDateFormat hourSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    return "昨天 " + hourSdf.format(date);
                }


                long days = diff / (24 * 60 * 60 * 1000);
                if (days < 3) {
                    return days + "天前";
                } else {

                    SimpleDateFormat monthDaySdf = new SimpleDateFormat("M-d", Locale.getDefault());
                    return monthDaySdf.format(date);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return timeStr;
        }
    }
    
    public static String getImageUrl(String oldPic, Context context) {
        Pattern pattern = Pattern.compile(".*http.*");
        Matcher matcher = pattern.matcher(oldPic);
        String savePath = getUrlFromAssets(context)+"upload/";

        if (!matcher.matches()) {
            return savePath+oldPic;
        }
        return oldPic;
    }



}

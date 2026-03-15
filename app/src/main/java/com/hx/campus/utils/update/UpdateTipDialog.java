
package com.hx.campus.utils.update;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hx.campus.utils.Utils;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xupdate.XUpdate;


public class UpdateTipDialog extends AppCompatActivity implements DialogInterface.OnDismissListener {

    public static final String KEY_CONTENT = "com.hx.campus.utils.update.KEY_CONTENT";

    public static final String DOWNLOAD_TYPE_NAME = "gitee";

    private static final String DOWNLOAD_URL = "https://gitee.com/hx_a/campus_app_android/releases/tag/1.0";

    /**
     * 显示版本更新重试提示弹窗
     *
     * @param content
     */
    public static void show(String content) {
        Intent intent = new Intent(XUpdate.getContext(), UpdateTipDialog.class);
        intent.putExtra(KEY_CONTENT, content);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        XUpdate.getContext().startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String content = getIntent().getStringExtra(KEY_CONTENT);
        if (TextUtils.isEmpty(content)) {
            content = "应用下载速度太慢了，是否考虑切换" + DOWNLOAD_TYPE_NAME + "下载？";
        }

        DialogLoader.getInstance().showConfirmDialog(this, content, "是", (dialog, which) -> {
                    dialog.dismiss();
                    Utils.goWeb(UpdateTipDialog.this, DOWNLOAD_URL);
                }, "否")
                .setOnDismissListener(this);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

}

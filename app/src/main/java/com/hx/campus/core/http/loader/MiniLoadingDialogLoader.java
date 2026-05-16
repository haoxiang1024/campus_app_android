package com.hx.campus.core.http.loader;

import android.content.Context;
import android.content.DialogInterface;

import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader;
import com.xuexiang.xhttp2.subsciber.impl.OnProgressCancelListener;
import com.xuexiang.xui.widget.dialog.MiniLoadingDialog;


public class MiniLoadingDialogLoader implements IProgressLoader {
    
    private final MiniLoadingDialog mDialog;
    
    private OnProgressCancelListener mOnProgressCancelListener;

    public MiniLoadingDialogLoader(Context context) {
        this(context, "请求中...");
    }

    public MiniLoadingDialogLoader(Context context, String msg) {
        mDialog = new MiniLoadingDialog(context, msg);
    }

    @Override
    public boolean isLoading() {
        return mDialog != null && mDialog.isShowing();
    }

    @Override
    public void updateMessage(String msg) {
        if (mDialog != null) {
            mDialog.updateMessage(msg);
        }
    }

    @Override
    public void showLoading() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    @Override
    public void dismissLoading() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void setCancelable(boolean flag) {
        mDialog.setCancelable(flag);
        if (flag) {
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if (mOnProgressCancelListener != null) {
                        mOnProgressCancelListener.onCancelProgress();
                    }
                }
            });
        }
    }

    @Override
    public void setOnProgressCancelListener(OnProgressCancelListener listener) {
        mOnProgressCancelListener = listener;
    }
}

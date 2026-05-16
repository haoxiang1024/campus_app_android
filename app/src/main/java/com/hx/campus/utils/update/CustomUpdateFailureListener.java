
package com.hx.campus.utils.update;

import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;


public class CustomUpdateFailureListener implements OnUpdateFailureListener {

    
    private final boolean mNeedErrorTip;

    public CustomUpdateFailureListener() {
        this(true);
    }

    public CustomUpdateFailureListener(boolean needErrorTip) {
        mNeedErrorTip = needErrorTip;
    }

    
    @Override
    public void onFailure(UpdateError error) {
        if (mNeedErrorTip) {
            XToastUtils.error(error.getDetailMsg());
        }
        if (error.getCode() == UpdateError.ERROR.DOWNLOAD_FAILED) {
            UpdateTipDialog.show("应用下载失败，是否考虑切换" + UpdateTipDialog.DOWNLOAD_TYPE_NAME + "下载？");
        }
    }
}

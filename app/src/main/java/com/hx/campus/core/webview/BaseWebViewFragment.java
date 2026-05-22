

package com.hx.campus.core.webview;

import android.view.KeyEvent;

import com.hx.campus.core.BaseFragment;
import com.just.agentweb.core.AgentWeb;


public abstract class BaseWebViewFragment extends BaseFragment {

    protected AgentWeb mAgentWeb;


    @Override
    public void onResume() {
        if (mAgentWeb != null) {

            mAgentWeb.getWebLifeCycle().onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mAgentWeb != null) {

            mAgentWeb.getWebLifeCycle().onPause();
        }
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mAgentWeb != null && mAgentWeb.handleKeyEvent(keyCode, event);
    }

    @Override
    public void onDestroyView() {
        if (mAgentWeb != null) {
            mAgentWeb.destroy();
        }
        super.onDestroyView();
    }
}

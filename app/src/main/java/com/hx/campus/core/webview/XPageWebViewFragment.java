

package com.hx.campus.core.webview;

import static com.hx.campus.core.webview.AgentWebFragment.KEY_URL;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.hx.campus.MyApp;
import com.hx.campus.R;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentAgentwebBinding;
import com.just.agentweb.action.PermissionInterceptor;
import com.just.agentweb.core.AgentWeb;
import com.just.agentweb.core.client.DefaultWebClient;
import com.just.agentweb.core.client.MiddlewareWebChromeBase;
import com.just.agentweb.core.client.MiddlewareWebClientBase;
import com.just.agentweb.core.client.WebListenerManager;
import com.just.agentweb.core.web.AbsAgentWebSettings;
import com.just.agentweb.core.web.AgentWebConfig;
import com.just.agentweb.core.web.IAgentWebSettings;
import com.just.agentweb.download.AgentWebDownloader;
import com.just.agentweb.download.DefaultDownloadImpl;
import com.just.agentweb.download.DownloadListenerAdapter;
import com.just.agentweb.download.DownloadingService;
import com.just.agentweb.widget.IWebLayout;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageActivity;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.core.PageOption;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xutil.common.logger.Logger;
import com.xuexiang.xutil.net.JsonUtil;

import java.util.HashMap;


@Page(params = {KEY_URL})
public class XPageWebViewFragment extends BaseFragment<FragmentAgentwebBinding> implements View.OnClickListener {

    protected AgentWeb mAgentWeb;
    
    private final PopupMenu.OnMenuItemClickListener mOnMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.refresh) {
                if (mAgentWeb != null) {
                    mAgentWeb.getUrlLoader().reload();
                }
                return true;
            } else if (id == R.id.copy) {
                if (mAgentWeb != null) {
                    toCopy(getContext(), mAgentWeb.getWebCreator().getWebView().getUrl());
                }
                return true;
            } else if (id == R.id.default_browser) {
                if (mAgentWeb != null) {
                    openBrowser(mAgentWeb.getWebCreator().getWebView().getUrl());
                }
                return true;
            } else if (id == R.id.share) {
                if (mAgentWeb != null) {
                    shareWebUrl(mAgentWeb.getWebCreator().getWebView().getUrl());
                }
                return true;
            }
            return false;
        }
    };
    
    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                if (title.length() > 10) {
                    title = title.substring(0, 10).concat("...");
                }
                binding.includeTitle.toolbarTitle.setText(title);
            }
        }
    };
    
    protected WebViewClient mWebViewClient = new WebViewClient() {
        private final HashMap<String, Long> mTimer = new HashMap<>();

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl() + "");
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {



            return url.startsWith("intent://") && url.contains("com.youku.phone");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mTimer.put(url, System.currentTimeMillis());
            if (url.equals(getUrl())) {
                pageNavigator(View.GONE);
            } else {
                pageNavigator(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mTimer.get(url) != null) {
                long overTime = System.currentTimeMillis();
                Long startTime = mTimer.get(url);

                Logger.i(" page mUrl:" + url + "  used time:" + (overTime - startTime));
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    };
    
    protected PermissionInterceptor mPermissionInterceptor = new PermissionInterceptor() {
        
        @Override
        public boolean intercept(String url, String[] permissions, String action) {
            Logger.i("mUrl:" + url + "  permission:" + JsonUtil.toJson(permissions) + " action:" + action);
            return false;
        }
    };
    private PopupMenu mPopupMenu;
    private DownloadingService mDownloadingService;
    
    protected DownloadListenerAdapter mDownloadListenerAdapter = new DownloadListenerAdapter() {
        
        @Override
        public boolean onStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength, AgentWebDownloader.Extra extra) {
            Logger.i("onStart:" + url);

            extra.setOpenBreakPointDownload(true)

                    .setIcon(R.drawable.ic_file_download_black_24dp)

                    .setConnectTimeOut(6000)

                    .setBlockMaxTime(10 * 60 * 1000)

                    .setDownloadTimeOut(Long.MAX_VALUE)

                    .setParallelDownload(false)

                    .setEnableIndicator(true)

                    .addHeader("Cookie", "xx")

                    .setAutoOpen(true)

                    .setForceDownload(true);
            return false;
        }

        
        @Override
        public void onBindService(String url, DownloadingService downloadingService) {
            super.onBindService(url, downloadingService);
            mDownloadingService = downloadingService;
            Logger.i("onBindService:" + url + "  DownloadingService:" + downloadingService);
        }

        
        @Override
        public void onUnbindService(String url, DownloadingService downloadingService) {
            super.onUnbindService(url, downloadingService);
            mDownloadingService = null;
            Logger.i("onUnbindService:" + url);
        }

        
        @Override
        public void onProgress(String url, long loaded, long length, long usedTime) {
            int mProgress = (int) ((loaded) / (float) length * 100);
            Logger.i("onProgress:" + mProgress);
            super.onProgress(url, loaded, length, usedTime);
        }

        
        @Override
        public boolean onResult(String path, String url, Throwable throwable) {

            if (null == throwable) {

            } else {

            }

            return false;
        }
    };

    
    public static Fragment openUrl(XPageActivity xPageActivity, String url) {
        return PageOption.to(XPageWebViewFragment.class)
                .putString(KEY_URL, url)
                .open(xPageActivity);
    }

    
    public static Fragment openUrl(XPageFragment fragment, String url) {
        return PageOption.to(XPageWebViewFragment.class)
                .setNewActivity(true)
                .putString(KEY_URL, url)
                .open(fragment);
    }



    @NonNull
    @Override
    protected FragmentAgentwebBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentAgentwebBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected TitleBar initTitle() {
        return null;
    }

    
    @Override
    protected void initViews() {
        mAgentWeb = AgentWeb.with(this)

                .setAgentWebParent((LinearLayout) getRootView(), -1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

                .useDefaultIndicator(-1, 3)

                .setAgentWebWebSettings(getSettings())

                .setWebViewClient(mWebViewClient)

                .setWebChromeClient(mWebChromeClient)

                .useMiddlewareWebChrome(getMiddlewareWebChrome())

                .useMiddlewareWebClient(getMiddlewareWebClient())

                .setPermissionInterceptor(mPermissionInterceptor)

                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)

                .setAgentWebUIController(new UIController(getActivity()))

                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setWebLayout(getWebLayout())

                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)

                .interceptUnkownUrl()

                .createAgentWeb()
                .ready()

                .go(getUrl());

        if (MyApp.isDebug()) {
            AgentWebConfig.debug();
        }

        pageNavigator(View.GONE);

        addBackgroundChild(mAgentWeb.getWebCreator().getWebParentLayout());


        mAgentWeb.getWebCreator().getWebView().setOverScrollMode(WebView.OVER_SCROLL_NEVER);
    }



    protected IWebLayout getWebLayout() {
        return new WebLayout(getActivity());
    }

    protected void addBackgroundChild(FrameLayout frameLayout) {
        TextView textView = new TextView(frameLayout.getContext());
        textView.setText("技术由 AgentWeb 提供");
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#727779"));
        frameLayout.setBackgroundColor(Color.parseColor("#272b2d"));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        final float scale = frameLayout.getContext().getResources().getDisplayMetrics().density;
        params.topMargin = (int) (15 * scale + 0.5f);
        frameLayout.addView(textView, 0, params);
    }



    @Override
    protected void initListeners() {
        binding.includeTitle.ivBack.setOnClickListener(this);
        binding.includeTitle.ivFinish.setOnClickListener(this);
        binding.includeTitle.ivMore.setOnClickListener(this);
    }

    private void pageNavigator(int tag) {

        binding.includeTitle.ivBack.setVisibility(tag);
        binding.includeTitle.viewLine.setVisibility(tag);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {

            if (!mAgentWeb.back()) {
                popToBack();
            }
        } else if (id == R.id.iv_finish) {
            popToBack();
        } else if (id == R.id.iv_more) {
            showPoPup(view);
        }
    }



    
    public IAgentWebSettings getSettings() {
        return new AbsAgentWebSettings() {
            private AgentWeb mAgentWeb;

            @Override
            protected void bindAgentWebSupport(AgentWeb agentWeb) {
                this.mAgentWeb = agentWeb;
            }

            
            @Override
            public WebListenerManager setDownloader(WebView webView, android.webkit.DownloadListener downloadListener) {
                return super.setDownloader(webView,
                        DefaultDownloadImpl
                                .create(getActivity(),
                                        webView,
                                        mDownloadListenerAdapter,
                                        mDownloadListenerAdapter,
                                        mAgentWeb.getPermissionInterceptor()));
            }
        };
    }

    
    public String getUrl() {
        String target = "";
        Bundle bundle = getArguments();
        if (bundle != null) {
            target = bundle.getString(KEY_URL);
        }

        if (TextUtils.isEmpty(target)) {
            target = "https://gitee.com/hx_a";
        }
        return target;
    }

    
    private void showPoPup(View view) {
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(getContext(), view);
            mPopupMenu.inflate(R.menu.menu_toolbar_web);
            mPopupMenu.setOnMenuItemClickListener(mOnMenuItemClickListener);
        }
        mPopupMenu.show();
    }

    
    private void openBrowser(String targetUrl) {
        if (TextUtils.isEmpty(targetUrl) || targetUrl.startsWith("file://")) {
            XToastUtils.toast(targetUrl + " 该链接无法使用浏览器打开。");
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(targetUrl);
        intent.setData(uri);
        startActivity(intent);
    }

    
    private void shareWebUrl(String url) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }



    
    private void toCopy(Context context, String text) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) {
            return;
        }
        manager.setPrimaryClip(ClipData.newPlainText(null, text));
    }

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

    
    protected MiddlewareWebClientBase getMiddlewareWebClient() {
        return new MiddlewareWebViewClient() {
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("agentweb")) {
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        };
    }

    protected MiddlewareWebChromeBase getMiddlewareWebChrome() {
        return new MiddlewareChromeClient() {
        };
    }


}

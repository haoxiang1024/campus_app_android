

package com.hx.campus.core.webview;


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
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.hx.campus.MyApp;
import com.hx.campus.R;
import com.just.agentweb.action.PermissionInterceptor;
import com.just.agentweb.core.AgentWeb;
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
import com.just.agentweb.utils.LogUtils;
import com.just.agentweb.widget.IWebLayout;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xutil.net.JsonUtil;

import java.util.HashMap;


public class AgentWebFragment extends Fragment implements FragmentKeyDown {
    public static final String KEY_URL = "com.xuexiang.xuidemo.base.webview.key_url";
    public static final String TAG = AgentWebFragment.class.getSimpleName();
    
    protected PermissionInterceptor mPermissionInterceptor = new PermissionInterceptor() {
        
        @Override
        public boolean intercept(String url, String[] permissions, String action) {
            Log.i(TAG, "mUrl:" + url + "  permission:" + JsonUtil.toJson(permissions) + " action:" + action);
            return false;
        }
    };
    private ImageView mBackImageView;
    private View mLineView;
    protected WebViewClient mWebViewClient = new WebViewClient() {

        private final HashMap<String, Long> timer = new HashMap<>();

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
            Log.i(TAG, "mUrl:" + url + " onPageStarted  target:" + getUrl());
            timer.put(url, System.currentTimeMillis());
            if (url.equals(getUrl())) {
                pageNavigator(View.GONE);
            } else {
                pageNavigator(View.VISIBLE);
            }

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (timer.get(url) != null) {
                long overTime = System.currentTimeMillis();
                Long startTime = timer.get(url);
                Log.i(TAG, "  page mUrl:" + url + "  used time:" + (overTime - startTime));
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
    private ImageView mFinishImageView;
    private TextView mTitleTextView;
    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.i(TAG, "onProgressChanged:" + newProgress + "  view:" + view);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mTitleTextView != null && !TextUtils.isEmpty(title)) {
                if (title.length() > 10) {
                    title = title.substring(0, 10).concat("...");
                }
                mTitleTextView.setText(title);
            }
        }
    };
    private AgentWeb mAgentWeb;
    
    private final PopupMenu.OnMenuItemClickListener mOnMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.refresh:
                    if (mAgentWeb != null) {
                        mAgentWeb.getUrlLoader().reload();
                    }
                    return true;

                case R.id.copy:
                    if (mAgentWeb != null) {
                        toCopy(getContext(), mAgentWeb.getWebCreator().getWebView().getUrl());
                    }
                    return true;
                case R.id.default_browser:
                    if (mAgentWeb != null) {
                        openBrowser(mAgentWeb.getWebCreator().getWebView().getUrl());
                    }
                    return true;
                case R.id.share:
                    if (mAgentWeb != null) {
                        shareWebUrl(mAgentWeb.getWebCreator().getWebView().getUrl());
                    }
                    return true;
                default:
                    return false;
            }

        }
    };
    private ImageView mMoreImageView;
    private PopupMenu mPopupMenu;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_back:

                    if (!mAgentWeb.back()) {
                        AgentWebFragment.this.getActivity().finish();
                    }
                    break;
                case R.id.iv_finish:
                    AgentWebFragment.this.getActivity().finish();
                    break;
                case R.id.iv_more:
                    showPoPup(v);
                    break;
                default:
                    break;

            }
        }

    };
    private DownloadingService mDownloadingService;
    
    protected DownloadListenerAdapter mDownloadListenerAdapter = new DownloadListenerAdapter() {
        
        @Override
        public boolean onStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength, AgentWebDownloader.Extra extra) {
            LogUtils.i(TAG, "onStart:" + url);

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
            LogUtils.i(TAG, "onBindService:" + url + "  DownloadingService:" + downloadingService);
        }

        
        @Override
        public void onUnbindService(String url, DownloadingService downloadingService) {
            super.onUnbindService(url, downloadingService);
            mDownloadingService = null;
            LogUtils.i(TAG, "onUnbindService:" + url);
        }

        
        @Override
        public void onProgress(String url, long loaded, long length, long usedTime) {
            int mProgress = (int) ((loaded) / Float.valueOf(length) * 100);
            LogUtils.i(TAG, "onProgress:" + mProgress);
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

    public static AgentWebFragment getInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_URL, url);
        return getInstance(bundle);
    }

    public static AgentWebFragment getInstance(Bundle bundle) {
        AgentWebFragment fragment = new AgentWebFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agentweb, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAgentWeb = AgentWeb.with(this)

                .setAgentWebParent((LinearLayout) view, -1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

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
                .interceptUnkownUrl()

                .createAgentWeb()
                .ready()

                .go(getUrl());

        if (MyApp.isDebug()) {
            AgentWebConfig.debug();
        }




        initView(view);


        mAgentWeb.getWebCreator().getWebView().setOverScrollMode(WebView.OVER_SCROLL_NEVER);

        WebView webView = mAgentWeb.getWebCreator().getWebView();
        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setFocusable(true);
        webView.setNestedScrollingEnabled(false);
        webView.setFocusableInTouchMode(true);
        android.webkit.WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(false);
    }



    protected IWebLayout getWebLayout() {
        return new WebLayout(getActivity());
    }

    protected void initView(View view) {
        mBackImageView = view.findViewById(R.id.iv_back);
        mLineView = view.findViewById(R.id.view_line);
        mFinishImageView = view.findViewById(R.id.iv_finish);
        mTitleTextView = view.findViewById(R.id.toolbar_title);
        mBackImageView.setOnClickListener(mOnClickListener);
        mFinishImageView.setOnClickListener(mOnClickListener);
        mMoreImageView = view.findViewById(R.id.iv_more);
        mMoreImageView.setOnClickListener(mOnClickListener);
        pageNavigator(View.GONE);
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

    private void pageNavigator(int tag) {
        mBackImageView.setVisibility(tag);
        mLineView.setVisibility(tag);
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
                                        this.mAgentWeb.getPermissionInterceptor()));
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    
    private void showPoPup(View view) {
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(getContext(), view);
            mPopupMenu.inflate(R.menu.menu_toolbar_web);
            mPopupMenu.setOnMenuItemClickListener(mOnMenuItemClickListener);
        }
        mPopupMenu.show();
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
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    public boolean onFragmentKeyDown(int keyCode, KeyEvent event) {
        return mAgentWeb.handleKeyEvent(keyCode, event);
    }

    @Override
    public void onDestroyView() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroyView();
    }



    
    protected MiddlewareWebClientBase getMiddlewareWebClient() {
        return new MiddlewareWebViewClient() {
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("agentweb")) {
                    Log.i(TAG, "agentweb scheme ~");
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



package com.hx.campus.core.webview;

import static com.hx.campus.core.webview.WebViewInterceptDialog.APP_LINK_HOST;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.hx.campus.R;
import com.just.agentweb.core.client.MiddlewareWebClientBase;
import com.xuexiang.xui.utils.ResUtils;


public class MiddlewareWebViewClient extends MiddlewareWebClientBase {

    private static int count = 1;

    public MiddlewareWebViewClient() {
    }

    
    private static boolean hasAdUrl(String url) {
        String[] adUrls = ResUtils.getStringArray(R.array.adBlockUrl);
        for (String adUrl : adUrls) {
            if (url.contains(adUrl)) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.i("Info", "MiddlewareWebViewClient -- >  shouldOverrideUrlLoading:" + request.getUrl().toString() + "  c:" + (count++));
        if (shouldOverrideUrlLoadingByApp(view, request.getUrl().toString())) {
            return true;
        }
        return super.shouldOverrideUrlLoading(view, request);

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i("Info", "MiddlewareWebViewClient -- >  shouldOverrideUrlLoading:" + url + "  c:" + (count++));
        if (shouldOverrideUrlLoadingByApp(view, url)) {
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        url = url.toLowerCase();
        if (!hasAdUrl(url)) {

            return super.shouldInterceptRequest(view, url);
        } else {

            return new WebResourceResponse(null, null, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString().toLowerCase();
        if (!hasAdUrl(url)) {

            return super.shouldInterceptRequest(view, request);
        } else {

            return new WebResourceResponse(null, null, null);
        }
    }

    
    private boolean shouldOverrideUrlLoadingByApp(WebView webView, final String url) {
        if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {

            Uri uri = Uri.parse(url);
            if (uri != null && !(APP_LINK_HOST.equals(uri.getHost())

                    && url.contains("xpage"))) {
                return false;
            }
        }

        WebViewInterceptDialog.show(url);
        return true;
    }

}


package com.hx.campus.core.http.loader;

import android.content.Context;

import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader;


public final class ProgressLoader {

    private static IProgressLoaderFactory sIProgressLoaderFactory = new MiniProgressLoaderFactory();

    private ProgressLoader() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void setIProgressLoaderFactory(IProgressLoaderFactory sIProgressLoaderFactory) {
        ProgressLoader.sIProgressLoaderFactory = sIProgressLoaderFactory;
    }

    
    public static IProgressLoader create(Context context) {
        return sIProgressLoaderFactory.create(context);
    }

    
    public static IProgressLoader create(Context context, String message) {
        return sIProgressLoaderFactory.create(context, message);
    }
}

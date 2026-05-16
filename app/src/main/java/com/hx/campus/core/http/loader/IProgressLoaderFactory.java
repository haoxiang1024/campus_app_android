package com.hx.campus.core.http.loader;

import android.content.Context;

import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader;


public interface IProgressLoaderFactory {


    
    IProgressLoader create(Context context);


    
    IProgressLoader create(Context context, String message);
}

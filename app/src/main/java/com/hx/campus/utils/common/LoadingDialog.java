package com.hx.campus.utils.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;

import com.hx.campus.R;

public class LoadingDialog extends Dialog {

    private ProgressBar progressBar;

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progress_dialog);
        progressBar = findViewById(R.id.progressBar);
        setCancelable(false);
    }
}


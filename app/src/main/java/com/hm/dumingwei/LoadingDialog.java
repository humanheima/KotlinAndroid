package com.hm.dumingwei;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import com.hm.dumingwei.kotlinandroid.R;

/**
 * Created by dumingwei on 2017/9/20.
 */
public class LoadingDialog extends ProgressDialog {

    public LoadingDialog(Context context) {
        super(context, R.style.LoadingDialog);
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getContext());
    }

    private void init(Context context) {
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_loading);
    }
}

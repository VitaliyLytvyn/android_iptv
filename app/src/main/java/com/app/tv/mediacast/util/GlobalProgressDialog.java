package com.app.tv.mediacast.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.view.ContextThemeWrapper;

import com.app.tv.mediacast.R;

public class GlobalProgressDialog {
    private static ProgressDialog progressDialog;

    public static void show(Activity activity, String message) {
        //progressDialog = new ProgressDialog(activity);
        dismiss();
        progressDialog = new ProgressDialog(new ContextThemeWrapper(activity, R.style.AlertDialogCustom));
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void dismiss() {
        if (progressDialog != null) {
            //progressDialog.dismiss();
            progressDialog.cancel();
            progressDialog = null;
        }
    }
}

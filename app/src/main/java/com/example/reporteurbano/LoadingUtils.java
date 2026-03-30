package com.example.reporteurbano;

import android.app.AlertDialog;
import android.content.Context;

public final class LoadingUtils {

    private LoadingUtils() {
    }

    public static AlertDialog createLoadingDialog(Context context, String message) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .create();
        dialog.show();
        return dialog;
    }
}

package com.hriportfolio.speaketh.Utilities;

import android.app.ProgressDialog;
import android.content.Context;

public class Utils {

    public static ProgressDialog createProgressDialog(Context context){
        ProgressDialog progressDialog = ProgressDialog.show(context,"",
                "Loading Please Wait",
                true,false);
        progressDialog.setCanceledOnTouchOutside(true);
        return progressDialog;
    }
}

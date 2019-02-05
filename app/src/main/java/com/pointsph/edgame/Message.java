package com.pointsph.edgame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Message {
    // TODO: Place all the messages here.
    public static String ERROR_OCCURRED = "Ooops. Something is not right. Please try again.";
    public static String MISSING_SETTINGS = "Missing settings file.";
    public static String STORAGE_NOT_AVAILABLE = "External storage is not available.";

    public static void show(String msg, Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(msg)
                   .setCancelable(false)
                   .setPositiveButton("OK", (dialogInterface, i) -> {})
                   .create()
                   .show();

//        alertDialog.setMessage(msg);
//        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                return ;
//            }
//        });
//        alertDialog.setCancelable(false);
//        alertDialog.create();
//        alertDialog.show();
    }

}

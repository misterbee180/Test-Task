package com.deviousindustries.testtask;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

public class ExceptionHandler implements
        java.lang.Thread.UncaughtExceptionHandler {
    private final Activity myContext;


    public ExceptionHandler(Activity context) {
        myContext = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        final String LINE_SEPARATOR = "\n";

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        String errorReport = new String();
        errorReport += "************ CAUSE OF ERROR ************\n\n";
        errorReport += stackTrace.toString();

        errorReport += "\n************ DEVICE INFORMATION ***********\n";
        errorReport += "Brand: ";
        errorReport += Build.BRAND;
        errorReport += LINE_SEPARATOR;
        errorReport += "Device: ";
        errorReport += Build.DEVICE;
        errorReport += LINE_SEPARATOR;
        errorReport += "Model: ";
        errorReport += Build.MODEL;
        errorReport += LINE_SEPARATOR;
        errorReport += "Id: ";
        errorReport += Build.ID;
        errorReport += LINE_SEPARATOR;
        errorReport += "Product: ";
        errorReport += Build.PRODUCT;
        errorReport += LINE_SEPARATOR;
        errorReport += "\n************ FIRMWARE ************\n";
        errorReport += "SDK: ";
        errorReport += Build.VERSION.SDK;
        errorReport += LINE_SEPARATOR;
        errorReport += "Release: ";
        errorReport += Build.VERSION.RELEASE;
        errorReport += LINE_SEPARATOR;
        errorReport += "Incremental: ";
        errorReport += Build.VERSION.INCREMENTAL;
        errorReport += LINE_SEPARATOR;

        Intent intent = new Intent(myContext, Viewer_Tasklist.class);
        intent.putExtra("EXTRA_ERROR", errorReport.toString());
        myContext.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}

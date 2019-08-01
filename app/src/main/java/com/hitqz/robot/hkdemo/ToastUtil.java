package com.hitqz.robot.hkdemo;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {

    private static Handler sMainThreadHandler;
    private static Toast mToast;

    public static Handler getMainThreadHandler() {
        if (sMainThreadHandler == null) {
            synchronized (ToastUtil.class) {
                if (sMainThreadHandler == null) {
                    sMainThreadHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return sMainThreadHandler;
    }


    public static void showToast(final Context context, final String message, final int duration) {
        if (mToast != null) {
            mToast.cancel();
        }

        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mToast = Toast.makeText(context.getApplicationContext(), message, duration);
                mToast.show();
            }
        });
    }

    public static void showToastLong(final Context context, final String message) {
        showToast(context, message, Toast.LENGTH_LONG);
    }

    public static void showToastShort(final Context context, final String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }


}


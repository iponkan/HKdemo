package com.hitqz.robot.hkdemo;

import android.content.Context;


import java.util.HashMap;

public class HCSdkManager {

    private static HCSdkManager singleton;

    public static HCSdkManager getInstance() {
        if (singleton == null) {
            synchronized (HCSdkManager.class) {
                if (singleton == null) {
                    singleton = new HCSdkManager();
                }
            }
        }
        return singleton;
    }

    private boolean init;

    private static HashMap<LoginInfo, HCSdk> hcSdks = new HashMap<>();

    private static HCSdk getHCSdk(Context context, LoginInfo loginInfo) {
        HCSdk hcSdk = hcSdks.get(loginInfo);
        if (hcSdk == null) {
            hcSdk = HCSdk.getInstance(context, loginInfo);
            hcSdks.put(loginInfo, hcSdk);
        }
        return hcSdk;
    }

    public static HCSdk getNormalHCSdk(Context context) {
        return getHCSdk(context, LoginInfo.getNormalLogInfo());
    }


    public void initAndLogin(Context context) {

        if (init) {
            return;
        }
        // 高清摄像头初始化
        HCSdk normalHCSdk = getNormalHCSdk(context);
        boolean initResult = normalHCSdk.init();
        if (initResult) {
            boolean result = normalHCSdk.login();
            if (!result) {
                ToastUtil.showToastShort(context, "高清摄像头登录失败");
                return;
            }
        } else {
            ToastUtil.showToastShort(context, "高清摄像头初始化失败");
            return;
        }


        init = true;
    }
}

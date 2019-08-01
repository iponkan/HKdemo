## 海康摄像头音视频对讲Andorid客户端

### 环境

Android

#### sdk

jar：

AudioEngineSDK.jar(海康音频)

 HCNetSDK(海康网络SDK)

jna.jar(可以使用的jna接口)

PlayerSDK.jar(播放库，可播放网络流)

so：

libAudioEngine.so
libCpuFeatures.so
libgnustl_shared.so
libHCAlarm.so
libHCCore.so
libHCCoreDevCfg.so
libHCDisplay.so
libHCGeneralCfgMgr.so
libHCIndustry.so
libhcnetsdk.so
libHCPlayBack.so
libHCPreview.so
libHCVoiceTalk.so
libjnidispatch.so
libopensslwrap.so
libPlayCtrl.so
libSystemTransform.so

### 使用方法

1、设置对方 摄像头登录信息，在LoginInfo.java中修改

```java
public static final String NORMAL_IP = "192.168.3.101";//设备IP
public static final String NORMAL_PORT = "8000";//端口
public static final String NORMAL_USER = "admin";//用户名
public static final String NORMAL_PSD = "hit123456";//密码
```

2、使用HCSdkManager初始化

```java
HCSdkManager.getInstance().initAndLogin(this);//初始化和登录
```

3、初始化登录成功后，通过HCSdkManager获取HCSdk对象

```java
HCSdk normalHCSdk = HCSdkManager.getNormalHCSdk(this);
```

3、在Activity布局添加一个sufaceView,并将surfaceView传给HCSdk

```java
hcSdk.setSurfaceView(sufaceView);
```

4、在Activity生命周期里进行预览回调

```java
@Override
protected void onResume() {
	super.onResume();
	if (hcSdk != null) {
        hcSdk.startSinglePreview();
    }
}
```

5、在Activity生命周期里进行预览停止

```java
@Override
protected void onPause() {
    super.onPause();
    if (hcSdk != null) {
        hcSdk.stopSinglePreview();
    }
}
```


# HKdemo

海康摄像头Android端和Web端使用代码

- Android端提供摄像头预览，调焦，播放等功能

- Web端仅提供摄像头预览

## Android

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

## Web

web目录

### 平台

IE或火狐浏览器

### 环境

需要安装vlan并安装时勾选ie和火狐插件支持

[下载地址](https://www.videolan.org/vlc/index.zh.html)

### 使用

1、在htm里面修改rtsp地址，这里是用户名是admin，密码是hit123456,摄像头ip是192.168.3.101，根据使用作相应修改

```html
value="rtsp://admin:hit123456@192.168.3.101:554/h264/ch33/main/av_stream"
```

2、直接打开demo.html,如果是ie需要允许运行activitX插件
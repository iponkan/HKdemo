## 海康摄像头web端

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
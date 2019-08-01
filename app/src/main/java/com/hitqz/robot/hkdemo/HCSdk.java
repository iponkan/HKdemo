package com.hitqz.robot.hkdemo;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.PTZCommand;
import com.hikvision.netsdk.PlaybackCallBack;
import com.hikvision.netsdk.PlaybackControlCommand;
import com.hikvision.netsdk.RealPlayCallBack;
import com.orhanobut.logger.Logger;

import org.MediaPlayer.PlayM4.Player;
import org.MediaPlayer.PlayM4.PlayerCallBack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HCSdk implements SurfaceHolder.Callback {

    private String TAG = "HCSdk";

    private Context applicationContext;
    boolean init = false;
    private LoginInfo loginInfo;

    private int preFrameNumber = -100;
    private int m_iLogID = -1; // return by NET_DVR_Login_v30

    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30;
    private int m_iStartChan = 0; // start channel no
    private int m_iChanNum = 0; // channel number,通常是1
    private int m_iPlayID = -1; // return by NET_DVR_RealPlay_V30
    private int m_iPlaybackID = -1; // return by NET_DVR_PlayBackByTime
    private int m_iPort = -1; // play port
    private boolean m_bStopPlayback = false;


    public static final int STATE_IDLE = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_STOP = 3;
    private int playState = STATE_IDLE;
    public boolean recording;

    private PlaybackRunnable playbackRunnable;
    private List<PlayerCallback> callbacks;
    private boolean seeking;
    private WeakReference<SurfaceView> surfaceView;

    private HCSdk(Context context, LoginInfo li) {
        this.applicationContext = context.getApplicationContext();
        this.TAG = this.TAG + "-" + li.name;
        this.loginInfo = li;
    }

    static HCSdk getInstance(Context context, LoginInfo li) {
        return new HCSdk(context, li);
    }

    // @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 去掉没影响，会触发surface重建
//        holder.setFormat(PixelFormat.TRANSLUCENT);
        Logger.t(TAG).i("surfaceCreated");
        Surface surface = holder.getSurface();
        if (surface.isValid()) {
//            if (!Player.getInstance().setVideoWindow(m_iPort, 0, holder)) {
//                Logger.t(TAG).e("surfaceCreated Player setVideoWindow failed:" + Player.getInstance().getLastError(m_iPort));
//            } else {
//                Logger.t(TAG).e("surfaceCreated Player setVideoWindow succeed");
//            }
            if (playbackRunnable != null) {
                playbackRunnable.run();
                playbackRunnable = null;
            }

            if (needPreview) {
                startPreviewInternal();
                needPreview = false;
            }
        }


    }

    // @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    // @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.t(TAG).i("surfaceDestroyed");
    }

    public boolean isInit() {
        return init;
    }

    public boolean init() {
        if (init) {
            return true;
        }
        boolean result = HCNetSDK.getInstance().NET_DVR_Init();
        if (!result) {
            Logger.t(TAG).e("HCNetSDK init is failed! error code:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return false;
        }

        init = true;
        return true;
    }

    private int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        String strIP = loginInfo.m_oIPAddr;
        int nPort = Integer.parseInt(loginInfo.m_oPort);
        String strUser = loginInfo.m_oUser;
        String strPsd = loginInfo.m_oPsd;
        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort,
                strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Logger.t(TAG).e("NET_DVR_Login is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }
        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byIPChanNum
                    + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256;
        }
        Logger.t(TAG).i("NET_DVR_Login is Successful!");

        return iLogID;
    }

    private int loginDevice() {
        int iLogID = -1;

        iLogID = loginNormalDevice();

        return iLogID;
    }

    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                Logger.t(TAG).e("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }

    public boolean isLogin() {
        return !(m_iLogID < 0);
    }

    public boolean isPreviewing() {
        return !(m_iPlayID < 0);
    }

    public boolean isPlayback() {
        return !(m_iPlaybackID < 0);
    }

    public boolean login() {
        if (m_iLogID < 0) {
            // login on the device
            m_iLogID = loginDevice();
            if (m_iLogID < 0) {
                return false;
            } else {
                Logger.t(TAG).i("m_iLogID=" + m_iLogID);
            }
            // get instance of exception callback and set
            ExceptionCallBack oexceptionCbf = getExceptiongCbf();

            if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(
                    oexceptionCbf)) {
                Logger.e("NET_DVR_SetExceptionCallBack is failed! error code:", HCNetSDK.getInstance().NET_DVR_GetLastError());
                return false;
            }


            Logger.t(TAG).i("Login success");
            return true;
        } else {
            Logger.t(TAG).e("already login !");
            return true;
        }
    }

    public boolean logout() {
        // whether we have logout
        if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
            Logger.t(TAG).e(" NET_DVR_Logout is failed!, error code" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return true;
        }
        m_iLogID = -1;
        return false;
    }


    public void setSurfaceView(@NonNull SurfaceView svView) {
        surfaceView = new WeakReference<>(svView);
        svView.getHolder().addCallback(this);
    }

    private boolean needPreview = false;

    public void startSinglePreview() {
        if (surfaceView == null || surfaceView.get() == null) {
            Logger.t(TAG).e("must call setSurfaceView before preview");
            return;
        }

        Surface surface = surfaceView.get().getHolder().getSurface();
        if (!surface.isValid()) {
            needPreview = true;
        } else {
            startPreviewInternal();
        }

    }

    private boolean startPreviewInternal() {
        if (m_iLogID < 0) {
            Logger.t(TAG).e("startPreviewInternal error please login on device first");
            return false;
        }

        if (m_iPlaybackID >= 0) {
            Logger.t(TAG).e("startPreviewInternal Please stop palyback first");
            return false;
        }
        RealPlayCallBack fRealDataCallBack = getRealPlayerCbf();
        Logger.t(TAG).i("m_iStartChan:" + m_iStartChan);

        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = m_iStartChan;
        previewInfo.dwStreamType = 0; // substream
        previewInfo.bBlocked = 1;

        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID, previewInfo, fRealDataCallBack);
        if (m_iPlayID < 0) {
            Logger.t(TAG).e("NET_DVR_RealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return false;
        }

        Logger.t(TAG).i("NET_DVR_RealPlay success");
        return true;
    }

    public boolean stopSinglePreview() {
        if (m_iPlayID < 0) {
            Logger.t(TAG).e("m_iPlayID < 0");
            return false;
        }

        // net sdk stop preview
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            Logger.t(TAG).e("StopRealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return false;
        }

        m_iPlayID = -1;
        stopSinglePlayer();
        return true;
    }

    private void stopSinglePlayer() {
        Player.getInstance().stopSound();
        // player stop play
        if (!Player.getInstance().stop(m_iPort)) {
            Logger.t(TAG).e("stop is failed!");
            return;
        }

        if (!Player.getInstance().closeStream(m_iPort)) {
            Logger.t(TAG).e("closeStream is failed!");
            return;
        }
        if (!Player.getInstance().freePort(m_iPort)) {
            Logger.t(TAG).e("freePort is failed!" + m_iPort);
            return;
        }

        preFrameNumber = -100;
        m_iPort = -1;
    }

    private RealPlayCallBack getRealPlayerCbf() {
        RealPlayCallBack cbf = new RealPlayCallBack() {
            public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
                // player channel 1
                processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_REALTIME);
            }
        };
        return cbf;
    }


    public void processRealData(int iPlayViewNo, int iDataType,
                                byte[] pDataBuffer, int iDataSize, int iStreamMode) {
        if (HCNetSDK.NET_DVR_SYSHEAD == iDataType) {
            if (m_iPort >= 0) {
                return;
            }
            m_iPort = Player.getInstance().getPort();
            if (m_iPort == -1) {
                Logger.t(TAG).e("getPort is failed with: " + Player.getInstance().getLastError(m_iPort));
                return;
            }
            Logger.t(TAG).i("getPort succ with: " + m_iPort);
            if (iDataSize > 0) {
                if (!Player.getInstance().setStreamOpenMode(m_iPort, iStreamMode)) {// set stream mode
                    Logger.t(TAG).e("setStreamOpenMode failed");
                    return;
                }
                if (!Player.getInstance().openStream(m_iPort, pDataBuffer, iDataSize, 2 * 1024 * 1024)) {// open stream
                    Logger.t(TAG).e("openStream failed");
                    return;
                }
                if (surfaceView.get() == null || !Player.getInstance().play(m_iPort, surfaceView.get().getHolder())) {
                    Logger.t(TAG).e("play failed");
                    return;
                }
                if (!Player.getInstance().playSound(m_iPort)) {
                    Logger.t(TAG).e("playSound failed with error code:" + Player.getInstance().getLastError(m_iPort));
                }

//                Player.getInstance().setHSDetectCB(m_iPort, new PlayerCallBack.PlayerHSDetectCB() {
//                    @Override
//                    public void onHSDetect(int i, int i1) {
//                        Log.e(TAG, "onHSDetect");
//                    }
//                });

                Player.getInstance().setDisplayCB(m_iPort, new PlayerCallBack.PlayerDisplayCB() {
                    @Override
                    public void onDisplay(int i, byte[] bytes, int i1, int i2, int i3, int i4, int i5, int i6) {
//                        Logger.t(TAG).i(TAG, "onDisplay:" + bytes.length);
                        if (seeking) {
                            seeking = false;
                            notifySeekComplete();
                        }

//                        if (isEnd()) {
//                            stopPlayback();
//                        }
                    }
                });

//                Player.getInstance().setDecodeCB(m_iPort, new PlayerCallBack.PlayerDecodeCB() {
//                    @Override
//                    public void onDecode(int i, byte[] bytes, int i1, int i2, int i3, int i4, int i5, int i6) {
//                        Log.e(TAG, "onDecode");
//
//                    }
//                });

//                Player.getInstance().setFileRefCB(m_iPort, new PlayerCallBack.PlayerFileRefCB() {
//                    @Override
//                    public void onFileRefDone(int i) {
//                        Log.e(TAG, "onFileRefDone");
//
//                    }
//                });
//                Player.getInstance().setPreRecordCallBack(m_iPort, new PlayerCallBack.PlayerPreRecordCB() {
//                    @Override
//                    public void onPreRecord(int i, byte[] bytes, int i1) {
//                        Log.e(TAG, "onPreRecord");
//                    }
//                });
//                Player.getInstance().setEcnTypeChgCB(m_iPort, new PlayerCallBack.PlayerEncTypeChgCB() {
//                    @Override
//                    public void onEncTypeChg(int i) {
//                        Log.e(TAG, "onEncTypeChg");
//                    }
//                });
            }
        } else {
            if (!Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize)) {
                Logger.t(TAG).v("inputData failed with: " +
                        Player.getInstance().getLastError(m_iPort));
//                for (int i = 0; i < 4000 && m_iPlaybackID >= 0 && !m_bStopPlayback; i++) {
//                    if (Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize)) {
//                        break;
//                    }
//
////                    if (i % 100 == 0) {
////                        Log.e(TAG, "inputData failed with: " + Player.getInstance().getLastError(m_iPort) + ", i:" + i);
////                    }
//
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//
//                    }
//                }
            }

        }
    }

    private PlaybackCallBack getPlayerbackPlayerCbf() {
        PlaybackCallBack cbf = new PlaybackCallBack() {
            @Override
            public void fPlayDataCallBack(int iPlaybackHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
                // player channel 1
                processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_FILE);
            }
        };
        return cbf;
    }

    public void playBack(FileInfo fi) {

        fileInfo = fi;
        if (surfaceView == null || surfaceView.get() == null) {
            Logger.t(TAG).e("playback must call setSurfaceView before preview");
            return;
        }

        Surface surface = surfaceView.get().getHolder().getSurface();
        if (!surface.isValid()) {
            playbackRunnable = new PlaybackRunnable(fi.fileName);
        } else {
            playBackInternal(fi.fileName);
        }

    }

    private void playBackInternal(String fileName) {
        if (m_iLogID < 0) {
            Logger.t(TAG).e("playBackInternal please login on a device first");
            return;
        }
        if (m_iPlaybackID < 0) {
            if (m_iPlayID >= 0) {
                Logger.t(TAG).i("playBackInternal Please stop preview first");
                return;
            }

            m_iPlaybackID = HCNetSDK.getInstance().NET_DVR_PlayBackByName(m_iLogID, fileName);
            if (m_iPlaybackID >= 0) {
                PlaybackCallBack fPlaybackCallBack = getPlayerbackPlayerCbf();
                if (!HCNetSDK.getInstance().NET_DVR_SetPlayDataCallBack(m_iPlaybackID, fPlaybackCallBack)) {
                    Logger.t(TAG).e("Set playback callback failed!");
                    return;
                }
                if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                        PlaybackControlCommand.NET_DVR_PLAYSTART, null, 0, null)) {
                    Logger.t(TAG).e("net sdk playback start failed!");
                    return;
                }
                m_bStopPlayback = false;

                notifyPlayStart();

            } else {
                Logger.t(TAG).i("NET_DVR_PlayBackByName failed, error code: " +
                        HCNetSDK.getInstance().NET_DVR_GetLastError());
            }
        }
    }

    public void pausePlayBack() {
        if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                PlaybackControlCommand.NET_DVR_PLAYPAUSE, null, 0, null)) {
            Logger.t(TAG).e("net sdk playback pause failed! error code:", HCNetSDK.getInstance().NET_DVR_GetLastError());
        }
        Player.getInstance().pause(m_iPort, 1);
        Player.getInstance().stopSound();

        notifyPlayPause();
    }

    public void resumePlayBack() {
        if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                PlaybackControlCommand.NET_DVR_PLAYRESTART, null, 0, null)) {
            Logger.t(TAG).e("net sdk playback resume failed! error code:", HCNetSDK.getInstance().NET_DVR_GetLastError());
        }
        Player.getInstance().pause(m_iPort, 0);
        Player.getInstance().playSound(m_iPort);

        notifyPlayStart();
    }

    public static byte[] int2byte(int s) {
        byte[] arr = new byte[60];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) ((s >> i * 8) & 0xff);
        }
        return arr;
    }

    public void playbackSeekTo(float percent) {

        seeking = true;
        preFrameNumber = -100;

        // 需要把缓冲流清掉，不然播放器在这个时候会继续播放缓冲内容
        Player.getInstance().resetSourceBuffer(m_iPort);

        int progress = (int) (percent * 100);

        byte[] bytes = int2byte(progress);

        if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                12, bytes, 0, null)) {
            Logger.t(TAG).e("NET_DVR_PlayBackControl_V40 seek failed, error code: " +
                    HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).i("NET_DVR_PlayBackControl_V40 seek success");
        }

    }

    public void stopPlayback() {
        m_bStopPlayback = true;
        if (!HCNetSDK.getInstance().NET_DVR_StopPlayBack(m_iPlaybackID)) {
            Logger.t(TAG).e("net sdk stop playback failed! error code:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } // player stop play
        stopSinglePlayer();
        notifyPlayStop();

//        if (!Player.getInstance().setVideoWindow(m_iPort, 0, null)) {
//            Logger.t(TAG).e("stopPlayback Player setVideoWindow failed: " + Player.getInstance().getLastError(m_iPort));
//        }
        m_iPlaybackID = -1;
    }


    public List<FileInfo> findFile(TimeStruct startTime, TimeStruct stopTime) {
        if (m_iLogID < 0) {
            Logger.t(TAG).e("findFile please login on a device first");
            return new ArrayList<>();
        }
        List<FileInfo> fileList = HKCameraUtil.findFile(m_iLogID, startTime, stopTime);
        return fileList;
    }

    public void release() {
        stopSinglePreview();
        stopPlayback();
        logout();
    }


    public boolean isEnd() {

//        int total = Player.getInstance().getFileTotalFrames(m_iPort);
//        Log.i(TAG, "Player getFileTotalFrames====" + total);

        int current = Player.getInstance().getCurrentFrameNum(m_iPort);

//        Logger.t(TAG).i(TAG, "Player getCurrentFrameNum====" + current);
//        Logger.t(TAG).i(TAG, "Player preFrameNumber====" + preFrameNumber);
//
//        Logger.t(TAG).i(TAG, "Player Frame isEnd====" + (current == preFrameNumber));

        boolean result = current == preFrameNumber;

        if (current > 0) {
            preFrameNumber = current;
        }

        return result;
    }

    public int getPlayBackTime() {
        int progress = Player.getInstance().getPlayedTime(m_iPort);
        Logger.t(TAG).i("Player getPlayedTime====" + Player.getInstance().getPlayedTime(m_iPort));
        return progress;
    }

    public float getPlayBackPos() {
        float progress = Player.getInstance().getPlayPos(m_iPort);
        return progress;
    }

    private FileInfo fileInfo;

    public int getPlaybackDuration(FileInfo fileInfo) {
        if (fileInfo != null) {
            int duration = TimeStruct.getDurationSeconds(fileInfo.startTime, fileInfo.stopTime);
            Logger.t(TAG).i("getPlaybackDuration ==== " + duration);
            return duration;
        }
        return 0;
    }

    public boolean isStop() {
        return playState == STATE_STOP;
    }

    class PlaybackRunnable implements Runnable {

        String fileName;

        PlaybackRunnable(String fn) {
            fileName = fn;
        }

        @Override
        public void run() {
            playBackInternal(fileName);
        }
    }

    public void addPlayerCallBack(PlayerCallback callBack) {
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        callbacks.add(callBack);
    }

    public void removePlayerCallBack(PlayerCallback callBack) {
        if (callbacks == null) {
            return;
        }
        callbacks.remove(callBack);
        if (callbacks.size() == 0) {
            callbacks = null;
        }
    }

    private void notifyPlayStart() {
        playState = STATE_PLAYING;
        if (callbacks != null) {
            for (PlayerCallback callback : callbacks) {
                callback.onPlayStart();
            }
        }
    }

    private void notifyPlayPause() {
        playState = STATE_PAUSE;
        if (callbacks != null) {
            for (PlayerCallback callback : callbacks) {
                callback.onPlayPause();
            }
        }
    }

    private void notifyPlayStop() {
        playState = STATE_STOP;
        if (callbacks != null) {
            for (PlayerCallback callback : callbacks) {
                callback.onPlayStop();
            }
        }
    }

    private void notifySeekComplete() {
        if (callbacks != null) {
            for (PlayerCallback callback : callbacks) {
                callback.onSeekComplete();
            }
        }
    }


    public boolean isPlaying() {
        return playState == STATE_PLAYING;
    }

    public boolean isPause() {
        return playState == STATE_PAUSE;
    }

    public void focusFar() {
        if (!HCNetSDK.getInstance().NET_DVR_PTZControl(m_iPlayID, PTZCommand.FOCUS_FAR, 0)) {
            Logger.t(TAG).e("PTZControl  PAN_LEFT 0 faild!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).i("PTZControl  PAN_LEFT 0 succ");
        }
        if (!HCNetSDK.getInstance().NET_DVR_PTZControl(m_iPlayID, PTZCommand.FOCUS_FAR, 1)) {
            Logger.t(TAG).e("PTZControl  PAN_LEFT 1 faild!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).i("PTZControl  PAN_LEFT 1 succ");
        }
    }

    public void focusNear() {
        if (!HCNetSDK.getInstance().NET_DVR_PTZControl(m_iPlayID, PTZCommand.FOCUS_NEAR, 0)) {
            Logger.t(TAG).e("PTZControl  PAN_LEFT 0 faild!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).i("PTZControl  PAN_LEFT 0 succ");
        }
        if (!HCNetSDK.getInstance().NET_DVR_PTZControl(m_iPlayID, PTZCommand.FOCUS_NEAR, 1)) {
            Logger.t(TAG).e("PTZControl  PAN_LEFT 1 faild!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).i("PTZControl  PAN_LEFT 1 succ");
        }
    }

    public void getConfig() {
        HKCameraUtil.testGetCompressCfg(m_iPlayID, 1);
    }


    public void testGetAbility() {
        HKCameraUtil.testGetAbility(m_iLogID, 1);
    }


    public void startRecord() {
        if (recording) {
            Logger.t(TAG).e("已经在录制！！！！！");
            return;
        }
        if (!HCNetSDK.getInstance().NET_DVR_StartDVRRecord(m_iLogID, 1, 0)) {
            Logger.t(TAG).e("NET_DVR_StartDVRRecord err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).i("NET_DVR_StartDVRRecord succ!");
            recording = true;
        }
    }

    public void stopRecord() {
        if (!recording) {
            Logger.t(TAG).e("未在录制！！！！！");
        }

        if (!HCNetSDK.getInstance().NET_DVR_StopDVRRecord(m_iLogID, 1)) {
            Logger.t(TAG).i("NET_DVR_StopDVRRecord err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).i("NET_DVR_StopDVRRecord succ!");
            recording = false;
        }
    }
}

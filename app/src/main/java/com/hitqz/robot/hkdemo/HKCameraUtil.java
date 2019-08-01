package com.hitqz.robot.hkdemo;


import android.util.Log;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_ABILITY;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_V30;
import com.hikvision.netsdk.NET_DVR_COMPRESSION_INFO_V30;
import com.hikvision.netsdk.NET_DVR_FILECOND;
import com.hikvision.netsdk.NET_DVR_FINDDATA_V30;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class HKCameraUtil {

    public static final String TAG = "HKCameraUtil";

    // lChannel为1
    public static List<FileInfo> findFile(int iUserID, TimeStruct start, TimeStruct stop) {
        List<FileInfo> list = new ArrayList<>();
        int iFindHandle = -1;
        NET_DVR_FILECOND lpSearchInfo = new NET_DVR_FILECOND();
        lpSearchInfo.lChannel = 1;
        lpSearchInfo.dwFileType = 0xff;
        lpSearchInfo.dwIsLocked = 0xff;
        lpSearchInfo.dwUseCardNo = 0;
        lpSearchInfo.struStartTime = start.toNET_DVR_TIME();
        lpSearchInfo.struStopTime = stop.toNET_DVR_TIME();
        Logger.t(TAG).i("查找文件开始时间：" + start.toString() + " 结束时间：" + stop.toString());
        iFindHandle = HCNetSDK.getInstance().NET_DVR_FindFile_V30(iUserID, lpSearchInfo);
        if (iFindHandle == -1) {
            Logger.t(TAG).e("NET_DVR_FindFile_V30 failed,Error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return list;
        }
        int findNext = 0;
        NET_DVR_FINDDATA_V30 struFindData = new NET_DVR_FINDDATA_V30();
        while (findNext != -1) {
            findNext = HCNetSDK.getInstance().NET_DVR_FindNextFile_V30(iFindHandle, struFindData);
            if (findNext == HCNetSDK.NET_DVR_FILE_SUCCESS) {
                String fileName = toValidString(new String(struFindData.sFileName));
                FileInfo fileInfo = new FileInfo();
                fileInfo.fileName = fileName;
                fileInfo.startTime = TimeStruct.cloneFrom(struFindData.struStartTime);
                fileInfo.stopTime = TimeStruct.cloneFrom(struFindData.struStopTime);
                fileInfo.fileSize = struFindData.dwFileSize;
                list.add(fileInfo);
                Logger.t(TAG).i("~~~~~Find File" + fileName + "~~~~~File Size" + struFindData.dwFileSize +
                        "~~~~~File Time,from" + struFindData.struStartTime.ToString() + "~~~~~File Time,to" + struFindData.struStopTime.ToString());
            } else if (HCNetSDK.NET_DVR_FILE_NOFIND == findNext) {
                Logger.t(TAG).i("No file found");
                break;
            } else if (HCNetSDK.NET_DVR_NOMOREFILE == findNext) {
                Logger.t(TAG).i("All files are listed");
                break;
            } else if (HCNetSDK.NET_DVR_FILE_EXCEPTION == findNext) {
                Logger.t(TAG).e("Exception in searching");
                break;
            } else if (HCNetSDK.NET_DVR_ISFINDING == findNext) {
                Log.i(TAG, "NET_DVR_ISFINDING");
            }
        }
        HCNetSDK.getInstance().NET_DVR_FindClose_V30(iFindHandle);
        return list;
    }

    public static void testGetCompressCfg(int playid, int channel) {
        NET_DVR_COMPRESSIONCFG_V30 Compress_Cfg = new NET_DVR_COMPRESSIONCFG_V30();
        if (HCNetSDK.getInstance().NET_DVR_GetDVRConfig(playid, HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30, channel, Compress_Cfg)) {
            NET_DVR_COMPRESSION_INFO_V30 net_para = Compress_Cfg.struNetPara;
            NET_DVR_COMPRESSION_INFO_V30 record_para = Compress_Cfg.struNormHighRecordPara;
            NET_DVR_COMPRESSION_INFO_V30 alarm_para = Compress_Cfg.struEventRecordPara;
			/*
				int[] net_para_video = {0};
				net_para_video[0] = net_para.dwVideoBitrate;
				net_para_video[1] = net_para.dwVideoFrameRate;//			byte[] net_para_array = {0};
				net_para_array[0] = net_para.byAudioEncType;
				net_para_array[1] = net_para.byBitrateType;
				net_para_array[2] = net_para.byIntervalBPFrame;
				net_para_array[3] = net_para.byPicQuality;
				net_para_array[4] = net_para.byResolution;
				net_para_array[5] = net_para.byStreamType;
				net_para_array[6] = net_para.byVideoEncType;
				net_para_array[7] = (byte)net_para.wIntervalFrameI;
			*/
            Logger.t(TAG).i("Audio Enc Type =	" + net_para.byAudioEncType);                    //0-G722	1-G711_U	2-G711_A	5-MP2L2	6-G726	7-AAC，0xfe- 自动（和源一致），0xff-无效
            Logger.t(TAG).i("Bit Rate Type = 	" + net_para.byBitrateType);                    //0-变码率	1-定码率
            Logger.t(TAG).i("Frame Type = 		" + net_para.byIntervalBPFrame);                //0-BBP帧	1-BP帧 	2-P帧	0xff-无效
            Logger.t(TAG).i("Pic Qua = 			" + net_para.byPicQuality);                    //0 最高————>5最低		0xfe-自动(和源一致)
            Logger.t(TAG).i("Resolution = 		" + net_para.byResolution);                    //太多了，写不下，用到的时候查文档吧
            Logger.t(TAG).i("Stream Type = 		" + net_para.byStreamType);                    //0-视屏流	1-复合流	0xfe-自动(和源一致)
            Logger.t(TAG).i("Video Enc Type = 	" + net_para.byVideoEncType);                    //0-私有264，1-标准h264，2-标准mpeg4，7-M-JPEG，8-MPEG2，0xfe- 自动（和源一致），0xff-无效
            Logger.t(TAG).i("Video Bit Rate		" + net_para.dwVideoBitrate);                    //视频码率		太多不写	请查询文档
            Logger.t(TAG).i("Video Frame Rate = 	" + net_para.dwVideoFrameRate);                //视屏帧率		0-全部	1-1/16.....查文档
            Logger.t(TAG).i("IntervalFrameI = 	" + net_para.wIntervalFrameI);                //I 帧间隔	0xffee	和源一致	0xffff	无效
            channel = 0xFFFFFFFF;
//			Compress_Cfg = null;
        } else {
            Logger.t(TAG).e("获取视频参数失败,Error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        }

    }

    public static void testGetAbility(int userId, int channel) {
        NET_DVR_COMPRESSIONCFG_ABILITY Compress_Cfg = new NET_DVR_COMPRESSIONCFG_ABILITY();
        if (HCNetSDK.getInstance().NET_DVR_GetCompressionAbility(userId, channel, Compress_Cfg)) {
            Logger.t(TAG).e("NET_DVR_GetCompressionAbility,Success:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.t(TAG).e("NET_DVR_GetCompressionAbility,Error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        }

    }

    public static String toValidString(String s) {
        String[] sIP = new String[2];
        sIP = s.split("\0", 2);
        return sIP[0];
    }
}

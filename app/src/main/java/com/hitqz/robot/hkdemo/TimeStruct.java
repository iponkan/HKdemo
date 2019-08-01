package com.hitqz.robot.hkdemo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.hikvision.netsdk.NET_DVR_TIME;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeStruct implements Parcelable {
    public int dwYear;
    public int dwMonth;
    public int dwDay;
    public int dwHour;
    public int dwMinute;
    public int dwSecond;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.dwYear);
        dest.writeInt(this.dwMonth);
        dest.writeInt(this.dwDay);
        dest.writeInt(this.dwHour);
        dest.writeInt(this.dwMinute);
        dest.writeInt(this.dwSecond);
    }

    public TimeStruct() {
    }

    protected TimeStruct(Parcel in) {
        this.dwYear = in.readInt();
        this.dwMonth = in.readInt();
        this.dwDay = in.readInt();
        this.dwHour = in.readInt();
        this.dwMinute = in.readInt();
        this.dwSecond = in.readInt();
    }

    public static final Parcelable.Creator<TimeStruct> CREATOR = new Parcelable.Creator<TimeStruct>() {
        @Override
        public TimeStruct createFromParcel(Parcel source) {
            return new TimeStruct(source);
        }

        @Override
        public TimeStruct[] newArray(int size) {
            return new TimeStruct[size];
        }
    };

    public static TimeStruct cloneFrom(@NonNull NET_DVR_TIME net_dvr_time) {
        TimeStruct struct = new TimeStruct();
        struct.dwYear = net_dvr_time.dwYear;
        struct.dwDay = net_dvr_time.dwDay;
        struct.dwHour = net_dvr_time.dwHour;
        struct.dwMinute = net_dvr_time.dwMinute;
        struct.dwMonth = net_dvr_time.dwMonth;
        struct.dwSecond = net_dvr_time.dwSecond;
        return struct;
    }

    public static NET_DVR_TIME convertTo(@NonNull TimeStruct timeStruct) {
        NET_DVR_TIME struct = new NET_DVR_TIME();
        struct.dwYear = timeStruct.dwYear;
        struct.dwDay = timeStruct.dwDay;
        struct.dwHour = timeStruct.dwHour;
        struct.dwMinute = timeStruct.dwMinute;
        struct.dwMonth = timeStruct.dwMonth;
        struct.dwSecond = timeStruct.dwSecond;
        return struct;
    }

    public NET_DVR_TIME toNET_DVR_TIME() {
        NET_DVR_TIME struct = new NET_DVR_TIME();
        struct.dwYear = this.dwYear;
        struct.dwDay = this.dwDay;
        struct.dwHour = this.dwHour;
        struct.dwMinute = this.dwMinute;
        struct.dwMonth = this.dwMonth;
        struct.dwSecond = this.dwSecond;
        return struct;
    }

    @Override
    public String toString() {
        return this.dwYear + "/" + this.dwMonth + "/" + this.dwDay + " " + this.dwHour + ":" + this.dwMinute + ":" + this.dwSecond;
    }

    public String toHMS() {
        return this.dwHour + ":" + this.dwMinute + ":" + this.dwSecond;
    }

    public static final String TAG = "TimeStruct";


    public static int getDurationSeconds(@NonNull TimeStruct startTime, @NonNull TimeStruct stopTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.CHINA);
        try {

//            Log.i(TAG, "startTime.toString()=============" + startTime.toString());
//            Log.i(TAG, "stopTime.toString()=============" + stopTime.toString());

            Date startDate = formatter.parse(startTime.toString());
            Date stopDate = formatter.parse(stopTime.toString());

            int startSeconds = (int) (startDate.getTime() / 1000);
            int stopSeconds = (int) (stopDate.getTime() / 1000);
//            Log.i(TAG, "startSeconds=============" + startSeconds);
//            Log.i(TAG, "stopSeconds=============" + stopSeconds);


            return stopSeconds - startSeconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long toMillSeconds() {
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = formatter.parse(toString());
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static TimeStruct fromMillSeconds(long num) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(num);
        calendar.setTime(date);
        TimeStruct struct = new TimeStruct();
        struct.dwYear = calendar.get(Calendar.YEAR);
        struct.dwMonth = calendar.get(Calendar.MONTH) + 1;
        struct.dwDay = calendar.get(Calendar.DAY_OF_MONTH);
        struct.dwHour = calendar.get(Calendar.HOUR_OF_DAY);
        struct.dwMinute = calendar.get(Calendar.MINUTE);
        struct.dwSecond = calendar.get(Calendar.SECOND);
        return struct;
    }

    public static TimeStruct farFeature() {
        TimeStruct struct = new TimeStruct();
        struct.dwYear = 2069;
        struct.dwMonth = 12;
        struct.dwDay = 31;
        struct.dwHour = 24;
        struct.dwMinute = 0;
        struct.dwSecond = 0;
        return struct;
    }

    public Date toDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = formatter.parse(toString());
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}

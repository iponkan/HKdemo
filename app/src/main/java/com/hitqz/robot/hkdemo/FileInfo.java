package com.hitqz.robot.hkdemo;

import android.os.Parcel;
import android.os.Parcelable;

public class FileInfo implements Parcelable {

    public String fileName;
    public int fileSize;
    public TimeStruct startTime;
    public TimeStruct stopTime;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileName);
        dest.writeInt(this.fileSize);
        dest.writeParcelable(this.startTime, flags);
        dest.writeParcelable(this.stopTime, flags);
    }

    public FileInfo() {
    }

    protected FileInfo(Parcel in) {
        this.fileName = in.readString();
        this.fileSize = in.readInt();
        this.startTime = in.readParcelable(TimeStruct.class.getClassLoader());
        this.stopTime = in.readParcelable(TimeStruct.class.getClassLoader());
    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel source) {
            return new FileInfo(source);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
}

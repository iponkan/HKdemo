package com.hitqz.robot.hkdemo;

public interface PlayerCallback {
    void onPlayStart();

    void onPlayPause();

    void onPlayStop();

    void onPlaying(int progress);

    void onSeekComplete();
}

package com.hitqz.robot.hkdemo;

public class LoginInfo {

    public static final String NORMAL_IP = "192.168.3.101";
    public static final String NORMAL_PORT = "8000";
    public static final String NORMAL_USER = "admin";
    public static final String NORMAL_PSD = "hit123456";

    public String m_oIPAddr;
    public String m_oPort;
    public String m_oUser;
    public String m_oPsd;
    public String name;

    private LoginInfo(String ip, String port, String user, String psd, String n) {
        this.m_oIPAddr = ip;
        this.m_oPort = port;
        this.m_oUser = user;
        this.m_oPsd = psd;
        this.name = n;
    }

    private static LoginInfo normalLoginInfo = new LoginInfo(NORMAL_IP, NORMAL_PORT, NORMAL_USER, NORMAL_PSD, "normal");


    public static LoginInfo getNormalLogInfo() {
        return normalLoginInfo;
    }

}

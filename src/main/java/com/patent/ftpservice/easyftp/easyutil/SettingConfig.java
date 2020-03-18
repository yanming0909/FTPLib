package com.patent.ftpservice.easyftp.easyutil;

import android.os.Environment;

import java.io.Serializable;

public class SettingConfig implements Serializable {
    private static final long serialVersionUID = -6023666654724293490L;
    private boolean authentication = true;
    private boolean forbidSleep = true;
    private boolean idleCloseConnection = true;
    private int idleKeepTime = 5;
    private String password = "123456";
    private int port = 8888;
    private boolean readOnly = false;
    private String userName = "admin";
    private String homeDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String serverAddress = "192.168.100.91";

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port2) {
        this.port = port2;
    }

    public boolean getrRadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean readOnly2) {
        this.readOnly = readOnly2;
    }

    public boolean getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(boolean authentication2) {
        this.authentication = authentication2;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName2) {
        this.userName = userName2;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public boolean getForbidSleep() {
        return this.forbidSleep;
    }

    public void setForbidSleep(boolean forbidSleep2) {
        this.forbidSleep = forbidSleep2;
    }

    public boolean getIdleCloseConnection() {
        return this.idleCloseConnection;
    }

    public void setIdleCloseConnection(boolean idleCloseConnection2) {
        this.idleCloseConnection = idleCloseConnection2;
    }

    public int getIdleKeepTime() {
        return this.idleKeepTime;
    }

    public void setIdleKeepTime(int idleKeepTime2) {
        this.idleKeepTime = idleKeepTime2;
    }

}

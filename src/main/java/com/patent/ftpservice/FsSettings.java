/*
Copyright 2011-2013 Pieter Pareit
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.patent.ftpservice;

import android.os.Environment;

import java.io.File;
import java.util.Set;


public class FsSettings {

    private final static String TAG = FsSettings.class.getSimpleName();

    public static String getUserName() {
        return "admin";
    }

    public static String getPassWord() {
        return "123456";
    }

    public static boolean allowAnoymous() {
        return true;
    }

    public static File getChrootDir() {
        File chrootDir = new File(Environment.getExternalStorageDirectory().getPath());
        return chrootDir;
    }

    public static String getChrootDirAsString() {
        File dirFile = getChrootDir();
        return dirFile != null ? dirFile.getAbsolutePath() : "";
    }

    public static String getIP() {
        return "192.168.100.91";
    }
    public static int getPortNumber() {
        return 2121;
    }


    public static Set<String> getAutoConnectList() {
//        SharedPreferences sp = getSharedPreferences();
//        return sp.getStringSet("autoconnect_preference", new TreeSet<>());
        return null;
    }

    /**
     * @return the SharedPreferences for this application
     */
//    private static SharedPreferences getSharedPreferences() {
//        final Context context = ITransferApp.getAppContext();
//        return PreferenceManager.getDefaultSharedPreferences(context);
//    }

    // cleaning up after his
    protected static int inputBufferSize = 256;
    protected static boolean allowOverwrite = false;
    protected static int dataChunkSize = 8192; // do file I/O in 8k chunks
    protected static int sessionMonitorScrollBack = 10;
    protected static int serverLogScrollBack = 10;

    public static int getInputBufferSize() {
        return inputBufferSize;
    }

    public static void setInputBufferSize(int inputBufferSize) {
        FsSettings.inputBufferSize = inputBufferSize;
    }

    public static boolean isAllowOverwrite() {
        return allowOverwrite;
    }

    public static void setAllowOverwrite(boolean allowOverwrite) {
        FsSettings.allowOverwrite = allowOverwrite;
    }

    public static int getDataChunkSize() {
        return dataChunkSize;
    }

    public static void setDataChunkSize(int dataChunkSize) {
        FsSettings.dataChunkSize = dataChunkSize;
    }

    public static int getSessionMonitorScrollBack() {
        return sessionMonitorScrollBack;
    }

    public static void setSessionMonitorScrollBack(int sessionMonitorScrollBack) {
        FsSettings.sessionMonitorScrollBack = sessionMonitorScrollBack;
    }

    public static int getServerLogScrollBack() {
        return serverLogScrollBack;
    }

    public static void setLogScrollBack(int serverLogScrollBack) {
        FsSettings.serverLogScrollBack = serverLogScrollBack;
    }

}

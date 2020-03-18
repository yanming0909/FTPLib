package com.patent.ftpservice.ftpclient;

import android.os.Environment;
import android.os.RecoverySystem;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.log4j.helpers.ThreadLocalMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FTPUtil extends BaseModel {
    private static final String TAG = FTPUtil.class.getName();
    /**
     * FTP连接.
     */
    private FTPClient ftpClient;
    private int reUploadCount;
    private int totalsize;
    private float pre_inc;
    private String ip,user,psd;
    private int port;

    public  String rootPath;
    private String desDirectory;//服务器目标路径
    public static String dir = "/ftp";//dvr目录

    public FTPUtil(String ip, int port, String user, String psd,String ftpPath) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.psd = psd;
        this.desDirectory = ftpPath;
        createDirsFiles();
        reUploadCount = 5;
    }
    /**
     * 创建服务器配置文件
     */
    public void createDirsFiles(){
        try {
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+dir
                +desDirectory;
            File dir = new File(rootPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 连接到FTP服务器
     *
     * @param port 端口
     * @return 是否连接成功
     */
    private boolean ftpConnect(String hostName, int port, String userName, String password) {
        try {
            ftpClient = new FTPClient();
            Log.d(TAG, "connecting to the ftp server " + hostName + ":" + port);
            ftpClient.connect(hostName, port);
            // 根据返回的状态码，判断链接是否建立成功
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                Log.d(TAG, "login to the ftp server");
                boolean status = ftpClient.login(userName, password);

                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error: could not connect to host " + hostName);
            return false;
        }
        return false;
    }

    /**
     * 断开ftp服务器连接
     *
     * @return 断开结果
     */
    private boolean ftpDisconnect() {

        // 判断空指针
        if (ftpClient == null) {
            return true;
        }

        // 断开ftp服务器连接
        try {
            ftpClient.logout();
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
            ftpClient = null;
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server."+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * ftp 文件上传 续传
     *
     * @param srcFilePath  源文件目录
     * @param desFileName  文件名称
     * @return 文件上传结果
     */
    public void ftpUploadContinued(String srcFilePath, String desFileName, FTPCallback ftpCallback) {
        if (TextUtils.isEmpty(ip)){
            if (ftpCallback != null) ftpCallback.uploadFail("ip empty");
            return;
        }
        if (port == 0){
            if (ftpCallback != null) ftpCallback.uploadFail("port can not be 0");
            return;
        }
        if (TextUtils.isEmpty(user)){
            if (ftpCallback != null) ftpCallback.uploadFail("user empty");
            return;
        }
        if (TextUtils.isEmpty(desDirectory)){
            if (ftpCallback != null) ftpCallback.uploadFail("desDirectory empty");
            return;
        }
        if (TextUtils.isEmpty(srcFilePath)){
            if (ftpCallback != null) ftpCallback.uploadFail("srcFilePath empty");
            return;
        }
        if (TextUtils.isEmpty(desFileName)){
            if (ftpCallback != null) ftpCallback.uploadFail("desFileName empty");
            return;
        }
        reUploadCount --;
        if (reUploadCount < 0) {
            if (ftpCallback != null) ftpCallback.uploadFail("reUploadCount < 0");
            return;
        }
        /*
         * 设置文件传输模式
         * 避免一些可能会出现的问题，在这里必须要设定文件的传输格式。
         * 在这里我们使用BINARY_FILE_TYPE来传输文本、图像和压缩文件。
         */
        Observable.create(new ObservableOnSubscribe<Boolean>() {


            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                try {
                    if (ftpConnect(ip,port,user,psd)) {
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                        ftpClient.enterLocalPassiveMode();
                        boolean b = ftpChangeDir(desDirectory);
                        Log.d(TAG, "ftpChangeDir: " + b);
                        Log.d(TAG, "getFTPfile: " + getFTPfile());
                        //设置进度条监听
                        ftpClient.setCopyStreamListener(streamListener);

//=========================

                        String remoteFileName = desDirectory + "/" + desFileName;
                        FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
                        FileInputStream fileInputStream;
                        if (files.length == 1) {
                            long remoteSize = files[0].getSize();
                            File f = new File(srcFilePath);
                            long localSize = f.length();
                            Log.d(TAG, "localSize-" + localSize + " remoteSize-" + remoteSize);
                            if (remoteSize >= localSize) {
                                Log.d(TAG, "两个文件大小一样,重新上传");
                                if (ftpClient.deleteFile(remoteFileName)) {
                                    Log.d(TAG, "delete success :" + remoteFileName);
                                } else {
                                    Log.d(TAG, "delete failed :" + remoteFileName);
                                    Log.d(TAG, "ftpClient.getReplyCode() " + ftpClient.getReplyCode() + "FTP.getReplyString()  :" + ftpClient.getReplyString());
                                }
                                fileInputStream = new FileInputStream(srcFilePath);
                                boolean status = storeFile(fileInputStream, desFileName);
                                emitter.onNext(status);
                                emitter.onComplete();
                            } else {
                                Log.d(TAG, "remoteSize < localSize");
                                fileInputStream = new FileInputStream(srcFilePath);
                                if (fileInputStream.skip(remoteSize) == remoteSize) {
                                    totalsize = fileInputStream.available();
                                    ftpClient.setRestartOffset(remoteSize);
                                    Log.d(TAG, "112 start storeFile :" + ftpClient.getRestartOffset());
                                    boolean status = storeFile(fileInputStream,remoteFileName);
                                    emitter.onNext(status);
                                    emitter.onComplete();
                                }
                            }

                        }else {
                            Log.d(TAG, "no file " + remoteFileName);
                            fileInputStream = new FileInputStream(srcFilePath);
                            boolean status = storeFile(fileInputStream, desFileName);
                            emitter.onNext(status);
                            emitter.onComplete();
                        }
                    }else {
                        emitter.onError(new SecurityException("login fail"));
                        emitter.onComplete();
                    }
                } catch (IOException e) {
                    emitter.onError(e);
                    ftpClient.setCopyStreamListener(null);
                } finally {
                    if (ftpClient.isConnected()) {
                        try {
                            ftpClient.disconnect();
                        } catch (IOException ioe) {
                            // do nothing
                        }
                    }
                    emitter.onComplete();
                }

            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new BaseSafeObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean b) {
                        if (b) {
                            Log.d(TAG,"ftpUpload done");
                            reUploadCount = 0;
                            if (ftpCallback != null) ftpCallback.uploadSuccess();
                        }else {
                            ftpUploadContinued(srcFilePath, desFileName, ftpCallback);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"ftpUpload fail "+e.getMessage());
                        ftpUploadContinued(srcFilePath, desFileName, ftpCallback);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });

    }

    /**
     * 正式开始上传
     */
    private boolean storeFile(FileInputStream fileInputStream, String desFileName) throws IOException {
        if (ftpClient == null) return false;
        totalsize = fileInputStream.available();
        Log.d(TAG, "start storeFile");
        boolean status = ftpClient.storeFile(desFileName, fileInputStream);
        //退出登陆FTP，关闭ftpCLient的连接
        ftpClient.setCopyStreamListener(null);
        ftpClient.logout();
        ftpClient.disconnect();
        fileInputStream.close();
        return status;
    }

    /**
     * ftp 文件上传
     *
     * @param srcFilePath  源文件目录
     * @param desFileName  文件名称
     * @return 文件上传结果
     */
    public void ftpUpload(String srcFilePath, String desFileName,FTPCallback ftpCallback) {
        if (TextUtils.isEmpty(ip)
                || TextUtils.isEmpty(user)
                || TextUtils.isEmpty(psd)
                || TextUtils.isEmpty(desDirectory)
                || TextUtils.isEmpty(srcFilePath)
                ||TextUtils.isEmpty(desFileName)) {
            if (ftpCallback != null) ftpCallback.uploadFail("parmas empty");
            return;
        }
        /*
         * 设置文件传输模式
         * 避免一些可能会出现的问题，在这里必须要设定文件的传输格式。
         * 在这里我们使用BINARY_FILE_TYPE来传输文本、图像和压缩文件。
         */
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                try {
                    if (ftpConnect(ip,port,user,psd)) {
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                        ftpClient.enterLocalPassiveMode();
                        boolean b = ftpChangeDir(desDirectory);
                        Log.d(TAG, "ftpChangeDir: " + b);
                        Log.d(TAG, "getFTPfile: " + getFTPfile());
                        Log.d(TAG, "srcFilePath: " + srcFilePath);
                        Log.d(TAG, "desFileName: " + desFileName);
                        FileInputStream srcFileStream = new FileInputStream(srcFilePath);
                        totalsize = srcFileStream.available();
                        ftpClient.setCopyStreamListener(streamListener);
                        boolean status = ftpClient.storeFile(desFileName, srcFileStream);
                        srcFileStream.close();
                        emitter.onNext(status);
                        emitter.onComplete();
                    }else {
                        emitter.onError(new SecurityException());
                        emitter.onComplete();
                    }
                } catch (IOException e) {
                    emitter.onError(e);
                    emitter.onComplete();
                }

            }
        }).delay(2,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new BaseSafeObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean b) {
                        super.onNext(b);
                        if (ftpCallback!=null) ftpCallback.uploadSuccess();

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (ftpCallback!=null) ftpCallback.uploadFail(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        ftpDisconnect();
                    }
                });

    }

    /**
     * ftp 更改目录
     *
     * @param path 更改的路径
     * @return 更改是否成功
     */
    public boolean ftpChangeDir(String path) {
        boolean status = false;
        try {
            ftpClient.makeDirectory(path);
            status = ftpClient.changeWorkingDirectory(path);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "change directory failed: " + e.getLocalizedMessage());
        }
        return status;
    }

    public String getFTPfile() {
        String FTPWorkingPath = "";
        try {
            FTPWorkingPath = ftpClient.printWorkingDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FTPWorkingPath;
    }

    public interface FTPCallback {
        void uploadSuccess();

        void uploadFail(String error);
    }


    private CopyStreamListener streamListener = new CopyStreamListener() {
        @Override
        public void bytesTransferred(CopyStreamEvent copyStreamEvent) {
//totalsize = copyStreamEvent.getTotalBytesTransferred();
            Log.d(TAG, "bytesTransferred :" + copyStreamEvent.getStreamSize());
        }

        /**
         *
         * @param l 当前总共已传输字节数
         * @param i 最后一次传输字节数
         */
        @Override
        public void bytesTransferred(long l, int i, long l1) {
            float inc = (l * 100) / totalsize;
            if ( inc <= pre_inc) {
                return;
            }
            pre_inc = inc;
            Log.d(TAG, "upload progress = " + inc + " %");
        }
    };
    public void release(){
        ftpDisconnect();
    }
}

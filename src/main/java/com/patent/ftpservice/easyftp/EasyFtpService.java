package com.patent.ftpservice.easyftp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.patent.ftpservice.easyftp.easyutil.LocalUser;
import com.patent.ftpservice.easyftp.easyutil.LocalUserManager;
import com.patent.ftpservice.easyftp.easyutil.SettingConfig;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.listener.ListenerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EasyFtpService extends Service {
    private static final String TAG = EasyFtpService.class.getName();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static boolean ftpRunning = false;
    private FtpServer theFtpServer;
    private SettingConfig theSettingConfig;

    @Override
    public void onCreate() {
        Log.w(TAG, "ftp create");
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocalUser adminUser;
        LocalUser anonymousUser;
        Log.v(TAG, "execute ftp service");
        if (ftpRunning)
            stopFTP();
        Log.w(TAG, "get ftp settings");
        if (theSettingConfig == null) {
            theSettingConfig = new SettingConfig();
        }
        Log.w(TAG, "start ftp service");
        String userName = theSettingConfig.getUserName();
        String password = theSettingConfig.getPassword();
        String theFtpDir = theSettingConfig.getHomeDir();
        int theFtpPort = theSettingConfig.getPort();
        boolean theCanWrite = !theSettingConfig.getrRadOnly();
        int theIdleTime = 0;
        if (theSettingConfig.getIdleCloseConnection()) {
            theIdleTime = theSettingConfig.getIdleKeepTime() * 60;
        }
        boolean isAuthentication = theSettingConfig.getAuthentication();
        adminUser = new LocalUser(userName, password, isAuthentication, theFtpDir, theCanWrite, theIdleTime);
        anonymousUser = new LocalUser("anonymous", "", !isAuthentication, theFtpDir, theCanWrite, theIdleTime);
        LocalUserManager localUserManager = new LocalUserManager(adminUser, anonymousUser);
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        serverFactory.setUserManager(localUserManager);
        listenerFactory.setPort(theFtpPort);
        listenerFactory.setServerAddress(theSettingConfig.getServerAddress());
        serverFactory.addListener("default", listenerFactory.createListener());

//            DataConnectionConfigurationFactory dccFactory = new DataConnectionConfigurationFactory();
//            dccFactory.setActiveLocalPort(2121);//主动模式使用的端口
//            dccFactory.setPassiveIpCheck(true);
//            dccFactory.setPassivePorts("1025-9999");  //被动模式使用的端口范围
//            //↓ 就是这个，我的服务器内网IP是192.168.1.10，路由器的ip是另一个。当我没有设置这个选择，当客户端登录成功后
//            //这里默认返回的是192.168.1.10的IP给客户端，客户端当然连接不成功了，因为客户端根本不在我们一个局域网里面。
//            //所以这里我们一般要设置成外网ip（也可以设置域名，这样不用担心ip被变化的问题了）
//            dccFactory.setPassiveExternalAddress(theSettingConfig.getServerAddress());
//            DataConnectionConfiguration dcc=dccFactory.createDataConnectionConfiguration();
//            listenerFactory.setDataConnectionConfiguration(dcc);

        // 配置服务器被操作的命令等回复信息，下面详细介绍
        Map<String, Ftplet> ftplets = new HashMap<>();
        ftplets.put("miaFtplet", new MyFtpLet());
        serverFactory.setFtplets(ftplets);
        FtpServer ftpServer = serverFactory.createServer();
        theFtpServer = ftpServer;
        try {
            ftpServer.start();
            ftpRunning = true;
        } catch (Exception e) {
            Log.d(TAG, "ftp start error " + e.getMessage());
            e.printStackTrace();
        }
        return START_STICKY;
    }
    public class MyFtpLet extends DefaultFtplet {

        @Override
        public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException {
            Log.d(TAG, "beforeCommand: getRequestLine  "+request.getRequestLine());
            return super.beforeCommand(session, request);
        }

        @Override
        public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException {
            Log.d(TAG, "afterCommand: getRequestLine "+request.getRequestLine());
            return super.afterCommand(session, request, reply);
        }

        @Override
        public FtpletResult onUploadStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
            Log.d(TAG, "onUploadStart: getRequestLine  "+request.getRequestLine());
            return super.onUploadStart(session, request);
        }

        @Override
        public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
            Log.d(TAG, "onUploadEnd: getRequestLine  "+request.getRequestLine());
            return super.onUploadEnd(session, request);
        }
    }
    @Override
    public void onDestroy() {
        Log.w(TAG, "onDestroy");
        stopFTP();
    }

    private void stopFTP() {
        if (theFtpServer != null) {
            Log.w(TAG, "stop ftp service");
            theFtpServer.stop();
            theFtpServer = null;
            ftpRunning = false;
        }
    }

    public int ftpPort() {
        if (theSettingConfig == null) {
            return 0;
        }
        return theSettingConfig.getPort();
    }

    public static boolean isFtpRunning() {
        return ftpRunning;
    }

}

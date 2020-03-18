package com.patent.ftpservice.easyftp.easyutil;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalUser implements User {
    private List<Authority> authorities = new ArrayList();
    private String homeDir = null;
    private boolean isCanWrite = true;
    private boolean isEnabled = true;
    private int maxIdleTimeSec = 0;
    private String name = null;
    private String password = null;

    public LocalUser(String name2, String password2, boolean isEnabled2, String homeDir2, boolean isCanWrite2, int maxIdleTimeSec2) {
        this.name = name2;
        this.password = password2;
        this.isEnabled = isEnabled2;
        this.homeDir = homeDir2;
        this.isCanWrite = isCanWrite2;
        if (maxIdleTimeSec2 <= 0) {
            maxIdleTimeSec2 = 0;
        }
        this.maxIdleTimeSec = maxIdleTimeSec2;
        if (this.isCanWrite) {
            this.authorities.add(new WritePermission());
        }
        this.authorities.add(new ConcurrentLoginPermission(0, 0));
        this.authorities.add(new TransferRatePermission(0, 0));
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public List<Authority> getAuthorities() {
        if (this.authorities != null) {
            return Collections.unmodifiableList(this.authorities);
        }
        return null;
    }

    public int getMaxIdleTime() {
        return this.maxIdleTimeSec;
    }

    public boolean getEnabled() {
        return this.isEnabled;
    }

    public String getHomeDirectory() {
        return this.homeDir;
    }

    public AuthorizationRequest authorize(AuthorizationRequest request) {
        if (this.authorities == null) {
            return null;
        }
        boolean someoneCouldAuthorize = false;
        for (Authority authority : this.authorities) {
            if (authority.canAuthorize(request)) {
                someoneCouldAuthorize = true;
                request = authority.authorize(request);
                if (request == null) {
                    return null;
                }
            }
        }
        if (someoneCouldAuthorize) {
            return request;
        }
        return null;
    }

    public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
        List<Authority> selected = new ArrayList<>();
        for (Authority authority : this.authorities) {
            if (authority.getClass().equals(clazz)) {
                selected.add(authority);
            }
        }
        return selected;
    }

}

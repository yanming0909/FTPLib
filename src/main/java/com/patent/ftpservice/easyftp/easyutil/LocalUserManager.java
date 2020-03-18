package com.patent.ftpservice.easyftp.easyutil;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalUserManager implements UserManager {
    private LocalUser adminUser;
    private LocalUser anonymousUser;

    public LocalUserManager(LocalUser theAdminUser, LocalUser theAnonymousUser) {
        this.adminUser = theAdminUser;
        this.anonymousUser = theAnonymousUser;
    }

    public void save(User arg0) throws FtpException {
    }

    public void delete(String arg0) throws FtpException {
    }

    public String getAdminName() throws FtpException {
        if (this.adminUser == null) {
            return null;
        }
        return this.adminUser.getName();
    }

    public boolean isAdmin(String theUserName) throws FtpException {
        if (this.adminUser == null) {
            return false;
        }
        return this.adminUser.getName().equals(theUserName);
    }

    public String[] getAllUserNames() throws FtpException {
        List<String> userNameList = new ArrayList<>();
        if (this.adminUser != null) {
            userNameList.add(this.adminUser.getName());
        }
        if (this.anonymousUser != null) {
            userNameList.add("anonymous");
        }
        Collections.sort(userNameList);
        return (String[]) userNameList.toArray(new String[0]);
    }

    public User getUserByName(String theUserName) throws FtpException {
        if (doesExist(theUserName)) {
            return null;
        }
        if (this.adminUser == null || !this.adminUser.getName().equals(theUserName)) {
            return this.anonymousUser;
        }
        return this.adminUser;
    }

    public boolean doesExist(String theUserName) throws FtpException {
        if (this.adminUser != null && this.adminUser.getName().equals(theUserName)) {
            return true;
        }
        if (this.anonymousUser == null || !"anonymous".equals(theUserName)) {
            return false;
        }
        return true;
    }

    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            String userName = upauth.getUsername().trim();
            String password = upauth.getPassword().trim();
            if (userName == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            if (password == null) {
                password = "";
            }
            User theUser = null;
            if (this.adminUser != null && this.adminUser.getName().equals(userName)) {
                theUser = this.adminUser;
            } else if (this.anonymousUser != null && "anonymous".equals(userName)) {
                theUser = this.anonymousUser;
            }
            if (theUser != null && theUser.getPassword().equals(password)) {
                return theUser;
            }
            throw new AuthenticationFailedException("Authentication failed");
        } else if (!(authentication instanceof AnonymousAuthentication)) {
            throw new IllegalArgumentException("Authentication not supported by this user manager");
        } else if (this.anonymousUser != null) {
            return this.anonymousUser;
        } else {
            throw new AuthenticationFailedException("Authentication failed");
        }
    }

}

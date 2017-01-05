package com.android.projects.mateusz.olliecontroller.model;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Mateusz on 17.12.2016.
 */

public class ClientModel {

    private InetAddress hostAddress;
    private String serverResponse = "TEST";
    private String actualUserName;

    private static ClientModel instance;

    public static ClientModel getInstance(){
        if (instance == null){
            instance = new ClientModel();
        }
        return instance;
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        try {
            this.hostAddress = InetAddress.getByName(hostAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String getServerResponse() {
        return serverResponse;
    }

    public void setServerResponse(String serverResponse) {
        this.serverResponse = serverResponse;
    }

    public String getActualUserName() {
        return actualUserName;
    }

    public void setActualUserName(String actualUserName) {
        this.actualUserName = actualUserName;
    }
}

package com.william.xu.bean;

import java.util.Date;

/**
 * Created by william on 2017/3/11.
 */
public class ServerNode {

    private String mac;

    private String ip;

    private String port;

    private String status;

    private Date regTime;

    private EnumConstServerNodeStatus serverNodeStatus;


    private String masterId;


    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public EnumConstServerNodeStatus getServerNodeStatus() {
        return serverNodeStatus;
    }

    public void setServerNodeStatus(EnumConstServerNodeStatus serverNodeStatus) {
        this.serverNodeStatus = serverNodeStatus;
    }

    public String getMac() {
        return mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}

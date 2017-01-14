package com.cute.meido.adapter;

/**
 * 规则信息封装类
 */

public class RegularInfo {
    private String id;
    private String status;
    private String date;
    private String time;
    private String location;
    private String address;
    private String premise;
    private String premiseInfo;
    private String action;
    private String actionInfo;

    public RegularInfo(String id, String status, String date, String time,
       String location, String address,String premise, String premiseInfo,
       String action, String actionInfo) {
        this.id = id;
        this.status = status;
        this.date = date;
        this.time = time;
        this.location = location;
        this.address = address;
        this.premise = premise;
        this.premiseInfo = premiseInfo;
        this.action = action;
        this.actionInfo = actionInfo;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getPremise() {
        return premise;
    }

    public String getPremiseInfo() {
        return premiseInfo;
    }

    public String getAction() {
        return action;
    }

    public String getActionInfo() {
        return actionInfo;
    }

    public String getAddress() {
        return address;
    }
}

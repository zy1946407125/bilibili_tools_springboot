package com.example.entity;


public class Proxy {
    // 代理ip账号密码
    private String ProxyUser = "763633121274253312";
    private String ProxyPass = "SqLpBDcV";

    private Boolean ProxyOpen = true;

    // 代理服务器
    private String ProxyHost = "127.0.0.1";
    private Integer ProxyPort = 9999;

    public String getProxyUser() {
        return ProxyUser;
    }

    public void setProxyUser(String proxyUser) {
        ProxyUser = proxyUser;
    }

    public String getProxyPass() {
        return ProxyPass;
    }

    public void setProxyPass(String proxyPass) {
        ProxyPass = proxyPass;
    }

    public String getProxyHost() {
        return ProxyHost;
    }

    public void setProxyHost(String proxyHost) {
        ProxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return ProxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        ProxyPort = proxyPort;
    }

    public Boolean getProxyOpen() {
        return ProxyOpen;
    }

    public void setProxyOpen(Boolean proxyOpen) {
        ProxyOpen = proxyOpen;
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "ProxyUser='" + ProxyUser + '\'' +
                ", ProxyPass='" + ProxyPass + '\'' +
                ", ProxyOpen=" + ProxyOpen +
                ", ProxyHost='" + ProxyHost + '\'' +
                ", ProxyPort=" + ProxyPort +
                '}';
    }
}

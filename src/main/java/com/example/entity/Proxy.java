package com.example.entity;


public class Proxy {
    // 代理隧道验证信息
    private String ProxyUser = "756749985852575744";
    private String ProxyPass = "pVqDvkYF";

    // 代理服务器
    private String ProxyHost = "http-dynamic.xiaoxiangdaili.com";
    private Integer ProxyPort = 10030;

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

    @Override
    public String toString() {
        return "Proxy{" +
                "ProxyUser='" + ProxyUser + '\'' +
                ", ProxyPass='" + ProxyPass + '\'' +
                ", ProxyHost='" + ProxyHost + '\'' +
                ", ProxyPort=" + ProxyPort +
                '}';
    }
}

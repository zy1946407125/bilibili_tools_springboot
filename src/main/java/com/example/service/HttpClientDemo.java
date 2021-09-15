package com.example.service;

import com.alibaba.fastjson.JSONObject;
import com.example.entity.Proxy;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpClientDemo {
    //    // 代理隧道验证信息
//    private String ProxyUser = "";
//    private String ProxyPass = "";
//
//    // 代理服务器
//    private String ProxyHost = "http-dynamic.xiaoxiangdaili.com";
//    private Integer ProxyPort = 10030;
    private static Proxy proxyInfo = null;

    private static HttpHost proxy = null;
    private static HttpClientBuilder clientBuilder = null;

    static {
        proxyInfo = new Proxy();
        proxy = new HttpHost(proxyInfo.getProxyHost(), proxyInfo.getProxyPort(), "http");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyInfo.getProxyUser(), proxyInfo.getProxyPass()));

        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(10000).build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(10000)
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setExpectContinueEnabled(false)
                .setProxy(proxy)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        clientBuilder = HttpClients.custom()
                .setDefaultSocketConfig(socketConfig)
                .disableAutomaticRetries()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(credsProvider);
    }


    public void updateProxyInfo(String _ProxyUser, String _ProxyPass, String _ProxyHost, Integer _ProxyPort) {
        proxyInfo.setProxyUser(_ProxyUser);
        proxyInfo.setProxyPass(_ProxyPass);
        proxyInfo.setProxyHost(_ProxyHost);
        proxyInfo.setProxyPort(_ProxyPort);

        proxy = new HttpHost(proxyInfo.getProxyHost(), proxyInfo.getProxyPort(), "http");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyInfo.getProxyUser(), proxyInfo.getProxyPass()));

        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(10000).build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(10000)
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setExpectContinueEnabled(false)
                .setProxy(proxy)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        clientBuilder = HttpClients.custom()
                .setDefaultSocketConfig(socketConfig)
                .disableAutomaticRetries()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(credsProvider);
    }

    public Proxy getProxyInfo() {
        System.out.println(proxyInfo);
        System.out.println(proxy);
        System.out.println(clientBuilder);
        return proxyInfo;
    }

    public JSONObject getUrlContent_Get(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate");
        httpGet.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpGet.addHeader("Cache-Control", "max-age=0");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        CloseableHttpResponse httpResp = null;

        JSONObject jsonObject = null;
        String html = null;

        try {
            // JDK 8u111版本后，目标页面为HTTPS协议，启用proxy用户密码鉴权
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

            AuthCache authCache = new BasicAuthCache();
            authCache.put(proxy, new BasicScheme());

            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            httpResp = clientBuilder.build().execute(httpGet, localContext);

            html = IOUtils.toString(httpResp.getEntity().getContent(), "UTF-8");
            jsonObject = JSONObject.parseObject(html);
//            System.out.println(html);
//            System.out.println(jsonObject);
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            try {
                if (httpResp != null) {
                    httpResp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public Object getUrlContent_Post(String url, StringEntity stringEntity) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpPost.addHeader("Accept-Encoding", "gzip, deflate");
        httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.addHeader("Cache-Control", "max-age=0");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        if (stringEntity != null) {
            httpPost.setEntity(stringEntity);
        }

        CloseableHttpResponse httpResp = null;

        JSONObject jsonObject = null;

        try {
            // JDK 8u111版本后，目标页面为HTTPS协议，启用proxy用户密码鉴权
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

            AuthCache authCache = new BasicAuthCache();
            authCache.put(proxy, new BasicScheme());

            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            httpResp = clientBuilder.build().execute(httpPost, localContext);

            String html = IOUtils.toString(httpResp.getEntity().getContent(), "UTF-8");
            jsonObject = JSONObject.parseObject(html);
//            System.out.println(html);
//            System.out.println(jsonObject);
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            try {
                if (httpResp != null) {
                    httpResp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }


    public JSONObject getUrlContent_Post2(String url, StringEntity stringEntity, BasicHeader cookie) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpPost.addHeader("Accept-Encoding", "gzip, deflate");
        httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.addHeader("Cache-Control", "max-age=0");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");

//        BasicHeader cookie = new BasicHeader("cookie", "buvid2=F7A7D447-BBC8-41D1-B496-C6435F89E8AC148823infoc;buvid3=F7A7D447-BBC8-41D1-B496-C6435F89E8AC148823infoc;SESSDATA=b8b80c73%2C1647154555%2Cead1f%2A91;");
//        BasicHeader cookie = new BasicHeader("cookie", "SESSDATA=b8b80c73%2C1647154555%2Cead1f%2A91;");

//        BasicHeader cookie = new BasicHeader("cookie", "buvid2=A5C98A33-E9E2-4048-BD03-842F68F9DB86148822infoc;buvid3=A5C98A33-E9E2-4048-BD03-842F68F9DB86148822infoc;SESSDATA=6687671e%2C1646835788%2Ca6604%2A91");
        httpPost.addHeader(cookie);

        if (stringEntity != null) {
            httpPost.setEntity(stringEntity);
        }

        CloseableHttpResponse httpResp = null;

        JSONObject jsonObject = null;

        try {
            // JDK 8u111版本后，目标页面为HTTPS协议，启用proxy用户密码鉴权
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

            AuthCache authCache = new BasicAuthCache();
            authCache.put(proxy, new BasicScheme());

            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            httpResp = clientBuilder.build().execute(httpPost, localContext);

            String html = IOUtils.toString(httpResp.getEntity().getContent(), "UTF-8");
            jsonObject = JSONObject.parseObject(html);
//            System.out.println(html);
//            System.out.println(jsonObject);
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            try {
                if (httpResp != null) {
                    httpResp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static void main(String[] args) throws Exception {
        // 要访问的目标页面
//        String targetUrl = "http://httpbin.org/ip";
//        String targetUrl = "https://api.bilibili.com/x/web-interface/view?bvid=BV12Z4y1F7bk";
//        String targetUrl = "https://api.bilibili.com/x/report/click/h5";
        String targetUrl = "https://api.bilibili.com/x/web-interface/archive/like";
        StringEntity stringEntity = new StringEntity("bvid=BV1fK4y127Y6&csrf=ee47559073091d9f3810da664e71c6ff&like=1");//param参数，可以为"key1=value1&key2=value2"的一串字符串
//        StringEntity stringEntity = new StringEntity("bvid=BV1wb4y197By&csrf=6dccf677a340c68f3379b1ddc076cefd&like=1");//param参数，可以为"key1=value1&key2=value2"的一串字符串
        stringEntity.setContentType("application/x-www-form-urlencoded");

//        CookieStore cookieStores = new BasicCookieStore();
//        BasicClientCookie buvid2 = new BasicClientCookie("buvid2", "591A6386-913F-4FCF-B629-4D3FB3D8E8E1167629infoc");
//        BasicClientCookie buvid3 = new BasicClientCookie("buvid3", "591A6386-913F-4FCF-B629-4D3FB3D8E8E1167629infoc");
//        BasicClientCookie SESSDATA = new BasicClientCookie("SESSDATA", "dd6d5414%2C1646835922%2Cd2eaf%2A91");
//        cookieStores.addCookie(buvid2);
//        cookieStores.addCookie(buvid3);
//        cookieStores.addCookie(SESSDATA);
//        new HttpClientDemo().getUrlContent_Post2(targetUrl, stringEntity);
    }
}

package com.example.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private HttpClientDemo httpClientDemo;

    @Override
    public Boolean orderReturn(String id, String apikey) {
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=refund&order_id=" + id + "&apikey=" + apikey;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        Boolean status = false;
        try {
            status = urlContent_get.getBoolean("status");
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return status;
    }

    @Override
    public Boolean orderSetJXZ(String goodsId, String id, Integer startNum, Integer nowNum, String apikey) {
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=edit&goods_id=" + goodsId + "&order_id=" + id + "&start_num=" + startNum + "&now_num=" + nowNum + "&apikey=" + apikey;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        Boolean status = false;
        try {
            status = urlContent_get.getBoolean("status");
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return status;
    }

    @Override
    public Boolean orderSetWKS(String goodsId, String id, Integer startNum, Integer nowNum, String apikey) {
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=edit&goods_id=" + goodsId + "&order_state=wks&order_id=" + id + "&start_num=" + startNum + "&now_num=" + nowNum + "&apikey=" + apikey;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        Boolean status = false;
        try {
            status = urlContent_get.getBoolean("status");
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return status;
    }

    @Override
    public Boolean orderSetYWC(String goodsId, String id, Integer startNum, Integer nowNum, String apikey) {
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=edit&goods_id=" + goodsId + "&order_state=ywc&order_id=" + id + "&start_num=" + startNum + "&now_num=" + nowNum + "&apikey=" + apikey;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        Boolean status = false;
        try {
            status = urlContent_get.getBoolean("status");
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return status;
    }
}

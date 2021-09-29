package com.example.service;

import com.alibaba.fastjson.JSONObject;
import com.example.config.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private HttpClientDemo httpClientDemo;

    private Task task = Task.getTask();

    @Override
    public Boolean orderReturn(String goodsType, String id) {
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=refund&order_id=" + id + "&apikey=" + task.getGoodsIDAndKey(goodsType).getApikey();
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
    public JSONObject getOrder(String goodsType, String state, boolean update) {
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=" + task.getGoodsIDAndKey(goodsType).getGoodsId() + "&state=" + state + "&format=json&apikey=" + task.getGoodsIDAndKey(goodsType).getApikey() + "&chage_state=" + update;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        return urlContent_get;
    }

    @Override
    public Boolean updateOrder(String goodsType, String orderState, String id, Integer startNum, Integer nowNum) {
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=edit&order_state=" + orderState + "&goods_id=" + task.getGoodsIDAndKey(goodsType).getGoodsId() + "&order_id=" + id + "&start_num=" + startNum + "&now_num=" + nowNum + "&apikey=" + task.getGoodsIDAndKey(goodsType).getApikey();
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

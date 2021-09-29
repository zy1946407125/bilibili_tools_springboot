package com.example.service;

import com.alibaba.fastjson.JSONObject;


public interface OrderService {

    public Boolean orderReturn(String goodsType, String id);

    public JSONObject getOrder(String goodsType, String state, boolean update);

    public Boolean updateOrder(String goodsType, String orderState, String id, Integer startNum, Integer nowNum);
}

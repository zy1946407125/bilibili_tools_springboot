package com.example.service;

import org.springframework.stereotype.Service;


public interface OrderService {

    public Boolean orderReturn(String id);

    public Boolean orderSetJXZ(String goodsId, String id, Integer startNum, Integer nowNum);

    public Boolean orderSetWKS(String goodsId, String id, Integer startNum, Integer nowNum);

    public Boolean orderSetYWC(String goodsId, String id, Integer startNum, Integer nowNum);

}

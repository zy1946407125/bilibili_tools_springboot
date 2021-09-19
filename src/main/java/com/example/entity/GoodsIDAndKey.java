package com.example.entity;

/**
 * ClassName: GoodsIDAndKey
 * Description:
 * date: 2021/9/19 20:36
 * author: zouyuan
 */
public class GoodsIDAndKey {

    private String key;
    private String goodsId;
    private String apikey;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GoodsIDAndKey() {
    }

    public String getGoodsId() {
        return goodsId;
    }

    public GoodsIDAndKey(String goodsId, String apikey) {
        this.goodsId = goodsId;
        this.apikey = apikey;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    @Override
    public String toString() {
        return "GoodsIDAndKey{" +
                "key='" + key + '\'' +
                ", goodsId='" + goodsId + '\'' +
                ", apikey='" + apikey + '\'' +
                '}';
    }
}

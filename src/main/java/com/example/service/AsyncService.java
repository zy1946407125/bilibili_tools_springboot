package com.example.service;

import com.example.entity.BVInfo;
import com.example.entity.UserInfo;

import java.util.Map;


public interface AsyncService {
    void executeAsyncWatch(BVInfo bvInfo);

    void executeAsyncLike(BVInfo bvInfo);

    void executeAsyncFollow(UserInfo userInfo);

    Map<String, Integer> getBVViewAndLike(String bvid);

    public Map<String, Integer> getUserFans(String mid);
}

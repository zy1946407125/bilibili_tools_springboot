package com.example.service;

import com.example.entity.BVInfo;

import java.util.Map;


public interface AsyncService {
    void executeAsyncWatch(BVInfo bvInfo);

    void executeAsyncLike(String bvid);
}

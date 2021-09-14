package com.example.service;

import com.alibaba.fastjson.JSONObject;
import com.example.config.ExecutorConfig;
import com.example.config.Task;
import com.example.config.ThreadInfo;
import com.example.entity.BVInfo;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class AsyncServiceImpl implements AsyncService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);
    private Task task = Task.getTask();
    private ThreadInfo threadInfo = ThreadInfo.getThreadInfo();

    @Autowired
    private HttpClientDemo httpClientDemo;

    @Override
    @Async("asyncServiceWatch")
    public void executeAsyncWatch(BVInfo bvInfo) {
        logger.info("start executeAsync");
        System.out.println("start executeAsync");
        Integer nowView = bvInfo.getStartWatchNum();
        Integer oldView = bvInfo.getStartWatchNum();
        Integer tmpView = bvInfo.getStartWatchNum();
        while (true) {
            Map<String, Integer> bvViewAndLike = getBVViewAndLike(bvInfo.getBvid());
            tmpView = nowView;
            nowView = bvViewAndLike.get("view");
            oldView = bvInfo.getStartWatchNum();
            logger.info("当前数量：" + nowView);
            if (nowView != null) {
                if (oldView < nowView) {
                    //更新当前播放数
                    upDateView(bvInfo.getId(), nowView);
                }
            } else {
                nowView = tmpView;
            }
            if (task.getWatchTask(bvInfo.getBvid()) != null && (bvInfo.getStartWatchNum() + bvInfo.getNeedWatchNum() > nowView)) {
                String targetUrl = "https://api.bilibili.com/x/report/click/h5";
                StringEntity stringEntity = null;//param参数，可以为"key1=value1&key2=value2"的一串字符串
                try {
                    stringEntity = new StringEntity("bvid=" + bvInfo.getBvid());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                stringEntity.setContentType("application/x-www-form-urlencoded");
                Object urlContent_post = httpClientDemo.getUrlContent_Post(targetUrl, stringEntity);
                logger.info(JSONObject.toJSONString(urlContent_post));
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        logger.info("end executeAsync");
        System.out.println("end executeAsync");
        threadInfo.releaseWatchThreadNum();
        if (task.getWatchTask(bvInfo.getBvid()) != null) {
            task.releaseWatchTask(bvInfo.getBvid(), 1);
        }
        Map<String, Integer> bvViewAndLike = getBVViewAndLike(bvInfo.getBvid());
        Integer view = bvViewAndLike.get("view");
        //判断是否已经完成任务
        if ((bvInfo.getStartWatchNum() + bvInfo.getNeedWatchNum() < view)) {
            //完成任务
            logger.info("完成任务，当前数量：" + view);
            task.updateBVInfo(bvInfo.getId(), "完成");
        } else if (task.getWatchTask(bvInfo.getBvid()) == null) {
            //强制停止
            logger.info("强制停止");
            task.updateBVInfo(bvInfo.getId(), "停止");
        }
    }

    @Override
    @Async("asyncServiceLike")
    public void executeAsyncLike(String bvid) {
        logger.info("start executeAsync");
        System.out.println("start executeAsync");
        int i = 1;
        while (i < 10) {
            if (task.getLikeTask(bvid) > 0) {
                logger.info(String.valueOf(i));
                i++;
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        logger.info("end executeAsync");
        System.out.println("end executeAsync");
        threadInfo.releaseLikeThreadNum();
        task.releaseLikeTask(bvid);
    }


    Map<String, Integer> getBVViewAndLike(String bvid) {
        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        Map<String, Integer> map = new HashMap<>();
        try {
            JSONObject data = (JSONObject) urlContent_get.get("data");
            JSONObject stat = (JSONObject) data.get("stat");
            Integer view = (Integer) stat.get("view");
            Integer like = (Integer) stat.get("like");
            map.put("view", view);
            map.put("like", like);
            return map;
        } catch (Exception e) {
//            e.printStackTrace();
            return map;
        }
    }

    void upDateView(String id, Integer view) {
        for (int i = 0; i < task.getBVInfo().size(); i++) {
            if (task.getBVInfo().get(i).getId().equals(id)) {
                task.getBVInfo().get(i).setNowWatchNum(view);
                break;
            }
        }
    }
}

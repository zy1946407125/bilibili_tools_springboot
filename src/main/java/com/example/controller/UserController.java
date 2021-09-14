package com.example.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.config.Task;
import com.example.config.ThreadInfo;
import com.example.entity.BVInfo;
import com.example.entity.Proxy;
import com.example.service.AsyncService;
import com.example.service.HttpClientDemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;



@RestController
public class UserController {
    @Autowired
    private HttpClientDemo httpClientDemo;

    @Autowired
    private AsyncService asyncService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private Task task = Task.getTask();

    private ThreadInfo threadInfo = ThreadInfo.getThreadInfo();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    @RequestMapping("/getBVInfo")
    public Object getBVInfo(String bvid) {
        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        Object urlContent_get = httpClientDemo.getUrlContent_Get(url);
        int watchThreadNum = threadInfo.getWatchThreadNum();
        int likeThreadNum = threadInfo.getLikeThreadNum();
        Map<String, Object> map = new HashMap<>();
        map.put("bvinfo", urlContent_get);
        map.put("watchThreadNum", watchThreadNum);
        map.put("likeThreadNum", likeThreadNum);
        return map;
    }

//    @RequestMapping("/refresh")
//    public Object refresh(String bvid) {
//        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
//        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
//        try {
//            JSONObject data = (JSONObject) urlContent_get.get("data");
//            System.out.println("1: " + data);
//            JSONObject stat = (JSONObject) data.get("stat");
//            System.out.println("2: " + stat);
//            Integer view = (Integer) stat.get("view");
//            System.out.println("3: " + view);
//            Integer like = (Integer) stat.get("like");
//            System.out.println("4: " + like);
//            return "bbb";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "aaa";
//        }
//    }

    @RequestMapping("/getBVInfos")
    public Object getBVInfos() {
        List<String> bvids = new ArrayList<>();
        bvids.add("BV1mM4y1G7S6");
        bvids.add("BV1Yq4y1N7ir");
        bvids.add("BV1B34y1Q7f7");
        bvids.add("BV1Yq4y1Z7Rj");
        List<Object> data = new ArrayList<>();
        for (int i = 0; i < bvids.size(); i++) {
            String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvids.get(i);
            Object urlContent_get = httpClientDemo.getUrlContent_Get(url);
            data.add(urlContent_get);
        }
        return data;
    }

    @RequestMapping("/startWatch")
    public Object startWatch(BVInfo bvInfo) {
        Integer watchTask = task.getWatchTask(bvInfo.getBvid());
        if (watchTask != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 1);
            map.put("message", "已经存在一个相同任务");
            map.put("bvInfos", getBVInfoList());
            return map;
        } else {
            bvInfo.setThreadNum(5);
            Long startTimeStamp = new Date().getTime();
            String startTimeStr = simpleDateFormat.format(startTimeStamp);
            bvInfo.setStartTimeStamp(startTimeStamp);
            bvInfo.setStartTimeStr(startTimeStr);
            bvInfo.setId(bvInfo.getBvid() + "_" + bvInfo.getStartTimeStamp());
            if (threadInfo.getWatchThreadNum() - bvInfo.getThreadNum() >= 0) {
                bvInfo.setStatus("运行");
                System.out.println(bvInfo);
                task.addBVInfo(bvInfo);
                threadInfo.subWatchThreadNum(bvInfo.getThreadNum());
                task.setWatchTask(bvInfo.getBvid(), bvInfo.getThreadNum());
                logger.info("start Watch");
                //调用service层的任务
                for (int i = 0; i < bvInfo.getThreadNum(); i++) {
                    asyncService.executeAsyncWatch(bvInfo);
                }
                logger.info("end Watch");
                Map<String, Object> map = new HashMap<>();
                map.put("code", 0);
                map.put("message", "提交播放任务成功");
                map.put("bvInfos", getBVInfoList());
                return map;
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("code", 2);
                map.put("message", "空闲线程数不足");
                map.put("bvInfos", getBVInfoList());
                return map;
            }
        }

    }

    @RequestMapping("/stopWatch")
    public Object stopWatch(String id, String bvid) {
        logger.info("stop Watch");
        task.releaseWatchTask(bvid, 1000);
        task.updateBVInfo(id,"停止");
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "停止成功");
        map.put("bvInfos", getBVInfoList());
        return map;
    }

    @RequestMapping("/removeWatch")
    public Object removeWatch(String id, String bvid) {
        logger.info("remove Watch");
        Integer watchTask = task.getWatchTask(bvid);
        Map<String, Object> map = new HashMap<>();
        map.put("code", "1");
        map.put("message", "移除失败");
        map.put("bvInfos", getBVInfoList());
        if (watchTask == null) {
            for (int i = 0; i < task.getBVInfo().size(); i++) {
                if (id.equals(task.getBVInfo().get(i).getId())) {
                    task.getBVInfo().remove(task.getBVInfo().get(i));
                    map.put("code", "0");
                    map.put("message", "移除成功");
                    map.put("bvInfos", getBVInfoList());
                    break;
                }
            }
        }
        return map;
    }

    @RequestMapping("/startLike")
    public String startLike(String bvid, Integer num) {
        if (threadInfo.getLikeThreadNum() - num >= 0) {
            threadInfo.subLikeThreadNum(num);
            task.setLikeTask(bvid, num);
            logger.info("start Like");
            //调用service层的任务
            for (int i = 0; i < num; i++) {
                asyncService.executeAsyncLike(bvid);
            }
            logger.info("end Like");
            return "success_like";
        } else {
            return "error_like";
        }
    }

    @RequestMapping("/stopLike")
    public String stopLike(String bvid) {
        logger.info("stop Like");
        task.setLikeTask(bvid, 0);
        return "success";
    }

    @RequestMapping("/getThreadPoolInfo")
    public Object getThreadPoolInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("WatchThreadNum", threadInfo.getWatchThreadNum());
        info.put("LikeThreadNum", threadInfo.getLikeThreadNum());

        return info;
    }

    @RequestMapping("/getWatchTask")
    public Object getWatchTask(String id) {
        return task.getWatchTask(id);
    }

    @RequestMapping("/getLikeTask")
    public Object getLikeTask(String id) {
        return task.getLikeTask(id);
    }

    @RequestMapping("/getBVInfoList")
    public Object getBVInfoList() {
        return task.getBVInfo();
    }

    @RequestMapping("/updateProxyInfo")
    public Object updateProxyInfo(Proxy proxy) {
        System.out.println(proxy);
        httpClientDemo.updateProxyInfo(proxy.getProxyUser(), proxy.getProxyPass(), proxy.getProxyHost(), proxy.getProxyPort());
        Map<Object, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "更新成功");
        return map;
    }

    @RequestMapping("/getProxyInfo")
    public Object getProxyInfo() {
        Map<Object, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "获取成功");
        map.put("data", httpClientDemo.getProxyInfo());
        return map;
    }
}

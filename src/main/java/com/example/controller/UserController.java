package com.example.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.config.Task;
import com.example.config.ThreadInfo;
import com.example.entity.Account;
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
    //查询视频信息
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

    @RequestMapping("/startWatch")
    public Object startWatch(BVInfo bvInfo) {
        Integer watchTask = task.getWatchTask(bvInfo.getBvid());
        if (watchTask != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 1);
            map.put("message", "已经存在一个相同任务");
            map.put("bvInfos", getWatchBVInfoList());
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
                task.addWatchBVInfo(bvInfo);
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
                map.put("bvInfos", getWatchBVInfoList());
                return map;
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("code", 2);
                map.put("message", "空闲线程数不足");
                map.put("bvInfos", getWatchBVInfoList());
                return map;
            }
        }

    }

    @RequestMapping("/stopWatch")
    public Object stopWatch(String id, String bvid) {
        logger.info("stop Watch");
        task.releaseWatchTask(bvid, 1000);
        task.updateWatchBVInfo(id, "停止");
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "停止成功");
        map.put("bvInfos", getWatchBVInfoList());
        return map;
    }

    @RequestMapping("/removeWatch")
    public Object removeWatch(String id, String bvid) {
        logger.info("remove Watch");
        Integer watchTask = task.getWatchTask(bvid);
        Map<String, Object> map = new HashMap<>();
        map.put("code", "1");
        map.put("message", "移除失败");
        map.put("bvInfos", getWatchBVInfoList());
        if (watchTask == null) {
            for (int i = 0; i < task.getWatchBVInfo().size(); i++) {
                if (id.equals(task.getWatchBVInfo().get(i).getId())) {
                    task.getWatchBVInfo().remove(task.getWatchBVInfo().get(i));
                    map.put("code", "0");
                    map.put("message", "移除成功");
                    map.put("bvInfos", getWatchBVInfoList());
                    break;
                }
            }
        }
        return map;
    }

    @RequestMapping("/startLike")
    public Object startLike(BVInfo bvInfo) {
        Integer likeTask = task.getLikeTask(bvInfo.getBvid());
        if (likeTask != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 1);
            map.put("message", "已经存在一个相同任务");
            map.put("bvInfos", getLikeBVInfoList());
            return map;
        } else {
            bvInfo.setThreadNum(1);
            Long startTimeStamp = new Date().getTime();
            String startTimeStr = simpleDateFormat.format(startTimeStamp);
            bvInfo.setStartTimeStamp(startTimeStamp);
            bvInfo.setStartTimeStr(startTimeStr);
            bvInfo.setId(bvInfo.getBvid() + "_" + bvInfo.getStartTimeStamp());
            bvInfo.setRequestNum(0);
            bvInfo.setSuccessNum(0);
            if (threadInfo.getLikeThreadNum() - bvInfo.getThreadNum() >= 0) {
                bvInfo.setStatus("运行");
                System.out.println(bvInfo);
                task.addLikeBVInfo(bvInfo);
                threadInfo.subLikeThreadNum(bvInfo.getThreadNum());
                task.setLikeTask(bvInfo.getBvid(), bvInfo.getThreadNum());
                logger.info("start Like");
                //调用service层的任务
                for (int i = 0; i < bvInfo.getThreadNum(); i++) {
                    asyncService.executeAsyncLike(bvInfo);
                }
                logger.info("end Like");
                Map<String, Object> map = new HashMap<>();
                map.put("code", 0);
                map.put("message", "提交点赞任务成功");
                map.put("bvInfos", getLikeBVInfoList());
                return map;
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("code", 2);
                map.put("message", "空闲线程数不足");
                map.put("bvInfos", getLikeBVInfoList());
                return map;
            }
        }
    }

    @RequestMapping("/stopLike")
    public Object stopLike(String id, String bvid) {
        logger.info("stop Like");
        task.releaseLikeTask(bvid, 1000);
        task.updateLikeBVInfo(id, "停止");
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "停止成功");
        map.put("bvInfos", getLikeBVInfoList());
        return map;
    }

    @RequestMapping("/removeLike")
    public Object removeLike(String id, String bvid) {
        logger.info("remove Like");
        Integer likeTask = task.getLikeTask(bvid);
        Map<String, Object> map = new HashMap<>();
        map.put("code", "1");
        map.put("message", "移除失败");
        map.put("bvInfos", getLikeBVInfoList());
        if (likeTask == null) {
            for (int i = 0; i < task.getLikeBVInfo().size(); i++) {
                if (id.equals(task.getLikeBVInfo().get(i).getId())) {
                    task.getLikeBVInfo().remove(task.getLikeBVInfo().get(i));
                    map.put("code", "0");
                    map.put("message", "移除成功");
                    map.put("bvInfos", getLikeBVInfoList());
                    break;
                }
            }
        }
        return map;
    }

    @RequestMapping("/getWatchTask")
    public Object getWatchTask(String id) {
        return task.getWatchTask(id);
    }

    @RequestMapping("/getLikeTask")
    public Object getLikeTask(String id) {
        return task.getLikeTask(id);
    }

    @RequestMapping("/getWatchBVInfoList")
    public Object getWatchBVInfoList() {
        return task.getWatchBVInfo();
    }

    @RequestMapping("/getLikeBVInfoList")
    public Object getLikeBVInfoList() {
        return task.getLikeBVInfo();
    }

    @RequestMapping("/getThreadPoolInfo")
    public Object getThreadPoolInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("WatchThreadNum", threadInfo.getWatchThreadNum());
        info.put("LikeThreadNum", threadInfo.getLikeThreadNum());

        return info;
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


    @RequestMapping("/addAccount")
    public Object addAccount(Account account) {
        task.addAccount(account);
        Map<Object, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "添加成功");
        map.put("accountData", task.getAccounts());
        return map;
    }

    @RequestMapping("/removeAccount")
    public Object removeAccount(String dedeUserID) {
        boolean b = task.removeAccount(dedeUserID);
        Map<Object, Object> map = new HashMap<>();
        if (b) {
            map.put("code", 0);
            map.put("message", "移除成功");
        } else {
            map.put("code", 1);
            map.put("message", "移除失败");
        }
        map.put("accountData", task.getAccounts());
        return map;
    }

    @RequestMapping("/getAccounts")
    public Object getAccounts() {
        Map<Object, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "查询成功");
        map.put("accountData", task.getAccounts());
        return map;
    }
}

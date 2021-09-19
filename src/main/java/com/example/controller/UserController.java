package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.config.Task;
import com.example.config.ThreadInfo;
import com.example.entity.*;
import com.example.service.AsyncService;
import com.example.service.HttpClientDemo;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;


@RestController
@Service
public class UserController {
    @Autowired
    private HttpClientDemo httpClientDemo;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private OrderService orderService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private Task task = Task.getTask();

    private ThreadInfo threadInfo = ThreadInfo.getThreadInfo();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    @RequestMapping("/getBVInfo")
    //查询视频信息
    public Map<String, Object> getBVInfo(String bvid) {
        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        Object urlContent_get = httpClientDemo.getUrlContent_Get(url);
        int watchThreadNum = threadInfo.getWatchThreadNum();
        int likeThreadNum = threadInfo.getLikeThreadNum();
        int followThreadNum = threadInfo.getFollowThreadNum();
        Map<String, Object> map = new HashMap<>();
        map.put("bvinfo", urlContent_get);
        map.put("watchThreadNum", watchThreadNum);
        map.put("likeThreadNum", likeThreadNum);
        map.put("followThreadNum", followThreadNum);
        return map;
    }

    @RequestMapping("/getUserInfo")
    //查询用户信息
    public Object getUserInfo(String mid) {
        String url = "https://api.bilibili.com/x/web-interface/card?mid=" + mid;
        Object urlContent_get = httpClientDemo.getUrlContent_Get(url);
        int watchThreadNum = threadInfo.getWatchThreadNum();
        int likeThreadNum = threadInfo.getLikeThreadNum();
        int followThreadNum = threadInfo.getFollowThreadNum();
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", urlContent_get);
        map.put("watchThreadNum", watchThreadNum);
        map.put("likeThreadNum", likeThreadNum);
        map.put("followThreadNum", followThreadNum);
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
    public Map<String, Object> startWatch(BVInfo bvInfo) {
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
            if (bvInfo.getId() == null) {
                System.out.println("id为空");
                bvInfo.setId(bvInfo.getBvid() + "_" + bvInfo.getStartTimeStamp());
            }
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

    @RequestMapping("/getWatchTask")
    public Object getWatchTask(String id) {
        return task.getWatchTask(id);
    }

    @RequestMapping("/getWatchBVInfoList")
    public Object getWatchBVInfoList() {
        return task.getWatchBVInfo();
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

    @RequestMapping("/getLikeTask")
    public Object getLikeTask(String id) {
        return task.getLikeTask(id);
    }

    @RequestMapping("/getLikeBVInfoList")
    public Object getLikeBVInfoList() {
        return task.getLikeBVInfo();
    }


    @RequestMapping("/startFollow")
    public Object startFollow(UserInfo userInfo) {
        Integer followTask = task.getFollowTask(userInfo.getMid());
        if (followTask != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 1);
            map.put("message", "已经存在一个相同任务");
            map.put("userInfos", getFollowUserInfoList());
            return map;
        } else {
            userInfo.setThreadNum(1);
            Long startTimeStamp = new Date().getTime();
            String startTimeStr = simpleDateFormat.format(startTimeStamp);
            userInfo.setStartTimeStamp(startTimeStamp);
            userInfo.setStartTimeStr(startTimeStr);
            userInfo.setId(userInfo.getMid() + "_" + userInfo.getStartTimeStamp());
            userInfo.setRequestNum(0);
            userInfo.setSuccessNum(0);
            if (threadInfo.getFollowThreadNum() - userInfo.getThreadNum() >= 0) {
                userInfo.setStatus("运行");
                System.out.println(userInfo);
                task.addFollowUserInfo(userInfo);
                threadInfo.subFollowThreadNum(userInfo.getThreadNum());
                task.setFollowTask(userInfo.getMid(), userInfo.getThreadNum());
                logger.info("start Follow");
                //调用service层的任务
                for (int i = 0; i < userInfo.getThreadNum(); i++) {
                    asyncService.executeAsyncFollow(userInfo);
                }
                logger.info("end Follow");
                Map<String, Object> map = new HashMap<>();
                map.put("code", 0);
                map.put("message", "提交关注任务成功");
                map.put("userInfos", getFollowUserInfoList());
                return map;
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("code", 2);
                map.put("message", "空闲线程数不足");
                map.put("userInfos", getFollowUserInfoList());
                return map;
            }
        }
    }

    @RequestMapping("/stopFollow")
    public Object stopFollow(String id, String mid) {
        logger.info("stop Follow");
        task.releaseFollowTask(mid, 1000);
        task.updateFollowUserInfo(id, "停止");
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "停止成功");
        map.put("userInfos", getFollowUserInfoList());
        return map;
    }

    @RequestMapping("/removeFollow")
    public Object removeFollow(String id, String mid) {
        logger.info("remove Follow");
        Integer followTask = task.getFollowTask(mid);
        Map<String, Object> map = new HashMap<>();
        map.put("code", "1");
        map.put("message", "移除失败");
        map.put("userInfos", getFollowUserInfoList());
        if (followTask == null) {
            for (int i = 0; i < task.getFollowUserInfos().size(); i++) {
                if (id.equals(task.getFollowUserInfos().get(i).getId())) {
                    task.getFollowUserInfos().remove(task.getFollowUserInfos().get(i));
                    map.put("code", "0");
                    map.put("message", "移除成功");
                    map.put("userInfos", getFollowUserInfoList());
                    break;
                }
            }
        }
        return map;
    }

    @RequestMapping("/getFollowTask")
    public Object getFollowTask(String mid) {
        return task.getFollowTask(mid);
    }

    @RequestMapping("/getFollowUserInfoList")
    public Object getFollowUserInfoList() {
        return task.getFollowUserInfos();
    }


    @RequestMapping("/getThreadPoolInfo")
    public Object getThreadPoolInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("WatchThreadNum", threadInfo.getWatchThreadNum());
        info.put("LikeThreadNum", threadInfo.getLikeThreadNum());
        info.put("FollowThreadNum", threadInfo.getFollowThreadNum());

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


//    @PostConstruct
    //继续状态为进行中的订单
    //避免意外停止 状态丢失
    public void continueJXZWatchOrder() {
        System.err.println("获取进行中播放订单: " + LocalDateTime.now());
        String url = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=601&state=jxz&format=json&apikey=h8M6KeYvfvnvaw3g";
        String watchOrder_json = httpClientDemo.getUrlContent_Get_JSON(url);
        List<Order> orders = JSON.parseArray(JSON.parseObject(watchOrder_json).getString("rows"), Order.class);
        if (orders != null) {
            for (Order order : orders) {
                String BV = order.getAa();
                String id = order.getId();
                String startNum = order.getStart_num();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询视频信息，检查BV是否正确，获取当前播放量
                Map<String, Object> map = getBVInfo(BV);
                JSONObject bvinfoJSONObject = (JSONObject) map.get("bvinfo");
                Integer code = (Integer) bvinfoJSONObject.get("code");
                //视频BV号正确
                if (code == 0) {
                    BVInfo bvInfo = new BVInfo();
                    JSONObject data = (JSONObject) bvinfoJSONObject.get("data");
                    String bvid = data.getString("bvid");
                    String title = data.getString("title");
                    JSONObject owner = data.getJSONObject("owner");
                    String name = owner.getString("name");
                    JSONObject stat = data.getJSONObject("stat");
                    Integer view = stat.getInteger("view");

                    bvInfo.setId(id);
                    bvInfo.setBvid(bvid);
                    bvInfo.setTitle(title);
                    bvInfo.setAuthor(name);
                    bvInfo.setNowWatchNum(view);
                    bvInfo.setStartWatchNum(Integer.valueOf(startNum));
                    bvInfo.setNeedWatchNum(needNum);
                    bvInfo.setTaskType("播放");

                    Map<String, Object> mapStartWatch = startWatch(bvInfo);
                    Integer codeWatch = (Integer) mapStartWatch.get("code");
                    if (codeWatch == 0) {
                        Boolean status = orderService.orderSetJXZ("601", bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else {
                        System.out.println("线程数不足或已存在相同BV号视频");
                        Boolean status = orderService.orderSetWKS("601", bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《未开始》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《未开始》状态失败");
                        }
                    }

                } else {
                    //视频BV号不正确，进行退单
                    System.out.println("设置订单退单");
                    Boolean status = orderService.orderReturn(id);
                    if (status) {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        } else {
            System.out.println("暂无进行中订单");
        }
    }
}

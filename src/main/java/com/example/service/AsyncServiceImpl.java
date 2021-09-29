package com.example.service;

import com.alibaba.fastjson.JSONObject;
import com.example.config.ExecutorConfig;
import com.example.config.Task;
import com.example.config.ThreadInfo;
import com.example.entity.BVInfo;
import com.example.entity.UserInfo;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Service
public class AsyncServiceImpl implements AsyncService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);
    private Task task = Task.getTask();
    private ThreadInfo threadInfo = ThreadInfo.getThreadInfo();
    private Random random = new Random();

    @Autowired
    private HttpClientDemo httpClientDemo;
    @Autowired
    private OrderService orderService;

    @Override
    @Async("asyncServiceWatch")
    public void executeAsyncWatch(BVInfo bvInfo) {
        logger.info("start executeAsync watch");
        Integer nowView = bvInfo.getNowWatchNum();
        Integer tmpView;
        while (true) {
            Map<String, Integer> bvViewAndLike = getBVViewAndLike(bvInfo.getBvid());
            //tmp副本，避免nowView为空
            tmpView = nowView;
            nowView = bvViewAndLike.get("view");
            logger.info("<" + bvInfo.getBvid() + ">当前播放数量：" + nowView);
            if (nowView != null) {
                if (bvInfo.getNowWatchNum() < nowView) {
                    //更新当前播放数
                    upDateView(bvInfo.getId(), nowView);
                    if (bvInfo.getId().length() < 12) {
                        Boolean status = orderService.updateOrder("watch", "jxz", bvInfo.getId(), bvInfo.getStartWatchNum(), nowView);
                        if (status) {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面当前播放数成功");
                        } else {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面当前播放数失败");
                        }
                    }
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
                if (urlContent_post != null) {
                    logger.info("<" + bvInfo.getBvid() + ">播放: " + JSONObject.toJSONString(urlContent_post));
                    int randomNum = this.random.nextInt(4);
                    randomNum = randomNum + 2;
                    logger.info("<" + bvInfo.getBvid() + ">休眠" + randomNum + "秒");
                    try {
                        Thread.sleep(randomNum * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.info("<" + bvInfo.getBvid() + ">播放失败");
                }
            } else {
                break;
            }
        }
        logger.info("end executeAsync watch");
        threadInfo.releaseWatchThreadNum();
        if (task.getWatchTask(bvInfo.getBvid()) != null) {
            task.releaseWatchTask(bvInfo.getBvid(), 1);
        }
        Map<String, Integer> bvViewAndLike = getBVViewAndLike(bvInfo.getBvid());
        Integer view;
        while (true) {
            view = bvViewAndLike.get("view");
            if (view != null) {
                break;
            }
        }
        //判断是否已经完成任务
        if ((bvInfo.getStartWatchNum() + bvInfo.getNeedWatchNum() <= view)) {
            //完成任务
            logger.info("<" + bvInfo.getBvid() + ">完成播放任务，当前数量：" + view);
            task.updateWatchBVInfo(bvInfo.getId(), "完成");
            if (bvInfo.getId().length() < 12) {
                Boolean status = orderService.updateOrder("watch", "ywc", bvInfo.getId(), bvInfo.getStartWatchNum(), view);
                if (status) {
                    logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已完成》状态成功");
                } else {
                    logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已完成》状态成功");
                }
            }
        } else if (task.getWatchTask(bvInfo.getBvid()) == null) {
            //强制停止
            logger.info("<" + bvInfo.getBvid() + ">强制停止，当前数量：" + view);
            task.updateWatchBVInfo(bvInfo.getId(), "停止");
            if (bvInfo.getId().length() < 12) {
                Boolean status = orderService.orderReturn("watch", bvInfo.getId());
                if (status) {
                    System.out.println("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已退单》状态成功");
                } else {
                    System.out.println("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已退单》状态失败");
                }
            }
        }
    }

    @Override
    @Async("asyncServiceLike")
    public void executeAsyncLike(BVInfo bvInfo) {
        logger.info("start executeAsync like");
        Integer nowLike = bvInfo.getNowLikeNum();
        Integer tmpLike;
        for (int i = 0; i < task.getAccounts().size(); i++) {
            Map<String, Integer> bvViewAndLike = getBVViewAndLike(bvInfo.getBvid());
            //tmp副本，避免nowLike为空
            tmpLike = nowLike;
            nowLike = bvViewAndLike.get("like");
            logger.info("<" + bvInfo.getBvid() + ">当前点赞数量：" + nowLike);
            if (nowLike != null) {
                if (bvInfo.getNowLikeNum() < nowLike) {
                    //更新当前点赞数
                    upDateLike(bvInfo.getId(), nowLike);
                    if (bvInfo.getId().length() < 12) {
                        Boolean status = orderService.updateOrder("like", "jxz", bvInfo.getId(), bvInfo.getStartLikeNum(), nowLike);
                        if (status) {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面当前点赞数成功");
                        } else {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面当前点赞数失败");
                        }
                    }
                }
            } else {
                nowLike = tmpLike;
            }
            if (task.getLikeTask(bvInfo.getBvid()) != null && (bvInfo.getStartLikeNum() + bvInfo.getNeedLikeNum() > nowLike)) {
                String targetUrl = "https://api.bilibili.com/x/web-interface/archive/like";
                StringEntity stringEntity = null;//param参数，可以为"key1=value1&key2=value2"的一串字符串
                try {
                    stringEntity = new StringEntity("bvid=" + bvInfo.getBvid() + "&like=1" + "&csrf=" + task.getAccounts().get(i).getBili_jct());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                stringEntity.setContentType("application/x-www-form-urlencoded");
                String c = "buvid2=" + task.getAccounts().get(i).getBuvid2() + ";";
                c = c + "buvid3=" + task.getAccounts().get(i).getBuvid3() + ";";
                c = c + "SESSDATA=" + task.getAccounts().get(i).getSessData() + ";";
                BasicHeader cookie = new BasicHeader("cookie", c);
                JSONObject urlContent_post = httpClientDemo.getUrlContent_Post2(targetUrl, stringEntity, cookie);
                if (urlContent_post != null) {
                    logger.info("<" + bvInfo.getBvid() + ">点赞: " + task.getAccounts().get(i).getDedeUserID() + ": " + JSONObject.toJSONString(urlContent_post));
                    Integer code = urlContent_post.getInteger("code");
                    bvInfo.setRequestNum(bvInfo.getRequestNum() + 1);
                    boolean upSuccess = false;
                    if (code == 0) {
                        bvInfo.setSuccessNum(bvInfo.getSuccessNum() + 1);
                        upSuccess = true;
                    }
                    upDataRequestNum(bvInfo.getId(), bvInfo.getRequestNum(), bvInfo.getSuccessNum());
                    task.upAccountLikeRequestAndSuccess(task.getAccounts().get(i).getDedeUserID(), upSuccess);
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.info("<" + bvInfo.getBvid() + ">点赞失败");
                }
            } else {
                break;
            }
        }
        logger.info("end executeAsync like");
        threadInfo.releaseLikeThreadNum();
        if (task.getLikeTask(bvInfo.getBvid()) != null) {
            task.releaseLikeTask(bvInfo.getBvid(), 1);
        }
        Integer like = 0;
        while (true) {
            Map<String, Integer> bvViewAndLike = getBVViewAndLike(bvInfo.getBvid());
            like = bvViewAndLike.get("like");
            if (like != null) {
                upDateLike(bvInfo.getId(), like);
                if (bvInfo.getId().length() < 12) {
                    Boolean status = orderService.updateOrder("like", "jxz", bvInfo.getId(), bvInfo.getStartLikeNum(), like);
                    if (status) {
                        logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面当前点赞数成功");
                    } else {
                        logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面当前点赞数失败");
                    }
                }
                break;
            }
        }
        //判断是否已经完成任务
        if ((bvInfo.getStartLikeNum() + bvInfo.getNeedLikeNum() <= like)) {
            //完成任务
            logger.info("<" + bvInfo.getBvid() + ">完成点赞任务，当前数量：" + like);
            task.updateLikeBVInfo(bvInfo.getId(), "完成");
            if (bvInfo.getId().length() < 12) {
                Boolean status = orderService.updateOrder("like", "ywc", bvInfo.getId(), bvInfo.getStartLikeNum(), like);
                if (status) {
                    logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已完成》状态成功");
                } else {
                    logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已完成》状态成功");
                }
            }
        } else if (task.getLikeTask(bvInfo.getBvid()) == null) {
            //强制停止
            logger.info("<" + bvInfo.getBvid() + ">强制停止，当前数量：" + like);
            task.updateLikeBVInfo(bvInfo.getId(), "停止");
            if (bvInfo.getId().length() < 12) {
                Boolean status = orderService.orderReturn("like", bvInfo.getId());
                if (status) {
                    System.out.println("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已退单》状态成功");
                } else {
                    System.out.println("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《已退单》状态失败");
                }
            }
        }
    }


    @Override
    @Async("asyncServiceFollow")
    public void executeAsyncFollow(UserInfo userInfo) {
        logger.info("start executeAsync follow");
        Integer nowFollow = userInfo.getStartFollowNum();
        Integer tmpFollow;

        for (int i = 0; i < task.getAccounts().size(); i++) {
            Map<String, Integer> userFans = getUserFans(userInfo.getMid());
            //tmp副本，避免nowView为空
            tmpFollow = nowFollow;
            nowFollow = userFans.get("fans");
            logger.info("<" + userInfo.getMid() + ">当前关注数量：" + nowFollow);
            if (nowFollow != null) {
                if (userInfo.getNowFollowNum() < nowFollow) {
                    //更新当前关注数
                    upDateFans(userInfo.getId(), nowFollow);
                    if (userInfo.getId().length() < 12) {
                        Boolean status = orderService.updateOrder("follow", "jxz", userInfo.getId(), userInfo.getStartFollowNum(), nowFollow);
                        if (status) {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面当前关注数成功");
                        } else {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面当前关注数失败");
                        }
                    }
                }
            } else {
                nowFollow = tmpFollow;
            }
            if (task.getFollowTask(userInfo.getMid()) != null && (userInfo.getStartFollowNum() + userInfo.getNeedFollowNum() > nowFollow)) {
                String targetUrl = "https://api.bilibili.com/x/relation/modify";
                StringEntity stringEntity = null;//param参数，可以为"key1=value1&key2=value2"的一串字符串
                String str = "fid=" + userInfo.getMid();
                str = str + "&act=" + "1";
                str = str + "&re_src=" + "11";
                str = str + "&jsonp=" + "jsonp";
                str = str + "&csrf=" + task.getAccounts().get(i).getBili_jct();
                try {
                    stringEntity = new StringEntity(str);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                stringEntity.setContentType("application/x-www-form-urlencoded");
                String c = "buvid2=" + task.getAccounts().get(i).getBuvid2() + ";";
                c = c + "buvid3=" + task.getAccounts().get(i).getBuvid3() + ";";
                c = c + "SESSDATA=" + task.getAccounts().get(i).getSessData() + ";";
                BasicHeader cookie = new BasicHeader("cookie", c);
                JSONObject urlContent_post = httpClientDemo.getUrlContent_Post2(targetUrl, stringEntity, cookie);
                if (urlContent_post != null) {
                    logger.info("<" + userInfo.getMid() + ">关注: " + task.getAccounts().get(i).getDedeUserID() + ": " + JSONObject.toJSONString(urlContent_post));
                    Integer code = urlContent_post.getInteger("code");
                    userInfo.setRequestNum(userInfo.getRequestNum() + 1);
                    boolean upSuccess = false;
                    if (code == 0) {
                        userInfo.setSuccessNum(userInfo.getSuccessNum() + 1);
                        upSuccess = true;
                    }
                    task.upAccountFollowRequestAndSuccess(task.getAccounts().get(i).getDedeUserID(), upSuccess);
                    upDataRequestNumFollow(userInfo.getId(), userInfo.getRequestNum(), userInfo.getSuccessNum());
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.info("<" + userInfo.getMid() + ">关注失败");
                }
            } else {
                break;
            }
        }
        logger.info("end executeAsync follow");
        threadInfo.releaseFollowThreadNum();
        if (task.getFollowTask(userInfo.getMid()) != null) {
            task.releaseFollowTask(userInfo.getMid(), 1);
        }
        Integer fans = 0;
        while (true) {
            Map<String, Integer> userFans = getUserFans(userInfo.getMid());
            fans = userFans.get("fans");
            if (fans != null) {
                upDateFans(userInfo.getId(), fans);
                if (userInfo.getId().length() < 12) {
                    Boolean status = orderService.updateOrder("follow", "jxz", userInfo.getId(), userInfo.getStartFollowNum(), fans);
                    if (status) {
                        logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面当前关注数成功");
                    } else {
                        logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面当前关注数失败");
                    }
                }
                break;
            }
        }
        //判断是否已经完成任务
        if ((userInfo.getStartFollowNum() + userInfo.getNeedFollowNum() <= fans)) {
            //完成任务
            logger.info("<" + userInfo.getMid() + ">完成关注任务，当前数量：" + fans);
            task.updateFollowUserInfo(userInfo.getId(), "完成");
            if (userInfo.getId().length() < 12) {
                Boolean status = orderService.updateOrder("follow", "ywc", userInfo.getId(), userInfo.getStartFollowNum(), fans);
                if (status) {
                    logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《已完成》状态成功");
                } else {
                    logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《已完成》状态成功");
                }
            }
        } else if (task.getFollowTask(userInfo.getMid()) == null) {
            //强制停止
            logger.info("<" + userInfo.getMid() + ">强制停止，当前数量：" + fans);
            task.updateFollowUserInfo(userInfo.getId(), "停止");
            if (userInfo.getId().length() < 12) {
                Boolean status = orderService.orderReturn("follow", userInfo.getId());
                if (status) {
                    System.out.println("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《已退单》状态成功");
                } else {
                    System.out.println("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《已退单》状态失败");
                }
            }
        }
    }


    @Override
    public Map<String, Integer> getBVViewAndLike(String bvid) {
        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        while (urlContent_get == null) {
            urlContent_get = httpClientDemo.getUrlContent_Get(url);
        }
        Map<String, Integer> map = new HashMap<>();
        try {
            JSONObject data = (JSONObject) urlContent_get.get("data");
            JSONObject stat = (JSONObject) data.get("stat");
            Integer view = stat.getInteger("view");
            Integer like = stat.getInteger("like");
            map.put("view", view);
            map.put("like", like);
            return map;
        } catch (Exception e) {
//            e.printStackTrace();
            return map;
        }
    }

    @Override
    public Map<String, Integer> getUserFans(String mid) {
        String url = "https://api.bilibili.com/x/web-interface/card?mid=" + mid;
        JSONObject urlContent_get = httpClientDemo.getUrlContent_Get(url);
        while (urlContent_get == null) {
            urlContent_get = httpClientDemo.getUrlContent_Get(url);
        }
        Map<String, Integer> map = new HashMap<>();
        try {
            JSONObject data = (JSONObject) urlContent_get.get("data");
            JSONObject card = (JSONObject) data.get("card");
            Integer fans = card.getInteger("fans");
            map.put("fans", fans);
            return map;
        } catch (Exception e) {
//            e.printStackTrace();
            return map;
        }
    }

    synchronized void upDateView(String id, Integer view) {
        for (int i = 0; i < task.getWatchBVInfo().size(); i++) {
            if (task.getWatchBVInfo().get(i).getId().equals(id)) {
                task.getWatchBVInfo().get(i).setNowWatchNum(view);
                break;
            }
        }
    }

    synchronized void upDateLike(String id, Integer like) {
        for (int i = 0; i < task.getLikeBVInfo().size(); i++) {
            if (task.getLikeBVInfo().get(i).getId().equals(id)) {
                task.getLikeBVInfo().get(i).setNowLikeNum(like);
                break;
            }
        }
    }

    synchronized void upDateFans(String id, Integer fans) {
        for (int i = 0; i < task.getFollowUserInfos().size(); i++) {
            if (task.getFollowUserInfos().get(i).getId().equals(id)) {
                task.getFollowUserInfos().get(i).setNowFollowNum(fans);
                break;
            }
        }
    }

    synchronized void upDataRequestNum(String id, Integer requestNum, Integer successNum) {
        for (int i = 0; i < task.getLikeBVInfo().size(); i++) {
            if (task.getLikeBVInfo().get(i).getId().equals(id)) {
                task.getLikeBVInfo().get(i).setRequestNum(requestNum);
                task.getLikeBVInfo().get(i).setSuccessNum(successNum);
                break;
            }
        }
    }

    synchronized void upDataRequestNumFollow(String id, Integer requestNum, Integer successNum) {
        for (int i = 0; i < task.getFollowUserInfos().size(); i++) {
            if (task.getFollowUserInfos().get(i).getId().equals(id)) {
                task.getFollowUserInfos().get(i).setRequestNum(requestNum);
                task.getFollowUserInfos().get(i).setSuccessNum(successNum);
                break;
            }
        }
    }
}

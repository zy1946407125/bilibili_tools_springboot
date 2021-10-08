package com.example.timingTask;

import com.alibaba.fastjson.JSONObject;
import com.example.config.ExecutorConfig;
import com.example.config.Task;
import com.example.controller.UserController;
import com.example.entity.BVInfo;
import com.example.entity.Order;
import com.example.entity.UserInfo;
import com.example.service.HttpClientDemo;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.*;

@Configuration
@EnableScheduling
public class TimingTask {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserController userController;

    private Task task = Task.getTask();

    private static final Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);

    @Autowired
    private HttpClientDemo httpClientDemo;

    @Scheduled(fixedDelay = 10000)
    public void a() {
        httpClientDemo.updateProxyHostPort();
        String urlContent_get_json = httpClientDemo.getUrlContent_Get_JSON("http://pv.sohu.com/cityjson");
        logger.info(urlContent_get_json);
        logger.info("更新代理成功\n\n\n\n\n\n\n\n\n\n");
    }

    @Scheduled(fixedDelay = 60000)
    void getWatch() {
        logger.info("获取进行中播放订单: " + LocalDateTime.now());
        JSONObject watchJXZOrder_JSONObject = orderService.getOrder("watch", "jxz", false);
        while (watchJXZOrder_JSONObject == null) {
            logger.info("获取进行中播放订单出错,休眠五秒重新获取");
            watchJXZOrder_JSONObject = orderService.getOrder("watch", "jxz", false);
        }
        if (watchJXZOrder_JSONObject.getInteger("total") == 0) {
            logger.info("暂无进行中播放订单");
        } else {
            List<Order> ordersJXZ = JSONObject.parseArray(watchJXZOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersJXZ);
            logger.info(ordersJXZ.size() + "条进行中播放订单");
            for (Order order : ordersJXZ) {
                String BV = order.getAa();
                String id = order.getId();
                String startNum = order.getStart_num();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询视频信息，检查BV是否正确，获取当前播放量
                Map<String, Object> map = userController.getBVInfo(BV);
                JSONObject bvinfoJSONObject = (JSONObject) map.get("bvinfo");
                Integer code = null;
                try {
                    code = (Integer) bvinfoJSONObject.get("code");
                } catch (Exception e) {
                    logger.info(BV + "播放：获取视频信息出错");
                }
                if (code == null) {
                    continue;
                }
                //视频BV号正确
                else if (code == 0) {
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
                    if (startNum.equals("0")) {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  第一次进行播放任务");
                        bvInfo.setStartWatchNum(view);
                    } else {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  非首次进行播放任务");
                        bvInfo.setStartWatchNum(Integer.valueOf(startNum));
                    }
                    bvInfo.setNeedWatchNum(needNum);
                    bvInfo.setTaskType("播放");

                    Map<String, Object> mapStartWatch = userController.startWatch(bvInfo);
                    Integer codeWatch = (Integer) mapStartWatch.get("code");
                    if (codeWatch == 0) {
                        Boolean status = orderService.updateOrder("watch", "jxz", bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum());
                        if (status) {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeWatch == 1) {
                        boolean b = task.watchReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            logger.info("播放订单：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn("watch", bvInfo.getId());
                            if (status) {
                                logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeWatch == 2) {
                        logger.info("播放订单：线程数不足");
                        Boolean status = orderService.updateOrder("watch", "wks", bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum());
                        if (status) {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  线程数不足，由进行中重置为未开始成功");
                        } else {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  线程数不足，由进行中重置为未开始失败");
                        }
                        break;
                    }
                } else {
                    //视频BV号不正确，进行退单
                    logger.info("code:" + code + " 播放：进行中，BV号不正确，设置订单退单");
                    Boolean status = orderService.orderReturn("watch", id);
                    if (status) {
                        logger.info("播放订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("播放订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        }


        System.err.println("获取未开始播放订单: " + LocalDateTime.now());
        JSONObject watchWKSOrder_JSONObject = orderService.getOrder("watch", "wks", false);
        while (watchWKSOrder_JSONObject == null) {
            logger.info("获取未开始播放订单出错,休眠五秒重新获取");
            watchWKSOrder_JSONObject = orderService.getOrder("watch", "wks", false);
        }
        logger.info("获取未开始播放订单成功");
        if (watchWKSOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无未开始播放订单");
        } else {
            List<Order> ordersWKS = JSONObject.parseArray(watchWKSOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersWKS);
            logger.info(ordersWKS.size() + "条未开始播放订单");
            for (Order order : ordersWKS) {
                String BV = order.getAa();
                String id = order.getId();
                String startNum = order.getStart_num();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询视频信息，检查BV是否正确，获取当前播放量
                Map<String, Object> map = userController.getBVInfo(BV);
                JSONObject bvinfoJSONObject = (JSONObject) map.get("bvinfo");
                Integer code = null;
                try {
                    code = (Integer) bvinfoJSONObject.get("code");
                } catch (Exception e) {
                    logger.info(BV + "播放：获取视频信息出错");
                }
                if (code == null) {
                    continue;
                }
                //视频BV号正确
                else if (code == 0) {
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
                    if (startNum.equals("0")) {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  第一次进行播放任务");
                        bvInfo.setStartWatchNum(view);
                    } else {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  非首次进行播放任务");
                        bvInfo.setStartWatchNum(Integer.valueOf(startNum));
                    }
                    bvInfo.setNeedWatchNum(needNum);
                    bvInfo.setTaskType("播放");

                    Map<String, Object> mapStartWatch = userController.startWatch(bvInfo);
                    Integer codeWatch = (Integer) mapStartWatch.get("code");
                    if (codeWatch == 0) {
                        Boolean status = orderService.updateOrder("watch", "jxz", bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum());
                        if (status) {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeWatch == 1) {
                        boolean b = task.watchReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            logger.info("播放订单：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn("watch", bvInfo.getId());
                            if (status) {
                                logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                logger.info("播放订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeWatch == 2) {
                        logger.info("播放订单：线程数不足，不做任何操作");
                        break;
                    }

                } else {
                    //视频BV号不正确，进行退单
                    logger.info("code:" + code + "播放：未进行，BV号不正确，设置订单退单");
                    Boolean status = orderService.orderReturn("watch", id);
                    if (status) {
                        logger.info("播放订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("播放订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        }
    }


    @Scheduled(fixedDelay = 60000)
    void getLike() {
        System.err.println("获取进行中点赞订单: " + LocalDateTime.now());
        JSONObject likeJXZOrder_JSONObject = orderService.getOrder("like", "jxz", false);
        while (likeJXZOrder_JSONObject == null) {
            logger.info("获取进行中点赞订单出错,休眠五秒重新获取");
            likeJXZOrder_JSONObject = orderService.getOrder("like", "jxz", false);
        }
        logger.info("获取进行中点赞订单成功");
        if (likeJXZOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无进行中点赞订单");
        } else {
            List<Order> ordersJXZ = JSONObject.parseArray(likeJXZOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersJXZ);
            logger.info(ordersJXZ.size() + "条进行中点赞订单");
            for (Order order : ordersJXZ) {
                String BV = order.getAa();
                String id = order.getId();
                String startNum = order.getStart_num();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询视频信息，检查BV是否正确，获取当前点赞量
                Map<String, Object> map = userController.getBVInfo(BV);
                JSONObject bvinfoJSONObject = (JSONObject) map.get("bvinfo");
                Integer code = null;
                try {
                    code = (Integer) bvinfoJSONObject.get("code");
                } catch (Exception e) {
                    logger.info(BV + "点赞：获取视频信息出错");
                }
                if (code == null) {
                    continue;
                }
                //视频BV号正确
                else if (code == 0) {
                    BVInfo bvInfo = new BVInfo();
                    JSONObject data = (JSONObject) bvinfoJSONObject.get("data");
                    String bvid = data.getString("bvid");
                    String title = data.getString("title");
                    JSONObject owner = data.getJSONObject("owner");
                    String name = owner.getString("name");
                    JSONObject stat = data.getJSONObject("stat");
                    Integer like = stat.getInteger("like");

                    bvInfo.setId(id);
                    bvInfo.setBvid(bvid);
                    bvInfo.setTitle(title);
                    bvInfo.setAuthor(name);
                    bvInfo.setNowLikeNum(like);
                    if (startNum.equals("0")) {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  第一次进行点赞任务");
                        bvInfo.setStartLikeNum(like);
                    } else {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  非首次进行点赞任务");
                        bvInfo.setStartLikeNum(Integer.valueOf(startNum));
                    }
                    bvInfo.setNeedLikeNum(needNum);
                    bvInfo.setTaskType("点赞");

                    Map<String, Object> mapStartLike = userController.startLike(bvInfo);
                    Integer codeLike = (Integer) mapStartLike.get("code");
                    if (codeLike == 0) {
                        Boolean status = orderService.updateOrder("like", "jxz", bvInfo.getId(), bvInfo.getStartLikeNum(), bvInfo.getNowLikeNum());
                        if (status) {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeLike == 1) {
                        boolean b = task.likeReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            logger.info("点赞订单：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn("like", bvInfo.getId());
                            if (status) {
                                logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeLike == 2) {
                        logger.info("点赞：线程数不足");
                        Boolean status = orderService.updateOrder("like", "wks", bvInfo.getId(), bvInfo.getStartLikeNum(), bvInfo.getNowLikeNum());
                        if (status) {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  线程数不足，由进行中重置为未开始成功");
                        } else {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  线程数不足，由进行中重置为未开始失败");
                        }
                        break;
                    }
                } else {
                    //视频BV号不正确，进行退单
                    logger.info("code:" + code + " 点赞 进行中：BV号不正确，设置订单退单");
                    Boolean status = orderService.orderReturn("like", id);
                    if (status) {
                        logger.info("点赞订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("点赞订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        }


        System.err.println("获取未开始点赞订单: " + LocalDateTime.now());
        JSONObject likeWKSOrder_JSONObject = orderService.getOrder("like", "wks", false);
        while (likeWKSOrder_JSONObject == null) {
            logger.info("获取未开始点赞订单出错,休眠五秒重新获取");
            likeWKSOrder_JSONObject = orderService.getOrder("like", "wks", false);
        }
        logger.info("获取未开始点赞订单成功");
        if (likeWKSOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无未开始点赞订单");
        } else {
            List<Order> ordersWKS = JSONObject.parseArray(likeWKSOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersWKS);
            logger.info(ordersWKS.size() + "条未开始点赞订单");
            for (Order order : ordersWKS) {
                String BV = order.getAa();
                String id = order.getId();
                String startNum = order.getStart_num();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询视频信息，检查BV是否正确，获取当前点赞量
                Map<String, Object> map = userController.getBVInfo(BV);
                JSONObject bvinfoJSONObject = (JSONObject) map.get("bvinfo");
                Integer code = null;
                try {
                    code = (Integer) bvinfoJSONObject.get("code");
                } catch (Exception e) {
                    logger.info(BV + "点赞：获取视频信息出错");
                }
                if (code == null) {
                    continue;
                }
                //视频BV号正确
                else if (code == 0) {
                    BVInfo bvInfo = new BVInfo();
                    JSONObject data = (JSONObject) bvinfoJSONObject.get("data");
                    String bvid = data.getString("bvid");
                    String title = data.getString("title");
                    JSONObject owner = data.getJSONObject("owner");
                    String name = owner.getString("name");
                    JSONObject stat = data.getJSONObject("stat");
                    Integer like = stat.getInteger("like");

                    bvInfo.setId(id);
                    bvInfo.setBvid(bvid);
                    bvInfo.setTitle(title);
                    bvInfo.setAuthor(name);
                    bvInfo.setNowLikeNum(like);
                    if (startNum.equals("0")) {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  第一次进行点赞任务");
                        bvInfo.setStartLikeNum(like);
                    } else {
                        logger.info(bvInfo.getId() + "  " + bvInfo.getBvid() + "  非首次进行点赞任务");
                        bvInfo.setStartLikeNum(Integer.valueOf(startNum));
                    }
                    bvInfo.setNeedLikeNum(needNum);
                    bvInfo.setTaskType("点赞");

                    Map<String, Object> mapStartLike = userController.startLike(bvInfo);
                    Integer codeLike = (Integer) mapStartLike.get("code");
                    if (codeLike == 0) {
                        Boolean status = orderService.updateOrder("like", "jxz", bvInfo.getId(), bvInfo.getStartLikeNum(), bvInfo.getNowLikeNum());
                        if (status) {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeLike == 1) {
                        boolean b = task.likeReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            logger.info("点赞订单：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn("like", bvInfo.getId());
                            if (status) {
                                logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                logger.info("点赞订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeLike == 2) {
                        logger.info("点赞订单：线程数不足，不做任何操作");
                        break;
                    }
                } else {
                    //视频BV号不正确，进行退单
                    logger.info("code:" + code + " 点赞 未开始：BV号不正确，设置订单退单");
                    Boolean status = orderService.orderReturn("like", id);
                    if (status) {
                        logger.info("点赞订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("点赞订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        }
    }


    @Scheduled(fixedDelay = 60000)
    void getFollow() {
        System.err.println("获取进行中关注订单: " + LocalDateTime.now());
        JSONObject followJXZOrder_JSONObject = orderService.getOrder("follow", "jxz", false);
        while (followJXZOrder_JSONObject == null) {
            logger.info("获取进行中关注订单出错,休眠五秒重新获取");
            followJXZOrder_JSONObject = orderService.getOrder("follow", "jxz", false);
        }
        logger.info("获取进行中关注订单成功");
        if (followJXZOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无进行中关注订单");
        } else {
            List<Order> ordersJXZ = JSONObject.parseArray(followJXZOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersJXZ);
            logger.info(ordersJXZ.size() + "条进行中关注订单");
            for (Order order : ordersJXZ) {
                String mid = order.getAa();
                String id = order.getId();
                String startNum = order.getStart_num();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询用户信息，检查mid是否正确，获取当前关注数
                Map<String, Object> map = userController.getUserInfo(mid);
                JSONObject userInfoJSONObject = (JSONObject) map.get("userInfo");
                Integer code = null;
                try {
                    code = (Integer) userInfoJSONObject.get("code");
                } catch (Exception e) {
                    logger.info(mid + "关注：获取用户信息出错");
                }
                if (code == null) {
                    continue;
                }
                //用户mid号正确
                else if (code == 0) {
                    UserInfo userInfo = new UserInfo();
                    JSONObject data = userInfoJSONObject.getJSONObject("data");
                    JSONObject card = data.getJSONObject("card");
                    String name = card.getString("name");
                    Integer fans = card.getInteger("fans");

                    userInfo.setId(id);
                    userInfo.setMid(mid);
                    userInfo.setName(name);
                    userInfo.setNowFollowNum(fans);
                    userInfo.setNeedFollowNum(needNum);

                    if (startNum.equals("0")) {
                        logger.info(userInfo.getId() + "  " + userInfo.getMid() + "  第一次进行关注任务");
                        userInfo.setStartFollowNum(fans);
                    } else {
                        logger.info(userInfo.getId() + "  " + userInfo.getMid() + "  非首次进行关注任务");
                        userInfo.setStartFollowNum(Integer.valueOf(startNum));
                    }

                    Map<String, Object> mapStartFollow = userController.startFollow(userInfo);
                    Integer codeFollow = (Integer) mapStartFollow.get("code");
                    if (codeFollow == 0) {
                        Boolean status = orderService.updateOrder("follow", "jxz", userInfo.getId(), userInfo.getStartFollowNum(), userInfo.getNowFollowNum());
                        if (status) {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeFollow == 1) {
                        boolean b = task.followReturnFlag(userInfo.getId(), userInfo.getMid());
                        if (!b) {
                            logger.info("关注：已存在相同mid号用户");
                            Boolean status = orderService.orderReturn("follow", userInfo.getId());
                            if (status) {
                                logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单成功");
                            } else {
                                logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单失败");
                            }
                        }
                    } else if (codeFollow == 2) {
                        logger.info("关注：线程数不足");
                        Boolean status = orderService.updateOrder("follow", "wks", userInfo.getId(), userInfo.getStartFollowNum(), userInfo.getNowFollowNum());
                        if (status) {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  线程数不足，由进行中重置为未开始成功");
                        } else {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  线程数不足，由进行中重置为未开始失败");
                        }
                        break;
                    }
                } else {
                    //用户mid号不正确，进行退单
                    logger.info("code:" + code + " 关注 进行中：用户mid不正确，设置订单退单");
                    Boolean status = orderService.orderReturn("follow", id);
                    if (status) {
                        logger.info("关注订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("关注订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        }


        System.err.println("获取未开始关注订单: " + LocalDateTime.now());
        JSONObject followWKSOrder_JSONObject = orderService.getOrder("follow", "wks", false);
        while (followWKSOrder_JSONObject == null) {
            logger.info("获取未开始关注订单出错,休眠五秒重新获取");
            followWKSOrder_JSONObject = orderService.getOrder("follow", "wks", false);
        }
        logger.info("获取未开始关注订单成功");
        if (followWKSOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无未开始关注订单");
        } else {
            List<Order> ordersWKS = JSONObject.parseArray(followWKSOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersWKS);
            logger.info(ordersWKS.size() + "条未开始关注订单");
            for (Order order : ordersWKS) {
                String mid = order.getAa();
                String id = order.getId();
                String startNum = order.getStart_num();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询用户信息，检查mid是否正确，获取当前关注数
                Map<String, Object> map = userController.getUserInfo(mid);
                JSONObject userInfoJSONObject = (JSONObject) map.get("userInfo");
                Integer code = null;
                try {
                    code = (Integer) userInfoJSONObject.get("code");
                } catch (Exception e) {
                    logger.info(mid + "关注：获取用户信息出错");
                }
                if (code == null) {
                    continue;
                }
                //用户mid号正确
                else if (code == 0) {
                    UserInfo userInfo = new UserInfo();
                    JSONObject data = userInfoJSONObject.getJSONObject("data");
                    JSONObject card = data.getJSONObject("card");
                    String name = card.getString("name");
                    Integer fans = card.getInteger("fans");

                    userInfo.setId(id);
                    userInfo.setMid(mid);
                    userInfo.setName(name);
                    userInfo.setNowFollowNum(fans);
                    userInfo.setNeedFollowNum(needNum);

                    if (startNum.equals("0")) {
                        logger.info(userInfo.getId() + "  " + userInfo.getMid() + "  第一次进行关注任务");
                        userInfo.setStartFollowNum(fans);
                    } else {
                        logger.info(userInfo.getId() + "  " + userInfo.getMid() + "  非首次进行关注任务");
                        userInfo.setStartFollowNum(Integer.valueOf(startNum));
                    }


                    Map<String, Object> mapStartFollow = userController.startFollow(userInfo);
                    Integer codeFollow = (Integer) mapStartFollow.get("code");
                    if (codeFollow == 0) {
                        Boolean status = orderService.updateOrder("follow", "jxz", userInfo.getId(), userInfo.getStartFollowNum(), userInfo.getNowFollowNum());
                        if (status) {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeFollow == 1) {
                        boolean b = task.followReturnFlag(userInfo.getId(), userInfo.getMid());
                        if (!b) {
                            logger.info("关注订单：已存在相同mid号用户");
                            Boolean status = orderService.orderReturn("follow", userInfo.getId());
                            if (status) {
                                logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单成功");
                            } else {
                                logger.info("关注订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单失败");
                            }
                        }
                    } else if (codeFollow == 2) {
                        logger.info("关注订单：线程数不足,不做任何操作");
                        break;
                    }

                } else {
                    //用户mid号不正确，进行退单
                    logger.info("code:" + code + "关注 未开始：用户mid不正确，设置订单退单");
                    Boolean status = orderService.orderReturn("follow", id);
                    if (status) {
                        logger.info("关注订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("关注订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        }
    }


    @Scheduled(fixedDelay = 60000)
    public void handleWatchReturn() {
        System.err.println("获取退单中播放订单: " + LocalDateTime.now());
        JSONObject watchTDZOrder_JSONObject = orderService.getOrder("watch", "tdz", false);
        while (watchTDZOrder_JSONObject == null) {
            logger.info("获取退单中播放订单出错,休眠五秒重新获取");
            watchTDZOrder_JSONObject = orderService.getOrder("watch", "tdz", false);
        }
        logger.info("获取退单中播放订单成功");
        if (watchTDZOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无退单中播放订单");
        } else {
            List<Order> ordersTDZ = JSONObject.parseArray(watchTDZOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersTDZ);
            logger.info(ordersTDZ.size() + "条退单中播放订单");
            for (Order order : ordersTDZ) {
                String bvid = order.getAa();
                String id = order.getId();
                Integer watchTask = task.getWatchTask(bvid);
                if (watchTask == null) {
                    //订单未开始或已完成，直接进行退单
                    logger.info("播放订单：用户申请退单   设置订单退单");
                    Boolean status = orderService.orderReturn("watch", id);
                    if (status) {
                        logger.info("播放订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("播放订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                } else {
                    userController.stopWatch(id, bvid);
                }
            }
        }
    }


    @Scheduled(fixedDelay = 60000)
    public void handleLikeReturn() {
        System.err.println("获取退单中点赞订单: " + LocalDateTime.now());
        JSONObject likeTDZOrder_JSONObject = orderService.getOrder("like", "tdz", false);
        while (likeTDZOrder_JSONObject == null) {
            logger.info("获取退单中点赞订单出错,休眠五秒重新获取");
            likeTDZOrder_JSONObject = orderService.getOrder("like", "tdz", false);
        }
        logger.info("获取退单中点赞订单成功");
        if (likeTDZOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无退单中点赞订单");
        } else {
            List<Order> ordersTDZ = JSONObject.parseArray(likeTDZOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersTDZ);
            logger.info(ordersTDZ.size() + "条退单中点赞订单");
            for (Order order : ordersTDZ) {
                String bvid = order.getAa();
                String id = order.getId();
                Integer likeTask = task.getLikeTask(bvid);
                if (likeTask == null) {
                    //订单未开始或已完成，直接进行退单
                    logger.info("点赞订单：用户申请退单   设置订单退单");
                    Boolean status = orderService.orderReturn("like", id);
                    if (status) {
                        logger.info("点赞订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("点赞订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                } else {
                    userController.stopLike(id, bvid);
                }
            }
        }
    }


    @Scheduled(fixedDelay = 60000)
    public void handleFollowReturn() {
        System.err.println("获取退单中关注订单: " + LocalDateTime.now());
        JSONObject followTDZOrder_JSONObject = orderService.getOrder("follow", "tdz", false);
        while (followTDZOrder_JSONObject == null) {
            logger.info("获取退单中关注订单出错,休眠五秒重新获取");
            followTDZOrder_JSONObject = orderService.getOrder("follow", "tdz", false);
        }
        logger.info("获取退单中关注订单成功");
        if (followTDZOrder_JSONObject.getInteger("total") == 0) {
            System.err.println("暂无退单中关注订单");
        } else {
            List<Order> ordersTDZ = JSONObject.parseArray(followTDZOrder_JSONObject.getJSONArray("rows").toJSONString(), Order.class);
            Collections.reverse(ordersTDZ);
            logger.info(ordersTDZ.size() + "条退单中关注订单");
            for (Order order : ordersTDZ) {
                String mid = order.getAa();
                String id = order.getId();
                Integer followTask = task.getFollowTask(mid);
                if (followTask == null) {
                    //订单未开始或已完成，直接进行退单
                    logger.info("关注订单：用户申请退单   设置订单退单");
                    Boolean status = orderService.orderReturn("follow", id);
                    if (status) {
                        logger.info("关注订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        logger.info("关注订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                } else {
                    userController.stopFollow(id, mid);
                }
            }
        }
    }
}

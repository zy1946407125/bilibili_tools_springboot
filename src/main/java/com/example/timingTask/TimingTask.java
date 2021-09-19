package com.example.timingTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.config.Task;
import com.example.controller.UserController;
import com.example.entity.BVInfo;
import com.example.entity.Order;
import com.example.entity.UserInfo;
import com.example.service.AsyncService;
import com.example.service.HttpClientDemo;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Configuration
@EnableScheduling
public class TimingTask {

    @Autowired
    private HttpClientDemo httpClientDemo;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private UserController userController;

    private Task task = Task.getTask();

    @Scheduled(fixedRate = 120000)
    private void getWatch() {
        System.err.println("获取进行中播放订单: " + LocalDateTime.now());
        String urlJXZ = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=" + task.getGoodsIDAndKey("watch").getGoodsId() + "&state=jxz&format=json&apikey=" + task.getGoodsIDAndKey("watch").getApikey();
        String watchJXZOrder_json = httpClientDemo.getUrlContent_Get_JSON(urlJXZ);
        List<Order> ordersJXZ = null;
        try {
            ordersJXZ = JSON.parseArray(JSON.parseObject(watchJXZOrder_json).getString("rows"), Order.class);
        } catch (Exception e) {
            System.out.println("获取进行中播放订单出错");
        }
        if (ordersJXZ != null) {
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
                    System.out.println("播放：获取视频信息出错");
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
                        System.out.println(bvInfo.getId() + "  " + bvInfo.getBvid() + "  第一次进行播放任务");
                        bvInfo.setStartWatchNum(view);
                    } else {
                        System.out.println(bvInfo.getId() + "  " + bvInfo.getBvid() + "  非首次进行播放任务");
                        bvInfo.setStartWatchNum(Integer.valueOf(startNum));
                    }
                    bvInfo.setNeedWatchNum(needNum);
                    bvInfo.setTaskType("播放");

                    Map<String, Object> mapStartWatch = userController.startWatch(bvInfo);
                    Integer codeWatch = (Integer) mapStartWatch.get("code");
                    if (codeWatch == 0) {
                        Boolean status = orderService.orderSetJXZ(task.getGoodsIDAndKey("watch").getGoodsId(), bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum(), task.getGoodsIDAndKey("watch").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeWatch == 1) {
                        boolean b = task.watchReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            System.out.println("播放：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("watch").getApikey());
                            if (status) {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeWatch == 2) {
                        System.out.println("播放：线程数不足");
                        Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("watch").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                        }
                    }

                } else {
                    //视频BV号不正确，进行退单
                    System.out.println("播放：设置订单退单");
                    Boolean status = orderService.orderReturn(id, task.getGoodsIDAndKey("watch").getApikey());
                    if (status) {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        } else {
            System.out.println("暂无进行中播放订单");
        }


        System.err.println("获取未开始播放订单: " + LocalDateTime.now());
        String urlWKS = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=" + task.getGoodsIDAndKey("watch").getGoodsId() + "&state=wks&format=json&apikey=" + task.getGoodsIDAndKey("watch").getApikey();
        String watchWKSOrder_json = httpClientDemo.getUrlContent_Get_JSON(urlWKS);
        List<Order> ordersWKS = null;
        try {
            ordersWKS = JSON.parseArray(JSON.parseObject(watchWKSOrder_json).getString("rows"), Order.class);
        } catch (Exception e) {
            System.out.println("获取未开始播放订单出错");
        }
        if (ordersWKS != null) {
            for (Order order : ordersWKS) {
                String BV = order.getAa();
                String id = order.getId();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询视频信息，检查BV是否正确，获取当前播放量
                Map<String, Object> map = userController.getBVInfo(BV);
                JSONObject bvinfoJSONObject = (JSONObject) map.get("bvinfo");
                Integer code = null;
                try {
                    code = (Integer) bvinfoJSONObject.get("code");
                } catch (Exception e) {
                    System.out.println("播放：获取视频信息出错");
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
                    bvInfo.setStartWatchNum(view);
                    bvInfo.setNeedWatchNum(needNum);
                    bvInfo.setTaskType("播放");

                    Map<String, Object> mapStartWatch = userController.startWatch(bvInfo);
                    Integer codeWatch = (Integer) mapStartWatch.get("code");
                    if (codeWatch == 0) {
                        Boolean status = orderService.orderSetJXZ(task.getGoodsIDAndKey("watch").getGoodsId(), bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum(), task.getGoodsIDAndKey("watch").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeWatch == 1) {
                        boolean b = task.watchReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            System.out.println("播放：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("watch").getApikey());
                            if (status) {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeWatch == 2) {
                        System.out.println("播放：线程数不足");
                        Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("watch").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                        }
                    }

                } else {
                    //视频BV号不正确，进行退单
                    System.out.println("播放：设置订单退单");
                    Boolean status = orderService.orderReturn(id, task.getGoodsIDAndKey("watch").getApikey());
                    if (status) {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        } else {
            System.out.println("暂无未开始播放订单");
        }
    }


    @Scheduled(fixedRate = 120000)
    private void getLike() {
        System.err.println("获取进行中点赞订单: " + LocalDateTime.now());
        String urlJXZ = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=" + task.getGoodsIDAndKey("like").getGoodsId() + "&state=jxz&format=json&apikey=" + task.getGoodsIDAndKey("like").getApikey();
        String likeJXZOrder_json = httpClientDemo.getUrlContent_Get_JSON(urlJXZ);
        List<Order> ordersJXZ = null;
        try {
            ordersJXZ = JSON.parseArray(JSON.parseObject(likeJXZOrder_json).getString("rows"), Order.class);
        } catch (Exception e) {
            System.out.println("获取进行中点赞订单出错");
        }
        if (ordersJXZ != null) {
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
                    System.out.println("点赞：获取视频信息出错");
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
                        System.out.println(bvInfo.getId() + "  " + bvInfo.getBvid() + "  第一次进行点赞任务");
                        bvInfo.setStartLikeNum(like);
                    } else {
                        System.out.println(bvInfo.getId() + "  " + bvInfo.getBvid() + "  非首次进行播放任务");
                        bvInfo.setStartLikeNum(Integer.valueOf(startNum));
                    }
                    bvInfo.setNeedLikeNum(needNum);
                    bvInfo.setTaskType("点赞");

                    Map<String, Object> mapStartLike = userController.startLike(bvInfo);
                    Integer codeLike = (Integer) mapStartLike.get("code");
                    if (codeLike == 0) {
                        Boolean status = orderService.orderSetJXZ(task.getGoodsIDAndKey("like").getGoodsId(), bvInfo.getId(), bvInfo.getStartLikeNum(), bvInfo.getNowLikeNum(), task.getGoodsIDAndKey("like").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeLike == 1) {
                        boolean b = task.likeReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            System.out.println("点赞：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("like").getApikey());
                            if (status) {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeLike == 2) {
                        System.out.println("点赞：线程数不足");
                        Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("like").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                        }
                    }

                } else {
                    //视频BV号不正确，进行退单
                    System.out.println("点赞：设置订单退单");
                    Boolean status = orderService.orderReturn(id, task.getGoodsIDAndKey("like").getApikey());
                    if (status) {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        } else {
            System.out.println("暂无进行中点赞订单");
        }


        System.err.println("获取未开始点赞订单: " + LocalDateTime.now());
        String urlWKS = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=" + task.getGoodsIDAndKey("like").getGoodsId() + "&state=wks&format=json&apikey=" + task.getGoodsIDAndKey("like").getApikey();
        String likeWKSOrder_json = httpClientDemo.getUrlContent_Get_JSON(urlWKS);
        List<Order> ordersWKS = null;
        try {
            ordersWKS = JSON.parseArray(JSON.parseObject(likeWKSOrder_json).getString("rows"), Order.class);
        } catch (Exception e) {
            System.out.println("获取未开始点赞订单出错");
        }
        if (ordersWKS != null) {
            for (Order order : ordersWKS) {
                String BV = order.getAa();
                String id = order.getId();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询视频信息，检查BV是否正确，获取当前点赞量
                Map<String, Object> map = userController.getBVInfo(BV);
                JSONObject bvinfoJSONObject = (JSONObject) map.get("bvinfo");
                Integer code = null;
                try {
                    code = (Integer) bvinfoJSONObject.get("code");
                } catch (Exception e) {
                    System.out.println("点赞：获取视频信息出错");
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
                    bvInfo.setStartLikeNum(like);
                    bvInfo.setNeedLikeNum(needNum);
                    bvInfo.setTaskType("点赞");

                    Map<String, Object> mapStartLike = userController.startLike(bvInfo);
                    Integer codeLike = (Integer) mapStartLike.get("code");
                    if (codeLike == 0) {
                        Boolean status = orderService.orderSetJXZ(task.getGoodsIDAndKey("like").getGoodsId(), bvInfo.getId(), bvInfo.getStartLikeNum(), bvInfo.getNowLikeNum(), task.getGoodsIDAndKey("like").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeLike == 1) {
                        boolean b = task.likeReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            System.out.println("点赞：已存在相同BV号视频");
                            Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("like").getApikey());
                            if (status) {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeLike == 2) {
                        System.out.println("点赞：线程数不足");
                        Boolean status = orderService.orderReturn(bvInfo.getId(), task.getGoodsIDAndKey("like").getApikey());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                        }
                    }

                } else {
                    //视频BV号不正确，进行退单
                    System.out.println("点赞：设置订单退单");
                    Boolean status = orderService.orderReturn(id, task.getGoodsIDAndKey("like").getApikey());
                    if (status) {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        } else {
            System.out.println("暂无未开始点赞订单");
        }
    }


    @Scheduled(fixedRate = 120000)
    private void getFollow() {
        System.err.println("获取进行中关注订单: " + LocalDateTime.now());
        String urlJXZ = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=" + task.getGoodsIDAndKey("follow").getGoodsId() + "&state=jxz&format=json&apikey=" + task.getGoodsIDAndKey("follow").getApikey();
        String likeJXZOrder_json = httpClientDemo.getUrlContent_Get_JSON(urlJXZ);
        List<Order> ordersJXZ = null;
        try {
            ordersJXZ = JSON.parseArray(JSON.parseObject(likeJXZOrder_json).getString("rows"), Order.class);
        } catch (Exception e) {
            System.out.println("获取进行中关注订单出错");
        }
        if (ordersJXZ != null) {
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
                    System.out.println("关注：获取用户信息出错");
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
                        System.out.println(userInfo.getId() + "  " + userInfo.getMid() + "  第一次进行关注任务");
                        userInfo.setStartFollowNum(fans);
                    } else {
                        System.out.println(userInfo.getId() + "  " + userInfo.getMid() + "  非首次进行关注任务");
                        userInfo.setStartFollowNum(Integer.valueOf(startNum));
                    }

                    Map<String, Object> mapStartFollow = userController.startFollow(userInfo);
                    Integer codeFollow = (Integer) mapStartFollow.get("code");
                    if (codeFollow == 0) {
                        Boolean status = orderService.orderSetJXZ(task.getGoodsIDAndKey("follow").getGoodsId(), userInfo.getId(), userInfo.getStartFollowNum(), userInfo.getNowFollowNum(), task.getGoodsIDAndKey("follow").getApikey());
                        if (status) {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeFollow == 1) {
                        boolean b = task.followReturnFlag(userInfo.getId(), userInfo.getMid());
                        if (!b) {
                            System.out.println("关注：已存在相同mid号用户");
                            Boolean status = orderService.orderReturn(userInfo.getId(), task.getGoodsIDAndKey("follow").getApikey());
                            if (status) {
                                System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单失败");
                            }
                        }
                    } else if (codeFollow == 2) {
                        System.out.println("关注：线程数不足");
                        Boolean status = orderService.orderReturn(userInfo.getId(), task.getGoodsIDAndKey("follow").getApikey());
                        if (status) {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单失败");
                        }
                    }

                } else {
                    //用户mid号不正确，进行退单
                    System.out.println("关注：设置订单退单");
                    Boolean status = orderService.orderReturn(id, task.getGoodsIDAndKey("follow").getApikey());
                    if (status) {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        } else {
            System.out.println("暂无进行中关注订单");
        }


        System.err.println("获取未开始关注订单: " + LocalDateTime.now());
        String urlWKS = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=" + task.getGoodsIDAndKey("follow").getGoodsId() + "&state=wks&format=json&apikey=" + task.getGoodsIDAndKey("follow").getApikey();
        String followWKSOrder_json = httpClientDemo.getUrlContent_Get_JSON(urlWKS);
        List<Order> ordersWKS = null;
        try {
            ordersWKS = JSON.parseArray(JSON.parseObject(followWKSOrder_json).getString("rows"), Order.class);
        } catch (Exception e) {
            System.out.println("获取未开始关注订单出错");
        }
        if (ordersWKS != null) {
            for (Order order : ordersWKS) {
                String mid = order.getAa();
                String id = order.getId();
                Integer needNum = Integer.valueOf(order.getNeed_num_0());
                //查询用户信息，检查mid是否正确，获取当前关注数
                Map<String, Object> map = userController.getUserInfo(mid);
                JSONObject userInfoJSONObject = (JSONObject) map.get("userInfo");
                Integer code = null;
                try {
                    code = (Integer) userInfoJSONObject.get("code");
                } catch (Exception e) {
                    System.out.println("关注：获取用户信息出错");
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
                    userInfo.setStartFollowNum(fans);
                    userInfo.setNeedFollowNum(needNum);

                    Map<String, Object> mapStartFollow = userController.startFollow(userInfo);
                    Integer codeFollow = (Integer) mapStartFollow.get("code");
                    if (codeFollow == 0) {
                        Boolean status = orderService.orderSetJXZ(task.getGoodsIDAndKey("follow").getGoodsId(), userInfo.getId(), userInfo.getStartFollowNum(), userInfo.getNowFollowNum(), task.getGoodsIDAndKey("follow").getApikey());
                        if (status) {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeFollow == 1) {
                        boolean b = task.followReturnFlag(userInfo.getId(), userInfo.getMid());
                        if (!b) {
                            System.out.println("关注：已存在相同mid号用户");
                            Boolean status = orderService.orderReturn(userInfo.getId(), task.getGoodsIDAndKey("follow").getApikey());
                            if (status) {
                                System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单失败");
                            }
                        }
                    } else if (codeFollow == 2) {
                        System.out.println("点赞：线程数不足");
                        Boolean status = orderService.orderReturn(userInfo.getId(), task.getGoodsIDAndKey("follow").getApikey());
                        if (status) {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + userInfo.getId() + "   mid: " + userInfo.getMid() + "  退单失败");
                        }
                    }

                } else {
                    //用户mid号不正确，进行退单
                    System.out.println("关注：设置订单退单");
                    Boolean status = orderService.orderReturn(id, task.getGoodsIDAndKey("follow").getApikey());
                    if (status) {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态成功");
                    } else {
                        System.out.println("订单: " + id + "  更新商品页面《已退单》状态失败");
                    }
                }
            }
        } else {
            System.out.println("暂无未开始关注订单");
        }
    }
}

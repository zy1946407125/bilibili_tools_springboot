package com.example.timingTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.config.Task;
import com.example.controller.UserController;
import com.example.entity.BVInfo;
import com.example.entity.Order;
import com.example.service.AsyncService;
import com.example.service.HttpClientDemo;
import com.example.service.OrderService;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.HashMap;
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

    @Scheduled(fixedRate = 60000)
    private void getWatch() {
        System.err.println("获取进行中播放订单: " + LocalDateTime.now());
        String urlJXZ = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=601&state=jxz&format=json&apikey=h8M6KeYvfvnvaw3g";
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
                    System.out.println("获取视频信息出错");
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
                        Boolean status = orderService.orderSetJXZ("601", bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeWatch == 1) {
                        boolean b = task.watchReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            System.out.println("已存在相同BV号视频");
                            Boolean status = orderService.orderReturn(bvInfo.getId());
                            if (status) {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeWatch == 2) {
                        System.out.println("线程数不足");
                        Boolean status = orderService.orderReturn(bvInfo.getId());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
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


        System.err.println("获取未开始播放订单: " + LocalDateTime.now());
        String urlWKS = "http://120.79.197.162/admin_jiuwuxiaohun.php?m=home&c=api&a=down_orders&goods_id=601&state=wks&format=json&apikey=h8M6KeYvfvnvaw3g";
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
                    System.out.println("获取视频信息出错");
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
                        Boolean status = orderService.orderSetJXZ("601", bvInfo.getId(), bvInfo.getStartWatchNum(), bvInfo.getNowWatchNum());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  更新商品页面《进行中》状态失败");
                        }
                    } else if (codeWatch == 1) {
                        boolean b = task.watchReturnFlag(bvInfo.getId(), bvInfo.getBvid());
                        if (!b) {
                            System.out.println("已存在相同BV号视频");
                            Boolean status = orderService.orderReturn(bvInfo.getId());
                            if (status) {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                            } else {
                                System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
                            }
                        }
                    } else if (codeWatch == 2) {
                        System.out.println("线程数不足");
                        Boolean status = orderService.orderReturn(bvInfo.getId());
                        if (status) {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单成功");
                        } else {
                            System.out.println("订单: " + bvInfo.getId() + "   BV: " + bvInfo.getBvid() + "  退单失败");
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
            System.out.println("暂无未开始订单");
        }
    }


//    @Scheduled(fixedRate=3000)
//    private void getLike(){
//        System.err.println("获取点赞订单: " + LocalDateTime.now());
//    }
//
//    @Scheduled(fixedRate=2000)
//    private void getFollow(){
//        System.err.println("获取关注订单: " + LocalDateTime.now());
//    }
}

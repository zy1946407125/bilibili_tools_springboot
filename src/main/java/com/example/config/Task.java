package com.example.config;

import com.example.entity.BVInfo;

import java.text.SimpleDateFormat;
import java.util.*;

public class Task {
    private static Task task = new Task();
    private Map<String, Integer> watchTask = new HashMap<String, Integer>();
    private Map<String, Integer> likeTask = new HashMap<String, Integer>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    private List<BVInfo> bvInfos = new ArrayList<>();

    private Task() {
    }

    public static Task getTask() {
        return task;
    }

    public synchronized void setWatchTask(String bvid, Integer num) {
        this.watchTask.put(bvid, num);
    }

    public synchronized Integer getWatchTask(String bvid) {
        return this.watchTask.get(bvid);
    }

    public synchronized void setLikeTask(String bvid, Integer num) {
        this.likeTask.put(bvid, num);
    }

    public synchronized Integer getLikeTask(String bvid) {
        return this.likeTask.get(bvid);
    }

    public synchronized void releaseWatchTask(String bvid, int num) {
        this.watchTask.put(bvid, this.watchTask.get(bvid) - num);
        if (this.watchTask.get(bvid) <= 0) {
            this.watchTask.remove(bvid);
        }
    }

    public synchronized void releaseLikeTask(String bvid) {
        int tmp = this.likeTask.get(bvid) - 1;
        this.likeTask.put(bvid, (tmp >= 0 ? tmp : 0));
    }


    public synchronized void addBVInfo(BVInfo bvInfo) {
        this.bvInfos.add(bvInfo);
    }

    public synchronized List<BVInfo> getBVInfo() {
        return this.bvInfos;
    }

    public synchronized void updateBVInfo(String id,String status) {
        for (int i = 0; i < bvInfos.size(); i++) {
            if (bvInfos.get(i).getId().equals(id)) {
                Long endTimeStamp = new Date().getTime();
                String endTimeStr = simpleDateFormat.format(endTimeStamp);
                bvInfos.get(i).setEndTimeStamp(endTimeStamp);
                bvInfos.get(i).setEndTimeStr(endTimeStr);
                bvInfos.get(i).setStatus(status);
                break;
            }
        }
    }

}

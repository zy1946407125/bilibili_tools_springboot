package com.example.entity;


public class BVInfo {
    private String id;
    private String bvid;
    private String title;
    private String author;
    private Integer startWatchNum;
    private Integer startLikeNum;
    private Integer nowWatchNum;
    private Integer nowLikeNum;
    private String startTimeStr;
    private Long startTimeStamp;
    private String endTimeStr;
    private Long endTimeStamp;
    private Integer threadNum;
    private String taskType;
    private String status;
    private Integer needWatchNum;
    private Integer needLikeNum;
    private Integer requestNum;
    private Integer successNum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBvid() {
        return bvid;
    }

    public void setBvid(String bvid) {
        this.bvid = bvid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getStartWatchNum() {
        return startWatchNum;
    }

    public void setStartWatchNum(Integer startWatchNum) {
        this.startWatchNum = startWatchNum;
    }

    public Integer getStartLikeNum() {
        return startLikeNum;
    }

    public void setStartLikeNum(Integer startLikeNum) {
        this.startLikeNum = startLikeNum;
    }

    public Integer getNowWatchNum() {
        return nowWatchNum;
    }

    public void setNowWatchNum(Integer nowWatchNum) {
        this.nowWatchNum = nowWatchNum;
    }

    public Integer getNowLikeNum() {
        return nowLikeNum;
    }

    public void setNowLikeNum(Integer nowLikeNum) {
        this.nowLikeNum = nowLikeNum;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
    }

    public Long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(Long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNeedWatchNum() {
        return needWatchNum;
    }

    public void setNeedWatchNum(Integer needWatchNum) {
        this.needWatchNum = needWatchNum;
    }

    public Integer getNeedLikeNum() {
        return needLikeNum;
    }

    public void setNeedLikeNum(Integer needLikeNum) {
        this.needLikeNum = needLikeNum;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }

    public Long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(Long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public Integer getRequestNum() {
        return requestNum;
    }

    public void setRequestNum(Integer requestNum) {
        this.requestNum = requestNum;
    }

    public Integer getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(Integer successNum) {
        this.successNum = successNum;
    }


    @Override
    public String toString() {
        return "BVInfo{" +
                "id='" + id + '\'' +
                ", bvid='" + bvid + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", startWatchNum=" + startWatchNum +
                ", startLikeNum=" + startLikeNum +
                ", nowWatchNum=" + nowWatchNum +
                ", nowLikeNum=" + nowLikeNum +
                ", startTimeStr='" + startTimeStr + '\'' +
                ", startTimeStamp=" + startTimeStamp +
                ", endTimeStr='" + endTimeStr + '\'' +
                ", endTimeStamp=" + endTimeStamp +
                ", threadNum=" + threadNum +
                ", taskType='" + taskType + '\'' +
                ", status='" + status + '\'' +
                ", needWatchNum=" + needWatchNum +
                ", needLikeNum=" + needLikeNum +
                ", requestNum=" + requestNum +
                ", successNum=" + successNum +
                '}';
    }
}

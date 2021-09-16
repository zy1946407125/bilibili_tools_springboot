package com.example.entity;

public class UserInfo {

    private String id;
    private String mid;
    private String name;
    private Integer startFollowNum;
    private Integer nowFollowNum;
    private String startTimeStr;
    private Long startTimeStamp;
    private String endTimeStr;
    private Long endTimeStamp;
    private Integer threadNum;
    private String taskType;
    private String status;
    private Integer needFollowNum;
    private Integer requestNum;
    private Integer successNum;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStartFollowNum() {
        return startFollowNum;
    }

    public void setStartFollowNum(Integer startFollowNum) {
        this.startFollowNum = startFollowNum;
    }

    public Integer getNowFollowNum() {
        return nowFollowNum;
    }

    public void setNowFollowNum(Integer nowFollowNum) {
        this.nowFollowNum = nowFollowNum;
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

    public Integer getNeedFollowNum() {
        return needFollowNum;
    }

    public void setNeedFollowNum(Integer needFollowNum) {
        this.needFollowNum = needFollowNum;
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
        return "User{" +
                "id='" + id + '\'' +
                ", mid='" + mid + '\'' +
                ", name='" + name + '\'' +
                ", startFollowNum=" + startFollowNum +
                ", nowFollowNum=" + nowFollowNum +
                ", startTimeStr='" + startTimeStr + '\'' +
                ", startTimeStamp=" + startTimeStamp +
                ", endTimeStr='" + endTimeStr + '\'' +
                ", endTimeStamp=" + endTimeStamp +
                ", threadNum=" + threadNum +
                ", taskType='" + taskType + '\'' +
                ", status='" + status + '\'' +
                ", needFollowNum=" + needFollowNum +
                ", requestNum=" + requestNum +
                ", successNum=" + successNum +
                '}';
    }
}

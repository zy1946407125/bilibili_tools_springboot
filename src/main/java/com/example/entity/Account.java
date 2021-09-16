package com.example.entity;

public class Account {
    private String dedeUserID;
    private String buvid2;
    private String buvid3;
    private String bili_jct;
    private String sessData;
    private Integer likeRequestNum = 0;
    private Integer likeSuccessNum = 0;
    private Integer followRequestNum = 0;
    private Integer followSuccessNum = 0;

    public String getDedeUserID() {
        return dedeUserID;
    }

    public void setDedeUserID(String dedeUserID) {
        this.dedeUserID = dedeUserID;
    }

    public String getBuvid2() {
        return buvid2;
    }

    public void setBuvid2(String buvid2) {
        this.buvid2 = buvid2;
    }

    public String getBuvid3() {
        return buvid3;
    }

    public void setBuvid3(String buvid3) {
        this.buvid3 = buvid3;
    }

    public String getBili_jct() {
        return bili_jct;
    }

    public void setBili_jct(String bili_jct) {
        this.bili_jct = bili_jct;
    }

    public String getSessData() {
        return sessData;
    }

    public void setSessData(String sessData) {
        this.sessData = sessData;
    }

    public Integer getLikeRequestNum() {
        return likeRequestNum;
    }

    public void setLikeRequestNum(Integer likeRequestNum) {
        this.likeRequestNum = likeRequestNum;
    }

    public Integer getLikeSuccessNum() {
        return likeSuccessNum;
    }

    public void setLikeSuccessNum(Integer likeSuccessNum) {
        this.likeSuccessNum = likeSuccessNum;
    }

    public Integer getFollowRequestNum() {
        return followRequestNum;
    }

    public void setFollowRequestNum(Integer followRequestNum) {
        this.followRequestNum = followRequestNum;
    }

    public Integer getFollowSuccessNum() {
        return followSuccessNum;
    }

    public void setFollowSuccessNum(Integer followSuccessNum) {
        this.followSuccessNum = followSuccessNum;
    }

    @Override
    public String toString() {
        return "Account{" +
                "dedeUserID='" + dedeUserID + '\'' +
                ", buvid2='" + buvid2 + '\'' +
                ", buvid3='" + buvid3 + '\'' +
                ", bili_jct='" + bili_jct + '\'' +
                ", sessData='" + sessData + '\'' +
                ", likeRequestNum=" + likeRequestNum +
                ", likeSuccessNum=" + likeSuccessNum +
                ", followRequestNum=" + followRequestNum +
                ", followSuccessNum=" + followSuccessNum +
                '}';
    }
}

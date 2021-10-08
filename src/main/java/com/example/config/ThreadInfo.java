package com.example.config;

public class ThreadInfo {

    private static ThreadInfo threadInfo = new ThreadInfo();
    private int watchThreadNum = 500;
    private int likeThreadNum = 50;
    private int followThreadNum = 50;

    private ThreadInfo() {
    }

    public static ThreadInfo getThreadInfo() {
        return threadInfo;
    }


    public synchronized int getWatchThreadNum() {
        return watchThreadNum;
    }

    public synchronized int getLikeThreadNum() {
        return likeThreadNum;
    }

    public synchronized int getFollowThreadNum() {
        return followThreadNum;
    }


    public synchronized void releaseWatchThreadNum() {
        watchThreadNum += 1;
    }

    public synchronized void releaseLikeThreadNum() {
        likeThreadNum += 1;
    }

    public synchronized void releaseFollowThreadNum() {
        followThreadNum += 1;
    }


    public synchronized void subWatchThreadNum(int cnt) {
        watchThreadNum -= cnt;
    }

    public synchronized void subLikeThreadNum(int cnt) {
        likeThreadNum -= cnt;
    }

    public synchronized void subFollowThreadNum(int cnt) {
        followThreadNum -= cnt;
    }

}

package com.example.config;

import com.example.entity.Account;
import com.example.entity.BVInfo;

import java.text.SimpleDateFormat;
import java.util.*;

public class Task {
    private static Task task = new Task();
    private Map<String, Integer> watchTask = new HashMap<String, Integer>();
    private Map<String, Integer> likeTask = new HashMap<String, Integer>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    private List<BVInfo> watchBVInfos = new ArrayList<>();
    private List<BVInfo> likeBVInfos = new ArrayList<>();

    private List<Account> accounts = new ArrayList<>();

    private Task() {
        Account account1 = new Account();
        account1.setDedeUserID("1971869828");
        account1.setBuvid2("F2FF6966-3F7B-474E-BAFD-F00AABCAA61C167616infoc");
        account1.setBuvid3("F2FF6966-3F7B-474E-BAFD-F00AABCAA61C167616infoc");
        account1.setBili_jct("1982d8f7fadb10a2ae532edcc2c5589a");
        account1.setSessData("23be4337%2C1646835674%2C1f988%2A91");
        accounts.add(account1);

        Account account2 = new Account();
        account2.setDedeUserID("1081758632");
        account2.setBuvid2("A5C98A33-E9E2-4048-BD03-842F68F9DB86148822infoc");
        account2.setBuvid3("A5C98A33-E9E2-4048-BD03-842F68F9DB86148822infoc");
        account2.setBili_jct("ee47559073091d9f3810da664e71c6ff");
        account2.setSessData("6687671e%2C1646835788%2Ca6604%2A91");
        accounts.add(account2);

        Account account3 = new Account();
        account3.setDedeUserID("1182595620");
        account3.setBuvid2("591A6386-913F-4FCF-B629-4D3FB3D8E8E1167629infoc");
        account3.setBuvid3("591A6386-913F-4FCF-B629-4D3FB3D8E8E1167629infoc");
        account3.setBili_jct("6dccf677a340c68f3379b1ddc076cefd");
        account3.setSessData("dd6d5414%2C1646835922%2Cd2eaf%2A91");
        accounts.add(account3);

        Account account4 = new Account();
        account4.setDedeUserID("1427651875");
        account4.setBuvid2("A59EE0D1-606D-4D22-807E-FCFD51491847148812infoc");
        account4.setBuvid3("A59EE0D1-606D-4D22-807E-FCFD51491847148812infoc");
        account4.setBili_jct("406616cfe75d723eae51d5c536b0d084");
        account4.setSessData("a15b4c91%2C1646836029%2C96ef2%2A91");
        accounts.add(account4);

        Account account5 = new Account();
        account5.setDedeUserID("1653713985");
        account5.setBuvid2("E0A54730-F0CD-4ADC-A825-1482380519E0167645infoc");
        account5.setBuvid3("E0A54730-F0CD-4ADC-A825-1482380519E0167645infoc");
        account5.setBili_jct("058fbd36ef8c62e4b7f2370e152b99ae");
        account5.setSessData("7d809b6c%2C1646836149%2C59a15%2A91");
        accounts.add(account5);
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

    public synchronized void releaseWatchTask(String bvid, int num) {
        this.watchTask.put(bvid, this.watchTask.get(bvid) - num);
        if (this.watchTask.get(bvid) <= 0) {
            this.watchTask.remove(bvid);
        }
    }

    public synchronized void addWatchBVInfo(BVInfo bvInfo) {
        this.watchBVInfos.add(bvInfo);
    }

    public synchronized List<BVInfo> getWatchBVInfo() {
        return this.watchBVInfos;
    }

    public synchronized void updateWatchBVInfo(String id, String status) {
        for (int i = 0; i < watchBVInfos.size(); i++) {
            if (watchBVInfos.get(i).getId().equals(id)) {
                Long endTimeStamp = new Date().getTime();
                String endTimeStr = simpleDateFormat.format(endTimeStamp);
                watchBVInfos.get(i).setEndTimeStamp(endTimeStamp);
                watchBVInfos.get(i).setEndTimeStr(endTimeStr);
                watchBVInfos.get(i).setStatus(status);
                break;
            }
        }
    }


    public synchronized void setLikeTask(String bvid, Integer num) {
        this.likeTask.put(bvid, num);
    }

    public synchronized Integer getLikeTask(String bvid) {
        return this.likeTask.get(bvid);
    }

    public synchronized void releaseLikeTask(String bvid, int num) {
        this.likeTask.put(bvid, this.likeTask.get(bvid) - num);
        if (this.likeTask.get(bvid) <= 0) {
            this.likeTask.remove(bvid);
        }
    }

    public synchronized void addLikeBVInfo(BVInfo bvInfo) {
        this.likeBVInfos.add(bvInfo);
    }

    public synchronized List<BVInfo> getLikeBVInfo() {
        return this.likeBVInfos;
    }

    public synchronized void updateLikeBVInfo(String id, String status) {
        for (int i = 0; i < likeBVInfos.size(); i++) {
            if (likeBVInfos.get(i).getId().equals(id)) {
                Long endTimeStamp = new Date().getTime();
                String endTimeStr = simpleDateFormat.format(endTimeStamp);
                likeBVInfos.get(i).setEndTimeStamp(endTimeStamp);
                likeBVInfos.get(i).setEndTimeStr(endTimeStr);
                likeBVInfos.get(i).setStatus(status);
                break;
            }
        }
    }


    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public boolean removeAccount(String dedeUserID) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getDedeUserID().equals(dedeUserID)) {
                accounts.remove(accounts.get(i));
                return true;
            }
        }
        return false;
    }

}

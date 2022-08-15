package com.imooc.pojo.vo;

/**
 * @author 小亮
 **/
public class AppUserVO {

    private String id;
    private String nickname;
    private String face;
    private Integer activeStatus;

    private Integer myFollowCounts;
    private Integer myFansCounts;

    public void setId(String id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public void setActiveStatus(Integer activeStatus) {
        this.activeStatus = activeStatus;
    }

    public void setMyFollowCounts(Integer myFollowCounts) {
        this.myFollowCounts = myFollowCounts;
    }

    public void setMyFansCounts(Integer myFansCounts) {
        this.myFansCounts = myFansCounts;
    }

    public String getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFace() {
        return face;
    }

    public Integer getActiveStatus() {
        return activeStatus;
    }

    public Integer getMyFollowCounts() {
        return myFollowCounts;
    }

    public Integer getMyFansCounts() {
        return myFansCounts;
    }
}

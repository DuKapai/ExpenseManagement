package com.example.campusexpensemanager.Entity;

public class Noti {

    private int NotiId;
    private int userId;
    private String title;
    private String email;
    private String type;
    private String createDate;

    public Noti() {
    }

    public Noti(int NotiId, int userId, String title, String email, String type, String createDate) {
        this.NotiId = NotiId;
        this.userId = userId;
        this.title = title;
        this.email = email;
        this.type = type;
        this.createDate = createDate;
    }

    // Getter và Setter
    public int getNotiId() {
        return NotiId;
    }

    public void setNotiId(int NotiId) {
        this.NotiId = NotiId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    // Phương thức tiện ích
    @Override
    public String toString() {
        return "Noti{" +
                "NotiId=" + NotiId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", email='" + email + '\'' +
                ", type='" + type + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }
}

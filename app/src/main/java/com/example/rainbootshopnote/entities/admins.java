package com.example.rainbootshopnote.entities;

public class admins {

    private int admin_id;
    private String name;
    private String user_name;
    private String user_password;
    private Boolean status;
    private Boolean deleteStatus;

    public admins() {
    }

    public admins(int admin_id, String name, String user_name, String user_password, Boolean status, Boolean deleteStatus) {
        this.admin_id = admin_id;
        this.name = name;
        this.user_name = user_name;
        this.user_password = user_password;
        this.status = status;
        this.deleteStatus = deleteStatus;
    }

    public admins(String name, String user_name, String user_password, Boolean status, Boolean deleteStatus) {
        this.name = name;
        this.user_name = user_name;
        this.user_password = user_password;
        this.status = status;
        this.deleteStatus = deleteStatus;
    }

    public int getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(int admin_id) {
        this.admin_id = admin_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(Boolean deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    @Override
    public String toString() {
        return "admins{" +
                "admin_id=" + admin_id +
                ", name='" + name + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_password='" + user_password + '\'' +
                ", status=" + status +
                ", deleteStatus=" + deleteStatus +
                '}';
    }
}

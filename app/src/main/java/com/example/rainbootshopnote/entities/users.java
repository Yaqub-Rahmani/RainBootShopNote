package com.example.rainbootshopnote.entities;

public class users {
    private int user_id;
    private String name;
    private String mobile;
    private String address;
    private String create_date;
    private String update_date;
    private boolean status;
    private String description;
    private boolean deleteStatus;

    public users() {
    }

    public users(int user_id, String name, String mobile, String address, String create_date, String update_date, boolean status, String description, boolean deleteStatus) {
        this.user_id = user_id;
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.create_date = create_date;
        this.update_date = update_date;
        this.status = status;
        this.description = description;
        this.deleteStatus = deleteStatus;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(boolean deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    @Override
    public String toString() {
        return "sales{" +
                "user_id=" + user_id +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", address='" + address + '\'' +
                ", update_date='" + update_date + '\'' +
                ", create_date='" + create_date + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", deleteStatus=" + deleteStatus +
                '}';
    }
}

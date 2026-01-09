package com.example.rainbootshopnote.Dto;

public class UserDTO {
    private int user_id;
    private String name;
    private String mobile;
    private Float depositAmount;
    private Float withdrawalsAmount;

    public UserDTO() {
    }

    public UserDTO(int user_id, String name, String mobile, Float depositAmount, Float withdrawalsAmount) {
        this.user_id = user_id;
        this.name = name;
        this.mobile = mobile;
        this.depositAmount = depositAmount;
        this.withdrawalsAmount = withdrawalsAmount;
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

    public Float getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Float depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Float getWithdrawalsAmount() {
        return withdrawalsAmount;
    }

    public void setWithdrawalsAmount(Float withdrawalsAmount) {
        this.withdrawalsAmount = withdrawalsAmount;
    }
}

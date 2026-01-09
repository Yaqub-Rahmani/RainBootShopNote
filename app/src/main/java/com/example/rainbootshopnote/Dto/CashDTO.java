package com.example.rainbootshopnote.Dto;

public class CashDTO {
    private int itemId;
    private int userId;
    private String name;
    private String payUser;
    private float price;
    private boolean depositOrWithdrawal;
    private String createDate;
    private String description;
    private boolean status;

    public CashDTO() {
    }

    public CashDTO(int itemId, int userId, String name, String payUser, float price, boolean depositOrWithdrawal, String createDate, String description, boolean status) {
        this.itemId = itemId;
        this.userId = userId;
        this.name = name;
        this.payUser = payUser;
        this.price = price;
        this.depositOrWithdrawal = depositOrWithdrawal;
        this.createDate = createDate;
        this.description = description;
        this.status = status;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayUser() {
        return payUser;
    }

    public void setPayUser(String payUser) {
        this.payUser = payUser;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }


    public boolean isDepositOrWithdrawal() {
        return depositOrWithdrawal;
    }

    public void setDepositOrWithdrawal(boolean depositOrWithdrawal) {
        this.depositOrWithdrawal = depositOrWithdrawal;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}

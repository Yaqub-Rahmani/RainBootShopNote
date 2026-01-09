package com.example.rainbootshopnote.entities;

public class withdrawals {

    private int withdrawal_id;
    private String name;
    private float amount;
    private String create_date;
    private String update_date;
    private int user_id;
    private boolean status;
    private String description;
    private boolean deleteStatus;

    public withdrawals() {
    }

    public withdrawals(int withdrawal_id, String name, float amount, String create_date, String update_date, int user_id, boolean status, String description, boolean deleteStatus) {
        this.withdrawal_id = withdrawal_id;
        this.name = name;
        this.amount = amount;
        this.create_date = create_date;
        this.update_date = update_date;
        this.user_id = user_id;
        this.status = status;
        this.description = description;
        this.deleteStatus = deleteStatus;
    }

    public int getWithdrawal_id() {
        return withdrawal_id;
    }

    public void setWithdrawal_id(int withdrawal_id) {
        this.withdrawal_id = withdrawal_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
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

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
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
        return "withdrawals{" +
                "withdrawal_id=" + withdrawal_id +
                "user_id=" + user_id +
                "amount=" + amount +
                ", name='" + name + '\'' +
                ", update_date='" + update_date + '\'' +
                ", create_date='" + create_date + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", deleteStatus=" + deleteStatus +
                '}';
    }
}

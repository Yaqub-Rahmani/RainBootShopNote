package com.example.rainbootshopnote.entities;

public class deposits {

    private int deposit_id;
    private  String name;
    private  Float amount;
    private  String create_date;
    private  String update_date;
    private  Boolean status;
    private  String description;
    private  int user_id;
    private  boolean deleteStatus;

    public deposits() {
    }

    public deposits(int deposit_id, String name, Float amount, String create_date, String update_date, Boolean status, String description, int user_id, boolean deleteStatus) {
        this.deposit_id = deposit_id;
        this.name = name;
        this.amount = amount;
        this.create_date = create_date;
        this.update_date = update_date;
        this.status = status;
        this.description = description;
        this.user_id = user_id;
        this.deleteStatus = deleteStatus;
    }

    public int getDeposit_id() {
        return deposit_id;
    }

    public void setDeposit_id(int deposit_id) {
        this.deposit_id = deposit_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
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

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public boolean isDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(boolean deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    @Override
    public String toString() {
        return "Deposit{" +
                "deposit_id=" + deposit_id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", create_date='" + create_date + '\'' +
                ", update_date='" + update_date + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", user_id=" + user_id +
                ", deleteStatus=" + deleteStatus +
                '}';
    }

}

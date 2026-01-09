package com.example.rainbootshopnote.entities;

public class sales {
    private int sale_id;
    private String product_id;
    private float price;
    private float win;
    private int quantity;
    private String update_date;
    private String create_date;
    private String description;
    private boolean status;
    private boolean deleteStatus;

    public sales() {
    }

    public sales(int sale_id, String product_id, float price, float win, int quantity, String update_date, String create_date, String description, boolean status, boolean deleteStatus) {
        this.sale_id = sale_id;
        this.product_id = product_id;
        this.price = price;
        this.win = win;
        this.quantity = quantity;
        this.update_date = update_date;
        this.create_date = create_date;
        this.description = description;
        this.status = status;
        this.deleteStatus = deleteStatus;
    }

    public int getSale_id() {
        return sale_id;
    }

    public void setSale_id(int sale_id) {
        this.sale_id = sale_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getWin() {
        return win;
    }

    public void setWin(float win) {
        this.win = win;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
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

    public boolean isDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(boolean deleteStatus) {
        this.deleteStatus = deleteStatus;
    }


    @Override
    public String toString() {
        return "sales{" +
                "sale_id=" + sale_id +
                "product_id=" + product_id +
                "quantity=" + quantity +
                ", update_date='" + update_date + '\'' +
                ", create_date='" + create_date + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", deleteStatus=" + deleteStatus +
                '}';
    }

}

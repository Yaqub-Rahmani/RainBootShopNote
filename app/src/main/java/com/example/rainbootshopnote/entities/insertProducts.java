package com.example.rainbootshopnote.entities;

public class insertProducts {

    private int insert_id;
    private int product_id;
    private String create_date;
    private String update_date;
    private int quantity;
    private Float price;
    private String description;
    private boolean status;
    private boolean deleteStatus;


    public insertProducts() {
    }

    public insertProducts(int insert_id, int product_id, String create_date, String update_date, int quantity, Float price, String description, boolean status, boolean deleteStatus) {
        this.insert_id = insert_id;
        this.product_id = product_id;
        this.create_date = create_date;
        this.update_date = update_date;
        this.quantity = quantity;
        this.price = price;
        this.description = description;
        this.status = status;
        this.deleteStatus = deleteStatus;
    }

    public int getInsert_id() {
        return insert_id;
    }

    public void setInsert_id(int insert_id) {
        this.insert_id = insert_id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
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
        return "insertProducts{" +
                "insert_id=" + insert_id +
                "product_id=" + product_id +
                "quantity=" + quantity +
                "price=" + price +
                ", create_date='" + create_date + '\'' +
                ", update_date='" + update_date + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", deleteStatus=" + deleteStatus +
                '}';
    }
}

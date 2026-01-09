package com.example.rainbootshopnote.entities;

public class products {

    private int product_id;
    private String code;
    private String name;
    private float price;
    private int quantity;
    private boolean status;
    private String description;
    private boolean deleteStatus;

    public products() {
    }

    public products(int product_id, String code, String name, float price, int quantity, boolean status, String description, boolean deleteStatus) {
        this.product_id = product_id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.description = description;
        this.deleteStatus = deleteStatus;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        return "products{" +
                "product_id=" + product_id +
                "quantity=" + quantity +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", deleteStatus=" + deleteStatus +
                '}';
    }
}

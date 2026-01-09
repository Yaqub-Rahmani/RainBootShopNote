package com.example.rainbootshopnote.Dto;

public class ProductDTO {

    private int itemId;
    private int productId;
    private String name;
    private float price;
    private int quantity;
    private String createDate;
    private String description;
    private boolean status;
    private boolean saleOrInsert; // sale ise true değilse false
    private float win; //sale ise saleWin değilse 0

    public ProductDTO() {
    }

    public ProductDTO(int itemId, int productId, String name, float price, int quantity, String createDate, String description, boolean status, boolean saleOrInsert, float win) {
        this.itemId = itemId;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.createDate = createDate;
        this.description = description;
        this.status = status;
        this.saleOrInsert = saleOrInsert;
        this.win = win;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public boolean isSaleOrInsert() {
        return saleOrInsert;
    }

    public void setSaleOrInsert(boolean saleOrInsert) {
        this.saleOrInsert = saleOrInsert;
    }

    public float getWin() {
        return win;
    }

    public void setWin(float win) {
        this.win = win;
    }
}

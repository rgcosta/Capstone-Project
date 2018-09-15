package br.com.sociallinks.sociallinks.models;


public class Link {

    private int productId;  //same id used for the link index
    private String productName;
    private double productPrice;
    private String productPhotoUrl;
    private int commission;
    private int buyCounts;
    private String link;

    public Link(){

    }

    public Link (String link, int productId, String productName, double productPrice, String productPhotoUrl, int commission) {
        this.link = link;
        this.productId = productId;
        this.buyCounts = 0;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productPhotoUrl = productPhotoUrl;
        this.commission = commission;

    }

    public int getBuyCounts() {
        return buyCounts;
    }

    public int getProductId() {
        return productId;
    }

    public String getLink() {
        return link;
    }

    public String getProductName() {
        return productName;
    }

    public int getCommission() {
        return commission;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public String getProductPhotoUrl() {
        return productPhotoUrl;
    }

    public void setBuyCounts(int buyCounts) {
        this.buyCounts = buyCounts;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCommission(int commission) {
        this.commission = commission;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public void setProductPhotoUrl(String productPhotoUrl) {
        this.productPhotoUrl = productPhotoUrl;
    }
}

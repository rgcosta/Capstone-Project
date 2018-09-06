package br.com.sociallinks.sociallinks.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements Parcelable {

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("shortDesc")
    private String shortDesc;

    @JsonProperty("photoUrl")
    private String photoUrl;

    @JsonProperty("price")
    private double price;

    @JsonProperty("commission")
    private int commission;

    @JsonProperty("qtd")
    private int qtd;

    @JsonProperty("rating")
    private float rating;

    @JsonProperty("seller")
    private String seller;

    @JsonProperty("categoryName")
    private String categoryName;

    public Product(){
    }

    public Product(int id, String name, String description, String shortDesc, String photoUrl,
                   double price, int commission, int qtd, float rating, String seller, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.shortDesc = shortDesc;
        this.photoUrl = photoUrl;
        this.price = price;
        this.commission = commission;
        this.qtd = qtd;
        this.rating = rating;
        this.seller = seller;
        this.categoryName = categoryName;
    }

    protected Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        shortDesc = in.readString();
        photoUrl = in.readString();
        price = in.readDouble();
        commission = in.readInt();
        qtd = in.readInt();
        rating = in.readFloat();
        seller = in.readString();
        categoryName = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCommission(int commission) {
        this.commission = commission;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public double getPrice() {
        return price;
    }

    public int getCommission() {
        return commission;
    }

    public int getQtd() {
        return qtd;
    }

    public float getRating() {
        return rating;
    }

    public String getSeller() {
        return seller;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(shortDesc);
        dest.writeString(photoUrl);
        dest.writeDouble(price);
        dest.writeInt(commission);
        dest.writeInt(qtd);
        dest.writeFloat(rating);
        dest.writeString(seller);
        dest.writeString(categoryName);
    }
}

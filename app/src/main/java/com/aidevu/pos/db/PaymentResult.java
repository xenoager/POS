package com.aidevu.pos.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "paymentResult_table")
public class PaymentResult {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Integer id;
    private String img_path, title;
    private Integer price;
    private long payment_date;

    public PaymentResult(String img_path, long payment_date, String title, Integer price) {
        this.img_path = img_path;
        this.payment_date = payment_date;
        this.title = title;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public long getPayment_date() {
        return payment_date;
    }

    public void setPayment_date(long payment_date) {
        this.payment_date = payment_date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}

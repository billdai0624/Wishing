package com.intern.ab.starwish;

/**
 * Created by user on 2015/7/27.
 */
public class PublicWish_item {
    private String wish;
    private String distance;
    private String elapsedTime;
    private int cheeringNum;
    private int wish_id;
    private String country;
    private String city;
    private String originalTime;

    public PublicWish_item() {
        wish = "";
        distance = "";
        elapsedTime = "";
        cheeringNum = 0;
        wish_id = 0;
    }

    public PublicWish_item(String wish, String distance, String elapsedTime, int cheeringNum, String country, String city) {
        this.wish = wish;
        this.distance = distance;
        this.elapsedTime = elapsedTime;
        this.cheeringNum = cheeringNum;
        this.country = country;
        this.city = city;
    }

    public String getWish() {
        return wish;
    }

    public void setWish(String wish) {
        this.wish = wish;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(String originalTime) {
        this.originalTime = originalTime;
    }

    public int getCheeringNum() {
        return cheeringNum;
    }

    public void setCheeringNum(int cheeringNum) {
        this.cheeringNum = cheeringNum;
    }

    public int getWish_id() {
        return wish_id;
    }

    public void setWish_id(int wish_id) {
        this.wish_id = wish_id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

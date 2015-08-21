package com.intern.ab.starwish;


public class Blessing_item implements java.io.Serializable {
    private boolean realized;
    private String wish;
    private String time;
    private int cheeringNum;
    private int id;

    public Blessing_item() {
        realized = false;
        wish = "";
        time = "";
    }

    public Blessing_item(int id, String wish, String time, int cheeringNum, boolean realized) {
        this.id = id;
        this.realized = realized;
        this.wish = wish;
        this.time = time;
        this.cheeringNum = cheeringNum;
    }

    public boolean isRealized() {
        return realized;
    }

    public void setRealized(boolean realized) {
        this.realized = realized;
    }

    public String getWish() {
        return wish;
    }

    public void setWish(String wish) {
        this.wish = wish;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCheeringNum() {
        return cheeringNum;
    }

    public void setCheeringNum(int cheeringNum) {
        this.cheeringNum = cheeringNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

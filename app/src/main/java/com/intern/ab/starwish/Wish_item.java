package com.intern.ab.starwish;


public class Wish_item implements java.io.Serializable {
    private boolean realized;
    private String wish;
    private String time;
    private int id;
    private boolean isPublic;
    private int recNo;

    public Wish_item() {
        realized = false;
        wish = "";
        time = "";
    }

    public Wish_item(int id, String wish, String time, boolean realized, boolean isPublic, int recNo) {
        this.id = id;
        this.realized = realized;
        this.wish = wish;
        this.time = time;
        this.isPublic = isPublic;
        this.recNo = recNo;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public int getRecNo() {
        return recNo;
    }

    public void setRecNo(int recNo) {
        this.recNo = recNo;
    }
}

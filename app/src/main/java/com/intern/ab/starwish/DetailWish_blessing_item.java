package com.intern.ab.starwish;


public class DetailWish_blessing_item implements java.io.Serializable {
    private String blessing;
    private String location;
    private String time;
    private int fakeBlesser_icon;

    public DetailWish_blessing_item() {
        blessing = "";
        location = "";
        time = "";
    }

    public DetailWish_blessing_item(String blessing, String location, String time, int fakeBlesser_icon) {
        this.blessing = blessing;
        this.location = location;
        this.time = time;
        this.fakeBlesser_icon = fakeBlesser_icon;
    }

    public int getFakeBlesser_icon() {
        return fakeBlesser_icon;
    }

    public void setFakeBlesser_icon(int fakeBlesser_icon) {
        this.fakeBlesser_icon = fakeBlesser_icon;
    }

    public String getBlessing() {
        return blessing;
    }

    public void setBlessing(String blessing) {
        this.blessing = blessing;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

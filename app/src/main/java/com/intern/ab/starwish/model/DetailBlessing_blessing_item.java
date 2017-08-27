package com.intern.ab.starwish.model;


public class DetailBlessing_blessing_item implements java.io.Serializable {
    private String blessing;
    private String location;
    private String time;

    public DetailBlessing_blessing_item() {
        blessing = "";
        location = "";
        time = "";
    }

    public DetailBlessing_blessing_item(String blessing, String location, String time) {
        this.blessing = blessing;
        this.location = location;
        this.time = time;
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

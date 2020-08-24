package com.andrew.link.models;

public class FriModel {
    private String name, des;
    private int image;

    public FriModel(String name, String des, int image){
        this.name = name;
        this.des = des;
        this.image = image;
    }

    public int getImage() {
        return image;
    }

    public String getDes() {
        return des;
    }

    public String getName() {
        return name;
    }

}

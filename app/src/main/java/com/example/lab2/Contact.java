package com.example.lab2;

public class Contact {
    private int id;
    private String name;
    private String phone;
    private boolean status;
    private String imagePath;

    public Contact() {
    }

    public Contact(int id, String name, String phone, boolean status, String imagePath) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}

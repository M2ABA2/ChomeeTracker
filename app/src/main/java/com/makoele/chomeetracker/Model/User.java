package com.makoele.chomeetracker.Model;

public class User {
    private String email;
    private String status;
    private String code;
    private String userid;
    private String imageUrl;
    private String name;

    public  User(){};

    public User(String email, String online){}

    public User(String email, String status, String userid, String code) {

        this.email = email;
        this.status = status;

    }

    public User(String imageUrl){
        this.imageUrl= imageUrl;
    }

    public String getEmail(){
        return email;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getUserid() {
        return userid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

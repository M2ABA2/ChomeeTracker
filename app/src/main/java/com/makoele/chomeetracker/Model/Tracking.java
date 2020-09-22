package com.makoele.chomeetracker.Model;

public class Tracking {

    private String email;
    private String uid;
    private double lat;
    private double lng;

    public Tracking(String email, String uid, String s, String valueOf){}

    public  Tracking(double lat, double lng){
        this.lat= lat;
        this.lng = lng;
    }

    public Tracking(String email, String uid,double lat,double lng){
        this.email = email;
        this.uid= uid;
        this.lat= lat;
        this.lng = lng;
    }

    public String getEmail(){
        return email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}

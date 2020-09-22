package com.makoele.chomeetracker.Model;

public class Group {
    private String name;
    private String email;

    public Group(){}

    public Group(String name){
        this.name = name;
    }

    public  Group(String name, String email){
        this.name= name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

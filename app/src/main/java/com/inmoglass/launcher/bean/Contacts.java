package com.inmoglass.launcher.bean;

import java.io.Serializable;

public class Contacts implements Serializable {
    private String number;
    private String name;

    public Contacts(String number, String name) {
        this.name = name;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

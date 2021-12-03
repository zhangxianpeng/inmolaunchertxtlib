package com.inmoglass.launcher.bean;

public class Contacts2 {
    private String name;
    private String number;
    private String address;
    private String date;
    private boolean isOutgoing;

    public Contacts2(String name, String number, String address, String date, boolean isOutgoing) {
        this.name = name;
        this.number = number;
        this.address = address;
        this.date = date;
        this.isOutgoing = isOutgoing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isOutgoing() {
        return isOutgoing;
    }

    public void setOutgoing(boolean outgoing) {
        isOutgoing = outgoing;
    }

    @Override
    public String toString() {
        return "Contacts2{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", address='" + address + '\'' +
                ", date='" + date + '\'' +
                ", isOutgoing=" + isOutgoing +
                '}';
    }
}

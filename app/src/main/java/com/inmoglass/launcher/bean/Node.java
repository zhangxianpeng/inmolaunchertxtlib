package com.inmoglass.launcher.bean;

import android.widget.ImageView;


public class Node {
    private ImageView label;
    private String name;
    private int action;
    private int pos;
    public Node(ImageView label, String name, int action, int pos) {
        this.label = label;
        this.name = name;
        this.action = action;
        this.pos = pos;
    }

    public ImageView getLabel() {
        return label;
    }

    public void setLabel(ImageView label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "Node{" +
                "label=" + label +
                ", name='" + name + '\'' +
                ", action=" + action +
                ", pos=" + pos +
                '}';
    }
}

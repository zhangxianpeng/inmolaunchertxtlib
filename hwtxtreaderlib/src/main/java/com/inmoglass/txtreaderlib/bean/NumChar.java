package com.inmoglass.txtreaderlib.bean;

import android.graphics.Color;

/*
* create by bifan-wei
* 2017-11-13
*/
public class NumChar extends TxtChar {
    //数字默认颜色
//    public static  int DefaultTextColor = Color.parseColor("#45a1cf");
//    public static  int DefaultTextColor = Color.parseColor("#00d823");
    public static  int DefaultTextColor = Color.WHITE;
    public NumChar(char aChar) {
        super(aChar);
    }
    @Override
    public int getTextColor() {
        return DefaultTextColor;
    }

    @Override
    public int getCharType() {
        return Char_Num;
    }
}

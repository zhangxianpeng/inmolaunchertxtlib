package com.inmoglass.txtreaderlib.bean;

import android.graphics.Color;

/*
* create by bifan-wei
* 2017-11-13
*/
public class EnChar extends TxtChar {
//    public static int DefaultTextColor = Color.parseColor("#45a1cd");
    //英文默认颜色
//    public static int DefaultTextColor = Color.parseColor("#00d823");
    public static int DefaultTextColor = Color.WHITE;
    public EnChar(char aChar) {
        super(aChar);
    }
    @Override
    public int getTextColor() {
        return DefaultTextColor;
    }

    @Override
    public int getCharType() {
        return Char_En;
    }
}

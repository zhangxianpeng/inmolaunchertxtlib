package com.inmoglass.txtreaderlib.interfaces;

import com.inmoglass.txtreaderlib.bean.TxtChar;

/**
 * created by ： bifan-wei
 */

public interface ISliderListener {
    void onShowSlider(TxtChar txtChar);
    void onShowSlider(String CurrentSelectedText);
    void onReleaseSlider();
}

package com.inmoglass.txtreaderlib.interfaces;


import com.inmoglass.txtreaderlib.bean.TxtMsg;

/*
* create by bifan-wei
* 2017-11-13
*/
public interface ILoadListener {
    void onSuccess();
    void onFail(TxtMsg txtMsg);
    void onMessage(String message);
}

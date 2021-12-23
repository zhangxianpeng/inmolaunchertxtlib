package com.inmoglass.launcher.util;

import com.inmoglass.launcher.R;

/**
 * @author Administrator
 * @github : https://192.168.3.113:8443/IMC-ROM/imc-launcher.git
 * @time : 2019/09/01
 * @desc : 获取对应的图片资源
 */

public class WeatherResUtil {

    public static int getEqualRes(String iconCode) {
        if (iconCode.equals("100")) {
            return R.drawable.ic_100;
        } else if (iconCode.equals("101")) {
            return R.drawable.ic_101;
        } else if (iconCode.equals("102")) {
            return R.drawable.ic_102;
        } else if (iconCode.equals("103")) {
            return R.drawable.ic_103;
        } else if (iconCode.equals("104")) {
            return R.drawable.ic_104;
        } else if (iconCode.equals("150")) {
            return R.drawable.ic_150;
        } else if (iconCode.equals("153")) {
            return R.drawable.ic_153;
        } else if (iconCode.equals("154")) {
            return R.drawable.ic_154;
        } else if (iconCode.equals("300")) {
            return R.drawable.ic_300;
        } else if (iconCode.equals("301")) {
            return R.drawable.ic_301;
        } else if (iconCode.equals("302")) {
            return R.drawable.ic_302;
        } else if (iconCode.equals("303")) {
            return R.drawable.ic_303;
        } else if (iconCode.equals("304")) {
            return R.drawable.ic_304;
        } else if (iconCode.equals("305")) {
            return R.drawable.ic_305;
        } else if (iconCode.equals("306")) {
            return R.drawable.ic_306;
        } else if (iconCode.equals("307")) {
            return R.drawable.ic_307;
        } else if (iconCode.equals("308")) {
            return R.drawable.ic_308;
        } else if (iconCode.equals("309")) {
            return R.drawable.ic_309;
        } else if (iconCode.equals("310")) {
            return R.drawable.ic_310;
        } else if (iconCode.equals("312")) {
            return R.drawable.ic_312;
        } else if (iconCode.equals("313")) {
            return R.drawable.ic_313;
        } else if (iconCode.equals("314")) {
            return R.drawable.ic_314;
        } else if (iconCode.equals("315")) {
            return R.drawable.ic_315;
        } else if (iconCode.equals("316")) {
            return R.drawable.ic_316;
        } else if (iconCode.equals("317")) {
            return R.drawable.ic_317;
        } else if (iconCode.equals("318")) {
            return R.drawable.ic_318;
        } else if (iconCode.equals("350")) {
            return R.drawable.ic_350;
        } else if (iconCode.equals("351")) {
            return R.drawable.ic_351;
        } else if (iconCode.equals("399")) {
            return R.drawable.ic_399;
        } else {
            return R.drawable.ic_102;
        }
    }
}

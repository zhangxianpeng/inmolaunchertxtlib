package com.inmoglass.launcher.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    /**
     * 时间戳转时间字符串
     * @param timeStamp
     * @return
     */
    public static String getStrTime(long timeStamp) {
        Date date = new Date(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}

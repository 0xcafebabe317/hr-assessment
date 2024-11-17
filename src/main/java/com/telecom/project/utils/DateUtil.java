package com.telecom.project.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: Toys
 * @date: 2024年11月01 15:58
 **/
public class DateUtil {

    public static String getCurrentDateAsDate() {
        // 获取当前日期的字符串形式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月");
        String currentDateStr = dateFormat.format(new Date());
        return currentDateStr;
    }

}

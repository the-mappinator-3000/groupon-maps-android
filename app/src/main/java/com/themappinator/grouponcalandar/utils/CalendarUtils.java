package com.themappinator.grouponcalandar.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lsilberstein on 11/23/15.
 */
public class CalendarUtils {
    public static String getResourceString(String name, Context context) {
        int nameResourceID = context.getResources().getIdentifier(name, "string", context.getApplicationInfo().packageName);
        if (nameResourceID == 0) {
            return ""; //throw new IllegalArgumentException("No resource string found with name " + name);
        } else {
            return context.getString(nameResourceID);
        }
    }

    public static Date trimSeconds(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }
}

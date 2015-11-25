package com.themappinator.grouponcalandar;

import android.content.Context;

/**
 * Created by lsilberstein on 11/23/15.
 */
public class CalendarUtils {
    public static String getResourceString(String name, Context context) {
        int nameResourceID = context.getResources().getIdentifier(name, "string", context.getApplicationInfo().packageName);
        if (nameResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + name);
        } else {
            return context.getString(nameResourceID);
        }
    }
}

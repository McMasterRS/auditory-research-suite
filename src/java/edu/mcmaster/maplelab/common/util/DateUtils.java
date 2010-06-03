/*
 * Copyright (c) 2009 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.common.util;

import java.util.*;

public class DateUtils {
    public static String formatTime(long ms) {
        Date d = new Date(ms);
        Calendar c = new GregorianCalendar();
        c.setTime(d);
        String hours = String.valueOf(c.get(Calendar.HOUR));
        String minutes = String.format("%02d", c.get(Calendar.MINUTE));
        String seconds = String.format("%02d", c.get(Calendar.SECOND));
        String millis = String.valueOf(c.get(Calendar.MILLISECOND));
        String retval = hours + ":" + minutes + ":" + seconds + "." + millis
                + (c.get(Calendar.AM_PM) == Calendar.AM ? " AM" : " PM");
        return retval;
    }
    public static void main(String[] arg) {
        long now = System.currentTimeMillis();
        System.out.println(formatTime(now));
    }
}

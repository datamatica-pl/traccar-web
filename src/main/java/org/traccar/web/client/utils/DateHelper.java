package org.traccar.web.client.utils;

import java.util.Date;

public class DateHelper {
    public static final long MILLISECONDS_A_DAY = 24 * 60 * 60 * 1000;
    
//    public static Date truncateTime(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTime();
//    }
    
    public static long dayDifference(Date from, Date to) {
        return (to.getTime() - from.getTime())/MILLISECONDS_A_DAY;
    }
}

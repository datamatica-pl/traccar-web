package org.traccar.web.client.utils;

import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

public class DateHelper {
    public static final long MILLISECONDS_A_DAY = 24 * 60 * 60 * 1000;
    
    public static Date today() {
        return truncateTime(new Date());
    }
    
    public static long dayDifference(Date from, Date to) {
        return (to.getTime() - from.getTime())/MILLISECONDS_A_DAY;
    }
    
    public static Date truncateTime(Date date) {
        DateTimeFormat formatter = DateTimeFormat.getFormat("dd/MM/yyyy");
        return formatter.parse(formatter.format(date));
    }
}

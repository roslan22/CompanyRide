/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 *
 * @author Vlada
 */
public class DateAndTime {
 
    //starts from 1
    public static int retrieveDayOfTheWeek(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    
    public static boolean isHoursMatch(double fromHour1, double toHour1, double fromHour2, double toHour2)
    {
        return (
                 ((fromHour1  <=  toHour2) && (fromHour1  >= fromHour2))
                || ((toHour1  <=  toHour2) && (toHour1 >= fromHour2))
                || ((toHour1 <= toHour2) && (fromHour1 >= fromHour2))
                || ((toHour2 <= toHour1) && (fromHour2 >= fromHour1))
        );
    }

    
    public static double converTimeToNumber(Date time)
    {
       Calendar calendar = GregorianCalendar.getInstance();
       calendar.setTime(time);
        
       double hours =  calendar.get(Calendar.HOUR_OF_DAY);
       double minutes  = calendar.get(Calendar.MINUTE);
       double expandedMinutes =  minutes/60;

        return hours + expandedMinutes;
    }
    
    
    
    public static Date setTimeToMidnightInUTC(Date date) 
    { 
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    } 
    
    
        public static Date setTimeToMidnight(Date date) 
    { 
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    } 
    

    public static int getNowHourInUTC()
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(new Date());
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

        
    public static int getWeekdayInUTC()
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(new Date());
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static long getTimeOffset(){
        return TimeZone.getDefault().getOffset(new Date().getTime());
    }

    public static Date utcToDefaultTime(Date utcTime, long offset){
        return new Date(utcTime.getTime() + offset);
    }

    public static Date defaultToUTCTime(Date defaultTime, long offset){
        return new Date(defaultTime.getTime() - offset);
    }
    
    
    public static double getNowMinutesStretchedTo100InUTC()
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(new Date());
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        double minute =  calendar.get(Calendar.MINUTE);
        return minute / 60.0;
    }
    
    
    public static double getNowTimeInUTC()
    {
        int nowHour = getNowHourInUTC();
        double minutes = getNowMinutesStretchedTo100InUTC();
         return nowHour + minutes;
    }
}

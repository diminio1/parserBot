/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rita.blowup.com;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author Маргарита
 */
public class DateEdit {
    
    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTime(new Date());
        return calendar.get(Calendar.YEAR);
    }
    
    public static int getMonth(String src) {
        if (src.contains("января"))
            return 0;
        if (src.contains("февраля"))
            return 1;
        if (src.contains("марта"))
            return 2;
        if (src.contains("апреля"))
            return 3;
        if (src.contains("мая"))
            return 4;
        if (src.contains("июня"))
            return 5;
        if (src.contains("июля"))
            return 6;
        if (src.contains("августа"))
            return 7;
        if (src.contains("сентября"))
            return 8;
        if (src.contains("октября"))
            return 9;
        if (src.contains("ноября"))
            return 10;
        if (src.contains("декабря"))
            return 11;
        return -1;
    }
    
    public static Date add(Date d, int i) {
        int day = d.getDate();
        int mon = d.getMonth();
        int year = d.getYear();
        
        day += i;
        int qnt = daysQnt(mon, isLeap(year));
        while (day > qnt) {
            if (mon == 11) {
               year++;
               mon = 0;
            }
            else {
                mon++;
            }
            day -= qnt;
        }
        return new Date(year, mon, day);
    }
    
    public static boolean before(Date d1, Date d2) {
        if (d1.getYear() < d2.getYear())
            return true;
        if (d1.getYear() > d2.getYear())
            return false;
        if (d1.getMonth() < d2.getMonth())
            return true;
        if (d1.getMonth() > d2.getMonth())
            return false;
        if (d1.getDate() < d2.getDate())
            return true;
        if (d1.getDate() > d2.getDate())
            return false;
        return false;
    }
    
    private static int daysQnt(int i, boolean flag) {
        if (i == 0 || i == 2 || i == 4 || i == 6 || i == 7 || i == 9 || i == 11)
            return 31;
        if (i == 3 || i == 5 || i == 8 || i == 10)
            return 30;
        if (i == 1) {
            if (flag)
                return 29;
            else
                return 28;
        }
        return 0;
    }
    
    private static boolean isLeap(int y) {
        return (y % 4 == 0);
    }
}

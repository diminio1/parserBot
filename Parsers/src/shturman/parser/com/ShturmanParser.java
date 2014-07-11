package shturman.parser.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import main.parser.com.TourObject;
import money.currency.Currency;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import banan.file.writer.BananFileWriter;

import rita.blowup.com.DateEdit;
import term.filter.parser.TermFilter;

/**
 *
 * @author Маргарита
 */
public class ShturmanParser {
 
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.tour-shturman.com/main";
    private static final int    source = 4;
    private static Date last;
    private static double dollar;
    private static double euro;
    private static int days = 14;
    
    public ShturmanParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        
    	bananLog.write(null, "Shturman start!\n");
    	
        tours = new ArrayList<TourObject>();
        Document shturmanTourDoc = null;
                        
        dollar = getCurrency(true);           
        euro = getCurrency(false);

        last = new Date();
        last = DateEdit.add(last, days);

        
        try {
            shturmanTourDoc = Jsoup.connect(website).timeout(5000).get();
            Elements tourElems = shturmanTourDoc.select("a[class = readon]");
            if (tourElems == null)
            	bananLog.write(null, "Elements are null!\n");
                //throw new NullPointerException("Elements are null");
            String basePath = website.substring(0, website.lastIndexOf('/'));
            Elements depCities = shturmanTourDoc.select("strong:contains(из)");
            if (depCities == null)
            	bananLog.write(null, "Depart Cities are null!\n");
                //throw new NullPointerException("Depart Cities are null");
            
            int index = 0;
            
            for (Element x: tourElems) {
                try {
                    String s = x.attr("href");
                
                    String path = basePath + s;
                    Document xDoc = Jsoup.connect(path).timeout(100000).get();
                    if (xDoc == null)
                    	bananLog.write(null, "Html document is nul!\n");
                    	//throw new NullPointerException("Html document is null");                    
                    Element country = xDoc.select("td[class = contentheading]").first();
                    if (country == null)bananLog.write(null, "Countries are null!\n");
                        //throw new  NullPointerException("Countries are null");
                    String tempCountry = country.text();
                    tempCountry = tempCountry.substring(0, tempCountry.indexOf("!")); // 
                    Element depCity = depCities.get(index);
                    index++;
                    Elements allTours = xDoc.select("tbody[style *= margin: 0px; padding: 0px; border: 0px; font-size: 12px; font: inherit; vertical-align: baseline;]");
                    if (allTours == null)bananLog.write(null, "Tours are null!\n");
                        //throw new  NullPointerException("Tours are null");
                    for (Element z: allTours) {
                        Elements info = z.select("td[style *= font-size: 10px;]");
                    try {
                        TourObject tObj = new TourObject();
                        tObj.setSource(source);
    
                        tObj.departDate = getDate(info.get(2).text());
                        if(tObj.departDate == null)
                            continue;
                        if(!(DateEdit.before(tObj.departDate, last)) || DateEdit.before(tObj.departDate, new Date()))
                            continue;
                        
                        tObj.setLink(path);
                        tObj.setCountry(tempCountry, countryStand, bananLog);
                        tObj.setHotel(getHotel(info.get(1).text()));
                        tObj.setTown(getTown(info.get(1).text(), bananLog), cityStand, "Shturman: ", bananLog);
                        tObj.setStars(getStars(info.get(1).text(),bananLog));
                        tObj.setPrice(getPrice(z.select("p[style *= margin-bottom: 0px; font: inherit; vertical-align: baseline;]").text(),bananLog));
                        if (0 == tObj.price)
                            continue;
                        tObj.setDuration(getDuration(z.select("td[style *= width: 170px;]").first().text(), bananLog));
                        tObj.setDepartCity(getDepCity(depCity.text(), bananLog));
                        tObj.setNutrition(getNutrition(z.select("td[style *= width: 170px;]").text()));
                        tObj.setRoomType(getRoomType(z.text()));
                        tObj.setPreviousPrice(null);
                        
                        tours.add(tObj);
                        }
                        catch (NullPointerException ex) {
                            continue;
                        }
                    }
                }
                catch (NullPointerException ex) {
                }
            }
        }
        catch (Exception ex) {
//            System.out.println("Failed to connect!");
        	if(ex.getMessage() != null){
				
				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
			}
			else{
				
				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
			}
        }
    }
    
    /**
     * 
     * @param src - string which contains the name of our hotel
     * @return - name of the hotel
     */
    private String getHotel(String src) {
        if(src.equals(""))
            return "";
        String res = "";
        res = src; 
        res = res.replace("\u00a0", " "); // replaces &nbsp with " "
        while (res.charAt(0) == ' ') // delets spaces at the beginning
            res = res.substring(1);
        res = res.trim().toUpperCase();
        if (res.contains("*")) { // we cut info about stars
            int placeStar = res.indexOf("*");
            if (res.charAt(placeStar - 1) == '+' || res.charAt(placeStar - 1) == ' ') // sometimes we have *+ or +*
                res = res.substring(0, placeStar - 3).trim().toUpperCase();
            else
                res = res.substring(0, placeStar - 2).trim().toUpperCase();
        }
        return res.replace('\'', '"');
    }
    
    /**
     * 
     * @param src - string which contains the name of destination
     * @return - name of town that is our destination
     */
    private String getTown(String src, FileWriter bananLog) {
        int placeStart = src.indexOf("*"); // info about town goes after stars
        String res = src.substring(placeStart + 1);
        try { 
            while (res.charAt(0) == ' ' ||res.charAt(0) == '+')
                res = res.substring(1);
            if (res.startsWith("DELUXE")) // deletes unnecessary information 
                res = res.substring(6);
            res = res.replace("\u00a0", " "); // replaces &nbsp with spaces
            while (res.charAt(0) == ' ')
                res = res.substring(1);
            return res.trim().toUpperCase();
        }
        catch (IndexOutOfBoundsException ex) {
//            System.out.println("No town is mentioned in this tour");
        	bananLog.write(null, "No town is mentioned in this tour!\n");
            return "";
        }
    }
    
    private Date getDate(String src) {
    	
    	try {
			
        String res = src; 
        res = res.replace("\u00a0", " ");
        if (res.contains("Вылет"))
            res = res.substring(res.indexOf(" "));
        while (res.charAt(0) == ' ')
            res = res.substring(1);
        int year = Integer.parseInt(res.substring(6, 10)) - 1900;
        int month = Integer.parseInt(res.substring(3, 5)) - 1;
        int day = Integer.parseInt(res.substring(0, 2));
        Date depDate = new Date(year, month, day);
        return depDate;
        
    	} catch (NumberFormatException e) {
    		// TODO: handle exception
    		return null;
    	} catch (Exception ex){
    		
    		return null;   		
    	}
    }
    
    private int getStars(String src, FileWriter bananLog) {
        try {
            String res = src; 
            res = res.replace("\u00a0", " ");
            int k = res.charAt(0);
            while (!(k >= 50 && k <= 54)) {
                res = res.substring(1);
                k = res.charAt(0);
            }
            return Integer.parseInt("" + res.charAt(0));
        }
        catch(IndexOutOfBoundsException ex) {
//            System.out.println("No information about stars is mentioned.");
            bananLog.write(null, "No information about stars is mentioned!\n");
            return 0;
        }
    }
    
    private int getDuration(String src, FileWriter bananLog) {
        try {
            int pos = src.indexOf("дн");
            String str = src.substring(0, pos);
            str = str.replace("\u00a0", " ").trim();
            str = str.substring(str.lastIndexOf(" ") + 1);
            int k = str.charAt(0);
            while(!(k >= 48 && k <= 57))
                str = str.substring(1);
            return Integer.parseInt(str);
        }
        catch (IndexOutOfBoundsException ex) {
//            System.out.println("No information about the duration of the tour.");
        	bananLog.write(null, "No information about the duration of the tour!\n");
            return 0;
        }
    }
    
    private String getDepCity(String src, FileWriter bananLog) {
        try {
            String res = src.replace("\u00a0", " ").trim();
            int pos = res.lastIndexOf(' ');
            res = res.substring(pos + 1, res.length() - 1);
            return res.trim().toUpperCase();
        }
        catch (IndexOutOfBoundsException ex) {
//            System.out.println("No depart city is mentioned in this tour.");
            bananLog.write(null, "No depart city is mentioned in this tour!\n");
            return "";
        }        
    }
    
    private String getNutrition(String src) {
        String res = src.trim().substring(src.lastIndexOf(':') + 1, src.lastIndexOf('('));
        res = res.replace("\u00a0", " ").trim().toUpperCase();
        res = res.replace("SELECT ", "");
        res = res.replace("SUPER ", "");
        res = res.replace("24 HOURS ", "");
        res = res.replace("ULTRA ", "U");
        return res;
    }
    
    private String getRoomType(String src) {
        String res = src.toUpperCase();
        if (res.contains("DBL") || res.contains("DOUBLE"))
           return "DBL";
        if (res.contains("BG"))
           return "BGL";
        if (res.contains("SGL"))
           return "SGL";
        if (res.contains("TRPL"))
           return "TRPL";
        if (res.contains("QDPL"))
           return "QDPL";
        return "";
    }
    
    private int getPrice(String src, FileWriter bananLog) {
        try {
        if (src.equals(""))
                return 0;
        String res = src;
        res = res.replace("\u00a0", " ").trim();
        if (res.startsWith("от"))
            res = res.substring(2);
        int price = 0;
        if (res.contains("$")) {
            res = res.substring(0, res.indexOf("$"));
            res = res.trim();
            return (int) (Integer.parseInt(res) * dollar);
        }
        if (res.contains("€")) {
            res = res.substring(0, res.indexOf("€"));
            res = res.trim();
            return (int) (Integer.parseInt(res) * euro);            
        }
        if (res.contains("грн")) {
            res = res.substring(0, res.lastIndexOf(" "));
            res = res.trim();
        }
        price = (int) (Integer.parseInt(res.trim()));            
        
        return price;
        }
        catch (IndexOutOfBoundsException ex) {
//            System.out.println("No price is mentioned in this tour.");
            bananLog.write(null, "No price is mentioned in this tour!\n");
            
            return 0;
        }
        catch (NumberFormatException ex) {
        	bananLog.write(null, "No price is mentioned in this tour!\n");
            //System.out.println("No price is mentioned in this tour.");
            return 0;            
        }
    }
    
    private double getCurrency(boolean cur) {
        Document curDoc = null;
        Currency c = new Currency();
        if (cur) {
            return c.dollar;
        }
        else {
            return c.euro;
        }
    }
    
////    private static Date add(Date d, int i) {
////        int day = d.getDate();
////        int mon = d.getMonth();
////        int year = d.getYear();
////        
////        day += i;
////        int qnt = daysQnt(mon, isLeap(year));
////        while (day > qnt) {
////            if (mon == 11) {
////               year++;
////               mon = 0;
////            }
////            else {
////                mon++;
////            }
////            day -= qnt;
////        }
////        return new Date(year, mon, day);
////    }
////    
////    private static boolean before(Date d1, Date d2) {
////        if (d1.getYear() < d2.getYear())
////            return true;
////        if (d1.getYear() > d2.getYear())
////            return false;
////        if (d1.getMonth() < d2.getMonth())
////            return true;
////        if (d1.getMonth() > d2.getMonth())
////            return false;
////        if (d1.getDate() < d2.getDate())
////            return true;
////        if (d1.getDate() > d2.getDate())
////            return false;
////        return false;
////    }
////    
////    private static int daysQnt(int i, boolean flag) {
////        if (i == 0 || i == 2 || i == 4 || i == 6 || i == 7 || i == 9 || i == 11)
////            return 31;
////        if (i == 3 || i == 5 || i == 8 || i == 10)
////            return 30;
////        if (i == 1) {
////            if (flag)
////                return 29;
////            else
////                return 28;
////        }
////        return 0;
////    }
////    
//    private static boolean isLeap(int y) {
//        return (y % 4 == 0) ? true : false;
//    }
}
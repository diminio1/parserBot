package columpus.parser.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
 *
 * @author Маргарита
 */
public class HColumbusParser {
        
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.hcolumbus.com.ua/hot_tours/";
    private static final int    source = 2;
    private static Date last;
    private static int  days = 30; 
    
    public HColumbusParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
    	
    	bananLog.write(null, "HColumbus start!\n");
    	
        tours = new ArrayList <TourObject>();
        Document tourDoc = null;
        
        last = new Date();
        last = DateEdit.add(last, days);
        
        try {
            tourDoc = Jsoup.connect(website).timeout(5000).get();
            Elements tourElems = tourDoc.select("a[class = index_tour_href]");
            for (Element x: tourElems) {
                String s = x.attr("href");
                fillTour(s, countryStand, cityStand, bananLog);
            }
        }
        catch (Exception ex) {
            //System.out.println("Failed to connect!");
        	bananLog.write(null, "Failed to connect!\n");
        	
        	if(ex.getMessage() != null){
				
				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
			}
			else{
				
				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
			}
        }
    }
    
        private void fillTour(String url, TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        
        // Init block 
        Document tourDoc = null;
        TourObject tObj = new TourObject();
        
        try {
            tourDoc = Jsoup.connect(url).timeout(5000).get();

            Elements date = tourDoc.select("div[class *= tcategory_name]:containsOwn(Дат)");
            date = date.select("span");            
            if (date == null)bananLog.write(null, "Date is null!\n");
//                throw new NullPointerException("Date is null");
            String myDate = date.text();
            if(myDate.contains(";")) {
                myDate = myDate.substring(myDate.lastIndexOf(';') + 1).trim();
            }
            try {
                tObj.departDate = new Date(Integer.parseInt(myDate.substring(6, 10)) - 1900, Integer.parseInt(myDate.substring(3,5)) - 1, Integer.parseInt(myDate.substring(0,2)));
                if (tObj.departDate == null)
                    return;
                if(!(DateEdit.before(tObj.departDate, last)) || DateEdit.before(tObj.departDate, new Date()))
                    return;
            }
            catch (NumberFormatException ex) {
                //System.out.println("No information about depart date is mentioned.");
            	bananLog.write(null, "No information about depart date is mentioned!\n");
                return;
            }
            catch (IndexOutOfBoundsException ex) {
            	bananLog.write(null, "No information about depart date is mentioned!\n");
                //System.out.println("No information about depart date is mentioned.");
                return;
            }

            Elements country = tourDoc.select("a[class = country_name_link]");
            if (country == null)bananLog.write(null, "Country is null!\n");
                //throw new NullPointerException("Country is null");
            
            Elements city = tourDoc.select("div[class = tcategory_name]:containsOwn(Город)");
            city = city.select("span");
            if (city == null)bananLog.write(null, "City is null!\n");
                //throw new NullPointerException("City is null");
            
            Elements hotel = tourDoc.select("div[class = tcategory_name]:containsOwn(Отель)");
            hotel = hotel.select("span");
            if (hotel == null)bananLog.write(null, "Hotel is null!\n");    
//                throw new NullPointerException("Hotel is null");
            
            
            Elements dur = tourDoc.select("div[class = tcategory_name]:containsOwn(Длительность)");
            if (dur == null)bananLog.write(null, "Duration is null!\n");
//                throw new NullPointerException("Duration is null");
            String myDur = dur.text().trim();
            try {
                myDur = myDur.substring(myDur.indexOf(":") + 1, myDur.lastIndexOf(" ")).trim();
            }
            catch (IndexOutOfBoundsException ex) {
                //System.out.println("No information about the duration of the tour is mentioned.");
            	bananLog.write(null, "No information about the duration of the tour is mentioned!\n");
                return;
            }
            
            Elements nutrition = tourDoc.select("div[class *= tcategory_name]:containsOwn(Вид питания)");
            nutrition = nutrition.select("span");
            if (nutrition == null)bananLog.write(null, "Nutrition is null!\n");
//                throw new NullPointerException("Nutrition is null");
            
            Elements roomType = tourDoc.select("span:contains(Проживание)");
            
            if (roomType == null)bananLog.write(null, "Room Type is null!\n");
//                throw new NullPointerException("Room Type is null");
            
            Elements price = tourDoc.select("div[class = tour_price]");
            price = price.select("span");
            if (price == null) {
                //throw new NullPointerException("Price is null");
            	bananLog.write(null, "Price is null!\n");
            }
            
            tObj.setCountry(country.text().trim().toUpperCase(), countryStand, bananLog);
            
            tObj.setTown(city.text().trim().toUpperCase(), cityStand, "HColumbus: ", bananLog);
            tObj.setSource(source);
            
            tObj.setHotel(getHotel(hotel.text()).trim().toUpperCase());
            if(hotel.text().contains("-") || hotel.text().equals("3*")|| hotel.text().equals("4*")|| hotel.text().equals("5*")|| hotel.text().equals("2*"))
                tObj.setHotel(null);
            
            tObj.setDepartCity("КИЕВ");
                        
            tObj.setDuration(getDuration(myDur));
            
            tObj.setNutrition(getNutrition(nutrition.text()));
            
            tObj.setRoomType(getRoomType(roomType.text()));
            
            tObj.setStars(getStars(hotel.text().trim().toUpperCase()));
            
            tObj.setPrice(getPrice(price.text()));
            if (0 == tObj.price)
                return;

            tObj.setLink(url);
            
            
            tours.add(tObj);
        }
        catch (IOException ex) {
            //System.out.println("Failed to connect!");
        	bananLog.write(null, "Failed to connect!\n");
        }
        catch (NullPointerException ex) {
//            System.out.println("Null Pointer Exception!");
            bananLog.write(null, "Null Pointer Exception!\n");
            bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
        }
    }
        
    private String getRoomType(String src) {
        String res = src.toUpperCase();
        if (res.contains("BG"))
           return "BGL";
        if (res.contains("SGL"))
           return "SGL";
        if (res.contains("TRPL"))
           return "TRPL";
        if (res.contains("QDPL"))
           return "QDPL";
        return "DBL";
    }
   
    private int getPrice(String src) {
        Currency c = new Currency();
        if (src.contains("$"))
            return (int)((int) getDuration(src) * c.dollar);
        if (src.contains("€"))
            return (int)((int) getDuration(src) * c.euro);
        return getDuration(src);
    }
    
    private int getDuration(String src) {
        String res = src.trim();
        if (res.equals(""))
            return 0;
        int k = res.charAt(0);
        while (!(k >= 48 && k <= 57)) {
            res = res.substring(1);
            k = res.charAt(0);
        }
        for (int i = 0; i < res.length(); ++ i) {
            int p = res.charAt(i);
            if(!(p >= 48 && p <= 57)) {
                res = res.substring(0, i);
                try {
                    return Integer.parseInt(res);
                }
                catch (NumberFormatException ex) {
                    return 0;
                }
            }
        }
        return Integer.parseInt(res);
    }
    
    private String getHotel(String src) {
        String res = src.trim();
        try {
            return res.substring(0, res.indexOf('*') - 1);
        }
        catch(IndexOutOfBoundsException ex) {
            return res;
        }
    }  
    
    private int getStars(String src) {
        String res = src.trim();
        try {
            res = "" + res.charAt(res.indexOf("*") - 1);
            return Integer.parseInt(res);
        }
        catch (NumberFormatException ex) {
            return 0;
        }
        catch (IndexOutOfBoundsException ex) {
            return 0;
        }
    }
    
    private String getNutrition(String src) {
        String res = src.trim().toUpperCase();
        if (res.equals("БЕЗ ПИТАНИЯ"))
            return "RO";
        if (res.equals("ЗАВТРАКИ+УЖИНЫ"))
            return "HB";
        if (res.equals("ЗАВТРАКИ"))
            return "BB";
        if (res.equals("ПО ПРОГРАММЕ"))
            return "FB";
        if (res.equals("УЛЬТРА ВСЕ ВКЛЮЧЕНО"))
            return "UAI";
        if (res.equals("ВСЕ ВКЛЮЧЕНО"))
            return "AI";
        return "";
    }
    
//    private static Date add(Date d, int i) {
//        int day = d.getDate();
//        int mon = d.getMonth();
//        int year = d.getYear();
//        
//        day += i;
//        int qnt = daysQnt(mon, isLeap(year));
//        while (day > qnt) {
//            if (mon == 11) {
//               year++;
//               mon = 0;
//            }
//            else {
//                mon++;
//            }
//            day -= qnt;
//        }
//        return new Date(year, mon, day);
//    }
//    
//    private static boolean before(Date d1, Date d2) {
//        if (d1.getYear() < d2.getYear())
//            return true;
//        if (d1.getYear() > d2.getYear())
//            return false;
//        if (d1.getMonth() < d2.getMonth())
//            return true;
//        if (d1.getMonth() > d2.getMonth())
//            return false;
//        if (d1.getDate() < d2.getDate())
//            return true;
//        if (d1.getDate() > d2.getDate())
//            return false;
//        return false;
//    }
//    
//    private static int daysQnt(int i, boolean flag) {
//        if (i == 0 || i == 2 || i == 4 || i == 6 || i == 7 || i == 9 || i == 11)
//            return 31;
//        if (i == 3 || i == 5 || i == 8 || i == 10)
//            return 30;
//        if (i == 1) {
//            if (flag)
//                return 29;
//            else
//                return 28;
//        }
//        return 0;
//    }
//    
//    private static boolean isLeap(int y) {
//        return (y % 4 == 0) ? true : false;
//    }

}
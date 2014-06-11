package HTP.parser.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import main.parser.com.TourObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import com.sun.swing.internal.plaf.basic.resources.basic;

import pair.parser.Pair;
import rita.blowup.com.DateEdit;
import term.filter.parser.TermFilter;

/**
 *
 * @author Маргарита
 */
public class HTParser {
    
    public ArrayList <TourObject> tours;
    private static final String website = "http://ht.kiev.ua/tours/type1.html";
    private static final int    source = 3;
    
    public HTParser(TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {

    	bananLog.write(null, "HTParser srart!");
    	
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        try {
            tourDoc = Jsoup.connect(website).timeout(5000).get();
            Elements tourElems = tourDoc.select("a[class = t_price]"); // gets all tours
            String basePath = website.substring(0, website.lastIndexOf('/'));
            for(Element x: tourElems) {
                String s = x.attr("href"); // gets url of current tour 
                String path = basePath.substring(0, basePath.lastIndexOf('/')).concat(s); // gets web page of it
                fillTour(path, countryStand, cityStand, bananLog); // fills information in every tour
            }
        }
        catch(Exception ex) {
//          ex.printStackTrace();
			bananLog.write(null, "Exeption: " + ex.getStackTrace().toString() + "\n");
        }
    }
    
    private void fillTour(String url, TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
        
        // Init block 
        Document tourDoc = null;
        TourObject tObj = new TourObject();
            
        // we need these functions to get current year
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTime(new Date());
        int currentYear = calendar.get(Calendar.YEAR);
        
        try {
            tourDoc = Jsoup.connect(url).get();
        
            Element country = tourDoc.select("div[class = desc]").get(1);
            Element city = tourDoc.select("div[class = desc]").get(2);
            Element hotel = tourDoc.select("div[class = desc]").get(3);
            Element depCity = tourDoc.select("td:eq(1)").first();
            Elements stars = tourDoc.select("img.t_star");
            Element date = tourDoc.select("td:eq(1)").get(2);
            if(date.text().equals("")) {
                date = tourDoc.select("option[value = 0]").first();            
            }
            Element dur = tourDoc.select("td:eq(1)").get(3);
            Element nutrition = tourDoc.select("td:eq(1)").get(4);
            Element roomType = tourDoc.select("td:eq(1)").get(5);
            Element price = tourDoc.select("a[class = price]").first();
        
            tObj.setCountry(country.text().trim().toUpperCase(), countryStand, bananLog);
            
            tObj.setTown(city.text().trim().toUpperCase(), cityStand, "HT.kiev.ua: ", bananLog);
            
            String hotelTemp = hotel.text();
            try {
                hotelTemp = hotelTemp.substring(0, hotelTemp.indexOf("*") - 2);
            }
            catch(StringIndexOutOfBoundsException ex) {
            }
            tObj.setHotel(hotelTemp.trim().toUpperCase().replace('\'', '"'));
            tObj.setDepartCity(depCity.text().toUpperCase());
            tObj.departDate = new Date(currentYear - 1900, Integer.parseInt(date.text().substring(3,5)) - 1, Integer.parseInt(date.text().substring(0,2)));
            
            if(tObj.departDate == null)
                return;
            if((DateEdit.before(tObj.departDate, new Date())))
                return;
            
            tObj.setStars(stars.size());
        
            String durat = dur.text();
            durat = durat.substring(0, durat.length() - 5).trim();
            try {
                tObj.setDuration(Integer.parseInt(durat));
            }
            catch(NumberFormatException ex) {
                durat = durat.substring(0, durat.length() - 1);
                tObj.setDuration(Integer.parseInt(durat));          
            }
        
            tObj.setNutrition(nutrition.text().substring(0, 2).trim().toUpperCase());
            tObj.setRoomType(roomType.text().substring(0, roomType.text().indexOf('-')).trim().toUpperCase());
            tObj.setPrice(Integer.parseInt(price.text().substring(0, price.text().length() - 4)));
            tObj.setLink(url);
            tObj.setSource(source);
            
            //tObj.print();
            
            tours.add(tObj);
        }
        catch(IOException ex) {
            ex.printStackTrace();
			bananLog.write(null, "Exeption: " + ex.getStackTrace().toString() + "\n");
        }
    }
}
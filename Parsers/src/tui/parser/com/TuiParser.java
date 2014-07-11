package tui.parser.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import main.parser.com.TourObject;

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
public class TuiParser {
    
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.tui.ua/";
    private static final int    source = 1;
    
    public TuiParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList <TourObject>();
        Document tuiDoc = null;
        try {
            tuiDoc = Jsoup.connect(website).timeout(5000).get();
            if (tuiDoc == null) bananLog.write(null, "Document is null!\n");
                //throw new NullPointerException("Document is null");
            
            Elements tourElems = tuiDoc.select("div[class = offers-list]").select("li");
            if (tourElems == null) bananLog.write(null, "Tour Elements are null!\n");
                //throw new NullPointerException("Tour Elements are null");
            
            for (Element x: tourElems) {
                String country = x.select("div[class = country]").text().trim().toUpperCase();
                String time =  x.select("div[class = time]").first().ownText(); // depCity + duration
                String depDate = x.select("div[class = time]").select("b").text();
                int stars = x.select("span[class = stars]").select("img").size();
                String town = x.select("div[class = way]").select("a[class = item]").first().ownText().trim().toUpperCase();
                String hotel = x.select("a[class = name]").first().ownText().trim().toUpperCase();
                String price = x.select("a[class= price]").first().ownText().trim(); //do not forget to remove &nbsp(\u00a0)
                String link = website + x.select("a[class = all-link]").first().attr("href");
                
                TourObject tObj = new TourObject();
                tObj.setSource(source);
                tObj.setCountry(country, countryStand, bananLog);
                tObj.setTown(town, cityStand, "Tui: ", bananLog);
                tObj.setDepartCity(getDepCity(time));
                tObj.setDuration(getDuration(time));
                tObj.departDate = getDepDate(depDate);
                if(tObj.departDate == null)
                    continue;
                if((DateEdit.before(tObj.departDate, new Date())))
                    continue;

                tObj.setStars(stars);
                tObj.setHotel(getHotel(hotel));
                tObj.setPrice(getPrice(price));
                tObj.setLink(link);
                tObj.setRoomType("DBL");
                tObj.setPreviousPrice(null);
                tours.add(tObj);
            }
        }
        catch (IOException ex) {
            //System.out.println("Failed to connect!");
            bananLog.write(null, "Failed to connect!\n");
        }
        catch (Exception ex) {
            //System.out.println("Caught Null Pointer Exception!");
            bananLog.write(null, "Caught Null Pointer Exception!\n");
        	if(ex.getMessage() != null){
				
				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
			}
			else{
				
				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
			}
        }
    }
    
    private String getDepCity(String src) {
        try {
            String res = "" + src;
            int k = res.indexOf(" ");
            return res.substring(0, k).trim().toUpperCase();
        }
        catch (IndexOutOfBoundsException ex) {
            return "";
        }        
    }
    
    private int getDuration(String src) {
        try {
            String res = "" + src;
            int k = res.indexOf(" ");
            res = res.substring(k + 3);
            k = res.indexOf(" ");
            res = res.substring(0, k);        
            return Integer.parseInt(res);
        }
        catch (NumberFormatException ex) {
            return 0;
        }
        catch (IndexOutOfBoundsException ex) {
            return 0;
        }
    }
    
    private Date getDepDate(String src) {
        String res = "" + src;
        try {
            int year = Integer.parseInt(res.substring(6)) - 1900;
            int month = Integer.parseInt(res.substring(3,5)) - 1;
            int date = Integer.parseInt(res.substring(0,2));
            return new Date(year, month, date);
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }
    
    private String getHotel(String src) {
        try {
            String res = "" + src;
            int k = res.lastIndexOf(" ");
            return res.substring(0, k).replace('\'','"');
        }
        catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }
    
    private int getPrice(String src) {
        try {
            String res = "" + src;
            int n = res.lastIndexOf(" ");
            res = src.replace("\u00a0", "").substring(0, n - 1);
            return Integer.parseInt(res);
        }
        catch (NumberFormatException ex) {
            return 0;
        }
        catch (IndexOutOfBoundsException ex) {
            return 0;
        }
    }
 }
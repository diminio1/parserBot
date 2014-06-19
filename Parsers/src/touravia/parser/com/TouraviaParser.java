package touravia.parser.com;

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

public class TouraviaParser {
    
    public ArrayList <TourObject> tours;
    private static final String website = "http://touravia.info/goryashhie-tury-tour-turcey";
    private static final int    source = 6;
    
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36";
    
    public TouraviaParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        try {
            tourDoc = Jsoup.connect(website).timeout(5000).userAgent(userAgent).get();
            if (tourDoc == null) bananLog.write(null, "Failed to connect!\n");
                //throw new NullPointerException("Failed to connect");
            Elements tourElems = tourDoc.select("ul[class=pagination pagination-sm]").first().select("li").select("a"); // gets all countries
            if (tourElems == null) bananLog.write(null, "list of countries is null!\n"); 
                //throw new NullPointerException("list of countries is null");
            for (Element x: tourElems) {
                String site = "http://touravia.info" + x.attr("href");
                Document xDoc = Jsoup.connect(site).timeout(5000).userAgent(userAgent).get();
                if (xDoc == null) bananLog.write(null, "Failed to connect!\n");
                    //throw new NullPointerException("Failed to connect");

                String country = xDoc.select("li[class=active]").select("a").first().ownText().trim().toUpperCase();
                Elements tourInfo = xDoc.select("div[class = thumbnail text-center]");
                for (Element y: tourInfo) {
                    
                    String hotel = y.select("div").get(3).select("a").select("strong").text().trim().toUpperCase();
                    if (hotel.startsWith("ОТЕЛЬ"))
                        hotel = hotel.substring(6);
                    
                    String depDate = y.select("div").get(5).select("i").text().trim().toUpperCase();
                    if (depDate.startsWith("ВЫЛЕТ"))
                        depDate = depDate.substring(7);
                    
                    String price = y.select("div").get(2).select("span[class = text-danger]").text().trim();
                    
                    String duration = y.select("div").get(6).select("i").text().trim();
                    
                    String nutrition = y.select("div").get(7).select("strong").text().trim().toUpperCase();
                    if (nutrition.contains("ПИТАНИЕ")) 
                        nutrition = nutrition.substring(9);

                    String link = y.select("div").get(4).select("a").first().attr("href");
                    link = "http://touravia.info" + link;
                    
                    TourObject tObj = new TourObject();
                    tObj.setSource(source);
                    tObj.setCountry(country, countryStand, bananLog);
                    
                    tObj.setHotel(getHotel(hotel));
                    tObj.setDepartCity("КИЕВ");
                    tObj.departDate = getDepDate(depDate);
                    tObj.setRoomType("DBL");
                    if(tObj.departDate == null)
                        continue;
                    if((DateEdit.before(tObj.departDate, new Date())))
                        continue;
                    
                    tObj.setDuration(getDuration(duration));
                    tObj.setLink(link);
                    tObj.setNutrition(getNutrition(nutrition));
                    tObj.setPrice(getNumber(price));
                    if (tObj.price == 0)
                        continue;
                    
                    tObj.setStars(getStars(hotel));
                    
                    tours.add(tObj);
                }
            }
        }
        catch (NullPointerException ex) {
            //System.out.println("Caught NullPointerException");
            bananLog.write(null, "Caught NullPointerException!\n");
        }
        catch (IOException ex) {
            //System.out.println("Caught IOException");
            bananLog.write(null, "Caught IOException!\n");
        }
        catch (Exception ex) {
            //System.out.println("Caught Exception");
            bananLog.write(null, "Caught Exception!\n");
        	if(ex.getMessage() != null){
				
				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
			}
			else{
				
				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
			}
        }
    }
    
    private String getHotel(String src) {
        String res = "" + src;
        
        if (res.contains("("))
        {
            int k = res.indexOf("(");
            res = res.substring(0, k).trim();
        }
        
        int pos = res.lastIndexOf(" ");
        return res.substring(0, pos).replace('\'', '"');
    }
    
    private int getStars(String src) {
        if (src.contains("2"))
            return 2;
        if (src.contains("3"))
            return 3;
        if (src.contains("4"))
            return 4;
        if (src.contains("5"))
            return 5;
        return 0;
    }
    
    private Date getDepDate(String src) {
        int n = src.length();
        return new Date(Integer.parseInt(src.substring(n - 4)) - 1900, Integer.parseInt(src.substring(3,5)) - 1, Integer.parseInt(src.substring(0,2)));
    }
    
    private int getNumber(String src) {
        
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
    
    private int getDuration (String src) {
        String res = "" + src;
        
        if (res.contains("/")) {
            int k = res.indexOf("/");
            res = res.substring(k);
        }
        
        return getNumber(res);
    }
    
    private String getNutrition (String src) {
        String res = "" + src;
        if (res.equals("БЕЗ ПИТАНИЯ"))
            return "RO";
        if (res.equals("ЗАВТРАКИ"))
            return "BB";
        if (res.equals("ЗАВТРАК+УЖИН"))
            return "HB";
        if (res.equals("ПО ПРОГРАММЕ"))
            return "FB";
        if (res.equals("ВСЕ ВКЛЮЧЕНО"))
            return "AI";
        if (res.equals("УЛЬТРА ВСЕ ВКЛЮЧЕНО"))
            return "UAI";
        return "";

    }
    
    private String getUserAgent() {
        try {
            Document doc = Jsoup.connect("http://www.whatsmyuseragent.com/").timeout(100000).get();
            String s = doc.select("span[id = body_lbUserAgent]").text();
            return s;
        }     
        catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}

package kenar.parser.com;

import java.util.ArrayList;
import java.util.Date;

import main.parser.com.TourObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import rita.blowup.com.DateEdit;
import rita.blowup.com.Parsable;
import rita.blowup.com.UniversalParser;
import term.filter.parser.TermFilter;
import banan.file.writer.BananFileWriter;

public class KenarParser {
    public ArrayList <TourObject> tours;
    private static final String website = "http://kenar.com.ua/ru/trip/type/goryashchiy.html?Trip_page=1";
    private static final int    source = 16;
    
    public KenarParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        String url = "";
        String tempLink = "" + website;
        do {
        	try {
        		tourDoc = Jsoup.connect(tempLink).timeout(100000).get();
        		Elements tables = tourDoc.select("div[class = trip");
        		for (Element x: tables) {
            	
        			String linkStr = "kenar.com.ua" + x.select("div[class = image]").select("a").attr("href");

        			String countryStr = x.select("div[class = info]").select("h2").text().trim().toUpperCase(); 
            		
        			String roomTypeStr = "";
                
        			String dateStr = x.select("div[class = desc]").select("div[class = date]").select("div").get(2).ownText();
            	    
        			String hotelStr = x.select("div[class = desc]").select("div[class = hotel]").text().replace('\'', '"').trim().toUpperCase();
            	
        			String stars = x.select("div[class = desc]").select("div[class = stars]").text();
            	
        			String townStr = "" + countryStr;
            		
        			String departCityStr = "КИЕВ";
            	    
        			String nutritionStr = x.select("div[class = desc]").select("div[class = food]").text().trim().toUpperCase();
            	    
        			String durationStr = x.select("div[class = desc]").select("div[class = date]").select("div").get(1).ownText();
                    
        			String priceStr = x.select("div[class = info]").select("div[class = price]").select("span[class = uah]").text();
                
        			TourObject localTour = UniversalParser.parseTour(new Parsable() {
        				@Override
        				public Object get(String src) {
        					return "" + src;
        				}
        			}, countryStr, new Parsable() {

        				@Override
        				public Object get(String src) {
        					return "" + src;
        				}
                    }, townStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		try {
                    			if (src.equals(""))
                    				return null;
                    			int start = src.indexOf('-') + 1;
                    			int end = src.trim().lastIndexOf(" ");
                    			String res = src.substring(start, end); 
                    			return res;
                    		}
                    		catch(Exception ex) {
                    			return null;
                    		}
                    	}
                    }, hotelStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		try {
                    			return new Date(Integer.parseInt(src.substring(6, 10)) - 1900, Integer.parseInt(src.substring(3, 5)) - 1, Integer.parseInt(src.substring(0, 2)));
                    		}
                    		catch (IndexOutOfBoundsException ex) {
                    			return new Date(0, 0, 1);
                    		}
                    		catch (NumberFormatException ex) {
                    			return new Date(0, 0, 1);
                    		}
                    	}
                    }, dateStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		return "" + src;
                    	}
                    }, departCityStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		try {
                    			String res = "" + src;
                    			int k = res.charAt(0);
                    			while (!(k >= 48 && k <= 57)) {
                    				res = res.substring(1);
                    				k = res.charAt(0);
                    			}
                    			int l = res.charAt(1);
                    			if ((l >= 48) && (l <= 57))
                    				res = res.substring(0,2);
                    			else
                    				res = res.substring(0,1);
                    			return Integer.parseInt(res);
                    		}
                    		catch (IndexOutOfBoundsException ex) {
                    			return 0;
                    		}
                    		catch (NumberFormatException ex) {
                    			return 0;
                    		}
                    	}
                    }, durationStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		try {
                    			int pos = src.lastIndexOf(" ");
                    			int money = Integer.parseInt(src.substring(0, pos));
                    			return money;
                    		}
                    		catch (IndexOutOfBoundsException ex) {
                    			return 0;
                    		}
                    		catch (NumberFormatException ex) {
                    			return 0;
                    		}
                    	}
                    }, priceStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		return null;
                    	}
                    }, priceStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		if (src.contains("3"))
                    			return 3;
                    		if (src.contains("4"))
                    			return 4;
                    		if (src.contains("5"))
                    			return 5;
                    		if (src.contains("2"))
                    			return 2;
                    		return 0;
                    	}
                    }, stars, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		return "" + src;
                    	}
                    }, linkStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		String res = src.replace("BO", "RO");
                    		if (res.contains("RO"))
                    			return "RO";
                    		if (res.contains("BB"))
                    			return "BB";
                    		if (res.contains("HB"))
                    			return "HB";
                    		if (res.contains("FB"))
                    			return "FB";
                    		if (res.contains("UAL"))
                    			return "UAI";
                    		if (res.contains("AL"))
                    			return "AI";
                    		return null;
                    	}
                    }, nutritionStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		String res = "" + src;
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
                    }, roomTypeStr, source, countryStand, cityStand, bananLog, "Kenar: ");
                    
        			if (localTour != null) {
        				localTour.price /= 2;
        				tours.add(localTour);
        			}
        			else {
        				bananLog.write(null, "NullTour\n");            		                    	
        				continue;
        			}                
        		}
        		url = "" + tempLink;
        		tempLink = "http://kenar.com.ua/" + tourDoc.select("ul[class = yiiPager]").select("li[class *= next]").select("a").first().attr("href");
        	}
        	catch(Exception ex) {
        	
        		if(ex.getMessage() != null){
				
        			bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
        		}
        		else {
        			
        			bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
        		}
        	}
        }
        while (!(tempLink.equals(url)));
    }
}

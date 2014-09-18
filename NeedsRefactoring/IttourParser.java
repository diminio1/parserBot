package ittour.parser.com;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.parser.com.TourObject;
import money.currency.Currency;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import rita.blowup.com.Parsable;
import rita.blowup.com.UniversalParser;
import term.filter.parser.TermFilter;
import banan.file.writer.BananFileWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class IttourParser {
    public ArrayList<TourObject> tours;
    private static String         website = "";
    private static final int      source = 24;
    private static final String   userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36";

    public IttourParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;

        website = "http://www.ittour.com.ua/?action=get_showcase_tour&type=48&items_per_page=30&callback=";
        	try {
        		
        		tourDoc = Jsoup.connect(website).userAgent(userAgent).ignoreContentType(true).timeout(100000).get();
        		String html = tourDoc.html();
        		int beginIndex  = html.indexOf("<table"); 
        		int stopIndex  = html.lastIndexOf("&quot;,&quot;results_count&quot;:30");
        		
        		html = html.substring(beginIndex, stopIndex);
        		
        		String [] parts = html.split("tourPopup");
        		
        		ArrayList<String> argsStr = new ArrayList<String>(); // parameters for url
        		ArrayList<String> prices = new ArrayList<String>(); // price for 1 person
        		
        		Pattern pattern = Pattern.compile(("\'[0-9]*\',\'[0-9]*\'"));
        		
        		for (int i = 1; i < parts.length; ++i) {
            		Matcher matcher = pattern.matcher(parts[i]);
            		if (matcher.find())
            		{
            		    argsStr.add(matcher.group().toString());
            		}
        		}
         		
        		int index = 0;
        		for (String argStr: argsStr) {
        			
        			int coma = argStr.indexOf(",");
        			String param1 = argStr.substring(1, coma - 1); 
        			String param2 = argStr.substring(coma + 2, argStr.length() - 1);         			
        			
        			String linkStr = "http://www.ittour.com.ua/tour-popup-ajax.html?tour_price=" + param1 + "&is_archive=0&show_popup=0&sharding_rule_id=" + param2;
    				
        			Document tour = Jsoup.connect(linkStr).userAgent(userAgent).timeout(100000).get();
        			
        			String countryStr = tour.select("span[class = country_popup]").text().trim().toUpperCase();
        			
        			String townStr = "" + countryStr;
    				
        			String roomTypeStr = "";
    				
        			String dateStr = tour.select("li").get(6).select("b").text().trim();
    				        			
        			String departCityStr = tour.select("li").get(5).select("b").text().trim().toUpperCase();

        			String hotelStr = tour.select("a[class = tour_popup_hotel_url]").text().trim().toUpperCase().replace('\'', '"');

        			String nutritionStr = tour.select("li").get(11).select("b").text().toUpperCase();                	    
    				
        			String durationStr = tour.select("li").get(12).select("b").text();
    				
        			int stars = tour.select("a[class = gold_star]").size();
    				String starsStr = "" + stars;
    				
    				String priceStr = tour.select("div[class = price_box_popup]").select("b").text();
    				
    				index++;
    				
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
							return "" + src;
						}
    					
    				}, hotelStr, new Parsable() {

						@Override
						public Object get(String src) {
							String res = "" + src;
							try {
						//		System.out.println(res);
								int year = Integer.parseInt(res.substring(6, 8));
								int month = Integer.parseInt(res.substring(3, 5));
								int date = Integer.parseInt(res.substring(0, 2));
								return new Date(year + 100, month - 1, date);
							}
							catch(Exception ex) {
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
							String res = "" + src;
							try {
								return Integer.parseInt(res);
							}
							catch(Exception ex) {
								return 0;
							}
						}
    					
    				}, durationStr, new Parsable() {

						@Override
						public Object get(String src) {
							String res = src.substring(0, src.length() - 1).trim();
							try {
								int money = Integer.parseInt(res);
								Currency cur = new Currency();
								if (src.contains("$"))
									return (int) (money * cur.dollar) / 2;
								if (src.contains("€"))
									return (int) (money * cur.euro) / 2;
								return money / 2;
							}
							catch(Exception ex) {
								return 0;
							}
						}
    					
    				}, priceStr, new Parsable() {

						@Override
						public Object get(String src) {
							return null;
						}
    					
    				}, "", new Parsable() {

						@Override
						public Object get(String src) {
							String res = "" + src;
							try {
								return Integer.parseInt(res);
							}
							catch (Exception ex) {
								return 0;
							}
						}
    					
    				}, starsStr, new Parsable() {

						@Override
						public Object get(String src) {
							return "" + src;
						}
    					
    				}, linkStr, new Parsable() {

						@Override
						public Object get(String src) {
    						if (src.contains("UAI"))
    							return "UAI";
    						if (src.contains("AI"))
    							return "AI";
    						if (src.contains("HB"))
    							return "HB";
       						if (src.contains("FB"))
    							return "FB";
       						if (src.contains("BB"))
    							return "BB";
       						if (src.contains("RO") || src.contains("AO") || src.contains("BO") || src.contains("OB"))
    							return "RO";
    						return null;
						}
    					
    				}, nutritionStr, new Parsable() {

						@Override
						public Object get(String src) {
							return "DBL";
						}
    					
    				}, roomTypeStr, null, source, countryStand, cityStand, bananLog, "Ittour: ");
    				if (localTour != null) {
    					tours.add(localTour);
    				}
    				else { 
    					bananLog.write(null, "NullTour\n");            		                    	
    					continue;
    				}
        		}
        	}
        	catch(Exception ex) {
            	
            	if (ex.getMessage() != null){
    				
    				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
    			}
    			else{
    				
    				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
    			}

        	}
    }
}

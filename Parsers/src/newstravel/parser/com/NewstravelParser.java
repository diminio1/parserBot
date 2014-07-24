package newstravel.parser.com;

import java.util.ArrayList;
import java.util.Date;

import main.parser.com.TourObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import rita.blowup.com.Parsable;
import rita.blowup.com.UniversalParser;
import term.filter.parser.TermFilter;
import banan.file.writer.BananFileWriter;

public class NewstravelParser {
    public ArrayList <TourObject> tours;
    private static String         website = "";
    private static final int      source = 23;
    
    public NewstravelParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        int p = 0;
        while (true) {

        	website = "http://besthotels.org.ua/api/get_offers/?t=tours&f=p&d=0&c=0&r=0&p=" + p + "&pp=100";
        	try {
        		
        		tourDoc = Jsoup.connect(website).ignoreContentType(true).timeout(100000).get();

        		String jsonLine = tourDoc.select("body").text().trim();
        		JsonElement jElem = new JsonParser().parse(jsonLine);
        		JsonObject jObject = jElem. getAsJsonObject();
        		JsonArray jTours = jObject.getAsJsonArray("data");
        		if (jTours.toString().equals("[]"))
        			break;
        		
        		for (JsonElement jTour: jTours) {
        			
        			JsonObject tour = jTour.getAsJsonObject();
    			
        			String sold = tour.get("is_soldout").toString();
        			if (sold.equals("1"))
        				continue;
    			
    				String linkStr = "http://www.newstravel.com.ua/predlojeniya";
    				String countryStr = tour.get("country_name").toString().trim();
    				String townStr = tour.get("city_name").toString().trim().toUpperCase();
    				String roomTypeStr = "";
    				String dateStr = tour.get("travel_date").toString();
    				String departCityStr = tour.get("departure_city_name").toString().trim();
    				String hotelStr = tour.get("hotel_name").toString();
    				String nutritionStr = tour.get("board_name").toString();                	    
    				String durationStr = tour.get("travel_nights").toString();
    				String stars = tour.get("hotel_stars").toString(); 
    				String description = tour.get("description").toString();
    				description = description.substring(1, description.length() - 1);
    				description = description.replace("n", " ").trim();
    				description = description.replace("\\", "");
    				description = description.replace("\u00a0", " ");
    				String priceStr = tour.get("price").toString();
    				
    				TourObject localTour = UniversalParser.parseTour(new Parsable() {

						@Override
						public Object get(String src) {
							String res = src.substring(1, src.length() - 1);
							return res.toUpperCase();
						}
    					
    				}, countryStr, new Parsable() {

						@Override
						public Object get(String src) {
							String res = src.substring(1, src.length() - 1);
							return res.toUpperCase();
						}
    					
    				}, townStr, new Parsable() {

						@Override
						public Object get(String src) {
							String res = src.substring(1, src.length() - 1);
							res = res.replace('\'', '"');
							return res.toUpperCase();
						}
    					
    				}, hotelStr, new Parsable() {

						@Override
						public Object get(String src) {
							String res = src.substring(1, src.length() - 1);
							try {
								int year = Integer.parseInt(res.substring(6));
								int month = Integer.parseInt(res.substring(3, 5));
								int date = Integer.parseInt(res.substring(0, 2));
								return new Date(year - 1900, month - 1, date);
							}
							catch(Exception ex) {
								return new Date(0, 0, 1);
							}
						}
    					
    				}, dateStr, new Parsable() {

						@Override
						public Object get(String src) {
							String res = src.substring(1, src.length() - 1);
							return res.toUpperCase();
						}
    					
    				}, departCityStr, new Parsable() {

						@Override
						public Object get(String src) {
							String res = src.substring(1, src.length() - 1);
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
							String res = src.substring(1, src.length() - 1);
							if (!res.contains(".")) {
								return Integer.parseInt(res);
							}
							int dot = res.indexOf(".");
							try {
								int money = Integer.parseInt(res.substring(0, dot));
								try {
									int dec = Integer.parseInt(res.substring(dot + 1, dot + 2));
									if (dec < 5)
										return money;
									else
										return money + 1;
								}
								catch (Exception ex) {
									return 0;
								}
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
							String res = src.substring(1, src.length() - 1);
							res = res.replace("*", "");
							try {
								return Integer.parseInt(res);
							}
							catch(Exception ex) {
								return 0;
							}
						}
    					
    				}, stars, new Parsable() {

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
    					
    				}, roomTypeStr, description, source, countryStand, cityStand, bananLog, "Newstravel: ");
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
    		p++;
        }
    }
}

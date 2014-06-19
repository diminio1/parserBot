package turne.parser.com;

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
import rita.blowup.com.Parsable;
import rita.blowup.com.UniversalParser;
import term.filter.parser.TermFilter;

public class TurneParser {
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.turne.com.ua/hottours#iAP=site&iAL=block-hot&iAC=hottours";
    private static final int    source = 10;
    
    public TurneParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).ignoreHttpErrors(true).get();
            Elements tables = tourDoc.select("div[class = tour-item__back-box");
            for (Element x: tables) {
            	
            	String link = x.select("div[class = e-tour__img]").select("a").attr("href");
                String countryStr = x.select("span[class = e-tour__country]").text().trim().toUpperCase(); 
            		
            	String roomTypeStr = "";
                
            	String dateStr = x.select("div[class = e-tour__text]").select("span[class = date__tour]").text();
            	    
            	String hotelStr = "";
            	
            	Elements hotel = x.select("div[class = e-tour__hotel-name]");
            	if (hotel.size() != 0) {
            		hotelStr = hotel.first().ownText().trim().toUpperCase();
            	}
            	
            	if (countryStr == null || countryStr.equals("")) {
            	 	countryStr = x.select("div[class = e-tour__hotel-name]").select("span[class = country]").text();
            	}
            	
            	String stars = x.select("span[class *= hotel__stars]").attr("class"); 
            	
            	String townStr = "" + countryStr;
            		
            	String departCityStr = "";
            	    
            	String pers = x.select("span[class = e-tour__persons]").text().trim().toUpperCase();
            	    
            	int persons = 1;
            	if (pers.contains("2"))
            	  	persons = 2;
            	    
            	// delete wrong hotel names
            	if (hotelStr.contains("*"))
            	  	hotelStr = hotelStr.substring(0, hotelStr.indexOf("*") - 1).trim();
            	
            	String linkStr = "" + link;
            	    
            	String nutritionStr = x.select("div[class = first_meal]").select("b").text().trim().toUpperCase();
            	    
                String durationStr = x.select("div[class = e-tour__text]").select("span[class = days__tour]").text();
                    
                String priceStr = x.select("span[class = e-tour__price]").first().ownText();
                
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
                            String res = "" + src; 
                            return res.replace('\'', '"');
                        }
                        catch(Exception ex) {
                            return null;
                        }
                    }
                }, hotelStr, new Parsable() {

                    @Override
                    public Object get(String src) {
                        try {
                            return new Date(DateEdit.getCurrentYear() - 1900, Integer.parseInt(src.substring(3, 5)) - 1, Integer.parseInt(src.substring(0, 2)));
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
                            String res = src.replace(" ", "");
                            int money = Integer.parseInt(res);
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
                        if (res.contains("ULTRA ALL INC"))
                         	return "UAI";
                        if (res.contains("ALL INC"))
                         	return "AI";
                        if (res.contains("НЕ УКАЗАН"))
                          	return "";
                        return res;
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
                }, roomTypeStr, source, countryStand, cityStand, bananLog, "Turne: ");
                    
                if (localTour != null) {
                  	localTour.price = localTour.price / persons; 
                  	tours.add(localTour);
                }
                else {
                  	bananLog.write(null, "NullTour\n");            		                    	
                   	continue;
                }                
            }
        }
        catch(Exception ex) {
        	bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
        }
    }

}

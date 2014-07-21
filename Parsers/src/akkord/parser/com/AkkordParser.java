package akkord.parser.com;

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

public class AkkordParser {
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.akkord-tour.com.ua/choose-me.php";
    private static final int    source = 20;
    
    public AkkordParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).get();
            Elements tables = tourDoc.select("tr[rel_tour = tour_tr");
            for (Element x: tables) {
            	
            	String linkStr = x.select("td").get(1).select("div").get(1).select("a").attr("href");
                
            	Elements countries = x.select("td").first().select("div").select("img");
            	String countryStr = "";
            	
            	for (Element country: countries) {
            		countryStr += country.attr("title").toUpperCase();
            		countryStr += " - ";
            	}
            	countryStr.substring(0, countryStr.length() - 3);
            		
            	String roomTypeStr = "";
                
            	String dateStr = x.attr("rel_tour_date");
            	    
            	String hotelStr = "";
            	            	
            	String townStr = x.select("td").get(1).select("div").get(4).text().trim().toUpperCase();
            	if (townStr.isEmpty())
            		townStr = x.select("td").get(1).select("div").get(3).text().trim().toUpperCase();
            	
            	String departCityStr = "";
            	if (!townStr.isEmpty())
            		departCityStr = townStr.substring(0, townStr.indexOf(" ")).trim();
            	    
            	String nutritionStr = "";
            	    
                String durationStr = x.attr("rel_day");
                    
                String priceStr = x.select("span[class = currency_price_span]").select("span[rel = UAH]").get(1).ownText().trim();
                
                String previousPriceStr = x.select("span[class = currency_price_span]").select("span[rel = UAH]").first().ownText().trim();
                
                try {
                	Document doc = Jsoup.connect(linkStr).timeout(100000).get();
                	Elements hotels = doc.select("a:contains(Отель)").select("b");
                	if (hotels.size() == 1) {
                		hotelStr = hotels.text().trim().toUpperCase().replace('\'','"');
                	}
                	else
                		hotelStr = "";
                }
                catch (Exception ex) {
                	
                }
                
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
                        	if (src.contains("*"))
                        		res = src.substring(0, src.indexOf("*") - 1).trim();  
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
                            return new Date(Integer.parseInt(src.substring(0, 4)) - 1900, Integer.parseInt(src.substring(5, 7)) - 1, Integer.parseInt(src.substring(8)));
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
                        	String res = "" + src.trim();
                            return Integer.parseInt(res);
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
                        try {
                            String res = src.replace(" ", "");
                            int money = Integer.parseInt(res);
                            return money;
                        }
                        catch (IndexOutOfBoundsException ex) {
                            return null;
                        }
                        catch (NumberFormatException ex) {
                            return null;
                        }
                    }
                }, previousPriceStr, new Parsable() {

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
                }, hotelStr, new Parsable() {

                    @Override
                    public Object get(String src) {
                        return "" + src;
                    }
                }, linkStr, new Parsable() {

                    @Override
                    public Object get(String src) {
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
                }, roomTypeStr, source, countryStand, cityStand, bananLog, "Akkord: ");
                    
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
        	
        	if(ex.getMessage() != null){
				
				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
			}
			else{
				
				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
			}
        }
    }


}

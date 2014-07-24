package silver.parser.com;

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

public class SilverParser {
    public ArrayList <TourObject> tours;
    private static final String website = "http://silver-tour.com.ua/tour-topic/econom/";
    private static final int    source = 22;
    
    public SilverParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).get();
            Elements tables = tourDoc.select("div[class = tour-vertical");
            for (Element x: tables) {
            	
            	String linkStr = "http://silver-tour.com.ua" + x.select("a").first().attr("href");
                                
                try {
                	Document doc = Jsoup.connect(linkStr).timeout(100000).get();
                	
                	Elements infos = doc.select("table[class = tour-info]").first().select("tr");
                	
                	String countryStr = infos.get(2).select("td").get(1).text().trim().toUpperCase();
                	
                	String townStr = infos.get(3).select("td").get(1).text().trim().toUpperCase();
                	
                	String departCityStr = infos.get(4).select("td").get(1).text().trim().toUpperCase();
            	    
                	String roomTypeStr = "";
                    
                    String durationStr = infos.get(7).select("td").get(1).text().trim().toUpperCase();
                    
                	String nutritionStr = infos.get(8).select("td").get(1).text().trim().toUpperCase();
            	    
                	String dateStr = infos.get(12).select("td[class = dates]").select("tr").first().text().trim().toLowerCase();
                	    
                	String hotelStr = "";
                	            	
                    String priceStr = infos.get(9).select("td").get(1).text().trim();
                    
                    String previousPriceStr = "";

                    String description = doc.select("div[class = txt]").text();
                    
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
                    			int month = DateEdit.getMonth(src.toLowerCase());
                    			if (-1 == month)
                    				return new Date(Integer.parseInt(src.substring(6)) - 1900, Integer.parseInt(src.substring(3, 5)) - 1, Integer.parseInt(src.substring(0, 2)));
                    			int firstSpace = src.indexOf(" ");
                    			int year = Integer.parseInt(src.substring(firstSpace + 1, firstSpace + 5));
                    			
                    			int date = 0;
                    			if (src.contains(",")) {
                    				int coma = src.indexOf(",");
                    				date = Integer.parseInt(src.substring(coma - 2, coma));
                    			}
                    			else
                    				date = Integer.parseInt(src.substring(src.length() - 2));
                    			return new Date(year - 1900, month, date);
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
                    			String res = src.substring(0, src.indexOf(" "));
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
                    			String res = src.substring(0, src.indexOf(" "));
                    			res= res.replace(",", "");
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
                    		return null;
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
                    		return "" + src;
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
                    }, roomTypeStr, description, source, countryStand, cityStand, bananLog, "Silver: ");
                    
                    if (localTour != null) {
                    	tours.add(localTour);
                    }
                    else {
                    	bananLog.write(null, "NullTour\n");            		                    	
                    	continue;
                    }
                }
                catch (Exception ex) {
                	if(ex.getMessage() != null){
        				
        				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
        			}
        			else{
        				
        				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
        			}                	
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
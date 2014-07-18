package orion.parser.com;

import java.util.ArrayList;
import java.util.Date;

import main.parser.com.TourObject;
import money.currency.Currency;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import rita.blowup.com.DateEdit;
import rita.blowup.com.Parsable;
import rita.blowup.com.UniversalParser;
import term.filter.parser.TermFilter;
import banan.file.writer.BananFileWriter;

public class OrionParser {
    public ArrayList <TourObject> tours;
    private static final String website = "http://orion-intour.com/topic_tours/hottur/";
    private static final int    source = 21;
    
    public OrionParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).get();
            Elements tables = tourDoc.select("table[class = tour-list").select("tr");
            int k = 0;
            for (Element x: tables) {
            	if (k == 0)
            	{
            		k++;
            		continue;
            	}
            	
            	String linkStr = "http://orion-intour.com" + x.select("td[class = name]").select("a").attr("href");
                
            	String countryStr = x.select("td[class = name]").first().ownText().toUpperCase();
            	
            	String dateStr = x.select("td[class = dt]").first().ownText();
            	
               	String nutritionStr = x.select("td[class = food]").text().toUpperCase();

            	if (dateStr == null || dateStr.equals("") || dateStr.contains("ЛЮБЫЕ"))
            		continue;
            	
                try {
                	Document doc = Jsoup.connect(linkStr).timeout(100000).get();
                	
                	String hotelStr = doc.select("div[class = text-hider]").text();
                	
                	String roomTypeStr = "";
                	               	
                	String townStr = doc.select("table[class = tour-params]").select("tr").get(1).select("h2").text().trim().toUpperCase();
                	
                	String departCityStr = doc.select("table[class = tour-params]").select("tr").get(2).select("td").get(1).text().trim().toUpperCase();
                	                    	    
                    String durationStr = doc.select("table[class = tour-params]").select("tr").get(4).select("td").get(1).text();
                        
                    String priceStr = doc.select("span[class = price").text().trim();
                    
                    String previousPriceStr = "";
                
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
                    		return null;
                    	}
                    }, hotelStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		try {
                    			return new Date(DateEdit.getCurrentYear() - 1900, Integer.parseInt(src.substring(3, 5)) - 1, Integer.parseInt(src.substring(0,2)));
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
                    		if (src == null || src.contains("ВАШ ГОРОД") || src.equals(""))
                    			return null;
                    		return "" + src;
                    	}
                    }, departCityStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		try {
                    			String res = "" + src.trim();
                    			res = res.substring(0, res.indexOf(" "));
                    			return Integer.parseInt(res);
                    		}
                    		catch (Exception ex) {
                    			return 0;
                    		}
                    	}
                    }, durationStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		try {
                    			String res = src.substring(0, src.indexOf(" "));
                    			int money = Integer.parseInt(res);
                    			Currency cur = new Currency();
                    			if (src.contains("$"))
                    				return (int) (money * cur.dollar);
                    			if (src.contains("€"))
                    				return (int) (money * cur.euro);
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
                    		if (src.contains("1"))
                    			return 1;
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
                    }, hotelStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		return "" + src;
                    	}
                    }, linkStr, new Parsable() {

                    	@Override
                    	public Object get(String src) {
                    		String res = src.toUpperCase();
                    		res = res.replace("ЗАВТРАКИ + УЖИНЫ", "HB");
                    		res = res.replace("ЗАВТРАКИ", "BB");
                    		if (res.contains("/"))
                    			res = res.substring(0, res.indexOf("/"));
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
                    }, roomTypeStr, source, countryStand, cityStand, bananLog, "Orion: ");
                    
                    if (localTour != null) {
                    	tours.add(localTour);
                    }
                    else {
                    	bananLog.write(null, "NullTour\n");            		                    	
                    	continue;
                    }                
                }
                
                catch (Exception ex) {
            	
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

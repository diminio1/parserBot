package apltravel.parser.com;

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

public class ApltravelParser {
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.apltravel.ua";
    private static final int    source = 17;
    
    public ApltravelParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).get();
            
            Elements countries = tourDoc.select("a[class = directionName]");

            for (Element country: countries) {
            	
            	String page = country.attr("href");
            	Document countryDoc = null;
            	int k = 0;
            	do {
            		k++;
            		if (7 == k)
            			break;
            		try {
            			countryDoc = Jsoup.connect(page).ignoreHttpErrors(true).timeout(100000).get();
            			Elements tables = countryDoc.select("div[class = topHotelsBlock");
            			
            			for (Element x: tables) {
            				
            				String linkStr = x.select("div[class = topHotelsPhoto").select("a").attr("href");
            				String countryStr = x.select("div[class = hotelLocation]").select("a").first().ownText().trim().toUpperCase();
            				String townStr = "";
            				try {
            					townStr = x.select("div[class = hotelLocation]").select("a").get(1).ownText().trim().toUpperCase();
            				}
            				catch(Exception ex) {
            					townStr = null;
            				}
            				String roomTypeStr = "";
            				Elements info = x.select("span[class = priceInfo]");
            				String dateStr = info.select("strong").text().substring(info.select("strong").text().indexOf(" ") + 1).trim();
            				String departCityStr = "" + dateStr.trim().toUpperCase();
            				String hotelStr = x.select("h2[itemprop = name]").text().trim().toUpperCase().replace('\'', '"');
            				String nutritionStr = info.text().trim().toUpperCase();                	    
            				String durationStr = info.text().substring(info.text().indexOf("ноч") - 3);
            				String stars = x.select("div[class = stars]").attr("style"); 
            	    
            				int persons = 1;
            				if (info.text().contains("Тур на 2"))
            					persons = 2;
            				String priceStr = x.select("span[itemprop = price]").text().replace(" ", "");
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
            						String res = src.substring(src.lastIndexOf(" "));
            						if (res.contains("КИЕВА"))
            							res = "КИЕВ";
            						if (res.contains("ДОНЕЦКА"))
        								res = "ДОНЕЦК";
            						if (res.contains("ХАРЬКОВА"))
        								res = "ХАРЬКОВ";
            						return res.trim();
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
            							Currency cur = new Currency();
            							return (int)(money * cur.dollar);
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
            						if (src.contains("42"))
            							return 3;
            						if (src.contains("56"))
            							return 4;
            						if (src.contains("70"))
            							return 5;
            						if (src.contains("28"))
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
            						if (src.contains("УЛЬТРА ВСЁ ВКЛЮЧЕНО"))
            							return "UAI";
            						if (src.contains("ВСЁ ВКЛЮЧЕНО"))
            							return "AI";
            						if (src.contains("ПОЛУПАНСИОН"))
            							return "HB";
               						if (src.contains("ПАНСИОН"))
            							return "FB";
               						if (src.contains("ЗАВТРАК"))
            							return "BB";
               						if (src.contains("БЕЗ ПИТАНИЯ"))
            							return "RO";
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
            				}, roomTypeStr, source, countryStand, cityStand, bananLog, "Apltravel: ");
                    
            				if (localTour != null) {
            					localTour.price = localTour.price / persons; 
            					tours.add(localTour);
            				}
            				else {
            					bananLog.write(null, "NullTour\n");            		                    	
            					continue;
            				}                
            			}
            			page = "http://www.apltravel.ua" + countryDoc.select("a[class = next]").attr("href");
            		}
            		catch(Exception ex) {
                    	if (ex.getMessage() != null){
            				
            				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
            			}
            			else{
            				
            				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
            			}            			
            			break;
            		}
            	}
            	while (!(page.equals("http://www.apltravel.ua")));
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

package tur777.parser.com;

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

public class Tur777Parser {
    public ArrayList <TourObject> tours;
    private static final String website = "http://xn-----flcbkcrfr7aphd1admd3hyb7d.777tur.com/";
    private static final int    source = 19;
    
    public Tur777Parser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).get();
            
            Elements countries = tourDoc.select("table[style = margin-top: 0px;]").select("tbody").select("tr").select("td").select("a");

            for (Element country: countries) {
            	
            	String page = "http://xn-----flcbkcrfr7aphd1admd3hyb7d.777tur.com" + country.attr("href");
            	Document countryDoc = null;
            	try {
            		countryDoc = Jsoup.connect(page).timeout(100000).ignoreHttpErrors(true).get();
            		
            		Elements tables = countryDoc.select("table[class = box_table");
            		
            		Elements headers = countryDoc.select("table[class = box_heder2");
            		
            		for (int i = 0; i < tables.size(); ++i) {

            			String countryStr = headers.get(i).select("tbody").select("tr").select("td").text().trim().toUpperCase();
            			//System.out.println("Country: " + countryStr);
            			
            			String hotelStr = "" + countryStr.replace('\'', '"');
            			
            			String townStr = "" + countryStr;
            			
            			String starsStr = "" + countryStr;
            			
            			String nutritionStr = tables.get(i).select("ul").text().toUpperCase();
            			//System.out.println("Nutrition: " + nutritionStr);
            			
            			String departDateStr = tables.get(i).select("tbody").select("tr").select("td").get(2).select("strong").text();
            		//	System.out.println("Depart Date: " + departDateStr);
            			
            			String durationStr = tables.get(i).select("tbody").select("tr").select("td").get(3).select("span").first().ownText();
            		//	System.out.println("Duration: " + durationStr);
            			
            			String priceStr = tables.get(i).select("tbody").select("tr").select("td").get(3).select("span").get(1).ownText();
            		//	System.out.println("Price: " + priceStr);
            			
            			String linkStr = tables.get(i).select("tbody").select("tr").get(1).select("td").get(1).select("a").attr("href");
            		//	System.out.println("Link: " + linkStr);
            			
            			String roomTypeStr = "";
           				
            			String departCityStr = "";
            			
            			TourObject localTour = UniversalParser.parseTour(new Parsable() {
            				@Override
            				public Object get(String src) {
            					try {
            						String res = src.substring(src.indexOf(',') + 1, src.lastIndexOf(',')).trim();
            						return res.replace("ШРИ ЛАНКА", "ШРИ-ЛАНКА");
            					}
            					catch(Exception ex) {
            						return null;
            					}
            				}
            			}, countryStr, new Parsable() {

           					@Override
           					public Object get(String src) {
           						try {
           							String res = src.substring(src.lastIndexOf(',') + 1).trim();
           							return res;
           						}
           						catch(Exception ex) {
           							return null;
           						}
           					}
           				}, townStr, new Parsable() {

           					@Override
           					public Object get(String src) {
           						try {
           							String res = src.substring(0, src.indexOf(',')).trim();
           							res = res.substring(0, res.lastIndexOf(" "));
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
           							return new Date(DateEdit.getCurrentYear() - 1900, Integer.parseInt(src.substring(3, 5)) - 1, Integer.parseInt(src.substring(0, 2)));
           						}
           						catch (IndexOutOfBoundsException ex) {
           							return new Date(0, 0, 1);
           						}
           						catch (NumberFormatException ex) {
           							return new Date(0, 0, 1);
           						}
           					}
           				}, departDateStr, new Parsable() {

           					@Override
           					public Object get(String src) {
           						if (src == null || src.equals(""))
           							return null;
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
           							res = res.substring(0, res.length() - 1);
           							int money = Integer.parseInt(res);
           							Currency cur = new Currency();
           							if (src.contains("$"))
           								return (int)(money * cur.dollar);
          							if (src.contains("€"))
           								return (int)(money * cur.euro);
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
           				}, starsStr, new Parsable() {

           					@Override
           					public Object get(String src) {
           						String res = "";
           						if (src.charAt(0) == '/')
           							res = "http://xn-----flcbkcrfr7aphd1admd3hyb7d.777tur.com"; 
           						return res + src;
           					}
           				}, linkStr, new Parsable() {

           					@Override
           					public Object get(String src) {
           						if (src.contains("УЛЬТРА ВСЕ ВКЛЮЧЕНО"))
           							return "UAI";
           						if (src.contains("ВСЕ ВКЛЮЧЕНО"))
           							return "AI";
           						if (src.contains("ЗАВТРАК, УЖИН"))
           							return "HB";
           						if (src.contains("ТРЕХРАЗОВОЕ ПИТАНИЕ"))
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
           				}, roomTypeStr, source, countryStand, cityStand, bananLog, "Tur777: ");
                    
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
           			break;
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

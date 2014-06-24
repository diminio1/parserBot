package hottoursin.parser.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import main.parser.com.TourObject;
import money.currency.Currency;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import banan.file.writer.BananFileWriter;
import rita.blowup.com.DateEdit;
import term.filter.parser.TermFilter;

public class HottoursInParser {

	public ArrayList <TourObject> tours;
	private static final String website = "http://hottours.in.ua/hot_tours/";
	private static final int source = 15;
	    
	public HottoursInParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
	    tours = new ArrayList<TourObject>();
	    Document tourDoc = null;
	    try {
	        tourDoc = Jsoup.connect(website).timeout(100000).get();
	        Elements tourBlocks = tourDoc.select("li[class = product]");
	        for (Element x: tourBlocks) {
	        	try {
	        		String url = "hottours.in.ua/" + x.select("a").attr("href");
	        		String country = x.select("h3").select("a").text().toUpperCase();
	        		String info = x.select("p").text();
	        		String test = "";
	        		for (int i = 0; i < info.length() - 4; ++i) {
	        			test = info.substring(i, i + 5);
	        			if (Pattern.matches("[0-9][0-9].[0-9][0-9]", test)) {
	        				if (test.charAt(2) != '.')
	        					continue;
	        				break;
	        			}
	        		}
	        		if (!(Pattern.matches("[0-9][0-9].[0-9][0-9]", test))) {
	        			for (int i = 0; i < country.length() - 4; ++i) {
	        				test = country.substring(i, i + 5);
	        				if (Pattern.matches("[0-9][0-9].[0-9][0-9]", test)) {
	        					if (test.charAt(2) != '.')
	        						continue;
	        					break;
	        				}
	        			}
	        		}
	                
	        		if (!(Pattern.matches("[0-9][0-9].[0-9][0-9]", test)))
	        			continue;
	                
	        		String price = x.select("div[class = price]").select("span").text().replace(" ", "");
	        		price = price.substring(0, price.indexOf(","));
	        		Currency c = new Currency();
	        		int myPrice = (int) (Integer.parseInt(price) * c.dollar); 
	        		TourObject tObj = new TourObject();
	             
	        		tObj.departDate = new Date(DateEdit.getCurrentYear() - 1900, Integer.parseInt(test.substring(3, 5)) - 1, Integer.parseInt(test.substring(0, 2)));
	                
	        		if (DateEdit.before(tObj.departDate, new Date()))
	        			continue;
	            
	        		tObj.setCountry(country, countryStand, bananLog);
	        		tObj.setTown(country, cityStand, "Hottours.in.ua: ", bananLog);
	        		tObj.setLink(url);
	        		tObj.setPrice(myPrice);
	        		tObj.setSource(source);
	               
	        		tours.add(tObj);
	            }
		        catch(NullPointerException ex) {
		            bananLog.write(null, "Caught NullPointerException!\n");
		        }
		        catch(Exception ex) {
		            bananLog.write(null, "Caught Exception!\n");
		        	if(ex.getMessage() != null){
						
						bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
					}
					else{
						
						bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
					}
		        }

	        }
	    }
	    catch(IOException ex) {
            bananLog.write(null, "Caught IOException!\n");
	    }
	    catch(NullPointerException ex) {
            bananLog.write(null, "Caught NullPointerException!\n");
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
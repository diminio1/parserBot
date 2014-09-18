package kazkamandriv.parser.com;

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

public class KazkamandrivParser {
    public ArrayList<TourObject> tours;
    private static final String website = "http://kazkamandriv.ua/";
    private static final int    source = 18;
    
    public KazkamandrivParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).ignoreHttpErrors(true).get();
        
            Elements excursions = tourDoc.select("tr[onclick *= urlOpen('http://kazkamandriv.ua/coach-tour-to-europe]");
            
            for (Element x: excursions) {
            	try {
            		
            		String linkStr = x.attr("onclick");
            		linkStr = linkStr.substring(9, linkStr.length() - 3);
            		if (linkStr ==  null || linkStr.equals(""))
            			continue;
            	
            		String dateStr = x.select("td").first().text();
            		Date date = new Date(DateEdit.getCurrentYear() - 1900, Integer.parseInt(dateStr.substring(3, 5)) - 1, Integer.parseInt(dateStr.substring(0, 2)));
            		if (date == null || DateEdit.before(date, new Date()))
            			continue;
            		
            		Elements countries = x.select("a[class = tRoute]").select("img"); 
            		String countryStr = "";
            		for (Element c: countries) {
            			countryStr = countryStr + c.attr("title") + "-";
            		}
            		countryStr = countryStr.replace('—', '-').trim().toUpperCase();
            		if (countryStr == null || countryStr.equals(""))
            			continue;
            		if (countryStr.length() > 60) {
            			countryStr = countryStr.substring(0, 60);
            			countryStr = countryStr.substring(0, countryStr.lastIndexOf("-"));
            		}
            			
            		String townStr = x.select("a[class = tRoute]").text().replace('—', '-').replace('–', '-').trim().toUpperCase();
            		if (townStr.length() > 60) {
            			townStr = townStr.substring(0, 60);
            			townStr = townStr.substring(0, townStr.lastIndexOf("-"));
            		}
            		
            		String departCityStr = townStr.substring(0, townStr.indexOf("-")).trim();
            	
            		String durationStr = x.select("td").get(2).select("a").text();
            		int duration = Integer.parseInt(durationStr.substring(0, durationStr.indexOf(" ")));
            	
            		String priceStr = x.select("td").get(4).select("strong").text();
            		String previousPriceStr = x.select("td").get(4).select("s").text();
            		int price;
            		try {
            			price = Integer.parseInt(priceStr.substring(0, priceStr.indexOf(" ")));
            		}
            		catch(Exception ex) {
            			continue;
            		}
            		Integer previousPrice;
            		try {
            			previousPrice = Integer.parseInt(priceStr.substring(0, priceStr.indexOf(" ")));
            		}
            		catch(Exception ex) {
            			previousPrice = null;
            		}
            		TourObject localTour = new TourObject();
            		
            		localTour.setCountry(countryStr, countryStand, bananLog);
            		localTour.setDepartCity(departCityStr);
            		localTour.departDate = date;
            		localTour.setDuration(duration);
            		localTour.setLink(linkStr);
            		localTour.setPrice(price);
            		localTour.setPreviousPrice(previousPrice);
            		localTour.setSource(source);
            		localTour.setTown(townStr, cityStand, "Kazkamandriv: ", bananLog);
            		
            		tours.add(localTour);
            	}
            	catch (Exception ex) {
            		continue;
            	}
            }
            
            Elements tables = tourDoc.select("ul[class = toursAction]").select("li");
            
            for (Element x: tables) {
            	
            	String linkStr = website;
            	
                String countryStr = x.select("p[class = iTitle]").select("strong").text().trim().toUpperCase(); 
            		
            	String roomTypeStr = x.select("p[class = iType]").text();
            	
            	String dateStr = x.select("p[class = iDate]").select("strong").text();
            	
            	String hotelStr = x.select("p[class = iTitle]").first().ownText().replace('\'', '"').trim().toUpperCase();
            	
            	String stars = "" + hotelStr; 
            	
            	String townStr = "" + hotelStr;
            		
            	String departCityStr = "" + roomTypeStr;
            	    
            	String nutritionStr = "" + roomTypeStr;
            	
                String durationStr = x.select("p[class = iDate]").first().ownText();
                
                String priceStr = x.select("p[class = iPrice]").select("strong").text();
                
                String previousPriceStr = x.select("p[class = iPrice]").select("strike").text();
                
                TourObject localTour = UniversalParser.parseTour(new Parsable() {
                	@Override
                	public Object get(String src) {
                        return "" + src;
                    }
                }, countryStr, new Parsable() {

                	@Override
                	public Object get(String src) {
                		try {
                			String res = src.substring(src.indexOf(',') + 1).trim();
                			res = res.substring(0, res.indexOf(','));
                			return "" + res;
                		}
                		catch (Exception ex) {
                			return null;
                		}
                }
                    }, townStr, new Parsable() {

                    @Override
                    public Object get(String src) {
                        try {
                        	if (src.equals(""))
                        		return null;
                			String res = src.substring(src.indexOf(',') + 1);
                			res = res.substring(res.indexOf(',') + 1).trim();
                			res = res.substring(0, res.indexOf(','));
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
                            return new Date(Integer.parseInt(src.substring(6)) + 100, Integer.parseInt(src.substring(3, 5)) - 1, Integer.parseInt(src.substring(0, 2)));
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
                    	String res = src.substring(src.indexOf('(') + 1, src.indexOf(')'));
                        return res.trim().toUpperCase();
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
                            String res = src.substring(0, src.indexOf(' '));
                            Currency c = new Currency();
                            int money = (int) (Integer.parseInt(res) * c.euro);
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
                            String res = src.substring(0, src.indexOf(' '));
                            Currency c = new Currency();
                            int money = (int) (Integer.parseInt(res) * c.euro);
                            return money;
                        }
                        catch (IndexOutOfBoundsException ex) {
                            return 0;
                        }
                        catch (NumberFormatException ex) {
                            return 0;
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
                }, stars, new Parsable() {

                    @Override
                    public Object get(String src) {
                        return "" + src;
                    }
                }, linkStr, new Parsable() {

                    @Override
                    public Object get(String src) {
                    	if (src.contains("RO") || src.contains("BO") || src.contains("OB"))
                    		return "RO";
                        if (src.contains("UALL"))
                         	return "UAI";
                        if (src.contains("ALL"))
                         	return "AI";
                        if (src.contains("HB"))
                          	return "HB";
                        if (src.contains("BB"))
                          	return "BB";
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
                }, roomTypeStr, null, source, countryStand, cityStand, bananLog, "Kazkamandriv: ");
                    
                if (localTour != null) {
                  	localTour.price = localTour.price; 
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
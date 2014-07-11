package otpusk.parser.com;

import term.filter.parser.TermFilter;
import banan.file.writer.BananFileWriter;

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

import rita.blowup.com.DateEdit;
import rita.blowup.com.Parsable;
import rita.blowup.com.UniversalParser;

public class OtpuskParser {
	
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.otpusk.com";
    private static final int source = 9;
    
    public OtpuskParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document otpuskDoc = null;
        try {
            otpuskDoc = Jsoup.connect(website).timeout(100000).get();
            Elements tourBlocks = otpuskDoc.select("div[class = tour row-fluid]");
            for (Element x: tourBlocks) {
                try {
                    String country = "";
                    try {
                        country = x.select("span[class = t-locale st-i]").text().trim().toUpperCase();
                    }
                    catch (Exception ex) {
                        country = "";
                    }
                    String town = "";
                    if (!country.equals(""))
                        town = "" + country;
                    
                    String persons = x.select("div[class = span4]").first().ownText();
                    
                    String nutrition = x.select("span[class = st-b]").first().text();
                    String duration = x.select("span[class = st-b]").get(1).text();
                    String date = x.select("div[class = fl]").first().ownText();
                    String price = x.select("a[class = hottour-price]").text();
                    String link = website + x.select("a[class = hottour-title]").attr("href");
                    
                    Document tourDoc = Jsoup.connect(link).timeout(100000).get();
                    
                    if (country.equals("")) {
                        country = tourDoc.select("div[class = tour-header-route]").select("span[class = t-locale]").text().trim().toUpperCase();
                        town = "" + country;
                    }
                    
                    if (country.equals(""))
                        throw new NullPointerException("There is no country mentioned in this tour");
                    
                    String hotel = "";
                    try {
                        hotel = tourDoc.select("h2[class = nomg]").first().ownText();
                    }
                    catch (Exception ex) {
                        hotel = "";
                    }

                    String roomType = "";
                    try {
                        roomType = tourDoc.select("div[class = col-2 type-room]").select("b").get(1).ownText();
                    }
                    catch (Exception ex) {
                        roomType = "";
                    }
                    
                    TourObject tObj = UniversalParser.parseTour(new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src;
                        }
                    }, country, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src;
                        }
                    }, town, new Parsable() {

                        @Override
                        public Object get(String src) {
                            try {
                            	if (src.equals(""))
                            		return null;
                                return src.substring(0, src.lastIndexOf(" ")).trim().toUpperCase();
                            }
                            catch(Exception ex) {
                                return null;
                            }
                        }
                    }, hotel, new Parsable() {

                        @Override
                        public Object get(String src) {
                            try {
                                String res = src.substring(0, src.indexOf(" "));
                                int month = DateEdit.getMonth(src);
                                return new Date(DateEdit.getCurrentYear() - 1900, month, Integer.parseInt(res));
                            }
                            catch(Exception ex) {
                                return new Date(0,0,1);
                            }
                        }
                    }, date, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "";
                        }
                    }, "", new Parsable() {

                        @Override
                        public Object get(String src) {
                            try {
                                return Integer.parseInt(src.substring(0, src.indexOf(" ")));
                            }
                            catch (NumberFormatException ex) {
                                return 0;
                            }
                        }
                    }, duration, new Parsable() {

                        @Override
                        public Object get(String src) {
                            try {
                                String res = src.substring(0, src.length() - 4);
                                res = res.replace("\u2009", "").replace("\u00a0", "").trim();
                                return (Integer.parseInt(res));
                            }
                            catch (Exception ex) {
                                return 0;
                            }
                        }
                    }, price, new Parsable() {

                        @Override
                        public Object get(String src) {
                        	return null;
                        }
                    }, price, new Parsable() {

                        @Override
                        public Object get(String src) {
                            if (src.contains("3"))
                                return 3;
                            if (src.contains("2"))
                                return 2;
                            if (src.contains("4"))
                                return 4;
                            if (src.contains("5"))
                                return 5;
                            return 0;
                        }
                    }, hotel, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src;
                        }
                    }, link, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return src.replace("OB", "RO");
                        }
                    }, nutrition, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src;
                        }
                    }, roomType, source, countryStand, cityStand, bananLog, "Otpusk: "
                            );
                    if (tObj != null) {
                        if (persons.contains("2"))
                            tObj.setPrice(tObj.price / 2);
                        tours.add(tObj);
                    }
                    
                }
                catch(Exception ex) {
                	if(ex.getMessage() != null){
        				
        				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
        			}
        			else{
        				
        				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
        			}
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

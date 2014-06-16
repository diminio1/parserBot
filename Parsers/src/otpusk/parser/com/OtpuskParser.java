package otpusk.parser.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import main.parser.com.TourObject;

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
                    String country = x.select("span[class = t-locale st-i]").select("a").first().ownText().trim().toUpperCase();
                    
                    if (country.equals(""))
                        throw new NullPointerException("There is no country mentioned in this tour");
            
                    String town = x.select("span[class = t-locale st-i]").select("a").get(1).ownText().trim().toUpperCase();
                    String nutrition = x.select("span[class = st-b]").first().text();
                    String duration = x.select("span[class = st-b]").get(1).text();
                    String date = x.select("div[class = fl]").first().ownText();
                    String price = x.select("a[class = hottour-price]").text();
                    String link = website + x.select("a[class = hottour-title]").attr("href");
                    
                    Document tourDoc = Jsoup.connect(link).timeout(100000).get();
                    
                    String hotel = tourDoc.select("h2[class = nomg]").first().ownText();
                    String roomType = tourDoc.select("div[class = col-2 type-room]").select("b").get(1).ownText();
                  
                    //System.out.println(nutrition);
                    
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
                            return src.substring(0, src.lastIndexOf(" ")).trim().toUpperCase();
                        }
                    }, hotel, new Parsable() {

                        @Override
                        public Object get(String src) {
                            String res = src.substring(0, src.indexOf(" "));
                            int month = DateEdit.getMonth(src);
                            return new Date(DateEdit.getCurrentYear() - 1900, month, Integer.parseInt(res));
                        }
                    }, date, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src;
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
                            String res = src.substring(0, src.length() - 4);
                            res = res.replace("\u2009", "").replace("\u00a0", "").trim();
                            return (Integer.parseInt(res) / 2);
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
                    if (tObj != null)
                        tours.add(tObj);
                }
                catch(Exception ex) {
                    continue;
                }
            }
        }
        catch(Exception ex) {
           	bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
        }
    }

}

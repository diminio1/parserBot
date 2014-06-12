/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mansana.parser.com;

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

/**
 *
 * @author Маргарита
 */
public class MansanaParser {
    
    public ArrayList <TourObject> tours;
    private static final String website = "http://www.mansana.com/hot_propositions.html";
    private static final int    source = 13;

    public MansanaParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).get();
            Elements tables = tourDoc.select("table[width = 99%]");
            for (Element x: tables) {
                String countryStr = x.select("td").first().select("b").text().trim().toUpperCase(); 
                
                Elements toursElems = x.select("table[width = 100%]").select("tr");  
                toursElems.remove(toursElems.size() - 1);
                
                String roomTypeStr = toursElems.last().text();
                
                toursElems.remove(toursElems.size() - 1);
                
                String firstTownStr = x.select("table[width = 100%]").select("tr").select("td[valign = top]").first().text();
                
                for (Element y: toursElems) {
                    
                    String townStr = y.select("td[valign = top]").text();
                    if (townStr.equals("")) { 
                        townStr = firstTownStr;
                    }
                    firstTownStr = townStr;
                    
                    String date = y.select("td[class = lightgrey]").get(2).text();
                    if (date.contains("май") || date.contains("в мае") || date.contains("начало мая") || date.contains("конец мая") ||
                        date.contains("июнь") || date.contains("в июне") || date.contains("начало июня") || date.contains("конец июня") ||
                        date.contains("июль") || date.contains("в июле") || date.contains("начало июля") || date.contains("конец июля") ||
                        date.contains("август") || date.contains("в августе") || date.contains("начало августа") || date.contains("конец августа") ||
                        date.contains("сентябрь") || date.contains("в сентябре") || date.contains("начало сентября") || date.contains("конец сентября") ||
                        date.contains("октябрь") || date.contains("в октябре") || date.contains("начало октября") || date.contains("конец октября") ||
                        date.contains("ноябрь") || date.contains("в ноябре") || date.contains("начало ноября") || date.contains("конец ноября") ||
                        date.contains("декабрь") || date.contains("в декабре") || date.contains("начало декабря") || date.contains("конец декабря") ||
                        date.contains("январь") || date.contains("в январе") || date.contains("начало января") || date.contains("конец января") ||
                        date.contains("февраль") || date.contains("в феврале") || date.contains("начало февраля") || date.contains("конец февраля") ||
                        date.contains("март") || date.contains("в марте") || date.contains("начало марта") || date.contains("конец марта") ||
                        date.contains("апрель") || date.contains("в апреле") || date.contains("начало апреля") || date.contains("конец апреля") || 
                        date.contains("лето") || date.contains("осень") || date.contains("зима") || date.contains("весна"))
                        continue;
                    
                    String hotelStr = "";
                    String linkStr = website;
                    
                    Elements idiotism = y.select("td[colspan = 2]");
                    if (idiotism.size() != 0) {
                        townStr = "";
                        hotelStr = "";
                    }
                    else {
                        hotelStr = y.select("td[width = 30%]").text();
                        linkStr = y.select("td[width = 30%]").select("a").attr("href");
                    }
                    
                    String nutritionStr = y.select("td[class = lightgrey]").first().text();
                    String durationStr = y.select("td[class = nowrap]").text();
                    String priceStr = y.select("span[style *= color: red]").text();
                    
                    TourObject localTour = UniversalParser.parseTour(new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src;
                        }
                    }, countryStr, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src.trim().toUpperCase();
                        }
                    }, townStr, new Parsable() {

                        @Override
                        public Object get(String src) {
                            try {
                                String res = "" + src.substring(0, src.indexOf(",")).trim().toUpperCase().replace('\'', '"'); 
                                return res;
                            }
                            catch(Exception ex) {
                                return "";
                            }
                        }
                    }, hotelStr, new Parsable() {

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
                                int year = DateEdit.getCurrentYear() - 1900;
                                int day = Integer.parseInt(res);
                                int month = DateEdit.getMonth(src);
                                if (-1 == month)
                                    month = Integer.parseInt(src.substring(3,5)) - 1;
                                
                                return new Date(year, month, day);
                            }
                            catch (IndexOutOfBoundsException ex) {
                                return new Date();
                            }
                            catch (NumberFormatException ex) {
                                return new Date();
                            }
                        }
                    }, date, new Parsable() {

                        @Override
                        public Object get(String src) {
                            return "" + src;
                        }
                    }, "КИЕВ", new Parsable() {

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
                                Currency cur = new Currency();
                                int money = Integer.parseInt(src.substring(0, src.length() - 1));
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
                            if (res.contains("DBL") || res.contains("DOUBLE"))
                                return "DBL";
                            if (res.contains("BG"))
                                return "BGL";
                            if (res.contains("SGL"))
                                return "SGL";
                            if (res.contains("TRPL"))
                                return "TRPL";
                            if (res.contains("QDPL"))
                                return "QDPL";
                            return "";                    
                        }
                    }, roomTypeStr, source, countryStand, cityStand, bananLog, "Mansana: ");
                    
                    if (localTour != null){
                    
                    	tours.add(localTour); 
                    }
                    else{
                    	continue;
                    }
                }
            }
        }
        catch(IOException ex) {
            //System.out.println("IOException");
        }
        catch(NullPointerException ex) {
            //System.out.println("NullPointerException");            
        }
        catch(Exception ex) {
            //System.out.println("Exception");  
        	bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
        }
    }
}

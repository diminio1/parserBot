/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package candytour.parser.com;

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

/**
 *
 * @author Маргарита
 */
public class CandytourParser {
    
    public ArrayList <TourObject> tours;
    private static final String website = "http://candytour.com.ua/cgi-bin/myAccount/myAccount.cgi?action=dp&vs=2/scId=6&p=hottour&showAll=1";
    private static final int    source  = 14;
    
    public CandytourParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
        tours = new ArrayList<TourObject>();
        Document tourDoc = null;
        try {
            tourDoc = Jsoup.connect(website).timeout(100000).get();
            Elements tourBlocks = tourDoc.select("div[class = news]");
            for (Element x: tourBlocks) {
                String destination = x.select("span").get(1).text().trim().toUpperCase(); // country + city
                String departDate = x.select("div").get(1).ownText();
                String price = x.select("div").get(1).select("span").text();
                String hotel = x.select("span").get(2).text().trim().toUpperCase();
                String info = x.select("span").get(3).text().trim().toUpperCase();
//                String link = source + x.select("span").get(2).select("a").attr("href");
                String link = website; 
                
                TourObject localTour = UniversalParser.parseTour(new Parsable() {

                    @Override
                    public Object get(String src) {
                        try {
                            return src.substring(0, src.indexOf(","));
                        }
                        catch (IndexOutOfBoundsException ex) {
                            return "" + src;
                        }
                    }
                }, destination, new Parsable() {

                    @Override
                    public Object get(String src) {
                        try {
                            return src.substring(src.indexOf(",")).trim();
                        }
                        catch (IndexOutOfBoundsException ex) {
                            return "";
                        }
                    }
                }, destination, new Parsable() {

                    @Override
                    public Object get(String src) {
                        String res = "" + src;
                        try {
                            if (res.contains("*"))
                                res = res.substring(0, res.lastIndexOf(" "));
                        }
                        catch (IndexOutOfBoundsException ex) {
                        }
                        finally {
                            return res.replace('\'', '"');
                        }
                    }
                }, hotel, new Parsable() {

                    @Override
                    public Object get(String src) {
                        try {
                            int n = src.length();
                            return new Date(Integer.parseInt(src.substring(n - 4)) - 1900, Integer.parseInt(src.substring(3,5)) - 1, Integer.parseInt(src.substring(0,2)));                    
                        }
                        catch (Exception ex) {
                            return new Date();
                        }
                    }
                }, departDate, new Parsable() {

                    @Override
                    public Object get(String src) {
                        try {
                            return src.substring(src.indexOf(":") + 1, src.indexOf("-")).trim();
                        }
                        catch (Exception ex) {
                            return "";
                        }
                    }
                    
                }, info, new Parsable() {

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
                }, info, new Parsable() {

                    @Override
                    public Object get(String src) {
                        try {
                            String res = "" + src;
                            int k = res.charAt(0);
                            while (!(k >= 48 && k <= 57)) {
                                res = res.substring(1);
                                k = res.charAt(0);
                            }
                            return Integer.parseInt(res);
                        }
                        catch (IndexOutOfBoundsException ex) {
                            return 0;
                        }
                        catch (NumberFormatException ex) {
                            return 0;
                        }
                    }
                }, price, new Parsable() {

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
                }, hotel, new Parsable() {

                    @Override
                    public Object get(String src) {
                        return "" + src;
                   }
                }, link, new Parsable() {

                    @Override
                    public Object get(String src) {
                        String res = "" + src;
                        if (res.contains("RO") || res.contains("BO"))
                            return "RO";
                        if (res.contains("BB"))
                            return "BB";
                        if (res.contains("HB"))
                            return "HB";
                        if (res.contains("FB"))
                            return "FB";
                        if (res.contains("AI") || res.contains("AL") || res.contains("All"))
                            return "AI";
                        if (res.contains("UAI") || res.contains("UAL") || res.contains("UAll"))
                            return "UAI";
                        return "";
                    }
                }, info, new Parsable() {

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
                }, info, source, countryStand, cityStand, bananLog, "CandyTour: "
                        );
                
                if(localTour != null){
                
                	tours.add(localTour);
                }
                else{
                	continue;
                }
            }
            
//            for (int i = 0; i < tours.size(); ++i) {
//                if (DateEdit.before(tours.get(i).departDate, new Date())) {
//                    tours.remove(i);
//                    --i;
//                }
//            }
        }
        catch(IOException ex) {
//            System.out.println("IOException");
        	bananLog.write(null, ex.getMessage().toString() + " \n" +  ex.getStackTrace().toString() + " \n");
        }
        catch(NullPointerException ex) {
//            System.out.println("NullPointerException");
        	bananLog.write(null, ex.getMessage().toString() + " \n" +  ex.getStackTrace().toString() + " \n");
        }
        catch(Exception ex) {
//            System.out.println("Exception"); 
        	bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
        }
    }

}

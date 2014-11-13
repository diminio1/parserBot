/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.banan.parser.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;
import static ua.banan.parser.impl.AbstractParser.CONNECTION_TIMEOUT;

/**
 *
 * @author User
 */
public class UnicondorParser extends AbstractParser implements Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnicondorParser.class.getName());    
    
    private static final String website1 = "http://unicondor.com.ua/#Main";
    private static final String website2 = "http://unicondor.com.ua/tour-japan";
    public static final int SOURCE_ID = 1609;

    public UnicondorParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

    @Override
    public List<Tour> parseTours() {

        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website1).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tables = tourDoc.select("table[id = center-banner");
            for (Element x: tables) {
            	
            	String linkStr = x.select("a").first().attr("href");
                
                try {
                    Document jDoc = Jsoup.connect(linkStr).timeout(CONNECTION_TIMEOUT).get();
                    
                    Elements inf = jDoc.select("strong");
                    
//                    String countryStr = jDoc.select("td[class = center-title]").select("h1").first().ownText();
                    String countryStr = jDoc.text();
                    String townStr = countryStr;
                    String durationStr = "";
                    String priceStr = "";
                    String dateStr = "";        
                    
                    Pattern duration = Pattern.compile("/\\d+ ночей");
                    Pattern date = Pattern.compile("(\\d+ \\p{L}+ -)|(\\d\\d\\.\\d\\d.\\d\\d –)|(\\d\\d\\.\\d\\d –)|(с \\d+ по \\d+ \\p{L}+ \\d\\d\\d\\d)|(\\d+\\.\\d+,)|(\\d+\\.\\d+\\.\\d\\d\\d\\d,)");
                    Pattern price = Pattern.compile("(\\d+ евро)|(\\d+ дол)|(\\d+ u)|(\\d+ $)");
                    
                    Matcher m = duration.matcher(countryStr);
                    if (m.find()) {
                        durationStr = m.group();
                    }
                    
                    m = date.matcher(countryStr);
                    
                    while (m.find()) {
                        dateStr += m.group() + " ";
                    }

                    m = price.matcher((inf.text().toLowerCase()));
                    if (m.find()) {
                        priceStr = m.group();
                    }
                    
                    priceStr = priceStr.replace("u", "USD");
                    
                    Tour tour = new Tour();

                    tour.setUrl(linkStr);
                    tour.setPrice(parsePrice(priceStr.replace("дол", "USD")));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                    tour.setRoomType(parseRoomType(""));
                    tour.setFlightDate(parseDate(dateStr));
                    
                    tour.setTourOperator(tourOperator);                                

                    tours.add(tour);
                    
                }
                catch (Exception ex) {
                    LOGGER.error("Parsinging error " + ex.getMessage(), ex); 
                }
                
            }
    
        }
        catch(IOException ex) {                           
            LOGGER.error("Connecting error " + ex.getMessage(), ex);            
        }

        try {
            Document document = Jsoup.connect(website2).timeout(CONNECTION_TIMEOUT).get();
            Elements tourBlocks = document.select("table[id = specs]").select("tr");
            tourBlocks.remove(0);
            for (Element block: tourBlocks) {
                
                String countryStr = block.select("td").get(6).text();
                
                String townStr = block.select("td").get(4).text();
                
                String durationStr = block.select("td").get(3).text();
                durationStr = durationStr.substring(durationStr.indexOf("/"));
                
                String dateStr = block.select("td").first().text();
                
                String priceStr = block.select("td").get(5).ownText();
                
                String linkStr = "http://unicondor.com.ua" + block.select("td").get(1).select("a").attr("href");
                
                Tour tour = new Tour();
                
                tour.setUrl(linkStr);
                tour.setPrice(parsePrice(priceStr));
                tour.setNightsCount(parseNightCount(durationStr));
                tour.setCountries(parseCountries(countryStr));
                tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                tour.setRoomType(parseRoomType(""));
                tour.setFlightDate(parseDate(dateStr));
                    
                tour.setTourOperator(tourOperator);                                

                tours.add(tour);
            }

        }
        catch (IOException ex) {
            LOGGER.error("Connecting error " + ex.getMessage(), ex);                        
        }
        
        return tours.isEmpty() ? null : tours;
 
    }
    
    @Override
    protected Date parseDate(String inputString) {
        boolean flag = false;
        
        Date today = new Date();
        
        Pattern date1 = Pattern.compile("\\d+ по \\d+ \\p{L}+ \\d\\d\\d\\d");
        Pattern date2 = Pattern.compile("\\d+ \\p{L}+");
        Pattern date3 = Pattern.compile("\\d+\\.\\d+\\.\\d\\d\\d\\d");
        Pattern date4 = Pattern.compile("\\d+\\.\\d+");
        Pattern date5 = Pattern.compile("\\d\\d\\.\\d\\d\\.\\d\\d");
        
        Matcher matcher;
        
        SimpleDateFormat format1 = new SimpleDateFormat("dd.MMMMM.yyyy", russianDateFormatSymbols);
        SimpleDateFormat format2 = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat format3 = new SimpleDateFormat("dd.MM.yy");
        
        matcher = date1.matcher(inputString);
        while (matcher.find()) {
            
            flag = true;
            
            String test  = matcher.group();
            
            String date  = test.substring(0, test.indexOf(" "));
            String month = test.substring(test.indexOf("по") + 2).trim();
            month        = month.substring(month.indexOf(" ") + 1, month.lastIndexOf(" "));
            String year  = test.substring(test.lastIndexOf(" ") + 1);
            
            String toParse = date + "." + month + "." + year;
            
            try {
                Date tryDate = format1.parse(toParse);
                if (tryDate.after(today)) {
                    return tryDate;
                }
            } 
            catch (ParseException ex) {
                LOGGER.error("Parsing date error");// + ex.getMessage(), ex); 
            }
        }
        
        matcher = date2.matcher(inputString); 
        while (matcher.find()) {
            if (flag) {
                break;
            }
            String test = matcher.group();
            test = test.replace(" ", ".");
           
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String toParse = test + "." + year;
            try {
                Date tourDate = format1.parse(toParse);
                if (tourDate.before(today)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(tourDate);
                    if (Calendar.getInstance().get(Calendar.MONTH) > Calendar.OCTOBER 
                            && cal.get(Calendar.MONTH) == Calendar.JANUARY) {
                        
                        year++;
                        toParse = test + "." + year;
                        tourDate = format1.parse(toParse);
                        if (tourDate.after(today)) {
                            return tourDate;
                        }
                    }
                }
                else {
                    return tourDate;
                }
            } 
            catch (ParseException ex) {
                LOGGER.error("Parsing date error");// + ex.getMessage(), ex); 
            }
            
        }
        
        matcher = date3.matcher(inputString);
        while (matcher.find()) {
            try {
                Date tourDate = format2.parse(matcher.group());
                if (tourDate.after(today)) {
                    return tourDate;
                }
            } catch (ParseException ex) {
                LOGGER.error("Parsing date error" + ex.getMessage(), ex); 
            }
        }
        
        matcher = date5.matcher(inputString);
        while (matcher.find()) {
            try {
                Date tourDate = format3.parse(matcher.group());
                if (tourDate.after(today)) {
                    return tourDate;
                }
            } catch (ParseException ex) {
                LOGGER.error("Parsing date error" + ex.getMessage(), ex); 
            }
        }
        
        
        matcher = date4.matcher(inputString);
        while (matcher.find()) {
            String test = matcher.group();
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String toParse = test + "." + year;
            try {
                Date tourDate = format2.parse(toParse);
                if (tourDate.before(today)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(tourDate);
                    if (Calendar.getInstance().get(Calendar.MONTH) > Calendar.OCTOBER 
                            && cal.get(Calendar.MONTH) == Calendar.JANUARY) {
                        
                        year++;
                        toParse = test + "." + year;
                        tourDate = format2.parse(toParse);
                        if (tourDate.after(today)) {
                            return tourDate;
                        }
                    }
                }
                else {
                    return tourDate;
                }
            } catch (ParseException ex) {
                LOGGER.error("Parsinging date error");// + ex.getMessage(), ex); 
            }
        }
        
        return null;

    }

    @Override
    protected String parseHotelName(String nameContainer) {
        return null;
    }

}

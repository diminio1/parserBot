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

/**
 *
 * @author User
 */
public class AsiaParser extends AbstractParser implements Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsiaParser.class.getName());    
    
    private static final String website1 = "http://asia-business.com.ua";
    private static final String website2 = "http://asia-business.com.ua/countries-yaponiya/tours/";
    public static final int SOURCE_ID = 1612;

    
    public AsiaParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId =  SOURCE_ID;
    }

    @Override
    public List<Tour> parseTours() {
        
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document document = Jsoup.connect(website1).timeout(CONNECTION_TIMEOUT).get();
            
            Elements countryBlocks = document.select("div[class = deals_country]");
            Elements tourBlocks = document.select("div[class = deals_wrap]");
            
            for (int i = 0; i < countryBlocks.size(); ++i) {
                
                String countryStr = countryBlocks.get(i).text();
                
                Elements tourItems = tourBlocks.get(i).select("div[class = deals]");
                
                for (Element item: tourItems) {
                    
                    String linkStr = website1 + item.select("div[class = deals_title]").select("a").attr("href");
                    
                    String townStr = item.select("div[class = deals_left]").text();
                    
                    String durationStr = "";
                    
                    if (townStr.contains("/")) {
                        durationStr = townStr.substring(townStr.indexOf("/"));
                    }
                    
                    String priceStr = item.select("div[class = deals_price]").text();
                    
                    String dateStr = item.select("div[class = deals_data]").text();
                    
                    Tour tour = new Tour();
                    
                    tour.setUrl(linkStr);
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                    tour.setRoomType(parseRoomType(""));
                    tour.setPrice(parsePrice(priceStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    
                    tour.setTourOperator(tourOperator);                                

                    tours.add(tour);

                }
            }
        } 
        catch (IOException ex) {
            LOGGER.error("Connecting error " + ex.getMessage(), ex);            
        }

        try {
            Document document = Jsoup.connect(website2).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tourBlocks = document.select("div[class = deals]");
            
            for (Element item: tourBlocks) {
                
                String countryStr = "Japan";
                
                String linkStr = website1 + item.select("div[class = deals_title]").select("a").attr("href");
                    
                String townStr = item.select("div[class = deals_left]").text();
                    
                String durationStr = "";
                try {
                    Document tourDoc = Jsoup.connect(linkStr).timeout(CONNECTION_TIMEOUT).get();
                    String info = tourDoc.text();
                    Pattern duration = Pattern.compile("\\d+ ноч");
                    Matcher matcher = duration.matcher(info);
                    if (matcher.find()) {
                        durationStr = matcher.group();
                    }
                }
                catch (IOException ex) {
                    LOGGER.error("Connecting error " + ex.getMessage(), ex);                                
                }
                
                String priceStr = item.select("div[class = deals_price]").text();
                if (!priceStr.contains("&")) {
                    priceStr += "$";
                }
                
                
                String dateStr = item.select("div[class = deals_data]").text();
                    
                Tour tour = new Tour();
                    
                tour.setUrl(linkStr);
                tour.setCountries(parseCountries(countryStr));
                tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                tour.setRoomType(parseRoomType(""));
                tour.setPrice(parsePrice(priceStr));
                tour.setNightsCount(parseNightCount(durationStr));
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
        Pattern date = Pattern.compile("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d");
        Matcher matcher = date.matcher(inputString);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        if (matcher.find()) {
            try {
                return dateFormat.parse(matcher.group());
            } catch (ParseException ex) {
                LOGGER.error("Parsing date error");
                return null;
            }
        }
        return null;
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        return null;
    }
    
}

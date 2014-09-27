/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;

/**
 *
 * @author Маргарита
 */
public class CandytourParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AkkordParser.class.getName());    
    
    private static final String website = "http://candytour.com.ua/cgi-bin/myAccount/myAccount.cgi?action=dp&vs=2/scId=6&p=hottour&showAll=1";

    public static final int SOURCE_ID = 14;
    
    public CandytourParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
        
            Elements tourBlocks = tourDoc.select("div[class = news]");
            for (Element x: tourBlocks) {
        
                String destination = x.select("span").get(1).text().trim(); // country + city
                
                String dateStr = x.select("div").get(1).ownText();
                
                String priceStr = x.select("div").get(1).select("span").text();
                
                String hotelStr = x.select("span").get(2).text().trim();
                
                String info = x.select("span").get(3).text().trim();
                
//                String previousPriceStr = x.select("div").get(1).select("s").text(); 
                
                String linkStr = website; 
            
                String details = x.select("span[style = color:#666666;]").text();
                
                Tour tour = new Tour();
                                
                tour.setUrl(linkStr);        
                Integer price = parsePrice(priceStr);
                
                tour.setPrice(price != null ? price / 2 : null);
  //              tour.setPreviousPrice(parsePrice(previousPriceStr));
                tour.setFeedPlan(parseFeedPlan(details));
                tour.setNightsCount(parseNightCount(details));
                tour.setFlightDate(parseDate(dateStr));
                tour.setDescription(details);
                tour.setCountries(parseCountries(destination));
                tour.setCities(parseCities(destination, Utils.getIds(tour.getCountries())));
                
                List<City> cities = tour.getCities();
                if (cities != null && cities.size() == 1){
                    tour.setHotel(parseHotel(hotelStr, hotelStr, cities.get(0).getId()));
                }
                                                
                tour.setTourOperator(tourOperator);   
                tour.setDescription(info);
                
                tours.add(tour);
            }
        }
        catch(Exception ex) {                           
            LOGGER.error("Parsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return dateFormat.parse(inputString);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        if (nameContainer.contains("*")) {
            return nameContainer.substring(0, nameContainer.indexOf("*") - 1).trim();
        }        
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}
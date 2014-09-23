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


public class KenarParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(KenarParser.class.getName());    
    
    private static final String website = "http://kenar.com.ua/ru/trip/type/goryashchiy.html?Trip_page=1";

    public static final int SOURCE_ID = 16;
    
    public KenarParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        String url = "";
        String tempLink = website;
        do {
            try {
                Document tourDoc = Jsoup.connect(tempLink).timeout(CONNECTION_TIMEOUT).get();
            	Elements tables = tourDoc.select("div[class = trip");
        	
                for (Element x: tables) {
            	
                    String linkStr = "http://kenar.com.ua" + x.select("div[class = image]").select("a").attr("href");

                    String countryStr = x.select("div[class = info]").select("h2").text().trim(); 
            	        
                    String dateStr = x.select("div[class = desc]").select("div[class = date]").select("div").get(2).ownText();
            	    
                    String hotelStr = x.select("div[class = desc]").select("div[class = hotel]").text().trim();
            	
                    String starsStr = x.select("div[class = desc]").select("div[class = stars]").text();
            	
                    String townStr = countryStr;
            		
                    String feedPlanStr = x.select("div[class = desc]").select("div[class = food]").text().trim();
            	    
                    String durationStr = x.select("div[class = desc]").select("div[class = date]").select("div").get(1).ownText();
                    
                    if (durationStr.contains("/")) {
                        durationStr = durationStr.substring(0, durationStr.indexOf("/"));
                    }
                    
                    String priceStr = x.select("div[class = info]").select("div[class = price]").select("span[class = uah]").text();
                    
                    String roomTypeStr = "";
                    
                    Tour tour = new Tour();
                                
                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr));
                    tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                    tour.setRoomType(roomTypeStr);
                    
                    List<City> cities = tour.getCities();
                    if (cities != null && cities.size() == 1){
                        tour.setHotel(parseHotel(hotelStr, starsStr, cities.get(0).getId()));
                    }
            
                
                    tours.add(tour);
                
                    tour.setTourOperator(tourOperator);                                
                }
                url = "" + tempLink;
                tempLink = "http://kenar.com.ua/" + tourDoc.select("ul[class = yiiPager]").select("li[class *= next]").select("a").first().attr("href");

            }
            catch(Exception ex) {                           
                LOGGER.error("Parsing error " + ex.getMessage(), ex);            
            }
        
            return tours.isEmpty() ? null : tours;
        }
        while (!(tempLink.equals(url)));
    }


    @Override
    protected Date parseDate(String inputString) {
        inputString = inputString.substring(0, 10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyyy");
        try {
            return dateFormat.parse(inputString);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        nameContainer = nameContainer.replace("отель -", "").trim();
        if (nameContainer.contains("*"))
            nameContainer = nameContainer.substring(0, nameContainer.indexOf("*") - 1);
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}
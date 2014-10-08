package ua.banan.parser.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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


public class PoehalisnamiParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoehalisnamiParser.class.getName());    
    
    private static final String website = "http://www.poehalisnami.ua/";

    public static final int SOURCE_ID = 8;
    
    public PoehalisnamiParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tables = tourDoc.select("div[class = small_info]");
            
            for (Element x: tables) {
            	
            	String linkStr = x.select("div[class = gray_x]").select("a").attr("href");
                
                String countryStr = x.select("div[class = gray_x]").text();
            	
            	String roomTypeStr = "";
                
            	String dateStr = x.select("div[class = gray777]").first().ownText().trim();
            	    
            	String townStr = countryStr;
                
            	String departCityStr = "";
                
                String descriptionStr = "";

                try {
                    Document document = Jsoup.connect(linkStr).timeout(CONNECTION_TIMEOUT).get();
                    departCityStr = document.select("span[class = label-bs]").get(4).text();
                    roomTypeStr = document.select("span[class = label-bs]").get(1).text();
                    descriptionStr = document.select("div[class = b-card-inclusive green-border clearfix]").text();
                    
                } catch(IOException ex) {
                    LOGGER.error("Connecting error " + ex.getMessage(), ex);                                                	
                }
                
                           	    
                String durationStr = x.select("div[class = gray777]").first().select("nobr").text();
                
                String feedPlanStr = x.select("span[class = blue13_b]").first().ownText();
                    
                String priceStr = x.select("div[class = ab_border]").select("a").first().text().trim();
                
                
                Tour tour = new Tour();
                                
                tour.setUrl(linkStr + "&ito=1509&itc=4460");        
                tour.setPrice(parsePrice(priceStr));
                tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                tour.setRoomType(parseRoomType(roomTypeStr));
                tour.setNightsCount(parseNightCount(durationStr));
                tour.setFlightDate(parseDate(dateStr));
                tour.setDescription(descriptionStr);
                tour.setCountries(parseCountries(countryStr));
                tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                
                List<City> departCities = parseCities(departCityStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                if (departCities != null && !departCities.isEmpty()){
                    tour.setDepartCity(departCities.get(0));                    
                }
                
                tours.add(tour);
                
                tour.setTourOperator(tourOperator);                                
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
        return null;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return null;
    }

}
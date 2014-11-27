package ua.banan.parser.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;


public class TEZTourParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TEZTourParser.class.getName());    
    
    private static final String website = "http://www.teztour.ua/hots.html";

    public static final int SOURCE_ID = 12;
    
    public TEZTourParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tables = tourDoc.select("div[class = slider-item");
            for (Element x: tables) {
            	
            	String linkStr = x.select("a[class = button-link]").first().attr("href");
                
            	String countryStr = x.select("div[class = slider-title]").select("h6").text();
            	            		
            	String dateStr = countryStr;
            	    
            	String hotelStr = x.select("div[class = slider-title]").select("h4").text();
            	            	
            	String townStr = countryStr;

                String durationStr = x.select("div[class = slider-sub-block]").select("h6").text();
                    
                String priceStr = x.select("span[class = best-price]").text().trim();
                
                String previousPriceStr = x.select("span[class = old-price]").text().trim();
                
                String feedPlanStr = durationStr;
                
                String roomTypeStr = "";
                
                String starsStr = x.select("span[class = raiting_c]").select("span").get(1).attr("style");
                
                Tour tour = new Tour();
                                
                tour.setUrl(website);        
                tour.setPrice(parsePrice(priceStr) / 2);
                tour.setPreviousPrice(parsePrice(previousPriceStr) / 2);
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
        }
        catch(Exception ex) {                           
            LOGGER.error("Parsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (inputString.startsWith("Заезд")) {
            inputString = inputString.substring(inputString.indexOf(" "), inputString.indexOf("/")).trim();
        }
        inputString += year;

        Locale locale = new Locale("ru");
        
        inputString = inputString.toLowerCase(locale).replace("нбр", "ноя").replace("dec", "дек");
        
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMyyyy", russianDateFormatSymbolsShort);
        try {
            return dateFormat.parse(inputString);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        Integer stars = parseInt(starsContainer);
        
        return stars != null ? ((stars + 2) / 14) : null;
    }

}
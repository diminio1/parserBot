/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.banan.parser.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;

/**
 *
 * @author User
 */
public class MouzenidisParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MouzenidisParser.class.getName());    
    
    private static final String website = "http://www.mouzenidis.ua/last-minute-tours";
    
    public static final int SOURCE_ID = 1608;
    
    public MouzenidisParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            String test = tourDoc.html();

            String reg = "(\\\"LastMinuteTours\\\":\\[.*\\\"CountryId\":\\d+}\\])";
            
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(test);
            
            String jsonStr = null;
            
            if (matcher.find()) {
                jsonStr = "{" + matcher.group() + "}";
            }
            JsonElement jelement = new JsonParser().parse(jsonStr);
            JsonObject  jobject = jelement.getAsJsonObject();
            JsonArray jarray = jobject.getAsJsonArray("LastMinuteTours");
            
            for (JsonElement t: jarray) {
                
                String alltours = t.getAsJsonObject().get("OnlineLink").toString();
                alltours = alltours.substring(1, alltours.length() - 1);
                
                String regex ="\"SearchResults\":\\[.*\\]";
                
                try {                    
                    alltours = alltours.replace("ru?country=", "ru/?country=");
                    
                    Document document = Jsoup.connect(alltours).timeout(CONNECTION_TIMEOUT).get();
                    test = document.html();
                    pattern = Pattern.compile(regex);
                    matcher = pattern.matcher(test);

                    if (matcher.find()) {
                        jsonStr = "{" + matcher.group() + "}";
                    }
                    
                    jelement = new JsonParser().parse(jsonStr);
                    jobject = jelement.getAsJsonObject();
                    JsonArray jTours = jobject.getAsJsonArray("SearchResults");
       
                    String townStr = t.getAsJsonObject().get("Hotel").getAsJsonObject().get("Region").toString();
                    townStr = townStr.substring(1, townStr.length() - 1);
                    
                    String dateStr = t.getAsJsonObject().get("TourPeriod").toString();
    			
                    String hotelStr = t.getAsJsonObject().get("Hotel").getAsJsonObject().get("Title").toString();
                    hotelStr = hotelStr.substring(1, hotelStr.length() - 1);
                    
                    String feedPlanStr = t.getAsJsonObject().get("Hotel").getAsJsonObject().get("PansionCode").toString();
                    feedPlanStr = feedPlanStr.substring(1, feedPlanStr.length() - 1);
                    
                    String starsStr = t.getAsJsonObject().get("Hotel").getAsJsonObject().get("Stars").toString();
    		
                    String roomTypeStr = t.getAsJsonObject().get("RoomType").toString();
                    roomTypeStr = roomTypeStr.substring(1, roomTypeStr.length() - 1);
                    
                    String countryStr = "Греция";
        	    
                    ArrayList<Integer> durations = new ArrayList<>();
                    
                    for(JsonElement j: jTours) {
                       
                        Integer dur = j.getAsJsonObject().get("Hls").getAsJsonArray().get(0).getAsJsonObject().get("NG").getAsInt();
                        
                        if (durations.contains(dur)) {
                            continue;
                        }
                        durations.add(dur);
                        
                	String durationStr = "" + dur;
                        
                        String priceKey = j.getAsJsonObject().get("V").toString();
                        
                        String dateKey = j.getAsJsonObject().get("TDL").toString();
                        dateKey = dateKey.substring(1, dateKey.length() - 1);
                        
                        String linkStr = "http://online215.mouzenidis-travel.ru/SimpleBasket/SimpleBasket?priceKey=" + priceKey + "&date=" + dateKey;
                        
                        String priceStr = j.getAsJsonObject().get("PS").toString();
    			
                        Tour tour = new Tour();
                        
                        tour.setUrl(linkStr);
                        Integer price  = parsePrice(priceStr);
                        if (price != null) {
                            tour.setPrice(price / 2);
                        }
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
                catch(IOException e) {
                    LOGGER.error("Parsing error " + e.getMessage(), e);          
                } 
                
            }
        }
        catch(Exception ex) {                           
            LOGGER.error("Parsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            return dateFormat.parse(inputString.substring(1, 11));
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
        return parseInt(starsContainer);
    }

    
}

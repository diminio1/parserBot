package ua.banan.parser.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;


public class NewstravelParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewstravelParser.class.getName());    
    
    private static final String website = "http://www.newstravel.com.ua/";

    public static final int SOURCE_ID = 23;
    
    public NewstravelParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        Document tourDoc = null;
        int p = 0;
        while (true) {
            String site = "http://besthotels.org.ua/api/get_offers/?t=tours&f=p&d=0&c=0&r=0&p=" + p + "&pp=100";
            try {
                tourDoc = Jsoup.connect(site).ignoreContentType(true).timeout(CONNECTION_TIMEOUT).get();

        	String jsonLine = tourDoc.select("body").text().trim();
        	JsonElement jElem = new JsonParser().parse(jsonLine);
        	JsonObject jObject = jElem. getAsJsonObject();
        	JsonArray jTours = jObject.getAsJsonArray("data");
        	if (jTours.toString().equals("[]")) {
                    break;
                }
        	for (JsonElement jTour: jTours) {
                    JsonObject jsonTour = jTour.getAsJsonObject();
                    String sold = jsonTour.get("is_soldout").toString();
                    if (sold.equals("1"))
        		continue;
    			
                    String linkStr = "http://www.newstravel.com.ua/predlojeniya";
                
                    String countryStr = jsonTour.get("country_name").toString().trim();
                    
                    String townStr = jsonTour.get("city_name").toString().trim().toUpperCase();
                    
                    String roomTypeStr = "";
                    
                    String dateStr = jsonTour.get("travel_date").toString();
                    
                    String departCityStr = jsonTour.get("departure_city_name").toString().trim();
                    
                    String hotelStr = jsonTour.get("hotel_name").toString();
                    
                    String feedPlanStr = jsonTour.get("board_name").toString();                	    
                    
                    String durationStr = jsonTour.get("travel_nights").toString();
                    
                    String starsStr = jsonTour.get("hotel_stars").toString(); 
                    
                    String descriptionStr = jsonTour.get("description").toString();
                    descriptionStr = descriptionStr.substring(1, descriptionStr.length() - 1);
                    descriptionStr = descriptionStr.replace("n", " ").trim();
                    descriptionStr = descriptionStr.replace("\\", "");
                    descriptionStr = descriptionStr.replace("\u00a0", " ");
                    
                    String priceStr = jsonTour.get("price").toString();
    				
                    Tour tour = new Tour();
                                
                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr));
                    tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                    tour.setRoomType(parseRoomType(roomTypeStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setDescription(descriptionStr);
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                
                    List<City> cities = tour.getCities();
                    if (cities != null && cities.size() == 1){
                        tour.setHotel(parseHotel(hotelStr, starsStr, cities.get(0).getId()));
                    }
                
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
        return nameContainer.substring(1, nameContainer.length() - 1);
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}
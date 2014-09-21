package ua.banan.parser.impl;

import java.io.IOException;
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

public class HColumbusParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AkkordParser.class.getName());    
    
    private static final String website = "http://www.hcolumbus.com.ua/hot_tours/";

    public static final int SOURCE_ID = 2;
    
    public HColumbusParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
      
            Elements tourElems = tourDoc.select("a[class = index_tour_href]");
            for (Element x: tourElems) {
                String linkStr = x.attr("href");
                try {
                    tourDoc = Jsoup.connect(linkStr).timeout(5000).get();

                    String dateStr = tourDoc.select("div[class *= tcategory_name]:containsOwn(Дат)").select("span").text();
                    if(dateStr.contains(";")) {
                       dateStr = dateStr.substring(dateStr.lastIndexOf(';') + 1).trim();
                    }
                    String countryStr = tourDoc.select("a[class = country_name_link]").text();
            
                    String townStr = tourDoc.select("div[class = tcategory_name]:containsOwn(Город)").select("span").text();
            
                    String hotelStr = tourDoc.select("div[class = tcategory_name]:containsOwn(Отель)").select("span").text();
            
                    String durationStr = tourDoc.select("div[class = tcategory_name]:containsOwn(Длительность)").text();
            
                    String feedPlanStr = tourDoc.select("div[class *= tcategory_name]:containsOwn(Вид питания)").select("span").text();
            
                    String roomTypeStr = tourDoc.select("span:contains(Проживание)").text();
            
                    String priceStr = tourDoc.select("div[class = tour_price]").select("span").text();
            
                    String descriptionStr = tourDoc.select("div[class = main_text]").text();
             
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
                        tour.setHotel(parseHotel(hotelStr, hotelStr, cities.get(0).getId()));
                    }
                                                    
                    tour.setTourOperator(tourOperator);             
                    
                    tours.add(tour);
                }
                catch(Exception ex) {
                    LOGGER.error("Parsing error " + ex.getMessage(), ex);
                }
            }
        }
        catch(IOException ex) {                           
            LOGGER.error("Connecting error " + ex.getMessage(), ex);            
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
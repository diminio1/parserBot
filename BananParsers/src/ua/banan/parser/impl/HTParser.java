package ua.banan.parser.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

public class HTParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AkkordParser.class.getName());    
    
    private static final String website = "http://ht.kiev.ua/tours/type1.html";

    public static final int SOURCE_ID = 3;
    
    public HTParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tourElems = tourDoc.select("a[class = t_price]"); // gets all tours
            
            String basePath = website.substring(0, website.lastIndexOf('/'));
            for(Element x: tourElems) {
                String linkStr = x.attr("href"); // gets url of current tour 
                linkStr = basePath.substring(0, basePath.lastIndexOf('/')).concat(linkStr); // gets web page of it

                try {
                    tourDoc = Jsoup.connect(linkStr).get();
        
                    String countryStr = tourDoc.select("div[class = desc]").get(1).text();
                    String townStr = tourDoc.select("div[class = desc]").get(2).text();
                    String hotelStr = tourDoc.select("div[class = desc]").get(3).text();
                    String departCityStr = tourDoc.select("td:eq(1)").first().text();
                    String starsStr = hotelStr;
                    String dateStr = tourDoc.select("td:eq(1)").get(2).text();
                    if(dateStr.equals("")) {
                        dateStr = tourDoc.select("option[value = 0]").first().text();            
                    }
                    String durationStr = tourDoc.select("td:eq(1)").get(3).text();
                    String feedPlanStr = tourDoc.select("td:eq(1)").get(4).text();
                    String roomTypeStr = tourDoc.select("td:eq(1)").get(5).text();
                    String priceStr = tourDoc.select("a[class = price]").first().text();
                    String descriptionStr = tourDoc.select("div[class = tour_incl_ht]").text();
                    
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
                                                    
                    tour.setTourOperator(tourOperator);     
                    
                    tours.add(tour);
                }
                catch (Exception ex) {
                    LOGGER.error("Connecting error " + ex.getMessage(), ex);            
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
        inputString = inputString.substring(0, 5) + ".";
        int year = Calendar.getInstance().get(Calendar.YEAR);
        inputString += year;
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
        nameContainer = nameContainer.replaceAll("\\u00A0", " ");
        if (nameContainer.contains("*")) {
            return nameContainer.substring(0, nameContainer.indexOf("*") - 1);
        }
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        if (starsContainer.contains("*")) {
            return parseInt(starsContainer);
        }
        return 0;
    }

}
package ua.banan.parser.impl;

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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;

public class HottoursParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HottoursParser.class.getName());    
    
    private static final String website = "http://www.hottour.com.ua/tours";

    public static final int SOURCE_ID = 5;
    
    public HottoursParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tables = tourDoc.select("td[class ^= tour]");
            
            Elements links = tourDoc.select("a[href ^= /tour?]");
				
            Elements prices = tourDoc.select("span[class = cost]");
				
            Elements previousPrices = tourDoc.select("span[class = old]").select("s");

            int index = 0;
            
            for (Element x: tables) {
                
                if (x.hasClass("tour-stop")){
                    index++;
                } else if (x.hasClass("tour")){                
                    String countryStr = x.select("span[class = country]").text();

                    String hotelStr = x.select("span[class = hotel]").text();

                    String roomTypeStr = x.select("span[class = room]").text();

                    String dateStr = x.select("span[class = data]").text().trim();

                    String townStr = x.select("span[class = region]").text();

                    String departCityStr = x.select("span[class = departure]").text();

                    String feedPlanStr = roomTypeStr;

                    String durationStr = x.select("span[class = night]").text();

                    String linkStr = "http://www.hottour.com.ua" + links.get(index).attr("href");

                    String priceStr = prices.get(index).text();

                    String previousPriceStr = previousPrices.get(index).text();

                    Tour tour = new Tour();

                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr));
                    tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                    tour.setRoomType(parseRoomType(roomTypeStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setPreviousPrice(parsePrice(previousPriceStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));

                    List<City> cities = tour.getCities();
                    if (cities != null && cities.size() == 1){
                        tour.setHotel(parseHotel(hotelStr, hotelStr, cities.get(0).getId()));
                    }

                    List<City> departCities = parseCities(departCityStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                    if (departCities != null && !departCities.isEmpty()){
                        tour.setDepartCity(departCities.get(0));                    
                    }

                    tours.add(tour);

                    tour.setTourOperator(tourOperator);     

                    index++;
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
        inputString = inputString.length() > 10 ? inputString.substring(0, 10) : "";
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
        if (nameContainer.contains(",")) {
            nameContainer = nameContainer.substring(0, nameContainer.indexOf(",")).trim();
        }
        
        if (nameContainer.contains("Отел") || nameContainer.contains("БЕЗ")) {
            return null;
        }
        return nameContainer;
    }    

}
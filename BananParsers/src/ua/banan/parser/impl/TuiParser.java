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

/**
 *
 * @author Маргарита
 */
public class TuiParser extends AbstractParser implements Parser{
    private static final Logger LOGGER = LoggerFactory.getLogger(TuiParser.class.getName());    
    
    private static final String website = "http://www.tui.ua/";

    public static final int SOURCE_ID = 1;
    
    public TuiParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

        @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();

            Elements tourElems = tourDoc.select("div[class = offers-list]").select("li");
            
            for (Element x: tourElems) {
                
                String countryStr = x.select("div[class = country]").text().trim().toUpperCase();
                
                String timeStr =  x.select("div[class = time]").first().ownText(); // depCity + duration
                
                String dateStr = x.select("div[class = time]").select("b").text();
                
                int stars = x.select("span[class = stars]").select("img").size();
                
                String townStr = x.select("div[class = way]").select("a[class = item]").first().ownText().trim().toUpperCase();
                
                String hotelStr = x.select("a[class = name]").first().ownText().trim().toUpperCase();
                
                String priceStr = x.select("a[class= price]").first().ownText().trim();
                
                String linkStr = website + x.select("a[class = all-link]").first().attr("href");
                
                String roonTypeStr = "";
                
                Tour tour = new Tour();
                                
                tour.setUrl(linkStr);        
                tour.setPrice(parsePrice(priceStr));
                tour.setNightsCount(parseNightCount(timeStr));
                tour.setFlightDate(parseDate(dateStr));
                tour.setCountries(parseCountries(countryStr));
                tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                tour.setRoomType(roonTypeStr);
                
                List<City> cities = tour.getCities();
                if (cities != null && cities.size() == 1){
                    tour.setHotel(parseHotel(hotelStr, "" + stars, cities.get(0).getId()));
                }
                
                List<City> departCities = parseCities(timeStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
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
        if (nameContainer != null && !(nameContainer.equals(""))) {
            if (nameContainer.contains("*")) {
                nameContainer = nameContainer.substring(0, nameContainer.indexOf("*") - 1).trim();
            }
            return nameContainer;
        }        
        return null;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}
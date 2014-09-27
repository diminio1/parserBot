package ua.banan.parser.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


/**
 *
 * @author Маргарита
 */
public class TouraviaParser extends AbstractParser implements Parser{
    private static final Logger LOGGER = LoggerFactory.getLogger(TouraviaParser.class.getName());    
    
    private static final String website = "http://touravia.info/goryashhie-tury-tour-turcey";

    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36";

    public static final int SOURCE_ID = 6;
    
    public TouraviaParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

        @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).userAgent(userAgent).timeout(CONNECTION_TIMEOUT).get();
 
            Elements tourElems = tourDoc.select("ul[class=pagination pagination-sm]").first().select("li").select("a"); // gets all countries
            
            for (Element x: tourElems) {
                String site = "http://touravia.info" + x.attr("href");
                Document xDoc = Jsoup.connect(site).timeout(CONNECTION_TIMEOUT).userAgent(userAgent).get();
            
                String countryStr = xDoc.select("li[class=active]").select("a").first().ownText().trim();
            
                Elements tourInfo = xDoc.select("div[class = thumbnail text-center]");
                
                for (Element y: tourInfo) {
                    
                    String hotelStr = y.select("div").get(3).select("a").select("strong").text().trim();
                   
                    String dateStr = y.select("div").get(5).select("i").text().trim();
                    if (dateStr.startsWith("ВЫЛЕТ"))
                        dateStr = dateStr.substring(7);
                    
                    String priceStr = y.select("div").get(2).select("span[class = text-danger]").text().trim();
                    
                    String previousPriceStr = y.select("div").get(1).text().trim();
                    
                    String durationStr = y.select("div").get(6).select("i").text().trim();
                    if (durationStr.contains("/")) {
                        durationStr = durationStr.substring(durationStr.indexOf("/"));
                    }
                    
                    String nutritionStr = y.select("div").get(7).select("strong").text().trim();
                
                    String linkStr = y.select("div").get(4).select("a").first().attr("href");
                
                    linkStr = "http://touravia.info" + linkStr;
                                    
                    String descriptionStr = y.select("div").get(8).select("small").text();
                    
                    String roomTypeStr = "";
                    
                    Tour tour = new Tour();
                                
                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr));
                    tour.setPreviousPrice(parsePrice(previousPriceStr));
                    tour.setFeedPlan(parseFeedPlan(nutritionStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(hotelStr, Utils.getIds(tour.getCountries())));
                    tour.setDescription(descriptionStr);
                    tour.setRoomType(roomTypeStr);
                    
                    List<City> cities = tour.getCities();
                    if (cities != null && cities.size() == 1){
                        tour.setHotel(parseHotel(hotelStr, hotelStr, cities.get(0).getId()));
                    }
                
                    tours.add(tour);
                
                    tour.setTourOperator(tourOperator);                                
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
        Pattern pattern = Pattern.compile("\\d\\d.\\d\\d.\\d\\d\\d\\d");
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.find()) {
            inputString = matcher.group(0);
        }
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
            if (nameContainer.startsWith("ОТЕЛЬ")) {
                nameContainer = nameContainer.substring(6);
            }
            
            if (nameContainer.contains("(")) {
                int k = nameContainer.indexOf("(");
                nameContainer = nameContainer.substring(0, k).trim();
            }
            
            if (nameContainer.contains("*")) {
                nameContainer = nameContainer.substring(0, nameContainer.indexOf("*") - 1);
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

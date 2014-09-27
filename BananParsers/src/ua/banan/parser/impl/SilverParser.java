package ua.banan.parser.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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


/**
 *
 * @author Маргарита
 */
public class SilverParser extends AbstractParser implements Parser{
    private static final Logger LOGGER = LoggerFactory.getLogger(SilverParser.class.getName());    
    
    private static final String website = "http://silver-tour.com.ua";

    public static final int SOURCE_ID = 22;
    
    public SilverParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

        @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
 
            Elements tables = tourDoc.select("div[class = tour-vertical");
            for (Element x: tables) {
            	
            	String linkStr = "http://silver-tour.com.ua" + x.select("a").first().attr("href");
                                
                try {
                    Document doc = Jsoup.connect(linkStr).timeout(CONNECTION_TIMEOUT).get();
                	
                    Elements infos = doc.select("table[class = tour-info]").first().select("tr");
                	
                    String countryStr = infos.get(2).select("td").get(1).text().trim();
                	
                    String townStr = infos.get(3).select("td").get(1).text().trim();
                	
                    String departCityStr = infos.get(4).select("td").get(1).text().trim();
                    
                    String durationStr = infos.get(7).select("td").get(1).text().trim();
                    
                    if (durationStr.contains("/")) {
                        durationStr = durationStr.substring(0, durationStr.indexOf("/"));
                    }
                    
                    String nutritionStr = infos.get(8).select("td").get(1).text().trim();
            	    
                    String dateStr = infos.select("td[class = dates]").select("tr").select("td").first().text();
                	    
                    String priceStr = infos.get(9).select("td").get(1).text().trim();
                    
                    String descriptionStr = doc.select("div[class = txt]").text();
                    
                    String roomTypeStr = "";
                    
                    int persons = infos.get(5).select("td").get(1).select("img").size();
                    
                    Tour tour = new Tour();
                                
                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr) / persons);
                    tour.setFeedPlan(parseFeedPlan(nutritionStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                    tour.setDescription(descriptionStr);
                    tour.setRoomType(roomTypeStr);
                    
                    List<City> departCities = parseCities(departCityStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                    if (departCities != null && !departCities.isEmpty()){
                        tour.setDepartCity(departCities.get(0));                    
                    }

                    tours.add(tour);
                
                    tour.setTourOperator(tourOperator);                                
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
        if (inputString.contains(",")) {
            inputString = inputString.substring(0, inputString.indexOf(",")).trim();
        }
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMMM yyyydd", new Locale("ru"));
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return dateFormat1.parse(inputString);
        } catch (ParseException ex) {
            try {
                return dateFormat2.parse(inputString);
            }
            catch(Exception exx) {
                LOGGER.error("Parsing date error " + ex.getMessage(), ex);
                return null;
            }
        }
        
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        return null;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }
    
    @Override
    protected Integer parsePrice(String priceString) {
        if(priceString.contains("(")) {
            priceString = priceString.substring(0, priceString.indexOf("("));
        }
        
        priceString = priceString.replaceAll(",", "");
        
        return super.parsePrice(priceString);
    }

}
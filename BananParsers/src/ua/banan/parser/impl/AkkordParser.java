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

public class AkkordParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AkkordParser.class.getName());    
    
    private static final String website = "http://www.akkord-tour.com.ua/choose-me.php";
    public static final int SOURCE_ID = 20;
    
    
    public AkkordParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tables = tourDoc.select("tr[rel_tour = tour_tr");
            for (Element x: tables) {
            	
            	String linkStr = x.select("td").get(1).select("div").get(1).select("a").attr("href");
                
            	Elements countries = x.select("td").first().select("div").select("img");
            	String countryStr = "";
            	
            	for (Element country: countries) {
            		countryStr += country.attr("title");
            		countryStr += " - ";
            	}
            	countryStr = countryStr.substring(0, countryStr.length() - 3);
            		
            	String roomTypeStr = "";
                
            	String dateStr = x.attr("rel_tour_date");
            	    
            	String hotelStr = "";
            	            	
            	String townStr = x.select("td").get(1).select("div").get(4).text().trim();
            	if (townStr.isEmpty())
            		townStr = x.select("td").get(1).select("div").get(3).text().trim();
            	
            	String departCityStr = "";
            	if (!townStr.isEmpty()){
                    departCityStr = townStr.substring(0, townStr.indexOf(" ")).trim();
                }
                           	    
                String durationStr = x.attr("rel_day");
                    
                String priceStr = x.select("span[class = currency_price_span]").select("span[rel = UAH]").get(1).ownText().trim();
                
                String previousPriceStr = x.select("span[class = currency_price_span]").select("span[rel = UAH]").first().ownText().trim();
                
                String descriptionStr = null;
                
                try {
                	Document doc = Jsoup.connect(linkStr).timeout(100000).get();
                	Elements hotels = doc.select("a:contains(Отель)").select("b");
                	if (hotels.size() == 1) {
                            hotelStr = hotels.text().replaceAll("(Отель|отель)", "").trim();
                	} else {
                            hotelStr = "";
                        }
                        descriptionStr = doc.select("div[id = include]").text();
                }
                catch (Exception ex) {
                    LOGGER.error("Exception while reding hotel", ex);                                                	
                }
                
                Tour tour = new Tour();
                                
                tour.setUrl(linkStr);        
                tour.setPrice(parsePrice(priceStr));
                tour.setPreviousPrice(parsePrice(previousPriceStr));
//                tour.setFeedPlan(parseFeedPlan(feedPlanStr));
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
                
                List<City> departCities = parseCities(departCityStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                if (departCities != null && !departCities.isEmpty()){
                    tour.setDepartCity(departCities.get(0));                    
                }
                                                
                tour.setTourOperator(tourOperator);  
                
                tours.add(tour);
            }
        }
        catch(Exception ex) {                           
            LOGGER.error("Parsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
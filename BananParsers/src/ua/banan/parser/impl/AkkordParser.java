package ua.banan.parser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ua.banan.data.model.City;
import ua.banan.data.model.CurrencyExchanger;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;

public class AkkordParser extends AbstractParser {
    private static final Logger LOGGER = LogManager.getLogger(AkkordParser.class.getName());    
    
    private static final String website = "http://www.akkord-tour.com.ua/choose-me.php";
    private static final int    sourceId = 20;

    public AkkordParser(DataOperator dataOperator, CurrencyExchanger currencyExchanger) {
        super(dataOperator, currencyExchanger);
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
            		countryStr += country.attr("title").toUpperCase();
            		countryStr += " - ";
            	}
            	countryStr = countryStr.substring(0, countryStr.length() - 3);
            		
            	String roomTypeStr = "";
                
            	String dateStr = x.attr("rel_tour_date");
            	    
            	String hotelStr = "";
            	            	
            	String townStr = x.select("td").get(1).select("div").get(4).text().trim().toUpperCase();
            	if (townStr.isEmpty())
            		townStr = x.select("td").get(1).select("div").get(3).text().trim().toUpperCase();
            	
            	String departCityStr = "";
            	if (!townStr.isEmpty()){
                    departCityStr = townStr.substring(0, townStr.indexOf(" ")).trim();
                }
                           	    
                String durationStr = x.attr("rel_day");
                    
                String priceStr = x.select("span[class = currency_price_span]").select("span[rel = UAH]").get(1).ownText().trim();
                
                String previousPriceStr = x.select("span[class = currency_price_span]").select("span[rel = UAH]").first().ownText().trim();
                
                try {
                	Document doc = Jsoup.connect(linkStr).timeout(100000).get();
                	Elements hotels = doc.select("a:contains(Отель)").select("b");
                	if (hotels.size() == 1) {
                            hotelStr = hotels.text().trim().toUpperCase();
                	} else {
                            hotelStr = "";
                        }
                }
                catch (Exception ex) {
                    LOGGER.error("Exception while reding hotel", ex);                                                	
                }
                
                Tour tour = new Tour();
                                
                tour.setUrl(linkStr);        
                tour.setPrice(parsePrice(priceStr));
                tour.setPreviousPrice(parsePrice(previousPriceStr));
//                tour.setFeedPlan(feedPlanStr);
                tour.setRoomType(roomTypeStr);
                tour.setNightsCount(parseNightCount(durationStr));
                tour.setFlightDate(parseDate(dateStr));
//                tour.setDescription(descriptionStr);
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
            }
        }
        catch(Exception ex) {                           
            LOGGER.error("Parsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
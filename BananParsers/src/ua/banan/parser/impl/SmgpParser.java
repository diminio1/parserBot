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


public class SmgpParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmgpParser.class.getName());    
    
    private static final String website = "http://smgp.com.ua/last-minute-tours/from-city/0/";

    public static final int SOURCE_ID = 11;
    
    public SmgpParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements countryBlocks = tourDoc.select("div[class = country]");
				
            for (Element countryBlock : countryBlocks) {
				
                if (countryBlock.hasText()) {
					
                    // for tours where more then five in one country
                    if (countryBlock.select("a[class = deeper]").first() != null) {
							
                        Document deeperTourDoc = null;
							
			try {
                            deeperTourDoc = Jsoup.connect("http://smgp.com.ua" + 
                            countryBlock.select("a[class = deeper]").first().attr("href")).get();
			}
			catch (IOException ex) {
                            LOGGER.error("Connecting error " + ex.getMessage(), ex);            
			}
					
			countryBlock = deeperTourDoc.select("div[class = country]").first();
                    }
                    
                    String countryStr = countryBlock.select("div[class = country]").first().text() ;
                    
                    Elements tourBlocks = countryBlock.select("dd[style = white-space:wrap;]");
                    
                    for (Element tourBlock: tourBlocks) {
                        
                        String townStr = tourBlock.select("div[class = city]").text();
			
                        String hotelStr = countryBlock.select("div[class = hotel]").text();
			
                        String linkStr = "http://smgp.com.ua" + countryBlock.select("div[class = hotel]").attr("href");
                        
                        String dateStr = countryBlock.select("div[class = date]").text();
			
                        String durationStr = countryBlock.select("div[class = nights]").text();
			
                        String priceStr = countryBlock.select("div[class = price]").text();
				
                        Tour tour = new Tour();
                                
                        tour.setUrl(linkStr);        
                        tour.setPrice(parsePrice(priceStr));
                        tour.setFeedPlan(parseFeedPlan(durationStr));
                        tour.setRoomType(parseRoomType(durationStr));
                        tour.setNightsCount(parseNightCount(durationStr));
                        tour.setFlightDate(parseDate(dateStr));
                        tour.setCountries(parseCountries(countryStr));
                        tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                
                        List<City> cities = tour.getCities();
                        if (cities != null && cities.size() == 1){
                            tour.setHotel(parseHotel(hotelStr, hotelStr, cities.get(0).getId()));
                        }
                
                        tours.add(tour);
                
                        tour.setTourOperator(tourOperator);                                
                        
                    }
                
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
        if (nameContainer.contains("(")) {
            return nameContainer.substring(0, nameContainer.indexOf("("));
        }
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}
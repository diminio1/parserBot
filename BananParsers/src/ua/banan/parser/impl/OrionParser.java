package ua.banan.parser.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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


public class OrionParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrionParser.class.getName());    
    
    private static final String website = "http://orion-intour.com/topic_tours/hottur/";

    public static final int SOURCE_ID = 21;
    
    public OrionParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tables = tourDoc.select("table[class = tour-list").select("tr");
            int k = 0;
            for (Element x: tables) {
            	if (k == 0) {
                    k++;
                    continue;
            	}
            	
            	String linkStr = "http://orion-intour.com" + x.select("td[class = name]").select("a").attr("href");
                
            	String countryStr = x.select("td[class = name]").first().ownText();
            	
            	String dateStr = x.select("td[class = dt]").first().ownText();
            	
               	String feedPlanStr = x.select("td[class = food]").text();

            	if (dateStr == null || dateStr.equals("") || dateStr.contains("ЛЮБЫЕ"))
                    continue;
            	
                try {
                    Document doc = Jsoup.connect(linkStr).timeout(CONNECTION_TIMEOUT).get();
                	
                    String hotelStr = doc.select("div[class = text-hider]").text();
                
                    String roomTypeStr = "";
                	               	
                    String townStr = doc.select("table[class = tour-params]").select("tr").get(1).select("h2").text().trim();
                	
                    String departCityStr = doc.select("table[class = tour-params]").select("tr").get(2).select("td").get(1).text().trim();
                	                    	    
                    String durationStr = doc.select("table[class = tour-params]").select("tr").get(4).select("td").get(1).text();
                    Pattern p = Pattern.compile("\\d++");
                    Matcher m = p.matcher(durationStr);
                    durationStr = m.find() ? m.group() : "";
                        
                    String priceStr = doc.select("span[class = price").text().trim();
                
                    Tour tour = new Tour();
                                
                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr));
                    tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                    tour.setRoomType(parseRoomType(roomTypeStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
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
                }
                catch (Exception ex) {
                    LOGGER.error("Parsing error" + ex.getMessage(), ex);
                }
            }
        }
        catch(Exception ex) {                           
            LOGGER.error("IOParsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        inputString = inputString.replace("/", ".");
        String toParse;
        
        Pattern p;
        Matcher m;
        SimpleDateFormat dateFormat;
                
        p = Pattern.compile("\\d\\d.\\d\\d.\\d\\d\\d\\d");
        m = p.matcher(inputString);
        toParse = m.find() ? m.group() : null;
        
        if (toParse == null) {
            p = Pattern.compile("\\d\\d\\.\\d\\d\\.\\d\\d");
            m = p.matcher(inputString);
            toParse = m.find() ? m.group() : null;        
        }
        
        if (toParse == null) {
            LOGGER.error("Parsing date error in Orion Parser input: " + inputString);
            return null;
        }
        
        dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        
        try {
            return dateFormat.parse(toParse);
        }
        catch (ParseException ex) {
            dateFormat = new SimpleDateFormat("dd.MM.yy");            
        }
        try {
            return dateFormat.parse(toParse);            
        }
        catch (ParseException ex) {
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
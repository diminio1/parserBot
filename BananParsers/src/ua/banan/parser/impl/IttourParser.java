package ua.banan.parser.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;

public class IttourParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(IttourParser.class.getName());    
    
    private static final String website = "http://www.ittour.com.ua/?action=get_showcase_tour&type=48&items_per_page=30&callback=";
    
    private static final String   userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36";

    public static final int SOURCE_ID = 24;
    
    public IttourParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).userAgent(userAgent).ignoreHttpErrors(true).ignoreContentType(true).timeout(CONNECTION_TIMEOUT).get();
            
            String html = tourDoc.html();
            int beginIndex  = html.indexOf("<table"); 
            int stopIndex  = html.lastIndexOf("&quot;,&quot;results_count&quot;");
        		
            html = html.substring(beginIndex, stopIndex);
        		
            String [] parts = html.split("tourPopup");
        		
            ArrayList<String> argsStr = new ArrayList<String>(); // parameters for url
            ArrayList<String> prices = new ArrayList<String>(); // price for 1 person
        		
            Pattern pattern = Pattern.compile(("\'[0-9]*\',\'[0-9]*\'"));
            
            for (int i = 1; i < parts.length; ++i) {
                Matcher matcher = pattern.matcher(parts[i]);
                if (matcher.find()){
                    argsStr.add(matcher.group().toString());
                }
            }
         		
            int index = 0;
            
            for (String argStr: argsStr) {
                
                int coma = argStr.indexOf(",");
        	String param1 = argStr.substring(1, coma - 1); 
                String param2 = argStr.substring(coma + 2, argStr.length() - 1);         			
        			
        	String linkStr = "http://www.ittour.com.ua/tour-popup-ajax.html?tour_price=" + param1 + "&is_archive=0&show_popup=0&sharding_rule_id=" + param2;
    				
        	Document document = Jsoup.connect(linkStr).userAgent(userAgent).timeout(CONNECTION_TIMEOUT).get();
        			
                String countryStr = document.select("span[class = country_popup]").text().trim();
        			
        	String townStr = countryStr;
    				
        	String dateStr = document.select("li").get(6).select("b").text().trim();
    				        			
        	String departCityStr = document.select("li").get(5).select("b").text().trim();

        	String hotelStr = document.select("a[class = tour_popup_hotel_url]").text().trim().replace('\'', '"');

                String persons = document.select("li").get(10).select("b").text();
                
        	String feedPlanStr = document.select("li").get(11).select("b").text();                	    
    				
        	String durationStr = document.select("li").get(12).select("b").text();
    				
        	int stars = document.select("a[class = gold_star]").size();
    		String starsStr = "" + stars;
    				
    		String priceStr = document.select("div[class = price_box_popup]").select("b").text();
    				
                String roomTypeStr = "";
                
    		index++;
    		 
                Tour tour = new Tour();
                                
                tour.setUrl(linkStr);        
                tour.setPrice(parsePrice(priceStr) / parseInt(persons));
                tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                tour.setNightsCount(parseNightCount(durationStr));
                tour.setFlightDate(parseDate(dateStr));
                tour.setCountries(parseCountries(countryStr));
                tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                tour.setRoomType(roomTypeStr);
                
                List<City> cities = tour.getCities();
                if (cities != null && cities.size() == 1){
                    tour.setHotel(parseHotel(hotelStr, starsStr, cities.get(0).getId()));
                }
                
                List<City> departCities = parseCities(departCityStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
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
        
        Pattern p = Pattern.compile("\\d\\d.\\d\\d.\\d\\d");
        Matcher m = p.matcher(inputString);
        
        inputString = m.find() ? m.group() : "";
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        try {
            return dateFormat.parse(inputString);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}
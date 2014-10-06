package ua.banan.parser.impl;

import java.io.IOException;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;

public class ShturmanParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShturmanParser.class.getName());    
    
    private static final String website = "http://www.tour-shturman.com/main";

    public static final int SOURCE_ID = 4;
    
    public ShturmanParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tourElems = tourDoc.select("a[class = readon]");
            
            String basePath = website.substring(0, website.lastIndexOf('/'));
            
            Elements depCities = tourDoc.select("strong:contains(из)");
            
            int index = 0;
            
            for (Element x: tourElems) {
                try {
                    String s = x.attr("href");
               
                    String linkStr = basePath + s;
                    
                    Document xDoc = Jsoup.connect(linkStr).timeout(CONNECTION_TIMEOUT).get();
           
                    String countryStr = xDoc.select("td[class = contentheading]").first().text();
                    
                    String departCityStr = depCities.get(index).text();
                    
                    index++;
                    
                    Elements allTours = xDoc.select("tbody[style *= margin: 0px; padding: 0px; border: 0px; font-size: 12px; font: inherit; vertical-align: baseline;]");
                    
                    for (Element z: allTours) {
                        Elements info = z.select("td[style *= font-size: 10px;]");
                    
                        String dateStr = info.get(2).text();
                    
                        String hotelStr = info.get(1).text();
                        
                        String priceStr = z.select("p[style *= margin-bottom: 0px; font: inherit; vertical-align: baseline;]").text();
                     
                        String durationStr = z.select("td[style *= width: 170px;]").first().text();

                        String feedPlanStr = z.select("td[style *= width: 170px;]").text(); 
                        
                        String roomTypeStr = z.text();

                        Tour tour = new Tour();
                                
                        tour.setUrl(linkStr);        
                        tour.setPrice(parsePrice(priceStr));
                        tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                        tour.setRoomType(parseRoomType(roomTypeStr));
                        
                        
                        tour.setNightsCount(parseNightCount(durationStr.substring(0, durationStr.indexOf('й'))));
                        tour.setFlightDate(parseDate(dateStr));
                        tour.setCountries(parseCountries(countryStr));
                        
                        List<City> cities = parseCities(hotelStr, Utils.getIds(tour.getCountries()));
                        tour.setCities(cities);                        
                        
                        if(cities != null && !cities.isEmpty()){
                            tour.setHotel(parseHotel(hotelStr, hotelStr, cities.get(0).getId()));
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
            }
            
        } 
        catch(IOException ex) {                           
            LOGGER.error("Connecting error " + ex.getMessage(), ex);            
        }
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        inputString = inputString.replace(" ", "");
        inputString = inputString.replace("\u00a0", "");
        inputString = inputString.replace(",", ".");
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
        if (nameContainer != null && nameContainer.contains("*")) {
            return nameContainer.substring(0, nameContainer.indexOf("*") - 1).trim();
        }
        
        return nameContainer;
    }  

}
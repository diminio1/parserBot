/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


public class MansanaParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MansanaParser.class.getName());    
    
    private static final String website = "http://www.mansana.com/hot_propositions.html";

    public static final int SOURCE_ID = 13;
    
    public MansanaParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tables = tourDoc.select("table[width = 99%]");
            for (Element x: tables) {
                String countryStr = x.select("td").first().select("b").text().trim(); 
                
                Elements toursElems = x.select("table[width = 100%]").select("tr");  
                toursElems.remove(toursElems.size() - 1);
                
                String roomTypeStr = toursElems.last().text();
                
                toursElems.remove(toursElems.size() - 1);
                
                String firstTownStr = x.select("table[width = 100%]").select("tr").select("td[valign = top]").first().text();
                
                for (Element y: toursElems) {
                    
                    String townStr = y.select("td[valign = top]").text();
                    if (townStr.equals("")) { 
                        townStr = firstTownStr;
                    }
                    firstTownStr = townStr;
                    
                    String dateStr = y.select("td[class = lightgrey]").get(2).text();
                    
                    String hotelStr = "";
                    String linkStr = website;
                    
                    Elements idiotism = y.select("td[colspan = 2]");
                    if (idiotism.size() != 0) {
                        townStr = "";
                        hotelStr = "";
                    }
                    else {
                        hotelStr = y.select("td[width = 30%]").text();
                        linkStr = y.select("td[width = 30%]").select("a").attr("href");
                    }
                    
                    String feedPlanStr = y.select("td[class = lightgrey]").first().text();
                    String durationStr = y.select("td[class = nowrap]").text();
                    String priceStr = y.select("span[style *= color: red]").text();
                
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
        
        Pattern p = Pattern.compile("\\d+ \\p{L}+");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMMyyyy");
        try {
            Matcher m = p.matcher(inputString);
            return m.find() ? (dateFormat.parse(m.group() + Calendar.getInstance().get(Calendar.YEAR))) : null;
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        if (nameContainer.contains("*")) {
            nameContainer = nameContainer.substring(0, nameContainer.indexOf("*") - 1);
        }
                
        return nameContainer;
  
    }   

}
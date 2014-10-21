/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.banan.parser.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.banan.data.model.City;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.Parser;
import static ua.banan.parser.impl.AbstractParser.CONNECTION_TIMEOUT;

/**
 *
 * @author User
 */
public class TravelhitParser extends AbstractParser implements Parser{
    private static final Logger LOGGER = LoggerFactory.getLogger(TravelhitParser.class.getName());    
    
    private static String website = "http://www.travelhit.com.ua";
    public static final int SOURCE_ID = 1605;
    
    
    public TravelhitParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        
        List<Tour> tours = new ArrayList<>();
            
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        ArrayList<String> pages = new ArrayList<>();
            
        
        try {
            Document document = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements elements = document.select("div[class = block]").select("a[class = light]");
            
            try {
                for (Element e: elements) {
                    String site = website + e.attr("href");
                    Document tourDoc = Jsoup.connect(site).timeout(CONNECTION_TIMEOUT).get();
                    
                    Elements tables = tourDoc.select("table[class = hot");
                    for (Element x: tables) {
                        
                        String linkStr = site;
                        
                        String countryStr = x.select("td").get(1).text();
                        
                        String durationStr = countryStr;
                        
                        String roomTypeStr = "";
                        
                        String dateStr = x.select("table[class = nopad]").select("tr").get(2).select("td").get(1).text();
                        
                        String hotelStr = x.select("table[class = nopad]").select("tr").first().select("a").text();
                        
                        int stars = x.select("table[class = nopad]").select("tr").first().select("nobr").select("img").size();
                        
                        String townStr = x.select("table[class = nopad]").select("tr").get(1).select("td").get(1).text();
                        
                        String feedPlanStr = x.select("table[class = nopad]").select("tr").get(3).select("td").get(1).text();
                        
                        String priceStr = x.select("tr").first().select("td").get(2).select("a").text();
                        
                        String descriptionStr = null;
                        
                        Tour tour = new Tour();
                        
                        tour.setUrl(linkStr);
                        tour.setPrice(parsePrice(priceStr));
                        tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                        tour.setRoomType(parseRoomType(roomTypeStr));
                        tour.setNightsCount(parseNightCount(durationStr));
                        tour.setFlightDate(parseDate(dateStr));
                        tour.setDescription(descriptionStr);
                        tour.setCountries(parseCountries(countryStr));
                        tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                        
                        List<City> cities = tour.getCities();
                        if (cities != null && cities.size() == 1){
                            tour.setHotel(parseHotel(hotelStr, "" + stars, cities.get(0).getId()));
                        }
                        
                        tour.setTourOperator(tourOperator);
                        
                        tours.add(tour);
                    }
                }
            }
            catch(Exception ex) {
                LOGGER.error("Parsing error " + ex.getMessage(), ex);
            }
            
            Elements excursions = document.select("table[class = results]").select("tr");
            int counter = 0;
            for(Element excursion: excursions) {
                
                if (counter == 0) {
                    counter++;
                    continue;
                }
                
                String site = website + excursion.select("a").attr("href");
                
                String durationStr = excursion.select("td").get(1).text();
                
                Document doc = Jsoup.connect(site).timeout(CONNECTION_TIMEOUT).get();
                
                String linkStr = site;
                        
                String countryStr = doc.select("div[class = minibr]").text();
                        
                String roomTypeStr = "";
                        
                String dateStr = doc.select("table[class = prices]").select("td").first().text();
                        
                String townStr = countryStr;
                        
                String priceStr = doc.select("table[class = prices]").select("td").get(1).text();
                
                String departCityStr = doc.select("div:contains(Отправление)").text();
                
                String descriptionStr = null;
                        
                Tour tour = new Tour();
                        
                tour.setUrl(linkStr);
                tour.setPrice(parsePrice(priceStr));
                tour.setRoomType(parseRoomType(roomTypeStr));
                tour.setNightsCount(parseNightCount(durationStr));
                tour.setFlightDate(parseDate(dateStr));
                tour.setDescription(descriptionStr);
                tour.setCountries(parseCountries(countryStr));
                tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                
                tour.setTourOperator(tourOperator);
                        
                tours.add(tour);
                
            }
            
        }
        catch(IOException ex) {                           
            java.util.logging.Logger.getLogger(TravelhitParser.class.getName()).log(Level.SEVERE, null, ex);            
        }
        return tours.isEmpty() ? null : tours;
 
    }

    @Override
    protected Date parseDate(String inputString) {
        inputString = inputString.replace("\u00a0", "");
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
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}

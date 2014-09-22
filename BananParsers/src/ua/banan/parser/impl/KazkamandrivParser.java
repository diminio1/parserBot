package ua.banan.parser.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

public class KazkamandrivParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(KazkamandrivParser.class.getName());    
    
    private static final String website = "http://kazkamandriv.ua/";

    public static final int SOURCE_ID = 18;
    
    public KazkamandrivParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements excursions = tourDoc.select("tr[onclick *= urlOpen('http://kazkamandriv.ua/coach-tour-to-europe]");
            
            for (Element x: excursions) {
            	try {
                    String linkStr = x.attr("onclick");
                    linkStr = linkStr.substring(9, linkStr.length() - 3);
            		
                    String dateStr = x.select("td").first().text();
            		
                    Elements countries = x.select("a[class = tRoute]").select("img"); 
                    String countryStr = "";
                    for (Element c: countries) {
            		countryStr = countryStr + c.attr("title") + "-";
                    }
            			
                    String townStr = x.select("a[class = tRoute]").text().replace('—', '-').replace('–', '-').trim();
                    if (townStr.length() > 60) {
            		townStr = townStr.substring(0, 60);
            		townStr = townStr.substring(0, townStr.lastIndexOf("-"));
                    }
                    
                    String departCityStr = townStr.substring(0, townStr.indexOf("-")).trim();
            	
                    String durationStr = x.select("td").get(2).select("a").text();
            		
                    String priceStr = x.select("td").get(4).select("strong").text();
                    
                    String previousPriceStr = x.select("td").get(4).select("s").text();
            	
                    String roomTypeStr = "";
                    
                    Tour tour = new Tour();
                                
                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr));
                    tour.setPreviousPrice(parsePrice(previousPriceStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                    tour.setRoomType(roomTypeStr);
                    
                    List<City> departCities = parseCities(departCityStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                    if (departCities != null && !departCities.isEmpty()){
                        tour.setDepartCity(departCities.get(0));                    
                    }
                
                    tours.add(tour);
                
                    tour.setTourOperator(tourOperator);  
                }
                catch (Exception ex) {
                    LOGGER.error("Parsing error " + ex.getMessage(), ex);
                }
            }            
        }
        catch(IOException ex) {                           
            LOGGER.error("Parsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        inputString += "." + year;
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
        return null;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return null;
    }

}
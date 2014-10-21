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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class AplParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AplParser.class.getName());    
    
    private static final String website = "http://www.apltravel.ua/Novosti/Akcii/crazy-menyu.html";

    public static final int SOURCE_ID = 17;
    
    public AplParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements paragraphs = tourDoc/*.select("text")*/.select("p");
            
            String countryStr = "";
            if (paragraphs != null && paragraphs.size() > 5) {
                
                countryStr = paragraphs.get(2).text();
                
                for (int i = 3; i < paragraphs.size() - 5; i += 4) {
                
                    Element info = paragraphs.get(i);
                 
                    while (!info.text().contains("вылет")) {
                        countryStr = info.text();
                        ++i;
                        info = paragraphs.get(i);
                    }
                    
                    while (info.text().equals("")) {
                        i++;
                        info = paragraphs.get(i);
                    }
                    
                    String descriptionStr = paragraphs.get(i + 1).text();
    
                    String townStr = descriptionStr;
                    
                    String linkStr = "http://www.apltravel.ua" + paragraphs.get(i + 2).select("a").attr("href");
                    
                    String hotelStr = info.select("a").text();
                    
                    String priceStr = info.select("span[style = color: #ff0000;]").text();
                    
                    String [] inf = new String[3];
                    
                    inf = info.select("strong").first().ownText().split(",");
                    
                    String feedPlanStr = inf[2];
                    
                    String durationStr = inf[1];
                    
                    Pattern p = Pattern.compile("\\d+ \\p{L}+");
                    Matcher m = p.matcher(inf[0]);
                    
                    String dateStr = "";
                    
                    String roomTypeStr = "";
                    
                    if (m.find()) {
                        dateStr = m.group();
                    }
                    
                    String departCityStr = inf[0];
                    
                    try {
                        Document forTown = Jsoup.connect(linkStr).timeout(CONNECTION_TIMEOUT).get();
                        townStr = forTown.select("span[class = lineData]").first().text();
                    }
                    catch (IOException exception) {
                        LOGGER.error("Parsing error " + exception.getMessage(), exception);
                    }
                    
                    Tour tour = new Tour();
                                
                    tour.setUrl(linkStr);        
                    tour.setPrice(parsePrice(priceStr));
                    tour.setFeedPlan(parseFeedPlan(feedPlanStr));
                    tour.setRoomType(parseRoomType(roomTypeStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                    tour.setDescription(descriptionStr);
                    
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
            }
        }
        catch(Exception ex) {                           
            LOGGER.error("Parsing error " + ex.getMessage(), ex);            
        }
        
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        if (inputString.equals("")) {
            return null;
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
  
        inputString += year;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMMyyyy", russianDateFormatSymbols);
        try {
            return dateFormat.parse(inputString);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        if (nameContainer.contains("*")) {
            nameContainer = nameContainer.substring(0, nameContainer.indexOf("*") - 1).trim();
        }
        
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }

}

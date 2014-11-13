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
import ua.banan.data.model.Country;
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
            if (paragraphs != null && !paragraphs.isEmpty()) {                
                
                List<Country> countries = null;
                
                for (int i = 0; i < paragraphs.size(); i++) {
//                    countryStr = paragraphs.get(2).text();                    
                    
                    try{              
                        Element info = paragraphs.get(i);
                                                                       
                        String infoStr = info.text();
                        
                        List<Country> countriesNew = parseCountries(infoStr);
                        
                        if(infoStr.length() < 20 && !countriesNew.isEmpty()){
                            countries = countriesNew;
                            continue;
                        }
                        
                        if(infoStr.contains("вылет")){

//                            while (!info.text().contains()) {
//                                countryStr = info.text();
//                                ++i;
//                                info = paragraphs.get(i);
//                            }
//
//                            while (info.text().isEmpty()) {
//                                i++;
//                                info = paragraphs.get(i);
//                            }

                            String descriptionStr = paragraphs.size() < (i + 1) ? paragraphs.get(i + 1).text() : null;

                            String townStr = descriptionStr;

                            String linkStr = "http://www.apltravel.ua" + paragraphs.get(i + 2).select("a").attr("href");

                            String hotelStr = info.select("a").text();

                            infoStr = infoStr.replace(hotelStr, "").toLowerCase().replace("вылет", "").trim();


                            int fromIndex = infoStr.indexOf("из");

                            String dateStr = infoStr.substring(0, fromIndex != -1 ? fromIndex : 0).replace(' ', ' ').replaceAll("\\s+", "");

                            Pattern p = Pattern.compile("\\d+\\s?(ночи|ночей)");
                            Matcher m = p.matcher(infoStr);

                            String durationStr = "";
                            if(m.find()){
                                durationStr = m.group();
                            }

                            p = Pattern.compile("\\d+\\s?\\$");
                            m = p.matcher(infoStr);

                            String priceStr = "";
                            if(m.find()){
                                priceStr = m.group();
                            }

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
                            tour.setFeedPlan(parseFeedPlan(infoStr));
                            tour.setNightsCount(parseNightCount(durationStr));
                            tour.setFlightDate(parseDate(dateStr));
                            tour.setCountries(countries);
                            tour.setCities(parseCities(townStr, Utils.getIds(tour.getCountries())));
                            tour.setDescription(descriptionStr);

                            List<City> cities = tour.getCities();
                            if (cities != null && cities.size() == 1){
                                tour.setHotel(parseHotel(hotelStr, hotelStr, cities.get(0).getId()));
                            }

                            List<City> departCities = parseCities(infoStr, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                            if (departCities != null && !departCities.isEmpty()){
                                tour.setDepartCity(departCities.get(0));                    
                            }

                            tour.setTourOperator(tourOperator);       

                            tours.add(tour);
                        }
                    }
                    catch(Exception ex){
                        LOGGER.error("Parsing error " + ex.getMessage(), ex);   
                    }
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
        if (inputString.equals("")) {
            return null;
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
  
        inputString += year;
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMMyyyy", russianDateFormatSymbols);
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

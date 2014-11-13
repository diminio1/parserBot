/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser.impl;

import java.text.DateFormatSymbols;
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
import ua.banan.data.model.City;
import ua.banan.data.model.Country;
import ua.banan.data.model.CurrencyExchanger;
import ua.banan.data.model.Hotel;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;

/**
 *
 * @author swat
 */
public abstract class AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractParser.class.getName());    
        
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat DATE_FORMATTER2 = new SimpleDateFormat("dd.MM.yy");
    
    protected static final DateFormatSymbols russianDateFormatSymbols = new DateFormatSymbols(){

        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
        
    };
    
    protected static final DateFormatSymbols russianDateFormatSymbolsShort = new DateFormatSymbols(){

        @Override
        public String[] getMonths() {
            return new String[]{"яна", "фев", "мар", "апр", "мая", "июн",
                "июл", "авг", "сен", "окт", "ноя", "дек"};
        }
        
    };
    
    public int sourceId;    
    
    protected static final int CONNECTION_TIMEOUT = 100000;
    
    protected final DataOperator dataOperator;
    

    public AbstractParser(DataOperator dataOperator) {
        this.dataOperator          = dataOperator;
    }            
    
    
    protected Integer parsePrice(String priceString){
        if (priceString != null) {
            int currencyId = CurrencyExchanger.findMoneyCurrency(priceString);
                        
            try{
                priceString = priceString.replaceAll("\\s", "");
     
                priceString = priceString.replaceAll("\u00a0", "");
               
                Pattern pattern = Pattern.compile("\\d+");
                
                Matcher matcher = pattern.matcher(priceString);

                Integer price = matcher.find() ? parseInt(matcher.group()) : null;                                                
                
                return CurrencyExchanger.exchangeToUah(currencyId, price);
            }
            catch (NumberFormatException nfe){
                LOGGER.error("Error parsing price from string: " + priceString, nfe);
                return null;
            }
        }
        
        return null;
    }
    
    protected String parseFeedPlan(String inputString){
        inputString = inputString.toLowerCase();

        if (Pattern.compile("(включено)|(inclusive)|(ai)|(uai)").matcher(inputString).find()){
            return "AI";
        }                
        
        if (Pattern.compile("(завтрак ужин)|(два раза)|(двухразовое)|(полупансион)|(hb)").matcher(inputString).find()){
            return "HB";
        }

        if (Pattern.compile("(завтрак)|(bb)|(вв)").matcher(inputString).find()){
            return "BB";
        }

        if (Pattern.compile("(без питания)|(нет питания)|(ro)").matcher(inputString).find()){
            return "RO";
        }

        if (Pattern.compile("(пансион)|(fb)").matcher(inputString).find()){
            return "FB";
        }

        return null;
    }
    
    protected abstract Date    parseDate(String inputString);
    protected abstract String  parseHotelName(String nameContainer);
    protected Integer parseHotelStars(String starsContainer){
        if (starsContainer != null && starsContainer.contains("*")) {
            starsContainer = starsContainer.replace(" ", "");
            
            int indexOfStar = starsContainer.indexOf('*');
            Integer stars = parseInt(starsContainer.substring(Math.max(0, indexOfStar - 2), indexOfStar).trim());
            return (stars != null && stars > 0 && stars <= 5) ? stars : null;
        }
        
        return null;
    }
    
    protected Hotel parseHotel(String nameContainer, String starsContainer, Integer cityId) {
        if (cityId != null){            
            String hotelName = parseHotelName(nameContainer);
            Integer stars = parseHotelStars(starsContainer);
            
            Hotel hotel = dataOperator.getHotelByProperties(hotelName, stars, cityId);
            
            if (hotel != null){
                return hotel;
            } else if (hotelName != null && !hotelName.isEmpty()){
                hotel = new Hotel();
                
                hotel.setCityId(cityId);
                hotel.setName(hotelName);
                hotel.setStars((stars != null && stars <= 5 && stars > 0) ? stars : null);
                
                return hotel;
            }
        }
        
        return null;
    }        
    
    protected Integer parseNightCount(String inputString){
        return parseInt(inputString);
    }
    
    private List<Integer> parseTourTypes(String inputString){
        if (inputString != null && !inputString.isEmpty()){           
            String[] strArray = inputString.split(",");
            
            List<Integer> intList = new ArrayList<>();
            for (String strInt : strArray) {
                try {
                    intList.add(Integer.parseInt(strInt.trim()));
                } catch (NumberFormatException ignore) {}
            }
            
            return intList;
        }
        
        return null;
    }
    
    protected List<Country> parseCountries(String inputString){
        if (inputString != null && !inputString.isEmpty()){           
            List<Integer> countriesIDs = dataOperator.getCountryTermFilter().filter(inputString);

            if(countriesIDs != null && !countriesIDs.isEmpty()){                
                return dataOperator.getCountriesByIds(countriesIDs);
            } else {
                LOGGER.error("[NO_SYNONYMS] for country: " + inputString);            
            }
        }
        
        return null;
    }
    
    protected List<City> parseCities(String inputString, List<Integer> countriesIds){
        if (inputString != null && countriesIds != null && !countriesIds.isEmpty()){           
            List<Integer> citiesIDs = dataOperator.getCityTermFilter().filter(inputString);

            if(citiesIDs != null && !citiesIDs.isEmpty()){                
                List<City> res = new ArrayList<>();

                for(Integer id : citiesIDs){            
                    City city = dataOperator.getCityById(id);
                    Integer countryId = city != null ? city.getCountryId() : null;
                    
                    if (countryId != null && countriesIds.contains(countryId)){
                        res.add(city);
                    }                    
                }

                return res;
            } else {
                LOGGER.error("[NO_SYNONYMS] for city: " + inputString);            
            }
        }
        
        return null;
    }
    
    
    protected String parseRoomType(String inputString){
        if (inputString != null && !inputString.isEmpty()){  
            inputString = inputString.toUpperCase();
            
            if (inputString.contains("BG")){
                return "BGL";
            }            
            if (inputString.contains("SGL")){
                return "SGL";
            }
            if (inputString.contains("TRPL")){
                return "TRPL";
            }
            if (inputString.contains("QDPL")){
                return "QDPL";
            }
        }
        
        return "DBL";
    }
       
    
    public static Integer parseInt(String inputString){
        try {
            inputString = inputString.replaceAll("\\D", "");
            
            return !inputString.isEmpty() ? Integer.parseInt(inputString) : null;
        } catch (NumberFormatException nfe) {
            LOGGER.error("Error parsing Integer from String: " + inputString, nfe);
            
            return null;
        }
    }

    public int getSourceId() {
        return sourceId;
    }        
    
    
    public Tour createTour(Integer id,
                            String types,
                            String countries,
                            String citiesString,
                            String hotel,
                            String stars,
                            String room,
                            String feed,
                            String price,
                            String oldPrice,
                            String dateStart,
                            String dateEnd,
                            String departCityString,
                            String description, 
                            String url,
                            TourOperator tourOperator){
        Tour tour = new Tour();                       
        
        if(url != null && !url.isEmpty()){
            if(!url.contains("http")){
                url = "http://" + url;
            }
            
            tour.setUrl(url);
        } else {
            tour.setUrl(Utils.prepandHttpIfNotExists(tourOperator.getUrl()));
        }
                
        tour.setTypes(parseTourTypes(types));
        tour.setPrice(parsePrice(price));
        tour.setPreviousPrice(parsePrice(oldPrice));
        tour.setFeedPlan(feed);
        tour.setRoomType(room);
        
        Date startDate = null;
        Date endDate = null;
        try{
            startDate = DATE_FORMATTER2.parse(dateStart);
            endDate = DATE_FORMATTER2.parse(dateEnd);
        } catch (ParseException pe) {}
        
        if (endDate != null && startDate != null){
            tour.setNightsCount((int)(endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000));
        }
        tour.setFlightDate(startDate);
        tour.setDescription(description);
        tour.setCountries(parseCountries(countries));
        tour.setCities(parseCities(citiesString, Utils.getIds(tour.getCountries())));

        List<City> cities = tour.getCities();
        if (cities != null && cities.size() == 1){
            tour.setHotel(parseHotel(hotel, stars, cities.get(0).getId()));
        }

        List<City> departCities = parseCities(departCityString, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
        if (departCities != null && !departCities.isEmpty()){
            tour.setDepartCity(departCities.get(0));                    
        }
        
        tour.setTourOperator(tourOperator);  
        tour.setId(id);
        
        return tour;
    }
    
}

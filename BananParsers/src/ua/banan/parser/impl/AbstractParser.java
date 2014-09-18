/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.banan.data.model.City;
import ua.banan.data.model.Country;
import ua.banan.data.model.CurrencyExchanger;
import ua.banan.data.model.Hotel;
import ua.banan.data.model.common.Pair;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.SourceIdHandler;

/**
 *
 * @author swat
 */
public abstract class AbstractParser implements SourceIdHandler {
    private static final Logger LOGGER = LogManager.getLogger(AbstractParser.class.getName());    
        
    public static int sourceId;    
    
    protected static final int CONNECTION_TIMEOUT = 100000;
    
    protected final DataOperator dataOperator;
    

    public AbstractParser(DataOperator dataOperator) {
        this.dataOperator          = dataOperator;
    }            
    
    
    protected Integer parsePrice(String priceString){
        if (priceString != null) {
            int currencyId = CurrencyExchanger.findMoneyCurrency(priceString);
                        
            try{
                Integer price = Integer.parseInt(priceString.replaceAll("\\D", ""));
                
                return CurrencyExchanger.exchangeToUah(currencyId, price);
            }
            catch (NumberFormatException nfe){
                LOGGER.error("Error parsing price from string: " + priceString, nfe);
                return null;
            }
        }
        
        return null;
    }
    
    protected String  parseFeedPlan(String inputString){
        inputString = inputString.toLowerCase();

        if (Pattern.compile("(включено)|(inclusive)|(ai)|(uai)").matcher(inputString).find()){
            return "AI";
        }                
        
        if (Pattern.compile("(завтрак ужин)|(два раза)|(двухразовое)|(полупансион)|(hb)").matcher(inputString).find()){
            return "HB";
        }

        if (Pattern.compile("(завтрак)|(bb)").matcher(inputString).find()){
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
    protected abstract Integer parseHotelStars(String starsContainer);
    
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
                hotel.setStars(stars);
                
                return hotel;
            }
        }
        
        return null;
    }        
    
    protected Integer parseNightCount(String inputString){
        return parseInt(inputString);
    }
    
    protected List<Country> parseCountries(String inputString){
        if (inputString != null && !inputString.isEmpty()){
            /*
                List<Pair<CountryID, 0>>
            */
            List<Pair<Integer, Integer>> countriesIDsPairs = dataOperator.getCountryTermFilter().filter(inputString.toUpperCase());

            if(countriesIDsPairs != null && !countriesIDsPairs.isEmpty()){
                Set<Integer> setOfIds = new HashSet<>();

                for(Pair<Integer, Integer> pairID : countriesIDsPairs){
                    setOfIds.add(pairID.getFirst());
                }

                return dataOperator.getCountriesByIds(new ArrayList<>(setOfIds));
            }
        }
        
        return null;
    }
    
    protected List<City> parseCities(String inputString, List<Integer> countriesIds){
        if (inputString != null && countriesIds != null && !countriesIds.isEmpty()){
            /*
                List<Pair<CityID, CountryID>>
            */
            List<Pair<Integer, Integer>> citiesIDsPairs = dataOperator.getCityTermFilter().filter(inputString.toUpperCase());

            if(citiesIDsPairs != null && !citiesIDsPairs.isEmpty()){                
                Set<Integer> setOfCitiesIds = new HashSet<>();

                for(Pair<Integer, Integer> pairID : citiesIDsPairs){                    
                    Integer countryId = pairID != null ? pairID.getSecond() : null;
                    
                    if(countryId != null && countriesIds.contains(countryId)){                    
                        setOfCitiesIds.add(pairID.getFirst());
                    }
                }

                return dataOperator.getCitiesByIds(new ArrayList<>(setOfCitiesIds));
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
        try{
            return Integer.parseInt(inputString.replaceAll("\\D", ""));
        } catch (NumberFormatException nfe){
            LOGGER.error("Error parsing nights count from string: " + inputString, nfe);
            
            return null;
        }
    }

    @Override
    public int getSourceId() {
        return sourceId;
    }        
}

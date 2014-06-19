/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rita.blowup.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import main.parser.com.TourObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import term.filter.parser.TermFilter;

/**
 *
 * @author Маргарита
 */
public class UniversalParser {
    
    
    public static TourObject parseTour(Parsable country, String countryStr,
                                       Parsable town, String townStr,
                                       Parsable hotel, String hotelStr,
                                       Parsable departDate, String departDateStr,
                                       Parsable departCity, String departCityStr,
                                       Parsable duration, String durationStr,
                                       Parsable price, String priceStr,
                                       Parsable stars, String starsStr,
                                       Parsable link, String linkStr,
                                       Parsable nutrition, String nutritionStr,
                                       Parsable roomType, String roomTypeStr,
                                       int source,
                                       TermFilter countryStand,
                                       TermFilter cityStand,
                                       FileWriter bananLog,
                                       String message) {
        try {
            TourObject tObj = new TourObject();
                
            tObj.setCountry((String)country.get(countryStr), countryStand, bananLog);
            
            try {
            	tObj.setHotel((String)hotel.get(hotelStr));
            }
            catch(NullPointerException ex) {
            	tObj.setHotel(null);
            }
            
            tObj.setDepartCity((String)departCity.get(departCityStr));
                
            tObj.setDuration((int)duration.get(durationStr));
                
            tObj.departDate = (Date)departDate.get(departDateStr);
            
            if(tObj.departDate == null) {
            	return null;
            }
            if((DateEdit.before(tObj.departDate, new Date()))) {
            	return null;
            }
            tObj.setStars((int)stars.get(starsStr));
                
            tObj.setTown((String)town.get(townStr), cityStand, message, bananLog);

            tObj.setPrice((int)price.get(priceStr));
                
            tObj.setLink((String)link.get(linkStr));
                
            tObj.setSource(source);
                
            tObj.setRoomType((String)roomType.get(roomTypeStr));
                
            tObj.setNutrition((String)nutrition.get(nutritionStr));
                
            return tObj;
        }
        catch (NullPointerException ex) {
            System.out.println("Caught Null Pointer Exception!");
            return null;
        }
    }
    
}
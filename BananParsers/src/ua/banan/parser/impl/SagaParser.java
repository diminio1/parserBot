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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

/**
 *
 * @author User
 */
public class SagaParser extends AbstractParser implements Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaParser.class.getName());    
    
    private static final String website = "http://www.saga.ua/2.html";
    public static final int SOURCE_ID = 1611;

    public SagaParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

    @Override
    public List<Tour> parseTours() {
        
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);

        try {
            
            Document document = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements countryBlocks = document.select("td[style = width: 450px;]");
            
            for (Element countryBlock: countryBlocks) {
                
                String countryStr = countryBlock.select("div[class = country]").text();
                String town = countryBlock.select("div[class = title]").text();
                String info = countryBlock.text().replace("\u00a0", " ");
                
                Pattern p = Pattern.compile("\\d\\d\\.\\d\\d");
                Matcher m = p.matcher(info);
                String dateStr = m.find() ? m.group() : null;
                
                p = Pattern.compile("\\d+ +ноч");
                m = p.matcher(info);
                String durationStr = m.find() ? m.group() : null;
                
                p = Pattern.compile("\\p{L}[\\p{L} ]+\\d\\*");
                m = p.matcher(info);
                
                ArrayList<String> hotels = new ArrayList<>();
                while (m.find()) {
                    hotels.add(m.group());
                } 
                
                p = Pattern.compile("(\\d[\\d ]+(EUR|USD|долл|евро))");
                m = p.matcher(info);
                
                ArrayList<String> prices = new ArrayList<>();
                while (m.find()) {
                    prices.add(m.group());
                } 
                
                p = Pattern.compile("(В стоимост((.)|\\n)*Дополнительно)");
                m = p.matcher(info);
                
                String descriptionStr = m.find() ? m.group().replace("Дополнительно", "") : null;
                
                for (int i = 0; i < hotels.size(); ++i) {

                    Tour tour = new Tour();

                    tour.setUrl(website);
                    
                    String price = prices.get(i);
                    if (!(price == null)) {
                        price = price.replace("евро", "€");
                        tour.setPrice(parsePrice(price.replace("долл", "USD")) / 2);
                    }
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(hotels.get(i) + " " + town, Utils.getIds(tour.getCountries())));
                    tour.setRoomType("");
                    tour.setFlightDate(parseDate(dateStr));
                    tour.setDescription(descriptionStr);
                    
                    List<City> cities = tour.getCities();
                    if (cities != null && cities.size() == 1){
                        tour.setHotel(parseHotel(hotels.get(i), hotels.get(i), cities.get(0).getId()));
                    }
                
                    tour.setTourOperator(tourOperator);                                
  
                    tours.add(tour);
                    
                }
                
            }

        }
        catch (IOException ex) {
            LOGGER.error("Connecting error " + ex.getMessage(), ex);            

        }
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
        if (inputString == null) {
            return null;
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MMyyyy");
        try {
            return dateFormat.parse(inputString + year);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        if (nameContainer.contains("Отель")) {
            int pos = nameContainer.indexOf("Отель") + 5;
            nameContainer = nameContainer.substring(pos).trim();
        }
        return nameContainer.substring(0, nameContainer.indexOf("*") - 2);
    }
    
}

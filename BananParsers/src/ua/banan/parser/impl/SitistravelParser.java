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
public class SitistravelParser extends AbstractParser implements Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SitistravelParser.class.getName());    
    
    private static final String website = "http://sitistravel.com/japonia";
    public static final int SOURCE_ID = 1613;

    
    public SitistravelParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);

        try {
            
            Document document = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements tourBlocks = document.select("div[class = search_tur_view]");
            
                for (Element tourBlock: tourBlocks) {
                
                    String countryStr = "Japan";
                    String town = tourBlock.text().replace("\u2013", " ");
                    if (town.contains("Тур и маршрут Длительность Даты Стоимость")) {
                        continue;
                    }
                
                    String info = tourBlock.text().replace("\u00a0", " ");
                
                    String  dateStr = tourBlock.select("span[style *= font-size:10px]").text().replace("\u2013", " ");
                    if (dateStr.contains("ежедневно")) {
                        break;
                    }
                    
                    
                    Pattern p = Pattern.compile("\\d+ +ноч");
                    Matcher m = p.matcher(info);
                    String durationStr = m.find() ? m.group() : null;
                
                
                    p = Pattern.compile("(\\d[\\d ]+USD)");
                    m = p.matcher(info);
                
                    String priceStr = m.find() ? m.group() : null;
                
                    Tour tour = new Tour();

                    tour.setUrl(website);
                    tour.setPrice(parsePrice(priceStr));
                    tour.setNightsCount(parseNightCount(durationStr));
                    tour.setCountries(parseCountries(countryStr));
                    tour.setCities(parseCities(town, Utils.getIds(tour.getCountries())));
                    tour.setRoomType(parseRoomType(""));
                    tour.setFlightDate(parseDate(dateStr));
                    
                    tour.setTourOperator(tourOperator);                                
  
                   tours.add(tour);
                    
                }
        }
        catch (IOException ex) {
            LOGGER.error("Connecting error " + ex.getMessage(), ex);            

        }
        return tours.isEmpty() ? null : tours;
    }
    
    @Override
    protected Date parseDate(String inputString) {
        
        Date today = new Date();
        
        Pattern p = Pattern.compile("\\d\\d\\.\\d\\d\\.\\d\\d");
        Matcher m = p.matcher(inputString);
        String fromStr = m.find() ? m.group() : null;
        
        String toStr = m.find() ? m.group() : null;
        
        Date from, to;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        try {
            if (fromStr == null) {
                return null;
            }
            from = dateFormat.parse(fromStr);
        } 
        catch (ParseException ex) {
            LOGGER.error("Parsing date error");
            return null;
        }
        try {
            if ((toStr == null)) {
                return from;
            }
            to = dateFormat.parse(toStr);
            
        } 
        catch (ParseException ex) {
            LOGGER.error("Parsing date error");
            return from; 
        }
        while (from.before(to)) {
            if (!from.after(today)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(from);
                calendar.add(Calendar.DATE, 1);
                from = calendar.getTime();
            }
            else {
                return from;
            }
        }
        return null;
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.banan.parser.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import static ua.banan.parser.impl.AbstractParser.CONNECTION_TIMEOUT;

/**
 *
 * @author User
 */
public class MixTourParser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MixTourParser.class.getName());    
    
    private static final String website = "http://www.mix-tour.com.ua";

    public static final int SOURCE_ID = 1604;
    
    public MixTourParser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }
            
    @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        String page = website;
        
        do {
            
            try {    
                Document tourDoc = Jsoup.connect(page).timeout(CONNECTION_TIMEOUT).get();
                
                Elements blocks = tourDoc.select("div[class = djcat_blog_item_in]");

                for (Element tourBlock: blocks) {
                    
                    String countryStr = tourBlock.select("span[class = djcat_producer]").text().replace("/", " ");
                    
                    String townStr = countryStr;
                    
                    String tmp = tourBlock.select("span[style = color: #ff0000;]").text().replace("\u00a0", "");
                    
                    String priceStr = null;
                    
                    String durationStr = null;
                    
                    Pattern price = Pattern.compile("\\d+ +у.е.");
                    Pattern duration = Pattern.compile("\\d+ ноч");
                    
                    Matcher m = price.matcher(tmp);
                    if (m.find()) {
                        priceStr = m.group();
                    }
                    priceStr = priceStr.replace("у.е.", "$");
                    
                    m = duration.matcher(tmp);
                    if (m.find()) {
                        durationStr = m.group();
                    }
                    
                    String link = tourBlock.select("div[class = djcat_intro_readmore]").select("a").attr("href");
                    link = link.substring(1);
                    
                    String text = link.substring(0, link.indexOf('/'));
                    link = link.substring(link.indexOf('/'));
                    
                    String linkStr = website + "/" + URLEncoder.encode(text, "UTF-8") + link;
                    
                    try {
                        Document document = Jsoup.connect(linkStr)/*.ignoreHttpErrors(true)*/.timeout(CONNECTION_TIMEOUT).get();
                        
                        String hotelStr = document.select("h3").first().text();
                        
                        String dateStr = null;
                        
                        String feedPlanStr = null;
                        
                        String roomTypeStr = "";
                        
                        String inf = document.select("div[class = djcat_description]")/*.select("p").first()*/.text();
                        
                        Pattern date = Pattern.compile("\\d\\d.\\d\\d.\\d\\d\\d\\d");
                        Pattern feed = Pattern.compile("итание: \\p{L}+");
                        
                        m = date.matcher(inf);
                        if(m.find()) {
                            dateStr = m.group();
                        }
                        
                        m = feed.matcher(inf);
                        if(m.find()) {
                            feedPlanStr = m.group();
                        }
                        
                        Tour tour = new Tour();
                                
                        tour.setUrl(linkStr);        
                        tour.setPrice(parsePrice(priceStr) / 2);
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
    
                        List<City> departCities = parseCities("Киев", Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                        if (departCities != null && !departCities.isEmpty()){
                            tour.setDepartCity(departCities.get(0));                    
                        }
 
                        tours.add(tour);
                
                        tour.setTourOperator(tourOperator);                                
                
                    }   
                    catch (IOException ioe) {
                        LOGGER.error("Parsing error " + ioe.getMessage(), ioe);            
                    }
                }     
        
                page = website + tourDoc.select("li:contains(Следующая)").select("a").attr("href");
            }
            catch (Exception e) {
                LOGGER.error("Parsing error " + e.getMessage(), e);            
            }
        }
        while(!page.equals(website));
            
        return tours.isEmpty() ? null : tours;
    }

    @Override
    protected Date parseDate(String inputString) {
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

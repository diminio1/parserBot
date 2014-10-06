package ua.banan.parser.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

/**
 *
 * @author Маргарита
 */
public class Tur777Parser extends AbstractParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tur777Parser.class.getName());    
    
    private static final String website = "http://xn-----flcbkcrfr7aphd1admd3hyb7d.777tur.com/";

    public static final int SOURCE_ID = 19;
    
    public Tur777Parser(DataOperator dataOperator) {
        super(dataOperator);
        sourceId = SOURCE_ID;
    }

        @Override
    public List<Tour> parseTours() {
        List<Tour> tours = new ArrayList<>();
        
        TourOperator tourOperator = dataOperator.getTourOperatorById(sourceId);
        
        try {
            Document tourDoc = Jsoup.connect(website).timeout(CONNECTION_TIMEOUT).get();
            
            Elements countries = tourDoc.select("table[style = margin-top: 0px;]").select("tbody").select("tr").select("td").select("a");

            for (Element country: countries) {
            	
            	String page = "http://xn-----flcbkcrfr7aphd1admd3hyb7d.777tur.com" + country.attr("href");
            	Document countryDoc = null;
            	try {
                    countryDoc = Jsoup.connect(page).timeout(CONNECTION_TIMEOUT).ignoreHttpErrors(true).get();
            		
                    Elements tables = countryDoc.select("table[class = box_table");
            		
                    Elements headers = countryDoc.select("table[class = box_heder2");
            		
                    for (int i = 0; i < tables.size(); ++i) {

                        String countryStr = headers.get(i).select("tbody").select("tr").select("td").text().trim();
            		
            		String nutritionStr = tables.get(i).select("ul").text();
            			
            		String dateStr = tables.get(i).select("tbody").select("tr").select("td").get(2).select("strong").first().text();
            		
            		String durationStr = tables.get(i).select("tbody").select("tr").select("td").get(3).select("span").first().ownText();
            			
            		String priceStr = tables.get(i).select("tbody").select("tr").select("td").get(3).select("span").get(1).ownText();
            			
            		String linkStr = tables.get(i).select("tbody").select("tr").get(1).select("td").get(1).select("a").attr("href");

                        if (!linkStr.contains("http://")) {
                            linkStr = "http://xn-----flcbkcrfr7aphd1admd3hyb7d.777tur.com" + linkStr;
                        }
                        
                        String roomTypeStr = "";
                        
                        Tour tour = new Tour();
                                
                        tour.setUrl(linkStr);        
                        tour.setPrice(parsePrice(priceStr));
                        tour.setFeedPlan(parseFeedPlan(nutritionStr));
                        tour.setNightsCount(parseNightCount(durationStr));
                        tour.setFlightDate(parseDate(dateStr));
                        tour.setCountries(parseCountries(countryStr));
                        tour.setCities(parseCities(countryStr, Utils.getIds(tour.getCountries())));
                        tour.setRoomType(roomTypeStr);
                        
                        List<City> cities = tour.getCities();
                        if (cities != null && cities.size() == 1){
                            tour.setHotel(parseHotel(countryStr, countryStr, cities.get(0).getId()));
                        }
                
                        tours.add(tour);
                
                        tour.setTourOperator(tourOperator);                                
                    }
                }
                catch(Exception ex) {                           
                    LOGGER.error("Parsing error " + ex.getMessage(), ex);            
                }
            }
        }
        catch(IOException ex) {
            LOGGER.error("СConnecting error " + ex.getMessage(), ex);            
        }
        return tours.isEmpty() ? null : tours;

    }
    
    @Override
    protected Date parseDate(String inputString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        int year = Calendar.getInstance().get(Calendar.YEAR);
        try {
            return dateFormat.parse(inputString + "." + year);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        if (nameContainer.contains(","))
            nameContainer = nameContainer.substring(0, nameContainer.indexOf(","));
        if (nameContainer.contains("*")) {
            return nameContainer.substring(0, nameContainer.indexOf("*") - 1).trim();
        }        
        return nameContainer;
    }   

}

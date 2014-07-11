package hottours.parser.com;

import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import banan.file.writer.BananFileWriter;
import pair.parser.Pair;
import term.filter.parser.TermFilter;
import main.parser.com.TourObject;
import main.parser.com.Parsers;
import money.currency.Currency;

public class HottoursParser {

	public ArrayList <TourObject> tours;
	private static final int    source = 5;
	
	@SuppressWarnings("deprecation")
	public HottoursParser(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
		
		try {
					
			bananLog.write(null, "Hottours start!\n");
			
			tours = new ArrayList<TourObject>();
			
			Document hotTourDoc = null;
			
			try {
				hotTourDoc = Jsoup.connect("http://www.hottour.com.ua/tours").timeout(5000).get();
			}
			catch (IOException e) {
				//e.printStackTrace();
	        	if(e.getMessage() != null){
					
					bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");
				}
				else{
					
					bananLog.write(null, bananLog.bananStackTraceToString(e) + " \n");
				}
			}
			
			if (hotTourDoc != null){
				
				// needed jsoup elements
				Elements countries          = hotTourDoc.select("span[class = country]");
				Elements towns              = hotTourDoc.select("span[class = region]");
				Elements hotelStars         = hotTourDoc.select("span[class = hotel]");		
				Elements links              = hotTourDoc.select("a[href ^= /tour?]");
				Elements roomTypeNutritions = hotTourDoc.select("span[class = room]");
				Elements departDates        = hotTourDoc.select("span[class = data]");
				Elements durations          = hotTourDoc.select("span[class = night]");
				Elements departCities       = hotTourDoc.select("span[class = departure]");
				Elements prices             = hotTourDoc.select("span[class = cost]");
				
				Elements previousPrices     = hotTourDoc.select("span[class = old]");
				
				int counter = 0;
				
				ArrayList <String> countryStr = new ArrayList <String> ();
				
				for (Element country : countries) {
					
					if (country != null){
						
						countryStr.add(country.ownText().toUpperCase());
						
						counter ++;
					}
					else{
	//					System.out.println("WARNING: No html element!" + 
	//							Thread.currentThread().getStackTrace()[2].getLineNumber());
						bananLog.write(null, "WARNING: No html element country!\n");
					}
					
				}
				
				counter = 0;
				
				for (Element town : towns) {
					
					String countryPlusTown = new String(countryStr.get(counter) + " " + town.ownText().toUpperCase());
					
					TourObject tour = new TourObject();
					
					if(town != null){
						
						tour.setCountry(countryPlusTown, countryStand, bananLog);
	//					ArrayList<Pair<Integer, Integer>> niceTmp = (ArrayList<Pair<Integer, Integer>>) countryStand.filter(countryPlusTown);
	//					
	//					if(niceTmp != null){
	//						// to dell all repeated elems
	//						Set <Pair<Integer, Integer>> correctSet = new HashSet <Pair<Integer, Integer>> (niceTmp);
	//						tour.country = new ArrayList<Pair<Integer, Integer>> (correctSet);
	//						if(tour.country == null) System.out.println("The problem: " + countryPlusTown);
	//					}
					
	//					ArrayList <Pair<Integer, Integer>> cityTmp = (ArrayList <Pair<Integer, Integer>>)cityStand.filter(countryPlusTown);
	//					
	//					if (cityTmp != null && tour.country.size() != 0){
	//						
	//						ArrayList<Pair<Integer, Integer>> niceTowns = new ArrayList<Pair<Integer, Integer>> ();
	//						
	//						for (Pair <Integer, Integer> localCityPair : cityTmp){
	//							
	//							int flag = 0;
	//							
	//							for (Pair <Integer, Integer> localCountryPair : tour.country) {
	//								
	//								if (localCityPair.getSecond().equals(localCountryPair.getFirst())) {
	//									
	//									flag ++;
	//								}
	//							}
	//							
	//							if (flag != 0) {
	//								
	//								niceTowns.add(localCityPair);
	//							}
	//						}
	//						
	//						// to dell all repeated elems
	//						Set <Pair<Integer, Integer>> correctSetTown = new HashSet <Pair<Integer, Integer>> (niceTowns);
	//						
	//						System.out.println("Hottour.com.ua: " + town.ownText() + " " + correctSetTown);
	//						
	//						tour.town = new ArrayList<Pair<Integer, Integer>> (correctSetTown);
						
						tour.setTown(countryPlusTown, cityStand, "Hottour.com.ua: ", bananLog);
						
						tours.add(counter, tour);
						
						counter ++;
					}
					else{
	//					System.out.println("WARNING: No html element!" + 
	//							Thread.currentThread().getStackTrace()[2].getLineNumber());
						bananLog.write(null, "WARNING: No html element town!\n");
					}
				}
				
				counter = 0;
				
				for (Element hotelStar : hotelStars) {
					
					if (hotelStar != null){
					
						StringBuffer tmp = new StringBuffer(hotelStar.ownText().toUpperCase());
						
						int i = 0;
						
						StringBuffer hotelBuffer = new StringBuffer();
						
						while (tmp.charAt(i) != ',' && !((int)tmp.charAt(i) >= 48 && (int)tmp.charAt(i) <= 57)) {
							
								if ((int)tmp.charAt(i) == 39){
								
									hotelBuffer.append('"');
								
								i ++;
								
								if (i == tmp.length()) {
									
									break;
								}
							}
							
								hotelBuffer.append(tmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).setHotel(hotelBuffer.toString());
						
						if(tours.get(counter).hotel.isEmpty())
							tours.get(counter).hotel = null;
						
						if (tours.get(counter).hotel.equals("ОТЕЛЬ") || 
							tours.get(counter).hotel.equals("ОТЕЛИ")) {
						
							tours.get(counter).hotel = null;
						}
						
						while (tmp.charAt(i) == ' ' || tmp.charAt(i) == ',') {i ++;}
						
						if ((int)tmp.charAt(i) >= 48 && (int)tmp.charAt(i) <= 57) {
						
							tours.get(counter).setStars((int)tmp.charAt(i) - 48);
						}
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element hotelStar!\n");
					}
				}
				
				counter = 0;
				
				for (Element link : links) {
					
					if(link != null){
					
						tours.get(counter).setLink(("http://www.hottour.com.ua" + link.attr("href")));
						
						tours.get(counter).setSource(source);
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element link!\n");
					}
				}
				
				counter = 0;
				
				for (Element roomTypeNutrition : roomTypeNutritions) {
					
					if(roomTypeNutrition != null){
				
						StringBuffer tmp = new StringBuffer(roomTypeNutrition.ownText());
						
						int i = 0;
						
						StringBuffer roomTypeBuffer = new StringBuffer();
						
						while (tmp.charAt(i) != ',') {
							
							roomTypeBuffer.append(tmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).setRoomType(roomTypeBuffer.toString());
						
						i = tmp.length() - 1;
						
						StringBuffer nutritionBuffer = new StringBuffer();
						
						while (tmp.charAt(i) != ',') {
							
							nutritionBuffer.insert(0, tmp.charAt(i));
							
							i --;
						}
						
						tours.get(counter).setNutrition(nutritionBuffer.deleteCharAt(0).toString());
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element roomTypeNutrition!\n");
					}
				}
				
				counter = 0;
				
				for (Element departDate : departDates) {
					
					if(departDate != null){
						StringBuffer tmp = new StringBuffer(departDate.ownText());
						
						int i = 0;
						
						StringBuffer dayTmp = new StringBuffer(); 
						
						while (tmp.charAt(i) != '.') {
							
							dayTmp.append(tmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).departDate.setDate(Integer.parseInt(dayTmp.toString()));
						
						i ++;
						
						StringBuffer monthTmp = new StringBuffer();
						
						while (tmp.charAt(i) != '.') {
							
							monthTmp.append(tmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).departDate.setMonth(Integer.parseInt(monthTmp.toString()) - 1);
						
						i ++;
						
						StringBuffer yearTmp = new StringBuffer();
						
						while (tmp.charAt(i) != ' ') {
							
							yearTmp.append(tmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).departDate.setYear(Integer.parseInt(yearTmp.toString()) - 1900);
			
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element departDate!\n");
					}
				}
				
				counter = 0;
				
				for (Element duration : durations) {
					
					if(duration != null){
	
						StringBuffer durationTmp = new StringBuffer(duration.ownText().toUpperCase());
						
						int i = 0;
						
						while (!((int)durationTmp.charAt(i) >= 48 && (int)durationTmp.charAt(i) <= 57)) {i ++;}
						
						StringBuffer numTmp = new StringBuffer();
						
						while (((int)durationTmp.charAt(i) >= 48 && (int)durationTmp.charAt(i) <= 57)) {
							
							numTmp.append(durationTmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).setDuration(Integer.parseInt(numTmp.toString()));
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element duration!\n");
					}
				}
				
				counter = 0;
				
				for (Element departCity : departCities) {
					
					if(departCity != null){
					
						StringBuffer cityTmp = new StringBuffer(departCity.ownText().toUpperCase());
						
						int i = 0;
						
						StringBuffer departCityBuffer = new StringBuffer();
						
						while (cityTmp.charAt(i) != '(') {
							
							departCityBuffer.append(cityTmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).setDepartCity(departCityBuffer.toString());
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element departCity!\n");
					}
				}
				
				counter = 0;
				
				for (Element price : prices) {
					
					if(price != null){
						StringBuffer tmp = new StringBuffer(price.ownText());
						
						StringBuffer priceTmp = new StringBuffer();
						
						int  i = 0;
						
						while (((int)tmp.charAt(i) >= 48 && (int)tmp.charAt(i) <= 57)) {
							
							priceTmp.append(tmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter).setPrice(Integer.parseInt(priceTmp.toString()));
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element price!\n");
					}
				}
				
				counter = 0;
				
				for (Element previousPrice : previousPrices) {
					
					if(previousPrice != null){
						
						String tmp = previousPrice.select("s").text();
						
						tmp = tmp.replace("\u00a0", "");
						
						String priceTmp = tmp.replace("грн", "");
						priceTmp = priceTmp.replace("$", "");
						priceTmp = priceTmp.replace("€", "");
						
						Currency c = new Currency();
						int price = Integer.parseInt(priceTmp);
						if (tmp.contains("$"))
							price = (int) (price * c.dollar);
						if (tmp.contains("€"))
							price = (int) (price * c.euro);
						
						tours.get(counter).setPreviousPrice(price);
						
						counter ++;
					}
					else{
						bananLog.write(null, "WARNING: No html element previousPrice!\n");
					}
				}
			}
		} catch (Exception ex) {
			// TODO: handle exception
			
        	if(ex.getMessage() != null){
				
				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
			}
			else{
				
				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
			}
		}
	}
}


















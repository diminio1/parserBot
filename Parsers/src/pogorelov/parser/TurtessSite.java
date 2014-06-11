package pogorelov.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import term.filter.parser.TermFilter;
import main.parser.com.*;
/**
 * @author Pogorelov
 *
 */
public class TurtessSite extends AbstractSite {
	
	private String regexHotel = "k\">([A-Za-z]+\\s?){1,}";
	private String regexStars = ",\\s\\d[*]";
	private String regexDuration = "ter\">(\\d+)</td>";
	private String regexLink = "<td><a\\shref=\"h(.+)\"\\s";
	private String regexDepartCity = "ter\">(.+),\\s";
	private String regexNutrition = "r\">[A-Z]{1}(.+)<";
	private String regexDate = "rev=\"(.+)\"\\sh";
	private String regexPrice = ";\">(.+)</a>";
	
	private static final int    source = 7;
	
	public TurtessSite() {
		setURL("http://www.turtess.com");
	}
	
	public void parseHTML(TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		try {
			//System.out.println("Connection to " + this.URL + "...");
			Document doc = Jsoup.connect("http://www.turtess.com/ru/price/hotproposal/index/source/hot/countryId/all").timeout(50000).get();
			//System.out.println(this.URL + "... loaded.");
			Elements element = doc.getElementsByClass("content_table");
			
			String result = "";
			List<String> dataTours = new ArrayList<>();
			Pattern pattern = Pattern.compile("<td" + "(.+\\s+)?");
			Matcher machPat = pattern.matcher(element.toString());
			while(machPat.find()) {
				result = machPat.group().trim();
				dataTours.add(result);
			}
			
			StringBuilder adder = new StringBuilder();

			for(int i = 0; i < dataTours.size(); i++) {
				if((i%7 == 0 && i > 0) || i == dataTours.size()-1) {
					splitAndAddTour(adder.toString(), countryStand, cityStand, bananLog);
					adder = new StringBuilder();
				}
				adder = adder.append(dataTours.get(i) + "\n");				
			}
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void splitAndAddTour(String elem, TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		TourObject tour = new TourObject();
		try {
			
			tour.setSource(source);
			tour.setRoomType("DBL");
			String link = getLink(elem, regexLink);
			if(link == null) {
				tour.setLink(null);
			} else {
				tour.setLink(link.substring(13,  link.length()-2));
			}
			
			String hotel = getHotel(elem, regexHotel);
			if(hotel == null) {
				tour.setHotel(null);
			} else {
				tour.setHotel(hotel.substring(3).toUpperCase());
			}
			
			String stars = getStars(elem, regexStars);
			if(stars == null) {
				tour.setStars(null);
			} else {
				tour.setStars(new Integer(stars.substring(2, stars.length()-1)));
			}
			
			String country = getCountry(elem);
			if(country == null) {
				tour.setCountry("", countryStand, bananLog);
			} else {
				tour.setCountry(country.toUpperCase(), countryStand, bananLog);
			}
			
			String town = getTown(elem);
			if(town == null) {
				tour.setTown("", cityStand, "TutTess: ", bananLog);
			} else {
				tour.setTown(town.toUpperCase(), cityStand, "TurTess: ", bananLog);
			}
			
			String departCity = getDepartCity(elem, regexDepartCity);
			if(departCity == null) {
				tour.setDepartCity(null);
			} else {
				tour.setDepartCity(departCity.substring(5, departCity.length()-2).toUpperCase());
			}
			
			String duration = getDuration(elem, regexDuration);
			if(duration == null) {
				tour.setDuration(null);
			} else {
				tour.setDuration(new Integer(duration.substring(5, duration.length()-5)));
			}
			
			String nutritionParse = getNutrition(elem, regexNutrition);
			if(nutritionParse == null) {
				tour.setNutrition(null);
			} else {
				String nutrition = nutritionParse.substring(3, nutritionParse.length()-1).replaceAll("(&amp;)", "&");
				if(nutrition.equals("не предоставляется")) nutrition = "BO";
				if(nutrition.equals("Bed & Breakfast")) nutrition = "BB";
				if(nutrition.equals("Halfboard")) nutrition = "HB";
				if(nutrition.equals("Fullboard")) nutrition = "FB";
				if(nutrition.equals("All Inclusive") || nutrition.equals("24A")) nutrition = "AI";
				if(nutrition.equals("Ultra All Inclusive") || nutrition.equals("HAI") || nutrition.equals("SAI")) nutrition = "UAI";
				tour.setNutrition(nutrition);
			}
			
			String dd = getDate(elem, regexDate);
			String mm = getDate(elem, regexDate);
			String date;
			if(mm == null && dd == null) {
				date = null;
			} else {
				date = dd.substring(dd.length()-5, dd.length()-3) + "." + mm.substring(mm.length()-8, mm.length()-6);
			}
			tour.setDepartDate(date);
			
			String price = getPrice(elem, regexPrice);
			if(price == null) {
				tour.setPrice(null);
			} else {
				tour.setPrice(new Integer(price.substring(3, price.length()-4).replaceAll("(\\s)?", "")));
			}
			
			listOfHotTours.add(tour);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	

	private static String getTown(String s) {
		String result = "";
		Pattern pattern = Pattern.compile("blank\">(.+)</a><");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
			result = result.substring(7, result.length()-5);
		}
		return result;
	}
	
	private static String getCountry(String s) {
		String result = "";
		Pattern pattern = Pattern.compile("</a>,(.+)</td>");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
			result = result.substring(6, result.length()-5);
		}
		return result;
	}
	
}
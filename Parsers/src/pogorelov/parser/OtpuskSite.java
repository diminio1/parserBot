/**
 * 
 */
package pogorelov.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import term.filter.parser.TermFilter;
import main.parser.com.*;
/**
 * @author Pogorelov
 *
 */
public class OtpuskSite extends AbstractSite {

	private String regexNutriton = "class=\"st-b\">([A-Za-z]+)";
	private String regexStars = "name\">(.+)<";
	private String regexPrice = ">(.+)&";
	private String regexLink = "\" href=\"(.+)(\\d|\\w)\"(\\s|>)";
	private String regexDuration = "st-b\">(\\s+)?\\d+";
	private String regexHotel = "name\">(.+)<";
	private String regexDate = "<br />\\s?\\d{2}(.+)";
	private String regexTownHottour = ">\\s{2}(.)+\\s{2}";
	
	private static final int    source = 9;

	private List<String> tourTitle = new ArrayList<>();

	public OtpuskSite() {
		setURL("http://www.otpusk.com");
	}
	
	public List<TourObject> getToursList(TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		this.parseHTML(countryStand, cityStand, bananLog);
		return listOfHotTours;
	}
	
	public void parseHTML(TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		try {
			
			//System.out.println("Connection to " + this.URL + "...");
			Document doc = Jsoup.connect(this.URL).timeout(50000).get();
			//System.out.println(this.URL + "... loaded.");
			
			
			List<String> listURL = new ArrayList<>();
			Elements elemLink = doc.getElementsByClass("span8");
			for(int i = 0; i < elemLink.size(); i++) {
				String result = getLink(elemLink.get(i).toString(), regexLink);
				if(result.contains("/tour/goto?")) {
					result = splitGoToTour(result);
					if(result != null) listURL.add(result);	
				} else {
					listURL.add(this.URL + result.substring(8, result.length()-2));	
				}
			}
			
			TourObject tour;
			
			Elements elem1 = doc.getElementsByClass("tour");
			if(elem1.size() == listURL.size()) {
				for(int i = 0; i < elem1.size(); i++) {
					tour = new TourObject();
					if(listURL.get(i).contains("www.otpusk.com")) {
						splitAndAddTour(doc, elem1.get(i), tour);
						splitAndAddTourWithURL(listURL.get(i), tour, i, countryStand, cityStand, bananLog);
					} else {
						splitAndAddTour(doc, elem1.get(i), tour);
						splitFromHottourAndAdd(listURL.get(i), tour, i, countryStand, cityStand, bananLog);
						tour.setSource(source);
					}
					listOfHotTours.add(tour);
				}
			}
			

			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void splitAndAddTour(Document doc, Element elem, TourObject tour) {
		try {
			
			String[] month = {"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг",
					"сен", "окт", "ноя", "дек"};
			
			String nutrition = getNutrition(elem.toString(), regexNutriton);
			if(nutrition == null) {
				tour.setNutrition(null);
			} else {
				tour.setNutrition(nutrition.substring(13));
			}
			
			String titleParse = elem.getElementsByClass("hottour-title").toString();									//special for get Hotel stars
			if(titleParse != null) {
				String title = getTitle(titleParse);
				tourTitle.add(title);
				String starsParse = getStars(title, "\\s\\d");
				if(starsParse == null) {
					tour.setStars(0);
				} else {
					Integer stars = new Integer(starsParse.substring(1));
					tour.setStars(stars);
				}
			}
			
			Integer countPeople = getCountPeople(doc);
			String price = getPrice(elem.toString(), regexPrice);
			if(price == null) {
				tour.setPrice(0);
			} else if(countPeople != null){
				Integer priceTour = new Integer(price.substring(1, price.length()-1).replaceAll("\\W", ""));
				tour.setPrice(priceTour/countPeople);
			} else {
				Integer priceTour = new Integer(price.substring(1, price.length()-1).replaceAll("\\W", ""));
				tour.setPrice(priceTour);
			}
			
			String duration = getDuration(elem.toString(), regexDuration);
			if(duration== null) {
				tour.setDuration(0);
			} else {
				tour.setDuration(new Integer(duration.substring(6).replaceAll("(\\s+)", "")));
			}
			
			
			String date = getDate(elem.toString(), regexDate);
			if(date == null) {
				tour.setDepartDate(null);
			} else if(date.length() > 5) {
				String mm = date.substring(date.length()-4, date.length()-1);
				for(int i = 0; i < month.length; i++) {
					if(mm.equals(month[i])) {
						mm = i+1 + "";
						break;
					}
				}
				tour.setDepartDate(date.substring(7, 9) + "." + mm);
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void splitFromHottourAndAdd(String url, TourObject tour, int index, TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		try {
			
			tour.setLink(url);
			
			Document doc = Jsoup.connect(url).timeout(50000).get();
			
			
			String roomParse = doc.getElementsByClass("detail").get(0).getElementsByClass("value").get(0).toString();
			String roomType = getRoomType(roomParse, ">[A-Z]+");
			if(roomType == null) {
				tour.setRoomType(null);
			} else {
				tour.setRoomType(roomType.substring(1).toUpperCase());
			}
			
			String hotelParse = doc.select("#page_body > div.content_box > div.content > div.blank > div.content > div > h1").toString();
			if(hotelParse == null) {
				tour.setHotel(null);
			} else {
				String hotel = hotelParse.substring(4, hotelParse.length()-5).replaceAll("(&amp;)", "&");
				tour.setHotel(hotel.toUpperCase());
			}
			
			String countryParse = doc.select("#page_body > div.content_box > div.content > div.blank > div.content > div > div.country > span").toString();
			if(countryParse == null) {
				tour.setCountry(tourTitle.get(index), countryStand, bananLog);
			} else {
				String country = countryParse.substring(6, countryParse.length()-7);
				tour.setCountry(country.toUpperCase(), countryStand, bananLog);
			}
			
			String townParse = doc.select("#page_body > div.content_box > div.content > div.blank > div.content > div > div.country").toString();
			String townTMP = getTown(townParse, regexTownHottour);
			if(townTMP == null) {
				tour.setTown(tourTitle.get(index), cityStand, "Otpusk: ",bananLog);
			} else {
				String town = townTMP.substring(3, townTMP.length()-2);
				tour.setTown(town.toUpperCase(), cityStand, "Otpusk: ", bananLog);
			}
			
			String departCityParse = doc.getElementsByClass("detail").get(1).getElementsByClass("value").get(0).toString();
			if(departCityParse == null) {
				tour.setDepartCity(null);
			} else {
				String departCity = departCityParse.substring(20, departCityParse.length()-7);
				tour.setDepartCity(departCity.toUpperCase());
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void splitAndAddTourWithURL(String url, TourObject tour, int index, TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		try {
			tour.setSource(9);
			tour.setLink(url);
			
			Document doc = Jsoup.connect(tour.getLink()).get();
			Elements elem = doc.getElementsByClass("h-info");

			String hotel = getHotel(elem.toString(), regexHotel);
			if(hotel == null) {
				tour.setHotel(null);
			} else {
				tour.setHotel(hotel.substring(6, hotel.length()-5).replaceAll("(&amp;)", "&").toUpperCase());
			}
			
			String stars = getStars(elem.toString(), regexStars);
			if(stars == null) {
				tour.setStars(0);
			} else {
				tour.setStars(new Integer(stars.substring(stars.length()-3, stars.length()-2)));
			}
			
			Elements elem1 = doc.getElementsByClass("t-locale");
			if(tour.getLink().contains("http://www.otpusk.com/excursion")) {
				tour.setCountry(elem1.text(), countryStand, bananLog);
				tour.setTown(elem1.text(), cityStand, "Otpusk: ", bananLog);
			} else {
				String country = getCountry(elem1.toString());
				if(country == null) {
					tour.setCountry(tourTitle.get(index), countryStand, bananLog);
				} else {
					tour.setCountry(country.toUpperCase(), countryStand, bananLog);
				}
				
				String town = getTown(elem1.toString());
				if(town == null) {
					tour.setTown(tourTitle.get(index), cityStand, "Otpusk: ", bananLog);
				} else {
					tour.setTown(town.toUpperCase(), cityStand, "Otpusk: ", bananLog);
				}
			}

			tour.setRoomType("DBL");
			tour.setDepartCity("КИЕВ");
		
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	private static String splitGoToTour(String s) {												//special for hottour.com.ua
		String result = null;
		Pattern pattern = Pattern.compile("w{3}.([a-z]+).(\\w+)\\.[a-z]{1,}");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group().trim();
		}
		
		String id = "";
		Pattern pattern1 = Pattern.compile("id(\\W\\d[A-Z])\\d+");
		Matcher machPat1 = pattern1.matcher(s);
		while(machPat1.find()) {
			id = machPat1.group().trim();
			id = id.substring(5);
		}
		if(result == null) return null;
		result += "/tour?tour_id=".concat(id);
		result = "http://".concat(result);
		return result;
	}
	
	private static Integer getCountPeople(Document doc) {
		Integer result = null;
		String tmp = doc.select("#brandContainer > div:nth-child(8) > div > div > div > div.span6.hottours-block > div:nth-child(2) > div.tour-title.span7 > div.row-fluid > div.span4").toString();
		Pattern pattern = Pattern.compile("/>\\s\\d");
		Matcher machPat = pattern.matcher(tmp);
		while(machPat.find()) {
			tmp = machPat.group();
		}
		if(tmp != null) {
			result = new Integer(tmp.substring(3));
		}
		return result;
	}
	
	private static String getTitle(String s) {
		String result = null;
		Pattern pattern = Pattern.compile("\">(.)+<");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		if(result != null) {
			result = result.substring(2, result.length()-2);
		}
		return result;
	}
	
	private static String getTownTour(String s) {
		StringBuilder result = new StringBuilder();
		Pattern pattern = Pattern.compile("[а-я][А-Я]{1,}");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result.append(machPat.group() + "-");
		}
		
		if(result.length() == 0) {
			return result.substring(0, 0).toString();
		}
		
		return result.substring(0, result.length()-1).toString();
	}
	
	private static String getTown(String s) {
		String result = null;
		Pattern pattern = Pattern.compile(">(.+)<");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		
		if(result != null) {
			result = result.substring(1, result.length()-1);
		}
		return result;
	}
	
	private static String getCountry(String s) {
		String result = null;
		Pattern pattern = Pattern.compile(">(.+)<");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
			break;
		}
		
		if(result != null) {
			result = result.substring(1, result.length()-1);
		}
		return result;
	}
	
}
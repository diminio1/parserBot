/**
 * 
 */
package pogorelov.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import banan.file.writer.BananFileWriter;
import term.filter.parser.TermFilter;
import main.parser.com.*;
/**
 * @author Pogorelov
 *
 */
public class TurneSite extends AbstractSite {
	
	private String regexForNumOfWrap = "b-wrapper__tour-boxes";
	private String regexNutriton = "<b>\\s(.+)<";
	private String regexStars = "class=\"hotel__stars-(\\d)";
	private String regexPrice = "e-tour__price\"> (\\d+)(\\s\\d+)?";
	private String regexLink = "href=\"(.+)\" ";
	private String regexDuration = "days__tour\">(\\d+)";
	private String regexHotel = "name\">(\\s+?(.+)(.+\\s+?){1,})<span";
	private String regexDate = "date__tour\"> (\\d+.\\d+)";
	
	private static final int    source = 10;
	
	public TurneSite() {
		setURL("http://www.turne.com.ua");
	}
	
	public void parseHTML(TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
		try {
			//System.out.println("Connection to " + this.URL + "...");
			Document doc = Jsoup.connect("http://www.turne.com.ua/hottours#iAP=site&iAL=block-hot&iAC=hottours").timeout(50000).get();
			//System.out.println(this.URL + "... loaded.");
			String id = null;
			Boolean excursionTour;
			Element element;
			int numWrapper = findNumberOfWrappers(doc.toString(), regexForNumOfWrap);
			int numBoxes;
			for(int i = 0; i < numWrapper; i++) {
				numBoxes = this.findNumberOfBoxes(i, doc);
				for(int j = 1; j <= numBoxes; j++) {
					
					if(i > 9) {
						if(j > 9) {
							id = "ctl00_wpm_wp1500003299_wp2011094167_ctl" + i + "_repHotToursItem_ctl" + j + "_divCardContainer";
						} else {
							id = "ctl00_wpm_wp1500003299_wp2011094167_ctl" + i + "_repHotToursItem_ctl0" + j + "_divCardContainer";
						}
					} else {
						if(j > 9) {
							id = "ctl00_wpm_wp1500003299_wp2011094167_ctl0" + i + "_repHotToursItem_ctl" + j + "_divCardContainer";
						} else {
							id = "ctl00_wpm_wp1500003299_wp2011094167_ctl0" + i + "_repHotToursItem_ctl0" + j + "_divCardContainer";
						}
					}
				//	id = "ctl00_wpm_wp1500003299_wp2011094167_ctl07_repHotToursItem_ctl02_divCardContainer";
					element = doc.getElementById(id);
					excursionTour = element.toString().contains("Excursion");
					this.splitAndAddTour(element, excursionTour, countryStand, cityStand, bananLog);
					//break;
				}
				//break;
			}

		} catch(Exception e) {
//			e.printStackTrace();
			bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");
		}
	}
	
	private int findNumberOfBoxes(int i, Document doc) {
		int result = 0;
		
		if(i > 9) {
			Pattern pattern = Pattern.compile("ctl00_wpm_wp1500003299_wp2011094167_ctl" + i + "_repHotToursItem_ctl" + "(\\d+)" +"_divCardContainer");
			Matcher machPat = pattern.matcher(doc.toString());
			while(machPat.find()) {
				result++;
			}
		} else {
			Pattern pattern = Pattern.compile("ctl00_wpm_wp1500003299_wp2011094167_ctl0" + i + "_repHotToursItem_ctl" + "(\\d+)" +"_divCardContainer");
			Matcher machPat = pattern.matcher(doc.toString());
			while(machPat.find()) {
				result++;
			}
		}
		
		return result;
	}

	private void splitAndAddTour(Element elem, Boolean excursionTour, TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		TourObject tour = new TourObject();
		Integer countPeople = null;
		try {
			tour.setSource(source);

			countPeople = getCountPeople(elem.getElementsByClass("e-tour__persons").toString());
			
			String stars = getStars(elem.toString(), regexStars);
			if(stars == null) {
				tour.setStars(0);
			} else {
				tour.setStars(new Integer(stars.substring(stars.length()-1)));
			}
			
			Elements elem1 = elem.getElementsByClass("e-tour__text");
			String date = getDate(elem1.toString(), regexDate);
			if(date == null) {
				tour.setDepartDate(null);
			} else {
				tour.setDepartDate(date.substring(date.length()-5, date.length()));
			}
			
			String duration = getDuration(elem1.toString(), regexDuration);
			if(duration == null) {
				tour.setDuration(0);
			} else {
				tour.setDuration(new Integer(duration.substring(12)));
			}
			
			String link = getLink(elem1.toString(), regexLink);
			if(link == null) {
				tour.setLink(null);
			} else {
				tour.setLink(link.substring(6,  link.length()-2));
			}
			
			if(link != null) {
				String departCity = getDepartCity(link.substring(6,  link.length()-2), null);
				if(departCity == null) {
					tour.setDepartCity("КИЕВ");
				} else {
					tour.setDepartCity(departCity.toUpperCase());
				}
			}
			
			
			Elements elem2 = elem.getElementsByClass("e-tour__price-wrap");
			String price = getPrice(elem2.toString() , regexPrice);
			if(price == null) {
				tour.setPrice(0);
			} else {
				Integer priceTour = new Integer(price.substring(16).replaceAll("(\\s)?", ""));
				tour.setPrice(priceTour/countPeople);
			}
			
			//true - excursion tours, false - hot tours
			if(!excursionTour) {
				Elements elem3 = elem.getElementsByClass("e-tour__hotel-name");
				String hotel = getHotel(elem3.toString(), regexHotel);
				if(hotel == null) {
					tour.setHotel(null);
				} else {
					tour.setHotel(hotel.substring(9, hotel.length()-8).toUpperCase());
				}
				
				String country = getCountry(elem3.toString());
				if(country == null) {
					tour.setCountry("", countryStand, bananLog);
				} else {
					tour.setCountry(country.toUpperCase(), countryStand, bananLog);
				}
				
				String town = getTown(elem3.toString());
				if(town == null) {
					tour.setTown("", cityStand, "Turne: ", bananLog);
				} else {
					tour.setTown(town.toUpperCase(), cityStand, "Turne: ", bananLog);
				}
				tour.setRoomType("DBL");
			} else {
				Elements elem3 = elem.getElementsByClass("e-tour__country");
				String country = getCountryExcursion(elem3);
				if(country == null) {
					tour.setCountry("", countryStand, bananLog);
				} else {
					tour.setCountry(country.toUpperCase(), countryStand, bananLog);
				}
				tour.setRoomType("DBL");
			}
			
			
			
			Elements elem5 = elem.getElementsByClass("first_meal");
			String nutrition = getNutrition(elem5.toString(), regexNutriton);
			if(nutrition == null) {
				tour.setNutrition(null);
			} else {
				nutrition = nutrition.substring(4, nutrition.length()-1);
				if(nutrition.equals("Super all inc") || nutrition.equals("Ultra all inc"))
					nutrition = "UAI";
				if(nutrition.equals("All inc"))
					nutrition = "AI";
				tour.setNutrition(nutrition.toUpperCase());
			}
			
			
			listOfHotTours.add(tour);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static String getCountryExcursion(Elements elem) {
		String result = null;
		Pattern pattern = Pattern.compile("try\">" + "(\\s+)?.+");
		Matcher machPat = pattern.matcher(elem.toString());
		while(machPat.find()) {
			result = machPat.group();
			result = result.substring(5, result.length()-7);
		}
		return result;
	}
	
	private static Integer getCountPeople(String s) {
		Integer result = null;
		String tmp = null;
		Pattern pattern = Pattern.compile(">\\s\\d+");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			tmp = machPat.group();
		}
		result = new Integer(tmp.substring(2));

		return result;
	}
	
	private static String getTown(String s) {
		String result = null;
		Pattern pattern = Pattern.compile("country\">" + "(\\s+?(.+)(.+\\s+?){1,})");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
			result = result.substring(10, result.length()-9);
		}
		if(result != null)
		for(int i = 0; i < result.length(); i++) {
			if(result.charAt(i) == ',') {
				result = result.substring(0 ,i);
				break;
			}
		}
		return result;
	}
	
	public static String getDepartCity(String link, String regex) {
		String result = null;
		String tmp = null;
		try {
			Document doc = Jsoup.connect(link).timeout(50000).get();
			Elements elem = doc.select("#ctl00_centerZone > div:nth-child(1) > div > div > div > div.tour_info > table > tbody > tr:nth-child(5) > td:nth-child(2)");
			tmp = elem.toString();
			result = tmp.substring(5, tmp.length()-6);
		} catch(Exception e) {
				e.printStackTrace();
		}
			return result;
	}
	
	private static String getCountry(String s) {
		String result = null;
		Pattern pattern = Pattern.compile("country\">" + "(\\s+?(.+)(.+\\s+?){1,})");
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
			result = result.substring(10, result.length()-9);
		}
		if(result != null)
		for(int i = 0; i < result.length(); i++) {
			if(result.charAt(i) == ',') {
				result = result.substring(i+2);
				break;
			}
		}
		return result;
	}	
	
}
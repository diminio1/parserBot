package TEZtour.parser.com;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import pair.parser.Pair;
import rita.blowup.com.DateEdit;
import main.parser.com.TourObject;
import main.parser.com.Main;
import term.filter.parser.TermFilter;;

public class TEZTourParser{  
	
	public ArrayList <TourObject> tours;
	private static final int    source = 12;
	
	@SuppressWarnings("deprecation")
	public TEZTourParser(TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		
		bananLog.write(null, "TEZtour start!\n");
		
		tours = new ArrayList<TourObject>();
		
		Document tezDoc = null;
		
		try {
			tezDoc = Jsoup.connect("http://www.teztour.ua/hots.html").timeout(50000).get();
		}
		catch (IOException e) {
//			e.printStackTrace();
			bananLog.write(null, "Exeption: " + e.getStackTrace().toString() + "\n");
		}
		
		// needed jsoup elements
		
		Elements countryTownsDates           = null;
		Elements hotels                      = null;
		Elements durationRoomTypesNutritions = null;
		
		if(tezDoc != null){
		
			Elements preCountryTownsDate        = tezDoc.select("div[class = slider-title]");
			if (preCountryTownsDate != null){
				countryTownsDates           = preCountryTownsDate.select("h6");
				hotels                      = preCountryTownsDate.select("a[class = name]");
			}
			else {
//				System.out.println("No element in html!");
				bananLog.write(null, "WARNING!No html element preCountryTownsDate!\n");
			}
			Elements preDurationRoomTypesNutrition = tezDoc.select("div[class = slider-sub-block]");
			if (preDurationRoomTypesNutrition != null) {
				durationRoomTypesNutritions = preDurationRoomTypesNutrition.select("h6");
			}
			else {
//				System.out.println("No element in html!");
				bananLog.write(null, "WARNING!No html element preDurationRoomTypesNutrition!\n");
			}
			Elements links                      = tezDoc.select("a[class = button-link]");
			Elements prices                     = tezDoc.select("span[class = best-price]");
			Elements stars                      = tezDoc.select("span [class = raiting_u]");
	//        Elements photoAdress       = tezDoc.select("img[src ^= /images/photos/]");
			
	        int counter = 0;
	        		
			for (Element countryTownsDate : countryTownsDates) {
				
				if(countryTownsDate != null){
					TourObject tour = new TourObject();
					
		//			StringBuffer tmp = new StringBuffer(countryTownsDate.ownText());
					
					String[] arrayTmp = countryTownsDate.ownText().split("/");
					
					ArrayList <String> listTmp = new ArrayList <String> (Arrays.asList(arrayTmp));
					
					//set departDate
					int i = 0;
					
					while (listTmp.get(0).charAt(i) != ' ') {i ++;}
					while (listTmp.get(0).charAt(i) == ' ') {i ++;}
					
					StringBuffer dayTmp = new StringBuffer();
					
					while ((int)listTmp.get(0).charAt(i) >= 48 && (int)listTmp.get(0).charAt(i) <= 57) {
						
						dayTmp.append(listTmp.get(0).charAt(i));
						
						i ++;
					}
					
					tour.departDate.setDate(Integer.parseInt(dayTmp.toString()));
					
					while (listTmp.get(0).charAt(i) == ' ') {i ++;}
					
					StringBuffer monthTmpBuf = new StringBuffer();
					
					while (i < listTmp.get(0).length()) {
						
						monthTmpBuf.append(listTmp.get(0).charAt(i));
						
						i ++;
					}
					
					String monthTmp = new String(monthTmpBuf.toString());
					
						 if (monthTmp.equals("Янв")) {tour.departDate.setMonth(0); } else if (monthTmp.equals("Фвр")) {tour.departDate.setMonth(1);}  
				    else if (monthTmp.equals("Мрт")) {tour.departDate.setMonth(2); } else if (monthTmp.equals("Апр")) {tour.departDate.setMonth(3);}
				    else if (monthTmp.equals("Май")) {tour.departDate.setMonth(4); } else if (monthTmp.equals("Июн")) {tour.departDate.setMonth(5);}
				    else if (monthTmp.equals("Июл")) {tour.departDate.setMonth(6); } else if (monthTmp.equals("Авг")) {tour.departDate.setMonth(7); }
			        else if (monthTmp.equals("Снт")) {tour.departDate.setMonth(8); } else if (monthTmp.equals("Окт")) {tour.departDate.setMonth(9);}
				    else if (monthTmp.equals("Нбр")) {tour.departDate.setMonth(10);} else if (monthTmp.equals("Дек")) {tour.departDate.setMonth(11);}
					     
					if (tour.departDate.getMonth() == 11 && monthTmp.equals("Янв")) {
						
						tour.departDate.setYear(tour.departDate.getYear() + 1 + 1900);
					}
		
//	                if(tour.departDate == null){
//	                	
//	                	tours.remove(counter);
//	                    continue;
//	                }
//	                else if((DateEdit.before(tour.departDate, new Date()))){
//	                   
//	                	tours.remove(counter);
//	                	continue;
//	                }
					
					// set towns and countries
					i = 0;
					
					StringBuffer townTmp = new StringBuffer();
					
					while (listTmp.get(1).charAt(i) == ' ') {i ++;}
					
					while (listTmp.get(1).charAt(i) != ',') {
						
						townTmp.append(listTmp.get(1).charAt(i));
						
						i ++;
					}
					
					
					while (listTmp.get(1).charAt(i) == ' ' || listTmp.get(1).charAt(i) == ',') {
						
						i ++;
					}
					
					StringBuffer countryTmp = new StringBuffer();
					
					while (i < listTmp.get(1).length()) {
						
						countryTmp.append(listTmp.get(1).charAt(i));
						
						i ++;
					}
				
					tour.setCountry(countryTmp.toString().toUpperCase(), countryStand, bananLog);
					
					tour.setTown(townTmp.toString().toUpperCase(), cityStand, "TEZtour :", bananLog);
					
					tours.add(counter, tour);
					
					counter ++;
				}
				else{
					bananLog.write(null, "WARNING! No html element countryTownsDate! \n");
				}
			}
			
			counter = 0;
			
			for (Element durationRoomTypesNutrition : durationRoomTypesNutritions) {
				
				if (durationRoomTypesNutrition != null){
				
		//			StringBuffer tmp = new StringBuffer(durationRoomTypesNutrition.ownText().toUpperCase());
					
					String[] arrayTmp = durationRoomTypesNutrition.ownText().toUpperCase().split("/");
					
					ArrayList <String> listTmp = new ArrayList <String> (Arrays.asList(arrayTmp));
					
					int i = 0;
					
					while (listTmp.get(0).charAt(i) == ' ') {i ++;}
					
					StringBuffer durationTmp = new StringBuffer();
					
					while ((int)listTmp.get(0).charAt(i) >= 48 && (int)listTmp.get(0).charAt(i) <=  57) {
						
						durationTmp.append(listTmp.get(0).charAt(i));
						
						i ++;
					}
					
					tours.get(counter).setDuration(Integer.parseInt(durationTmp.toString()));
					
					tours.get(counter).setRoomType("DBL");
					
					while (listTmp.get(2).charAt(i) == ' ') {i ++;}
					
					StringBuffer nutritionBuffer = new StringBuffer();
					
					while (listTmp.get(2).charAt(i) != ' ') {
						
						nutritionBuffer.append(listTmp.get(2).charAt(i));
						
						i ++;
					}
					
					tours.get(counter).setNutrition(nutritionBuffer.toString());
					
					counter ++;
				}
				else{
					bananLog.write(null, "WORNING! No html element durationRoomTypesNutrition! \n");
				}
			}
			
			counter = 0;
			
			for (Element link : links) {
				
				if (link != null){
					tours.get(counter).setLink("http://www.teztour.ua" + link.attr("href"));
					
					counter ++;
				}
				else{
					bananLog.write(null, "WORNING! No html element link! \n");
				}
			}
			
			counter = 0;
			
			for (Element price : prices) {
				
				if(price != null){
					StringBuffer tmp      = new StringBuffer(price.ownText());
					StringBuffer priceTmp = new StringBuffer();
					
					int i = 0;
					
					while (tmp.charAt(i) != ' ') {
						
						priceTmp.append(tmp.charAt(i));
						
						i ++;
					}
					
					tours.get(counter).setPrice(Integer.parseInt(priceTmp.toString()) / 2);
					
					counter ++;
				}
				else{
					bananLog.write(null, "WORNING! No html element proce! \n");
				}
			}
			
			counter = 0;
			
			for (; counter < tours.size(); counter ++) {
				
				tours.get(counter).setDepartCity("КИЕВ");
			}
			
			counter = 0;
			
			for (Element hotel : hotels) {
				
				if (hotel != null){
					tours.get(counter).setHotel(hotel.ownText().toUpperCase());
					
					counter ++;
				}
				else{
					bananLog.write(null, "WORNING! No html element hotel! \n");
				}
			}
			
			counter = 0;
			
			for (; counter < tours.size(); counter ++) {
				
	            tours.get(counter).setSource(source);
			}
			
			counter = 0;
			
			for (Element star : stars) {
				
				if (star != null){
					String starTmp = star.attr("style");
					
					if (starTmp.equals("width: 54px;")) {
						
						tours.get(counter).setStars(4);
					}
					else if (starTmp.equals("width: 68px;")) {
						
						tours.get(counter).setStars(5);
					}
					else {
						
						tours.get(counter).setStars(3);
					}
					
					counter ++;
				}
				else{
					bananLog.write(null, "WORNING! No html element star! \n");
				}
			}
		}
	}
}
package smgp.parser.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import exception.parser.ua.ParserException;;

public class SmgpParser {

	public ArrayList <TourObject> tours;
//	ParserException ex;
	private static final int    source = 11;
	
	@SuppressWarnings("deprecation")
	public SmgpParser (TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog) {
		
		
		try {
			
			bananLog.write(null, "SMGP start!\n");
			
			tours = new ArrayList<TourObject>();
			
			Document smgpTourDoc = null;
			
			try {
				smgpTourDoc = Jsoup.connect("http://smgp.com.ua/last-minute-tours/from-city/0/").timeout(50000).get();
			}
			catch (IOException e) {
				//e.printStackTrace();
				bananLog.write(null, "Exeption: " + e.getStackTrace().toString() + "\n");
			}
			
			if (smgpTourDoc != null){
			
				// get block with the same country
				Elements countryBlocks = smgpTourDoc.select("div[class = country]");
				
				for (Element countryBlock : countryBlocks) {
				
					if (countryBlock.hasText()) {
					
						// for tours where more then five in one country
						if (countryBlock.select("a[class = deeper]").first() != null) {
							
							Document deeperTourDoc = null;
							
							try {
								deeperTourDoc = Jsoup.connect("http://smgp.com.ua" + 
							    countryBlock.select("a[class = deeper]").first().attr("href")).get();
							}
							catch (IOException e) {
								//e.printStackTrace();
								bananLog.write(null, "Exeption: " + e.getStackTrace().toString() + "\n");
							}
							
							countryBlock = deeperTourDoc.select("div[class = country]").first();
						}
						
						int toursSize = tours.size();
						
						// needed jsoup elements from one country
				//		Element country           = countryBlock.select("h2[style = margin-bottom:10px;]").first();
						Element country           = countryBlock.select("div[class = country]").first();
						
						Elements places           = countryBlock.select("span[class = place]");
						Elements cities           = countryBlock.select("div[class = city]");
						Elements hotelsStarsLinks = countryBlock.select("div[class = hotel]");
						Elements dates            = countryBlock.select("div[class = date]");
						Elements durNutrRoomTypes = countryBlock.select("div[class = nights]");
						Elements prices			  = countryBlock.select("div[class = price]");
						
						int counter = 0;
						
						for (Element preHotelStarLink : hotelsStarsLinks) {
			
							TourObject tour = new TourObject ();
							
							if (preHotelStarLink != null) {
								
									Element hotelStarLink = preHotelStarLink.select("a[href ^= /last-minute-tours/]").first();
									
									if (hotelStarLink != null){
									
										if(hotelStarLink.ownText().contains("*") || hotelStarLink.ownText().contains("(")){
										
											
											if(hotelStarLink.ownText().matches(".*\\d.*")){
										
												String hotelArr [] = hotelStarLink.ownText().toUpperCase().split("\\(");
												
												tour.setStars((int)hotelArr[1].charAt(0) - 48);
												
												tour.setHotel(hotelArr[0].trim().replace('\'', '"'));
												
											}
											else{
												
												String hotelArr [] = hotelStarLink.ownText().toUpperCase().split("\\(");
												
												tour.setHotel(hotelArr[0].trim().replace('\'', '"'));
												
											}
										}
										else{
											
											tour.setHotel(hotelStarLink.ownText().toUpperCase());
										}
	//									StringBuffer tmp = new StringBuffer (hotelStarLink.ownText().toUpperCase());
	//									
	////									System.out.println(tmp);
	//									
	//									int i = tmp.length() - 1;
	//									
	//									if(tmp.toString().contains("*")){
	//										
	//										StringBuffer starsTmp = new StringBuffer ();
	//										
	//										
	//										while (!((int)tmp.charAt(i) >= 48 && (int)tmp.charAt(i) <= 57)) {
	//											
	//											i --;
	//										}
	//										
	//										while ((int)tmp.charAt(i) >= 48 && (int)tmp.charAt(i) <= 57) {
	//											
	//											starsTmp.insert(0, tmp.charAt(i));
	//											
	//											i --;
	//										}
	//										
	//										tour.setStars(Integer.parseInt(starsTmp.toString()));
	//									}
	//									
	////									while (tmp.charAt(i) == '(' || tmp.charAt(i) == ' ') {
	////										
	////										i --;
	////									}
	//									
	//									String need [] = tmp.toString().split("\\(");
	//									
	//									StringBuffer hotelBuffer = new StringBuffer(need[0]);
	//									
	//									i = hotelBuffer.length() - 1;
	//									
	//									while (i >= 0) {
	//										
	//										if ((int)tmp.charAt(i) == 39){
	//											
	//											hotelBuffer.insert(0, '"');
	//											
	//											i --;
	//											
	//											if (i == -1) {
	//												
	//												break;
	//											}
	//										}
	//										
	//										hotelBuffer.insert(0, tmp.charAt(i));
	//										
	//										i --;
	//									}
	//									
	//									tour.setHotel(hotelBuffer.toString());
									}
									else{
										bananLog.write(null, "WARNING: No html element hotelStarLink!\n");
									}
									
									tour.setLink("http://smgp.com.ua" + hotelStarLink.attr("href"));
															
							}
							else {
	//							System.out.println("WARNING: No html element!" + 
	//												Thread.currentThread().getStackTrace()[2].getLineNumber());
								bananLog.write(null, "WARNING: No html element preHotelStarLink!\n");
								
								tour.link    = null;
								tour.hotel   = null;
								
								tours.add(counter + toursSize, tour);
							}
							
							// set departCity
							Document overSourceLinkDoc = null;
							
							try {
								overSourceLinkDoc = Jsoup.connect(tour.link.toString()).get();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
							
							Elements departCityElems = overSourceLinkDoc.select("h1[style = margin-top: 0;]");
							
							if(departCityElems != null){
								
								Element departCityElem = departCityElems.first();
								
								StringBuffer depCituTmp = new StringBuffer(departCityElem.ownText());
								
								int j = 0;
								
								StringBuffer hotTourStr = new StringBuffer();
								
								while (!hotTourStr.toString().equals("Горящий тур ")) {
									
									hotTourStr.append(depCituTmp.charAt(j));
									
									j ++;
								}
								
								StringBuffer depCityBuffer = new StringBuffer();
								
								while (depCituTmp.charAt(j) != '→') {
									
									depCityBuffer.append(depCituTmp.charAt(j));
									
									j ++;
								}
								
	//									depCityBuffer.deleteCharAt(tour.departCity.length());
								
								tour.setDepartCity(depCityBuffer.toString().toUpperCase());
							}
							
							if(country != null){
								// set country
								String needCountry = country.text().toUpperCase();
								
								ArrayList<Pair<Integer, Integer>> niceTmp = (ArrayList<Pair<Integer, Integer>>) countryStand.filter(needCountry);
								
								// to dell all repeated elems
								Set <Pair<Integer, Integer>> correctSet = new HashSet <Pair<Integer, Integer>> (niceTmp);
								
								tour.country = new ArrayList<Pair<Integer, Integer>> (correctSet);
								
								if(tour.country == null) System.out.println("The problem: " + country.ownText().toUpperCase());
							}
							else{
	//									System.out.println("WARNING: No html element!" + 
	//											Thread.currentThread().getStackTrace()[2].getLineNumber());
								
								tour.country = new ArrayList<Pair<Integer, Integer>> ();
								bananLog.write(null, "WARNING: No html element country!\n");
							}
							
							tours.add(counter + toursSize, tour);
							
							counter ++;
						}
						
						counter = 0;
				 		
						ArrayList <StringBuffer> placesArray = new ArrayList <StringBuffer> ();
						
						for (Element place : places) {
							
							if (place != null) {
								
								placesArray.add(new StringBuffer (place.ownText().toUpperCase()));
								
								counter ++;
							}else {
	//							System.out.println("WARNING: No html element!" + 
	//												Thread.currentThread().getStackTrace()[2].getLineNumber());
								bananLog.write(null, "WARNING: No html element ploce!\n");
							}
						}
						
						counter = 0;
						
						for (Element city : cities) {
							
							if (city != null) {
	
								String cityInput = new String(city.ownText().toUpperCase() + " " + placesArray.get(counter).toString().toUpperCase());
								
								ArrayList <Pair<Integer, Integer>> cityTmp = (ArrayList <Pair<Integer, Integer>>)cityStand.filter(cityInput);
								
								ArrayList<Pair<Integer, Integer>> niceTowns = new ArrayList<Pair<Integer, Integer>> ();
								
								if(tours.get(counter + toursSize).country.size() != 0){
									
									if (cityTmp != null){
										
											for (Pair <Integer, Integer> localCityPair : cityTmp){
												
												int flag = 0;
												
												for (Pair <Integer, Integer> localCountryPair : tours.get(counter + toursSize).country) {
													
													if (localCityPair.getSecond().equals(localCountryPair.getFirst())) {
														
														flag ++;
													}
												}
												
												if (flag != 0) {
													
													niceTowns.add(localCityPair);
												}
											}
									}
									
									// to dell all repeated elems
									Set <Pair<Integer, Integer>> correctSet = new HashSet <Pair<Integer, Integer>> (niceTowns);
									
									//System.out.println("Smgp: " + cityInput + " " + correctSet);
									bananLog.write(null, "Smgp: " + cityInput + " " + correctSet + "\n");
									
									tours.get(counter + toursSize).town = new ArrayList<Pair<Integer, Integer>> (correctSet);
									
								}
								else{
									// to dell all repeated elems
									Set <Pair<Integer, Integer>> correctSet = new HashSet <Pair<Integer, Integer>> (cityTmp);
									
									//System.out.println("Smgp: " + cityInput + " " + correctSet);
									bananLog.write(null, "Smgp: " + cityInput + " " + correctSet + "\n");
									
									tours.get(counter + toursSize).town = new ArrayList<Pair<Integer, Integer>> (correctSet);
									
									tours.get(counter + toursSize).country.add(new Pair <Integer, Integer> (tours.get(counter + toursSize).town.get(0).getSecond(), 0));
								}
								
								counter ++;
								
							}else {
	//							System.out.println("WARNING: No html element!" + 
	//												Thread.currentThread().getStackTrace()[2].getLineNumber());
								bananLog.write(null, "WARNING: No html element city!\n");
							}
						}
						
						counter = 0;
						
						for (Element date : dates) {
							
							if (date != null) {
							
								StringBuffer tmp = new StringBuffer(date.ownText());
								
								int i = 0;
								
								StringBuffer dayTmp = new StringBuffer(); 
								
								while (tmp.charAt(i) != '.') {
									
									dayTmp.append(tmp.charAt(i));
									
									i ++;
								}
								
								tours.get(counter + toursSize).departDate.setDate(Integer.parseInt(dayTmp.toString()));
								
								i ++;
								
								StringBuffer monthTmp = new StringBuffer();
								
								while (tmp.charAt(i) != '.') {
									
									monthTmp.append(tmp.charAt(i));
									
									i ++;
								}
								
								tours.get(counter + toursSize).departDate.setMonth(Integer.parseInt(monthTmp.toString()) - 1);
								
								i ++;
								
								StringBuffer yearTmp = new StringBuffer();
								
								while (i < tmp.length()) {
									
									yearTmp.append(tmp.charAt(i));
									
									i ++;
								}
								
								tours.get(counter + toursSize).departDate.setYear(Integer.parseInt(yearTmp.toString()) - 1900);
					
								
								counter ++;
								
							}
							else {
	//							System.out.println("WARNING: No html element!" + 
	//												Thread.currentThread().getStackTrace()[2].getLineNumber());
								bananLog.write(null, "WARNING: No html element date!\n");
							}
						}
						
						counter = 0;
						
						for (Element durNutrRoomType : durNutrRoomTypes) {
						
							if (durNutrRoomType != null) {
							
								StringBuffer tmp = new StringBuffer (durNutrRoomType.ownText());
								
								int i = 0;
								
								while (tmp.charAt(i) == ' ') {i ++;}
								
								StringBuffer durTmp = new StringBuffer ();
								
								while ((int)tmp.charAt(i) >= 48 && (int)tmp.charAt(i) <= 57) {
									
									durTmp.append(tmp.charAt(i));
									
									i ++;
								}
								
								tours.get(counter + toursSize).setDuration(Integer.parseInt(durTmp.toString()));
								
								while (tmp.charAt(i) == ' ') {i ++;}
								while (tmp.charAt(i) != ',') {i ++;}
								
								i ++;
								
								while (tmp.charAt(i) == ' ') {i ++;}
								
								StringBuffer nutrBuffer = new StringBuffer();
								
								while (tmp.charAt(i) != ',') {
								
									nutrBuffer.append(tmp.charAt(i));
									
									i ++;
								}
								
								tours.get(counter + toursSize).setNutrition(nutrBuffer.toString());
								
								i ++;
								
								while (tmp.charAt(i) == ' ') {i ++;}
								
								StringBuffer roomTBuffer = new StringBuffer();
								
								while (i < tmp.length()) {
									
									roomTBuffer.append(tmp.charAt(i));
									
									i ++;
								}
								
								tours.get(counter + toursSize).setRoomType(roomTBuffer.toString());
								
								counter ++;
								
							}
							else {
	//							System.out.println("WARNING: No html element!" + 
	//												Thread.currentThread().getStackTrace()[2].getLineNumber());
								bananLog.write(null, "WARNING: No html element durNutrRoomType!\n");
							}
							
						}
						
						counter = 0;
						
						for (Element price : prices) {
							
							if (price != null) {
							
								String priceTmp = new String(price.ownText()).replaceAll("\\s", "");
								
								tours.get(counter + toursSize).setPrice(Integer.parseInt(priceTmp.toString()));
								
								tours.get(counter + toursSize).setSource(source);
								
								counter ++;
							}
							else {
	//							System.out.println("WARNING: No html element in " + 
	//												Thread.currentThread().getStackTrace()[2].getLineNumber());
								bananLog.write(null, "WARNING: No html element price!\n");
							}
						}
					}
					else {
	//					System.out.println("WARNING: No html element!" + 
	//										Thread.currentThread().getStackTrace()[2].getLineNumber());
						bananLog.write(null, "WARNING: No html element countryBlock!\n");
					}
				}
			}
		} catch (Exception ex) {
			
        	if(ex.getMessage() != null){
				
				bananLog.write(null, ex.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(ex) + " \n");
			}
			else{
				
				bananLog.write(null, bananLog.bananStackTraceToString(ex) + " \n");
			}
		}
	}
}

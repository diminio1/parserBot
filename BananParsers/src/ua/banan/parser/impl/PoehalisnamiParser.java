package poehalisnami.parser.com;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.writers.FileWriter;

import banan.file.writer.BananFileWriter;
import pair.parser.Pair;
import term.filter.parser.TermFilter;
import main.parser.com.TourObject;
import exception.parser.ua.ParserException;

public class PoehalisnamiParser {

	public ArrayList<TourObject> tours;
//	ParserException ex;
	
	private static final int    source = 8;
	
	public PoehalisnamiParser (TermFilter countryStand, TermFilter cityStand, BananFileWriter bananLog){

		try {
			
			bananLog.write(null, "PoehaliSnami start!\n");
			
			tours = new ArrayList<TourObject>();
			
			Document poehalisnamiTourDoc = null;
			
			try {
				poehalisnamiTourDoc = Jsoup.connect("http://www.poehalisnami.ua/").timeout(50000).get();
			}
			catch (IOException e) {
				e.printStackTrace();
				bananLog.write(null, "Exeption: " + e.getStackTrace().toString() + "\n");
			}
			
			if(poehalisnamiTourDoc != null){
			
				// get block with the same over
				Elements overBloks = poehalisnamiTourDoc.select("div[class = over]");
		
				if (overBloks != null){
				
					parseBlock(overBloks, "div[class = gray_x]", countryStand, cityStand, bananLog);
				}
				else{
					//System.out.println("No block !!!");
					bananLog.write(null, "No block 1!\n");
				}
				
				// get block with the same country
				Elements countryBloks = poehalisnamiTourDoc.select("div[class = country]");
		
				if (countryBloks != null){
				
					parseBlock(countryBloks, "div[class = block425]", countryStand, cityStand, bananLog);
				}
				else{
					//System.out.println("Bad block !!!");
					bananLog.write(null, "No block 2!\n");
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
	
	public void parseBlock (Elements blocks, String linkFrom, TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		
		for (Element block : blocks) {
			
			int toursSize = tours.size();
			
			if (block.hasText()) {
			
				Elements sourceLinks   = block.select(linkFrom);
				Elements countries     = block.select("span[id *= _spnCountryName]");
				Elements cities        = block.select("span[id *= _spnResortName]");
				Elements prices        = block.select("a[class = price_orang]");
				Elements infoBlocks    = block.select("div[class = white5]");
				
				if(infoBlocks.isEmpty()) infoBlocks = block.select("div[class = big_info]");
				
				int counter = 0;
				
				for (Element country : countries) {
					
					if (country != null){
					
						TourObject tour = new TourObject ();
						
						if (country.hasText()) {
							
							tour.setCountry(country.ownText().toUpperCase(), countryStand, bananLog);
							
							tours.add (counter + toursSize, tour);
							
							counter ++;
						}
						else {
//							System.out.println("WARNING: No html element! " + 
//												Thread.currentThread().getStackTrace()[2].getLineNumber());
							
							bananLog.write(null, "WARNING: No html element country!\n");							
							tour.country = new ArrayList<Pair<Integer, Integer>>();
							//System.out.println("NO COUNTRY!");
							bananLog.write(null, "NO COUNTRY!\n");
							tour.country.add(new Pair<Integer, Integer>(0, 0));
							tours.add (counter + toursSize, tour);
						}
					}
					else {
//						System.out.println("WARNING: No html element!" + 
//											Thread.currentThread().getStackTrace()[2].getLineNumber());
						bananLog.write(null, "WARNING: No html element country!\n");
					}
				}
				
				counter = 0;
				
				for (Element city : cities) {
					
					if (city.hasText()) {

						tours.get(counter + toursSize).setTown(city.ownText().toUpperCase(), cityStand, "Poehaly s namy: ", bananLog);
					
						counter ++;
					}
					else {
//						System.out.println("WARNING: No html element!" + 
//											Thread.currentThread().getStackTrace()[2].getLineNumber());
						bananLog.write(null, "WARNING: No html element city!\n");
						
						tours.get(counter + toursSize).town = new ArrayList<Pair<Integer, Integer>>();
						System.out.println("NO CITY!");
					}
				}
				
				counter = 0;
				
				for (Element price : prices) {
					
					if (price.hasText()) {
					
						StringBuffer Tmp      = new StringBuffer (price.ownText());
						StringBuffer priceTmp = new StringBuffer ();
						
						int i = 0;
						
						while ((int)Tmp.charAt(i) >= 48 && (int)Tmp.charAt(i) <= 57) {
							
							priceTmp.append(Tmp.charAt(i));
							
							i ++;
						}
						
						tours.get(counter + toursSize).setPrice(Integer.parseInt(priceTmp.toString()));
						
						counter ++;
					}
					else {
//						System.out.println("WARNING: No html element!" + 
//											Thread.currentThread().getStackTrace()[2].getLineNumber());
						bananLog.write(null, "WARNING: No html element price!\n");
					}
				}
				
				counter = 0;
				
				for(Element infoBlock : infoBlocks){
					
					if (infoBlock != null){
						// set date
						
						Elements tmpDate = infoBlock.select("div[class = gray777]");
						
						if(tmpDate.isEmpty()) 
							tmpDate = infoBlock.select("span[class = gray777 margr10]");
						
						String tmpDateStr  = tmpDate.get(0).ownText();
						
						//if(tmpDate == null) tmpDate = infoBlock.select("div[class = gray777 margr10]").get(0).ownText();
						
						String tmp [] = tmpDateStr.split("\\.");
						
						tours.get(counter + toursSize).departDate.setDate(Integer.parseInt(tmp[0]));
						
						tours.get(counter + toursSize).departDate.setMonth(Integer.parseInt(tmp[1]) - 1);
						
						tours.get(counter + toursSize).departDate.setYear(Integer.parseInt(tmp[2]) - 1900);
						
						// set duration
						String durTmp1 [] = tmpDate.select("nobr").text().split(" ");
						
						tours.get(counter + toursSize).setDuration(Integer.parseInt(durTmp1[0].substring(1)));
						
						//set nutrition
						StringBuffer tmpNutr = new StringBuffer (tmpDate.get(1).select("span[class = blue13_b]").text());
						
						if (tmpNutr.toString().equals("без питания")) {
							
							tours.get(counter + toursSize).setNutrition("RO");
						}
						else if (tmpNutr.toString().equals("завтрак")){
							
							tours.get(counter + toursSize).setNutrition("BB");
						}
						else if (tmpNutr.toString().equals("завтрак + ужин")) {
							
							tours.get(counter + toursSize).setNutrition("HB");
						}
						else if (tmpNutr.toString().equals("полный пансион")) {
							
							tours.get(counter + toursSize).setNutrition("FB");
						}
						else if (tmpNutr.toString().equals("все включено (напитки местного производства)") ||
								 tmpNutr.toString().equals("все включено (напитки импортного производства)") ||
								 tmpNutr.toString().equals("все включено")) {
							
							tours.get(counter + toursSize).setNutrition("AI");
						}
						
						// bad situation with hotels here
						tours.get(counter + toursSize).setHotel(null);
						
						counter++;
					}
					else{
						bananLog.write(null, "WARNING: No html element infoBlock!\n");
					}
				}
				
				counter = 0;
				
				//to set room type
				for (Element sourceLink : sourceLinks) {
					
					if(sourceLink != null){
						tours.get(counter + toursSize).setLink(sourceLink.select("a").attr("href"));
						
						Document overSourceLinkDoc = null;
						
						try {
							overSourceLinkDoc = Jsoup.connect(sourceLink.select("a").attr("href")).timeout(50000).get();
						}
						catch (IOException e) {
							e.printStackTrace();
							bananLog.write(null, "Exeption: " + e.getStackTrace().toString() + "\n");
						}
						
						Element roomType = overSourceLinkDoc.select("span[class = label-bs]:contains(чел)").first();
						Element depCity  = overSourceLinkDoc.select("div[class = line_l fs16]:contains(Город выезда:)").select("span[class = label-bs]").first();
	
						if(roomType != null){
						//set roomType
							StringBuffer tmpRoomType = new StringBuffer (roomType.text());
							
							int i = tmpRoomType.length() - 1;
							
							while (!((int)tmpRoomType.charAt(i) >= 48 && (int)tmpRoomType.charAt(i) <= 57)) {i --;}
							
							int devider = (int)tmpRoomType.charAt(i) - 48;
							
							tours.get(counter + toursSize).price /= devider;
							
							i = 0;
							
							StringBuffer roomTypeBuffer = new StringBuffer();
							
							while (tmpRoomType.charAt(i) != '(') {
								
								roomTypeBuffer.append(tmpRoomType.charAt(i));
								
								i ++;
							}
							
							tours.get(counter + toursSize).setRoomType(roomTypeBuffer.toString());
						}
						else{
							tours.get(counter + toursSize).setRoomType("DBL");
							bananLog.write(null, "WARNING: No html element roomType!\n");
						}
						
						if(depCity != null)
						//set departCity
							tours.get(counter + toursSize).setDepartCity(depCity.ownText().toUpperCase());
						else{
							tours.get(counter + toursSize).setDepartCity("КИЕВ"); 
							bananLog.write(null, "WARNING: No html element depCity!\n");
						}
						
						tours.get(counter + toursSize).setSource(source);
						
						counter ++; 
					}
					else {
//						System.out.println("WARNING: No html element!" + 
//											Thread.currentThread().getStackTrace()[2].getLineNumber());
						bananLog.write(null, "WARNING: No html element sourceLink!\n");
					}
				}
			}
			else {
//				System.out.println("WARNING: No html element!" + 
//									Thread.currentThread().getStackTrace()[2].getLineNumber());
				bananLog.write(null, "WARNING: No html element block!\n");
			}
		}
	}
}

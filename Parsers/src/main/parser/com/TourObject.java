package main.parser.com;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.pmw.tinylog.writers.FileWriter;

import pair.parser.Pair;
import term.filter.parser.*;

public class TourObject {
	
	public String hotel;
	public ArrayList <Pair <Integer, Integer>> country;
	public ArrayList <Pair <Integer, Integer>> town;
	public String departCity;
	public String roomType;
	public String nutrition;
	public String link;
	public String description;
	public int          stars;
	public int          price;
	public Integer      previousPrice;
	public int          duration;
	public Date         departDate;
	public int          source;
	
	public int          tourId;
	
	public TourObject () {
		
		hotel     	  = null;
		country       = new ArrayList <Pair<Integer, Integer>> ();
		town          = new ArrayList <Pair<Integer, Integer>> ();
		departCity    = new String();
		roomType      = new String();
		nutrition     = new String();
		link          = new String();
		stars         = 0;
		previousPrice = null;
		price         = 0;
		duration      = 0;
		departDate    = new Date();
		source        = 0;
		tourId        = 0;
		description   = null;
	}
	
	public void setHotel(String h) {
		if(h != null)
			this.hotel = h.replace('\'', '"');
		else
			this.hotel = null;
	}
	
	public String getHotel() {
		if(this.hotel != null){
			return new String(this.hotel);			
		}
		return null;
	}
	
	public void setCountry(String c, TermFilter countryStand, FileWriter bananLog) {
		
		ArrayList<Pair<Integer, Integer>> niceTmp = (ArrayList<Pair<Integer, Integer>>) countryStand.filter(c.toUpperCase());
		
		if(niceTmp != null){
			// to dell all repeated elems
			Set <Pair<Integer, Integer>> correctSet = new HashSet <Pair<Integer, Integer>> (niceTmp);
			this.country = new ArrayList<Pair<Integer, Integer>> (correctSet);
		}
		else{
			//System.out.println("The problem: " + c.toUpperCase());
			bananLog.write(null, "The problem: " + c.toUpperCase() + "\n");
			this.country = new ArrayList<Pair<Integer, Integer>> ();
			//System.out.println("NO COUNTRY!");
			bananLog.write(null, "NO COUNTRY!\n");
			this.country.add(new Pair<Integer, Integer> (0, 0));
		}
	}
	
//	public String getCountry() {
//		return this.country;
//	}
	
	public void setTown(String t, TermFilter cityStand, String message, FileWriter bananLog) {
		
		ArrayList <Pair<Integer, Integer>> cityTmp = (ArrayList <Pair<Integer, Integer>>)cityStand.filter(t.toUpperCase());
		
		ArrayList<Pair<Integer, Integer>> niceTowns = new ArrayList<Pair<Integer, Integer>> ();
		
		if (cityTmp != null){
			
			for (Pair <Integer, Integer> localCityPair : cityTmp){
				
				int flag = 0;
				
				for (Pair <Integer, Integer> localCountryPair : this.country) {
					
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
		
		//System.out.println(message + t + " " + correctSet);
		bananLog.write(null, message + t + " " + correctSet + "\n");

		
		this.town = new ArrayList<Pair<Integer, Integer>> (correctSet);

	}
	
//	public String getTown() {
//		return this.town;
//	}
	
	public void setDepartCity(String s) {
		if(s != null)
		this.departCity = s;
		else
		this.departCity = null;
	}
	
	public String getDepartCity() {
		if (this.departCity != null){
			return new String(this.departCity);
		}
		return null;
	}
	
	public void setRoomType(String s) {
		if(s != null)
		this.roomType = s;
		else
		this.roomType = "";
	}
	
	public String getRoomType() {
		if(this.roomType != null){
			return new String(this.roomType);
		}
		return null;
	}
	
	public void setDescription(String s) {
		String res = "" + s;
		if(s != null) {
			if (res.length() >= 2048)
				res = res.substring(0, 2047);
			this.description = res;
		}
		else
			this.description = null;
	}
	
	public String getDescription() {
		if(this.description != null){
			return new String(this.description);
		}
		return null;
	}

	public void setNutrition(String s) {
		if(s != null)
		this.nutrition = s;
		else
		this.nutrition = "";
	}
	
	public String getNutrition() {
		if(this.nutrition != null){
			return new String(this.nutrition);
		}
		return null;
	}
	
	public void setLink(String s) {
		if(s != null)
		this.link = s;
		else
		this.link = "";
	}
	
	public String getLink() {
		if(this.link != null){
			return new String(this.link);
		}
		return null;
	}
	
	public void setStars(Integer s) {
		this.stars = s;
	}
	
	public Integer getStars() {
		return this.stars;
	}
	
	public void setPrice(Integer s) {
		this.price = s;
	}
	
	public Integer getPrice() {
		return this.price;
	}
	
	public void setPreviousPrice(Integer s) {
		this.previousPrice = s;
	}
	
	public Integer getPreviousPrice() {
		return this.previousPrice;
	}
	
	public void setDuration(Integer s) {
		this.duration = s;
	}
	
	public Integer getDuration() {
		return this.duration;
	}
	
	public void setDepartDate(String s) throws ParseException {
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
		String year = sdf1.format(date);
		String fullDate = s.concat("."+year);
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		Date depDate = format.parse(fullDate);
		this.departDate = depDate;
	}
	
	public Date getDepartDate() {
		return this.departDate;
	}
	
	public void setSource(int s) {
		this.source = s;
	}
	
	public int getSource() {
		return this.source;
	}

    public void print() {
        
        System.out.println("Hotel: " + hotel);
        System.out.println("Country: " + country);
        System.out.println("Town: " + town);
        System.out.println("Depart City: " + departCity);
        System.out.println("Room Type: " + roomType);
        System.out.println("Nutrition: " + nutrition);
        System.out.println("Link: " + link);
        System.out.println("Stars: " + stars);
        System.out.println("Price: " + price);
        System.out.println("Duration: " + duration);
        System.out.println("Depart Date: " + departDate);
        System.out.println("Source: " + source);
        System.out.println();
    }
}

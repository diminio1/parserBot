/**
 * 
 */
package pogorelov.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pmw.tinylog.writers.FileWriter;

import term.filter.parser.TermFilter;
import main.parser.com.*;
/**
 * @author Pogorelov
 *
 */
abstract class AbstractSite {
	protected List<TourObject> listOfHotTours = new ArrayList<>();
	protected String URL;
	
	public void setURL(String s) {
		this.URL = s;
	}
	
	public List<TourObject> getToursList(TermFilter countryStand, TermFilter cityStand, FileWriter bananLog) {
		parseHTML(countryStand, cityStand, bananLog);
		return listOfHotTours;
	}
	
	public abstract void parseHTML(TermFilter countryStand, TermFilter cityStand, FileWriter bananLog);
	
	public static int findNumberOfWrappers(String s, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		int i = -1;																		//-1 because one of this used in js code in this html.
		while(machPat.find()) {
			++i;
		}
		return i;
	}
	
	public static String getHotel(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
	
	public static String getNutrition(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
	
	public static String getDuration(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
	
	public static String getLink(String s, String regex) {
		String source = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			source = machPat.group();
		}
		return source;
	}
	
	public static String getStars(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
	
	public static String getPrice(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
	
	public static String getDate(String s, String regex) {
		String date = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			date = machPat.group();
		}
		return date;
	}
	
	public static String getRoomType(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
	
	public static String getDepartCity(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
	
	public static String getTown(String s, String regex) {
		String result = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher machPat = pattern.matcher(s);
		while(machPat.find()) {
			result = machPat.group();
		}
		return result;
	}
}
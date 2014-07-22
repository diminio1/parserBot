package main.parser.com;

import pair.parser.Pair;
import term.filter.parser.TermFilter;
import touravia.parser.com.TouraviaParser;
import tui.parser.com.TuiParser;
import tur777.parser.com.Tur777Parser;
import turne.parser.com.TurneParser;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kazkamandriv.parser.com.KazkamandrivParser;
import kenar.parser.com.KenarParser;
import mansana.parser.com.MansanaParser;
import newstravel.parser.com.NewstravelParser;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.LoggingLevel;
import org.pmw.tinylog.writers.FileWriter;

import orion.parser.com.OrionParser;
import otpusk.parser.com.OtpuskParser;
import akkord.parser.com.AkkordParser;
import apltravel.parser.com.ApltravelParser;
import banan.file.writer.BananFileWriter;
import candytour.parser.com.CandytourParser;
import columpus.parser.com.HColumbusParser;
import TEZtour.parser.com.*;
import hottours.parser.com.*;
import hottoursin.parser.com.HottoursInParser;
import smgp.parser.com.*;
import poehalisnami.parser.com.*;
import pogorelov.parser.*;
import HTP.parser.com.HTParser;
import shturman.parser.com.*;
import silver.parser.com.SilverParser;


public class Parsers {
	
	private static boolean HAS_COUNTRY;
	private static boolean HAS_CITY;
	
	private static HottoursParser     hottoursParser;
	private static TEZTourParser      teztourParser;
	private static SmgpParser         smgpParser;
	private static PoehalisnamiParser poehalisnamiParser;
	private static HTParser           HTParser;
	private static ShturmanParser     shturmanParser;
	private static HColumbusParser    hColumbusParser;
	private static TuiParser          tuiParser;
	private static TouraviaParser     touraviaParser;
	private static CandytourParser    candyTourParser;
	private static MansanaParser      mansanaParser;
	//private static HottoursInParser   hottoursInParser;
	private static OtpuskParser       otpuskParser;
	private static TurneParser        turneParser;
	private static KenarParser        kenarParser;
	private static ApltravelParser    apltravelParser;
	private static KazkamandrivParser kazkamandrivParser;
	private static Tur777Parser       tur777Parser;
	private static AkkordParser       akkordParser;
	private static OrionParser        orionParser;
	private static SilverParser       silverParser;
	private static NewstravelParser   newstravelParser;
	
	private static TermFilter countryStand;
	private static TermFilter cityStand;
	
	private static BananFileWriter bananLog;
	
	
	public static void startParsing(final Connection conn) {
		new Thread(new Runnable()
		{

			@Override
			public void run() {
				
				DateFormat formatter = new SimpleDateFormat("MM_dd__HH_mm");
				String date = formatter.format(new java.util.Date());
				
				bananLog = new BananFileWriter("bananLog" + date + ".txt");
				
				Configurator.currentConfig()
				.maxStackTraceElements(-1)
				.writer(bananLog)
				.formatPattern("{level}:{message},{\n}")
				.level(LoggingLevel.INFO)
				.activate();
				
				while(true){
					
					try {
						
						String dateInWhile = formatter.format(new java.util.Date());
						
						bananLog.write(LoggingLevel.INFO, "\n");
						bananLog.write(LoggingLevel.INFO, "\n");
						bananLog.write(LoggingLevel.INFO, "\n");
						
						bananLog.write(LoggingLevel.INFO, " PARSERS START AT " + dateInWhile + "\n");
						
						Statement selectCountry = null;
						ResultSet countryResult = null;
						Statement selectCity    = null;
						ResultSet cityResult    = null;
												
						if (conn != null){
							
							try {
								selectCountry = conn.createStatement();		
								countryResult = selectCountry.executeQuery("select * from COUNTRY;");   
								
								countryStand = new TermFilter(createBaseStand(countryResult, 0));
								
								selectCity = conn.createStatement();		
								cityResult = selectCountry.executeQuery("select * from CITY;");   
								
								cityStand = new TermFilter(createBaseStand(cityResult, 1));
							
							} catch (Exception e) {
								
								bananLog.write(LoggingLevel.INFO, "Cannot connect to Base!" + "\n");
								bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");								
							}
						
							teztourParser      = new TEZTourParser(countryStand, cityStand, bananLog);
							System.out.println("TEZ finish!");
							poehalisnamiParser = new PoehalisnamiParser(countryStand, cityStand, bananLog);
							System.out.println("Poehaly finish!");
							hottoursParser     = new HottoursParser(countryStand, cityStand, bananLog);
							System.out.println("hottours finish!");
							smgpParser         = new SmgpParser(countryStand, cityStand, bananLog);
							System.out.println("smgp finish!");
							HTParser           = new HTParser(countryStand, cityStand, bananLog);
							System.out.println("HT finish!");
							shturmanParser     = new ShturmanParser(countryStand, cityStand, bananLog);
							System.out.println("shturman finish!");
							hColumbusParser    = new HColumbusParser(countryStand, cityStand, bananLog);
							System.out.println("hColumbus finish!");
							tuiParser          = new TuiParser(countryStand, cityStand, bananLog);
							System.out.println("tuiParser finish!");
							touraviaParser     = new TouraviaParser(countryStand, cityStand, bananLog);
							System.out.println("touraviaParser finish!");
							candyTourParser    = new CandytourParser(countryStand, cityStand, bananLog);
							System.out.println("candyTourParser finish!");
							mansanaParser      = new MansanaParser(countryStand, cityStand, bananLog);
							System.out.println("mansanaParser finish!");
//							hottoursInParser   = new HottoursInParser(countryStand, cityStand, bananLog);
//							System.out.println("hottoursInParser finish!");
							otpuskParser       = new OtpuskParser(countryStand, cityStand, bananLog);
							System.out.println("otpuskParser finish!");
							turneParser        = new TurneParser(countryStand, cityStand, bananLog);
							System.out.println("turneParser finish!");
							kenarParser        = new KenarParser(countryStand, cityStand, bananLog);
							System.out.println("kenarParser finish!");
							apltravelParser    = new ApltravelParser(countryStand, cityStand, bananLog);
							System.out.println("apltravelParser finish!");
							kazkamandrivParser = new KazkamandrivParser(countryStand, cityStand, bananLog);
							System.out.println("kazkamandrivParser finish!");
							tur777Parser       = new Tur777Parser(countryStand, cityStand, bananLog);
							System.out.println("tur777Parser finish!");
							akkordParser       = new AkkordParser(countryStand, cityStand, bananLog);
							System.out.println("akkordParser finish!");
							orionParser       = new OrionParser(countryStand, cityStand, bananLog);
							System.out.println("orionParser finish!");
							silverParser       = new SilverParser(countryStand, cityStand, bananLog);
							System.out.println("silverParser finish!");
							newstravelParser       = new NewstravelParser(countryStand, cityStand, bananLog);
							System.out.println("newstravelParser finish!");
							
							ArrayList<TourObject> allTours = new ArrayList <TourObject> ();
							
							allTours.addAll(teztourParser.tours);
							allTours.addAll(poehalisnamiParser.tours);
							allTours.addAll(hottoursParser.tours);
							allTours.addAll(smgpParser.tours);
							allTours.addAll(HTParser.tours);
							allTours.addAll(shturmanParser.tours);
							allTours.addAll(hColumbusParser.tours);
							allTours.addAll(tuiParser.tours);
							allTours.addAll(touraviaParser.tours);
							allTours.addAll(candyTourParser.tours);
							allTours.addAll(mansanaParser.tours);
							//allTours.addAll(hottoursInParser.tours);
							allTours.addAll(otpuskParser.tours);
							allTours.addAll(turneParser.tours);
							allTours.addAll(kenarParser.tours);
							allTours.addAll(apltravelParser.tours);
							allTours.addAll(kazkamandrivParser.tours);
							allTours.addAll(tur777Parser.tours);
							allTours.addAll(akkordParser.tours);
							allTours.addAll(orionParser.tours);
							allTours.addAll(silverParser.tours);
							allTours.addAll(newstravelParser.tours);
							
							createBase(allTours, conn);
						}
						else{
							bananLog.write(LoggingLevel.INFO, "Cannot connect to CountryCityBase!" + "\n");
						}
						
						try {
							Thread.sleep(30 * 60000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					
					} catch (SQLException e) {
						bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");
					}
			}
			
		}
		}).start();
	}
	
	private static List<Pair<Pair<Integer, Integer>, List<String>>> createBaseStand (ResultSet resultSet, int place) throws SQLException {
		
		if (resultSet != null) {
			
			List<Pair<Pair<Integer, Integer>, List<String>>> termWithIdsList = new ArrayList<Pair<Pair<Integer, Integer>, List<String>>> (); 
			
			while (resultSet.next()) {
			
				if (resultSet.getString(place + 4) != null) {
					String[] arrayCountries = resultSet.getString(place + 4).split(",");
					
					List <String> countriesTmp = new ArrayList <String> ();
					
					for (int tmpCount = 0; tmpCount < arrayCountries.length; tmpCount ++) {
						
						if (arrayCountries[tmpCount] != null && !arrayCountries[tmpCount].isEmpty()){
						
							if (arrayCountries[tmpCount].charAt(0) == ' ') {
								
								countriesTmp.add(arrayCountries[tmpCount].substring(1));
							}
							else {
								countriesTmp.add(arrayCountries[tmpCount]);
							}
						}
					}
					
					countriesTmp.add(resultSet.getString(place + 2));
					
					countriesTmp.add(resultSet.getString(place + 3));
					
					Pair <Integer, Integer> tmpPair = null;
					
					if (place == 0) {
						
						tmpPair = new Pair <Integer, Integer> (resultSet.getInt(1), 0);
					} else{
						
						tmpPair = new Pair <Integer, Integer> (resultSet.getInt(1), resultSet.getInt(2));
					}
					
					Pair<Pair<Integer, Integer>, List<String>> myPair = new Pair<Pair<Integer, Integer>, List<String>>(tmpPair, countriesTmp);
					
					termWithIdsList.add(myPair);
				}
				else {
					List <String> countriesTmp = new ArrayList <String> ();
					
					countriesTmp.add(resultSet.getString(place + 2));
					
					countriesTmp.add(resultSet.getString(place + 3));
					
					Pair <Integer, Integer> tmpPair = null;
					
					if (place == 0) {
						
						tmpPair = new Pair <Integer, Integer> (resultSet.getInt(1), 0);
					} else{
						
						tmpPair = new Pair <Integer, Integer> (resultSet.getInt(1), resultSet.getInt(2));
					}
					
					Pair<Pair<Integer, Integer>, List<String>> myPair = new Pair<Pair<Integer, Integer>, List<String>>(tmpPair, countriesTmp);
					
					termWithIdsList.add(myPair);
				}
			}
			
			return termWithIdsList;
		}
		else{
			
			return null;
		}
	}
    
    @SuppressWarnings("deprecation")
    private static void createBase (ArrayList <TourObject> tours, Connection conn) throws SQLException {
    
    try {

		  PreparedStatement select = null;
		  
		  ArrayList <TourObject> exTours = new ArrayList <TourObject> ();
		 
	//	  select = conn.prepareStatement("select a.PRICE, a.FLIGHT_DATE, b.HOTEL_NAME, a.TOUR_ID " +
	//									 "FROM TOUR a " +
	//									 "JOIN HOTEL b " +
	//									 "ON a.HOTEL_ID = b.HOTEL_ID");
		  select = conn.prepareStatement("select * from TOUR;");
		  
		  ResultSet exToursRes = select.executeQuery();
		  
		 //create set for compare 
		  while (exToursRes.next()) {
			  
			  TourObject localObj = new TourObject();
			  
			  localObj.price = exToursRes.getInt(5);
			  
			  localObj.departDate.setYear(exToursRes.getDate(4).getYear());
			  localObj.departDate.setMonth(exToursRes.getDate(4).getMonth());
			  localObj.departDate.setDate(exToursRes.getDate(4).getDate());
			  
	//		  localObj.hotel = new StringBuffer(exToursRes.getString(3));
			  localObj.tourId = exToursRes.getInt(10);
			  
			  exTours.add(localObj);
		  }
		  
		  
	//	  for (int i = 0; i < tours.size(); i ++) {
	//		  
	//		  for (int j = 0; j < exTours.size(); j ++) {
	//			  
	//			  String exHotel  = exTours.get(j).hotel == null ? "NULL" : exTours.get(j).hotel.toString();
	//			  String newHotel = tours.get(i).hotel   == null ? "NULL" : tours.get(i).hotel.toString();
	//			  
	//			  if ((tours.get(i).price == exTours.get(j).price) && 
	//				  (tours.get(i).departDate.getYear()  == exTours.get(j).departDate.getYear()) &&
	//				  (tours.get(i).departDate.getMonth() == exTours.get(j).departDate.getMonth()) &&
	//				  (tours.get(i).departDate.getDay()   == exTours.get(j).departDate.getDay()) &&
	//				  (newHotel.equals(exHotel))) {
	//				  
	//				  System.out.println("MATCHING!!!: " + tours.get(i).price + " " + tours.get(i).departDate +" and " + exTours.get(j).price + " " + exTours.get(j).departDate);
	//				  
	//				  tours.remove(i);
	//				  exTours.remove(j);
	//				  
	//				  j --;
	//				  i --;
	//			  }
	//			  else{
	//				  
	////				  System.out.println("No matching: " + tours.get(i).price + " " + tours.get(i).departDate +" and " + exTours.get(j).price + " " + exTours.get(j).departDate);
	//			  }
	//		  }
	//	  }
		  
		  // del not needed tour from db
//		  for (TourObject tmpTour : exTours) {
//			  
//			  select = conn.prepareStatement("delete from TOURS_TO_CITIES where TOUR_ID = " + tmpTour.tourId +";");
//			  select.execute();
//			  select = conn.prepareStatement("delete from TOURS_TO_COUNTRIES where TOUR_ID = " + tmpTour.tourId +";");
//			  select.execute();
//			  select = conn.prepareStatement("delete from TOUR where TOUR_ID = " + tmpTour.tourId +";");
//			  select.execute();
//		  }
		  
		  conn.setAutoCommit(false);
		  
		  select = conn.prepareStatement("truncate TOURS_TO_CITIES;");
		  select.execute();
		  select = conn.prepareStatement("truncate TOURS_TO_COUNTRIES;");
		  select.execute();
		  select = conn.prepareStatement("truncate TOUR;");
		  select.execute();
		  
		  //add all needed tours to db
//		  select = conn.prepareStatement("select count(*) from TOUR;");
//		  ResultSet firstResult = select.executeQuery();     
//	
//		  firstResult.next();
		  
//		  int tourNumber = firstResult.getInt(1);
		  
//		  if (tourNumber != 0){
//			  
//			  select = conn.prepareStatement("SELECT TOUR_ID FROM ( SELECT * FROM TOUR ORDER BY TOUR_ID LIMIT 1 OFFSET ( SELECT COUNT(*) FROM TOUR ) - 1 );");
//	    	  ResultSet firstResultPost = select.executeQuery();     
//	
//	    	  firstResultPost.next();
//	    	  
//	    	  tourNumber = firstResultPost.getInt(1);
//			  
//		  }
	  
		  
	      for (int i = 0; i < tours.size(); i ++) {
	      	
	    	  
	    	HAS_COUNTRY = true;
	    	HAS_CITY    = true;
	    	
	      	// to base for country
	      	if (tours.get(i).country.size() != 0) {
	      	
		      	for (int countryCounter = 0; countryCounter < tours.get(i).country.size(); countryCounter ++) {
		      		
		      		select  = conn.prepareStatement("select RATE from COUNTRY where COUNTRY_ID = ? " + "");
		      		select.setInt(1, tours.get(i).country.get(countryCounter).getFirst().intValue() );
		      		ResultSet resultRate = select.executeQuery();
		      		resultRate.next();
		      		
		      		int rate = resultRate.getInt(1) + 1;
		      		
		      		
		      		select = conn.prepareStatement("update COUNTRY set RATE = " + rate + "where COUNTRY_ID = " +
		      										tours.get(i).country.get(countryCounter).getFirst() + "");
		      		select.executeUpdate();
		      	}
	      	}
	      	else {
	 
	      		bananLog.write(LoggingLevel.INFO, "No country on baseCreation!!!\n");
	      		
	      		HAS_COUNTRY = false;
	      	}
	      	
	      	//to base for city
	      	if (tours.get(i).town.size() != 0) {
		      	
		      	for (int cityCounter = 0; cityCounter < tours.get(i).town.size(); cityCounter ++) {
		      		
		      		select = conn.prepareStatement("select RATE from CITY where CITY_ID = ? " + ";");
		      		select.setInt(1, tours.get(i).town.get(cityCounter).getFirst().intValue() );
		      		ResultSet resultRate = select.executeQuery();
		      		
		      		resultRate.next();
		      		
		      		int rate  = resultRate.getInt(1) + 1;
		      		
		      		select = conn.prepareStatement("update CITY set RATE = " + rate + "where CITY_ID = " +
		      										tours.get(i).town.get(cityCounter).getFirst() + ";");
		      		select.executeUpdate();
		      	}
	      	}
	      	else {
	      		
	      		bananLog.write(LoggingLevel.INFO, "No city on baseCreation!!! \n");
	      		
	      		HAS_CITY = false;
	      	}
	      	
	      	//to base for hotel, tour, pairs     	
	      	if (tours.get(i).hotel != null) {
	      				      	
	      		select = conn.prepareStatement("select count(HOTEL_ID) from HOTEL where HOTEL_NAME LIKE '" + 
						   						tours.get(i).hotel + "';");
		      	ResultSet resultHotel = select.executeQuery();
	   	
			   	resultHotel.next();
			   	
			   	if (resultHotel.getInt(1) == 0) {
			   		
//			   		select = conn.prepareStatement("SELECT HOTEL_ID FROM ( SELECT * FROM HOTEL ORDER BY HOTEL_ID LIMIT 1 OFFSET ( SELECT COUNT(*) FROM HOTEL ) - 1 );");
//			   		ResultSet resultHotelNum = select.executeQuery();
//			         	
//			     	resultHotelNum.next();
//			     	
//			     	int rowsHotelNum = resultHotelNum.getInt(1);
//			     	
//			     	rowsHotelNum ++;
			         	
		     		if (tours.get(i).town.size() != 0) {
		     		
		     			select = conn.prepareStatement("INSERT INTO HOTEL (hotel_name, stars, description, city_id) VALUES ('" +
									     	            tours.get(i).hotel + "', " +  
									     	            tours.get(i).stars + ", " + "NULL, " +
									     	           tours.get(i).town.get(0).getFirst() + ");");
			         	select.execute();
		     		}
		     		else {
		     			
		     			select = conn.prepareStatement("INSERT INTO HOTEL (hotel_name, stars, description, city_id) VALUES ('" +
			     	             						tours.get(i).hotel + "', " +  
			     	             						tours.get(i).stars + ", NULL, NULL);");
			         	select.execute();
		     		}
		     		
//		     		System.out.println("PREPARE TO GET CURRENT NUM");
		     		
		     		int rowsHotelNum = 0;
		     		
		     		select = conn.prepareStatement("select currval('hotel_hotel_id_seq')");
		     		
		     		ResultSet rs = select.executeQuery();
		     		if ( rs.next() ) {
		     			
		     			rowsHotelNum = rs.getInt(1);
		     		}
		     		
//		     		System.out.println("CURRENT NUM is " + rowsHotelNum);
		     		
		         	int year  = tours.get(i).departDate.getYear();
		         	int month = tours.get(i).departDate.getMonth();
		         	int day   = tours.get(i).departDate.getDate();
			            	
		       		Date bdDate = new Date(year, month, day);
			            
//		       		int tourId = i + tourNumber + 1;
		       		
		       		if (!(tours.get(i).previousPrice == null))
		       			select = conn.prepareStatement("INSERT INTO TOUR (url, nutrition, room_type, flight_date, price, duration,"
		       				+ " depart_city, description, hotel_id, source_id, previous_price) VALUES ('" + 
								     				 	tours.get(i).link + "', '" +
								     				 	tours.get(i).nutrition + "', '" + 
								     				 	tours.get(i).roomType + "', '" +
								     				 	bdDate + "', " +
								     				 	tours.get(i).price + ", " +
								     				 	tours.get(i).duration + ", '" +
								     				 	tours.get(i).departCity + "', " + "NULL, " +
								     				 	rowsHotelNum + ", " + tours.get(i).source + ", " + tours.get(i).previousPrice + ");");

		       		else 
		       			select = conn.prepareStatement("INSERT INTO TOUR (url, nutrition, room_type, flight_date, price, duration,"
			       				+ " depart_city, description, hotel_id, source_id, previous_price) VALUES ('" + 
									     				 tours.get(i).link + "', '" +
									     			     tours.get(i).nutrition + "', '" + 
									     			     tours.get(i).roomType + "', '" +
									     			     bdDate + "', " +
									     			     tours.get(i).price + ", " +
									     			     tours.get(i).duration + ", '" +
									     			     tours.get(i).departCity + "', " + "NULL, " +
									     			     rowsHotelNum + ", " + tours.get(i).source + ", " + "NULL" + ");");
		       		try {
						
			       		select.execute();
			       		
			       		int tourId = 0;
			       		
			       		select = conn.prepareStatement("select currval('tour_tour_id_seq')");
			       		
			     		rs = select.executeQuery();
			     		if ( rs.next() ) {
			     			
			     			tourId = rs.getInt(1);
			     		}
			       		
			       		//set country/city conn
			       		if (HAS_COUNTRY) setToutToCountryConn(tours, tourId, select, i, conn);		       		
			       		if (HAS_CITY)    setTourToCityConn(tours, tourId, select, i, conn);
			       		
		       		} catch (Exception e) {
		       			bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");
		       		}
	
			   	}
			   	else {
			   		
			   		select = conn.prepareStatement("select HOTEL_ID from HOTEL where HOTEL_NAME LIKE '" + 
							   						tours.get(i).hotel + "';");
			   		ResultSet resultHotelElse = select.executeQuery();	            	
			   		resultHotelElse.next();
			
			   		int rowsHotelNumElse = resultHotelElse.getInt(1);
			         	
			     	int year  = tours.get(i).departDate.getYear();
			     	int month = tours.get(i).departDate.getMonth();
			     	int day   = tours.get(i).departDate.getDate();
			         	
			   		Date bdDate = new Date(year, month, day);
			   		
//			   		int tourId = i + tourNumber + 1;
			   		
					if (!(tours.get(i).previousPrice == null))
			   			select = conn.prepareStatement("INSERT INTO TOUR (url, nutrition, room_type, flight_date, price, duration,"
		       				+ " depart_city, description, hotel_id, source_id, previous_price) VALUES ('" + 
									 				 	tours.get(i).link + "', '" +
									 				 	tours.get(i).nutrition + "', '" + 
									 				 	tours.get(i).roomType + "', '" +
									 				 	bdDate + "', " +
									 				 	tours.get(i).price + ", " +
									 				 	tours.get(i).duration + ", '" +
									 				 	tours.get(i).departCity + "', " + "NULL, " +
									 				 	rowsHotelNumElse + ", " + tours.get(i).source + ", " + tours.get(i).previousPrice + ");");
			   		else
			   			select = conn.prepareStatement("INSERT INTO TOUR (url, nutrition, room_type, flight_date, price, duration,"
			       				+ " depart_city, description, hotel_id, source_id, previous_price) VALUES ('" + 
										 				 tours.get(i).link + "', '" +
										 			     tours.get(i).nutrition + "', '" + 
										 			     tours.get(i).roomType + "', '" +
										 			     bdDate + "', " +
										 			     tours.get(i).price + ", " +
										 			     tours.get(i).duration + ", '" +
										 			     tours.get(i).departCity + "', " + "NULL, " +
										 			     rowsHotelNumElse + ", " + tours.get(i).source + ", " + "NULL" + ");");
			   		try {
						
				   		select.execute();
				   		
			       		int tourId = 0;
			       		
			       		select = conn.prepareStatement("select currval('tour_tour_id_seq')");
			       		
			       		ResultSet rs = select.executeQuery();
			     		if ( rs.next() ) {
			     			
			     			tourId = rs.getInt(1);
			     		}
			       		
				   		//set country/city conn
			       		if (HAS_COUNTRY) setToutToCountryConn(tours, tourId, select, i, conn);		       		
			       		if (HAS_CITY)    setTourToCityConn(tours, tourId, select, i, conn);
			       		
			   		} catch (Exception e) {
			   			bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");
			   		}
			   	}
	      	}
	      	else {
	
	      		bananLog.write(LoggingLevel.INFO, "Hotel is null on baseCreation!!!\n");
	      		
	      		int year  = tours.get(i).departDate.getYear();
	         	int month = tours.get(i).departDate.getMonth();
	         	int day   = tours.get(i).departDate.getDate();
		            	
	       		Date bdDate = new Date(year, month, day);
		            
//	       		int tourId = i + tourNumber + 1;
	       		
	       		if (!(tours.get(i).previousPrice == null))
	       			select = conn.prepareStatement("INSERT INTO TOUR (url, nutrition, room_type, flight_date, price, duration,"
		       				+ " depart_city, description, hotel_id, source_id, previous_price) VALUES ('" + 
							     				 tours.get(i).link + "', '" +
							     			     tours.get(i).nutrition + "', '" + 
							     			     tours.get(i).roomType + "', '" +
							     			     bdDate + "', " +
							     			     tours.get(i).price + ", " +
							     			     tours.get(i).duration + ", '" +
							     			     tours.get(i).departCity + "', " + "NULL, " +
							     			     "NULL, " + tours.get(i).source + ", " + tours.get(i).previousPrice + ");");
	       		else
	       			select = conn.prepareStatement("INSERT INTO TOUR (url, nutrition, room_type, flight_date, price, duration,"
		       				+ " depart_city, description, hotel_id, source_id, previous_price) VALUES ('" + 
							     				 tours.get(i).link + "', '" +
							     			     tours.get(i).nutrition + "', '" + 
							     			     tours.get(i).roomType + "', '" +
							     			     bdDate + "', " +
							     			     tours.get(i).price + ", " +
							     			     tours.get(i).duration + ", '" +
							     			     tours.get(i).departCity + "', " + "NULL, " +
							     			     "NULL, " + tours.get(i).source + ", " + "NULL" + ");");
	       		
	       		try {
					
		       		select.execute();
		       		
		       		int tourId = 0;
		       		
		       		select = conn.prepareStatement("select currval('tour_tour_id_seq')");
		       		
		       		ResultSet rs = select.executeQuery();
		     		if ( rs.next() ) {
		     			
		     			tourId = rs.getInt(1);
		     		}
		       		
		       		//set country/city conn
		       		if (HAS_COUNTRY) setToutToCountryConn(tours, tourId, select, i, conn);		       		
		       		if (HAS_CITY)    setTourToCityConn(tours, tourId, select, i, conn);
		       		
	       		} catch (Exception e) {
	       			bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");
	       		}
	
	      	}
	      }
	      
	      conn.commit();
		  conn.setAutoCommit(true);
	      
	      System.out.println("ALL IS DONE!");
    } catch (Exception e) {
    	bananLog.write(null, e.getMessage().toString() + " \n" +  bananLog.bananStackTraceToString(e) + " \n");
    }	
          	
    }
    
    private static void setToutToCountryConn (ArrayList <TourObject> tours, int tourId, PreparedStatement select, int i, Connection conn) throws SQLException{
    	
    	if(tours != null){
    	
	   		for (int count = 0; count < tours.get(i).country.size(); count ++) {
	   			
	   			select = conn.prepareStatement("INSERT INTO TOURS_TO_COUNTRIES (country_id, tour_id) VALUES (" +  
						       					tours.get(i).country.get(count).getFirst() + ", " +
						       					 tourId + ");");
	   			select.execute();
	   		}
	    }
    }
        
    private static void setTourToCityConn (ArrayList <TourObject> tours, int tourId, PreparedStatement select, int i, Connection conn) throws SQLException{
    	
    	if(tours != null){
    		
       		for (int count = 0; count < tours.get(i).town.size(); count ++) {
       			
       			select = conn.prepareStatement("INSERT INTO TOURS_TO_CITIES (city_id, tour_id) VALUES (" +  
       					 tours.get(i).town.get(count).getFirst() + ", " +
       					 tourId + ");");
       			select.execute();
       		}
    	}    	
    }

}
package poehalisnami.parser.com;

import java.sql.SQLException;

import main.parser.com.Parsers;

public class PoehalisnamiThread extends Thread{

	@SuppressWarnings("static-access")
	public void run () {
		
//		PoehalisnamiParser obj = new PoehalisnamiParser();
		
		Parsers main = new Parsers();
		
//		try {
//			main.createBase(obj.tours);
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}

package TEZtour.parser.com;

import java.sql.SQLException;

import main.parser.com.Parsers;

public class TEZtourThread extends Thread {

	@SuppressWarnings("static-access")
	public void run () {
		
//		TEZTourParser obj = new TEZTourParser();
		
		Parsers main = new Parsers();
		
//		try {
//			main.createBase(obj.tours);
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}

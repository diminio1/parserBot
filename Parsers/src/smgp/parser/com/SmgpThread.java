package smgp.parser.com;

import java.sql.SQLException;

import main.parser.com.Parsers;

public class SmgpThread extends Thread {

	@SuppressWarnings("static-access")
	public void run () {
		
//		SmgpParser obj = new SmgpParser();
		
		Parsers main = new Parsers();
		
//		try {
//			main.createBase(obj.tours);
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}

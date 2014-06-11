package smgp.parser.com;

import java.sql.SQLException;

import main.parser.com.Main;

public class SmgpThread extends Thread {

	@SuppressWarnings("static-access")
	public void run () {
		
//		SmgpParser obj = new SmgpParser();
		
		Main main = new Main();
		
//		try {
//			main.createBase(obj.tours);
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}

package hottours.parser.com;

import java.sql.SQLException;

import main.parser.com.Parsers;

public class HottoursThread extends Thread {

	@SuppressWarnings("static-access")
	public void run() {
		
//		HottoursParser obj = new HottoursParser();
		
		Parsers main = new Parsers();
		
//		try {
//			main.createBase(obj.tours);
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}

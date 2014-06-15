package test.parser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Main {

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection connection = null;
		connection = DriverManager.getConnection(
		   "jdbc:postgresql://localhost:5432/banan_db","postgres", "postgres");

		main.parser.com.Parsers.startParsing(connection);
		
		try {
			Thread.sleep(10 * 60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		connection.close();
		
	}

}

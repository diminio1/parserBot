/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ua.banan.data.model.Comment;
import ua.banan.data.model.FileParsingResult;
import ua.banan.data.provider.impl.DataOperatorImpl;
import ua.banan.parser.impl.ExcelParser;

/**
 *
 * @author swat
 */
public class NewEmptyJUnitTest {
    
    public NewEmptyJUnitTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void hello() 
     {
     try {                        
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/banan_db","postgres", "postgres")) {
                DataOperatorImpl data = new DataOperatorImpl(connection);
                
                ExcelParser parser = new ExcelParser(data);
                
                FileParsingResult parsingResult = parser.parseTours(new File("1.xlsx"), "xlsx", data.getTourOperatorByCredentials("kenar", "kenar"));
                
                parsingResult.getErrors();
//                data.savePartnerComment(comment);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            //bananLog.write(null, DATE_FORMATTER.format(new java.util.Date()) + " SQLException: " + e.getMessage() + bananLog.bananStackTraceToString(e) + "\n");
        }
     }
}

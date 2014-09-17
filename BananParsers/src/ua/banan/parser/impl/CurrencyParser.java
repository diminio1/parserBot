package money.currency;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import banan.file.writer.BananFileWriter;

public class Currency {
    
        public double dollar = 0;
        public double euro = 0;
    
        public Currency() {
        Document curDoc = null;
        try {
            curDoc = Jsoup.connect("http://finance.ua/ua/").timeout(100000).get();
            Element currency = curDoc.select("table[class = table table-hover]").select("tbody").first();
            
            dollar = Double.valueOf(currency.select("tr").first().select("td").get(2).ownText());
                
            euro = Double.valueOf(currency.select("tr").get(1).select("td").get(2).ownText());
                
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        	dollar = 12.00;
        	euro = 16.48;
        }
    }
}

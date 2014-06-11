package money.currency;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Currency {
    
        public double dollar = 0;
        public double euro = 0;
    
        public Currency() {
        Document curDoc = null;
        try {
            curDoc = Jsoup.connect("http://finance.ua/ua/").timeout(100000).get();
            Element currency = curDoc.select("table[class = table table-hover]").first();
            
            dollar = Double.valueOf(currency.select("td").get(1).text());
                
            euro = Double.valueOf(currency.select("td").get(3).text());
                
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

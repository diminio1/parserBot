/**
 * 
 */
package pogorelov.parser;

import java.util.ArrayList;
import java.util.List;

import main.parser.com.*;
/**
 * @author Pogorelov
 *
 */
public class CollectTours {

	public static void main(String[] args) {
		

		CollectTours tours = new CollectTours();
		tours.getHotTours();

		
		
	}
	
	public List<TourObject> getHotTours() {
		
		List<TourObject> listTours = new ArrayList<>();
		
		try {
			
//			TurneSite turne = new TurneSite();
//			listTours.addAll(turne.getToursList());
//			
//			OtpuskSite otpusk = new OtpuskSite();
//			listTours.addAll(otpusk.getToursList());
//			
//			TurtessSite turtess = new TurtessSite();
//			listTours.addAll(turtess.getToursList());
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return listTours;
	}
	
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.banan.data.model.Tour;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.impl.AkkordParser;
import ua.banan.parser.impl.CandytourParser;
import ua.banan.parser.impl.HColumbusParser;
import ua.banan.parser.impl.HTParser;
import ua.banan.parser.impl.HottoursParser;
import ua.banan.parser.impl.IttourParser;
import ua.banan.parser.impl.KazkamandrivParser;
import ua.banan.parser.impl.KenarParser;
import ua.banan.parser.impl.MansanaParser;
import ua.banan.parser.impl.NewstravelParser;
import ua.banan.parser.impl.OrionParser;
import ua.banan.parser.impl.PoehalisnamiParser;
import ua.banan.parser.impl.ShturmanParser;
import ua.banan.parser.impl.SilverParser;
import ua.banan.parser.impl.SmgpParser;
import ua.banan.parser.impl.TEZTourParser;
import ua.banan.parser.impl.TouraviaParser;
import ua.banan.parser.impl.TuiParser;
import ua.banan.parser.impl.Tur777Parser;

/**
 *
 * @author swat
 */
public class IndexProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexProcessor.class.getName());    
    /*
        7200 000 milliseconds = 2hours
    */
    private static final int SLEEP_BETWEEN_INDEXATIONS = 7200000;
    
    private final DataOperator dataOperator;
    private final List<Integer> idsOfTourOperatorsToIndex;
    
    private volatile boolean running = true;
    

    public IndexProcessor(DataOperator dataOperator, List<Integer> idsOfTourOperatorsToIndex) {
        this.dataOperator = dataOperator;
        this.idsOfTourOperatorsToIndex = idsOfTourOperatorsToIndex;
    }        
    
    public void terminate() {
        running = false;
    }
    
    @Override
    public void run() {
        System.out.println("Parsers thread starting!");
        LOGGER.error("Parsers thread starting!");
        
        if (dataOperator != null && idsOfTourOperatorsToIndex != null && !idsOfTourOperatorsToIndex.isEmpty()){            
            System.out.println("Parsers thread starting!");
            System.out.println(idsOfTourOperatorsToIndex.toString());
            LOGGER.error("Parsers thread starting!");
            
            List<Parser> parsers = new ArrayList<>();
                                                
            if (idsOfTourOperatorsToIndex.contains(AkkordParser.SOURCE_ID)) {
                LOGGER.error("Akkord is included to index!");
                System.out.println("Akkord starting!");
                
                parsers.add(new AkkordParser(dataOperator));               
            }
//            if (idsOfTourOperatorsToIndex.contains(ApltravelParser.SOURCE_ID)) {
//                parsers.add(new ApltravelParser(dataOperator));               
//            }
            if (idsOfTourOperatorsToIndex.contains(CandytourParser.SOURCE_ID)) {
                LOGGER.error("CandytourParser is included to index!");
                System.out.println("CandytourParser starting!");
                parsers.add(new CandytourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HColumbusParser.SOURCE_ID)) {
                LOGGER.error("HColumbusParser is included to index!");
                parsers.add(new HColumbusParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HTParser.SOURCE_ID)) {
                LOGGER.error("HTParser is included to index!");
                parsers.add(new HTParser(dataOperator));               
            }
//            if (idsOfTourOperatorsToIndex.contains(HottoursInParser.SOURCE_ID)) {
//                LOGGER.error("HottoursInParser is included to index!");
//                parsers.add(new HottoursInParser(dataOperator));               
//            }
            if (idsOfTourOperatorsToIndex.contains(HottoursParser.SOURCE_ID)) {
                LOGGER.error("HottoursParser is included to index!");
                parsers.add(new HottoursParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(IttourParser.SOURCE_ID)) {
                LOGGER.error("IttourParser is included to index!");
                parsers.add(new IttourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(KazkamandrivParser.SOURCE_ID)) {
                LOGGER.error("KazkamandrivParser is included to index!");
                parsers.add(new KazkamandrivParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(KenarParser.SOURCE_ID)) {
                LOGGER.error("KenarParser is included to index!");
                parsers.add(new KenarParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(MansanaParser.SOURCE_ID)) {
                LOGGER.error("MansanaParser is included to index!");
                parsers.add(new MansanaParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(NewstravelParser.SOURCE_ID)) {
                LOGGER.error("NewstravelParser is included to index!");
                parsers.add(new NewstravelParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(OrionParser.SOURCE_ID)) {
                LOGGER.error("OrionParser is included to index!");
                parsers.add(new OrionParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(PoehalisnamiParser.SOURCE_ID)) {
                LOGGER.error("PoehalisnamiParser is included to index!");
                parsers.add(new PoehalisnamiParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(ShturmanParser.SOURCE_ID)) {
                LOGGER.error("ShturmanParser is included to index!");
                parsers.add(new ShturmanParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(SilverParser.SOURCE_ID)) {
                LOGGER.error("SilverParser is included to index!");
                parsers.add(new SilverParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(SmgpParser.SOURCE_ID)) {
                LOGGER.error("SmgpParser is included to index!");
                parsers.add(new SmgpParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TEZTourParser.SOURCE_ID)) {
                LOGGER.error("TEZTourParser is included to index!");
                parsers.add(new TEZTourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TouraviaParser.SOURCE_ID)) {
                LOGGER.error("TouraviaParser is included to index!");
                parsers.add(new TouraviaParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TuiParser.SOURCE_ID)) {
                LOGGER.error("TuiParser is included to index!");
                parsers.add(new TuiParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(Tur777Parser.SOURCE_ID)) {
                LOGGER.error("Tur777Parser is included to index!");
                parsers.add(new Tur777Parser(dataOperator));               
            }
            
            if (!parsers.isEmpty()){
                while (running) {
                    for(Parser parser : parsers){
                        LOGGER.error("Parse iteration : id " + parser.getSourceId());
                        System.out.println("Parse iteration : id " + parser.getSourceId());
                        saveParsedTours(parser.parseTours(), parser.getSourceId());
                    }

                    LOGGER.error("Parse iteration finished, SLEEPING!");
                    System.out.println("Parse iteration finished, SLEEPING!");
                    try {
                        Thread.sleep(SLEEP_BETWEEN_INDEXATIONS);                
                    } catch (InterruptedException e) {
                        LOGGER.error("Exception ", e);
                        running = false;
                    }
                }
            }
        }        
    }
    
    /*
        Pass tours only from one specific tour operator!
    */
    private void saveParsedTours(List<Tour> tours, int sourceId){
        if (tours != null && !tours.isEmpty()){
            List<Tour> toursInDB = dataOperator.getToursByTourOperator(sourceId, null);
            
            List<Tour> intersection = Utils.intersection(tours, toursInDB);
            
            //remove old tours
            toursInDB.removeAll(intersection);
                    
            dataOperator.deleteTours(toursInDB);
            
            dataOperator.saveTours(tours);            
        }
    }
}

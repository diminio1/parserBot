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
        LOGGER.info("Parsers thread starting!");
        
        if (dataOperator != null && idsOfTourOperatorsToIndex != null && !idsOfTourOperatorsToIndex.isEmpty()){            
            
            List<Parser> parsers = new ArrayList<>();
            
            if (idsOfTourOperatorsToIndex.contains(AkkordParser.sourceId)) {
                LOGGER.info("Akkord is included to index!");
                parsers.add(new AkkordParser(dataOperator));               
            }
//            if (idsOfTourOperatorsToIndex.contains(ApltravelParser.sourceId)) {
//                parsers.add(new ApltravelParser(dataOperator));               
//            }
            if (idsOfTourOperatorsToIndex.contains(CandytourParser.sourceId)) {
                LOGGER.info("CandytourParser is included to index!");
                parsers.add(new CandytourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HColumbusParser.sourceId)) {
                LOGGER.info("HColumbusParser is included to index!");
                parsers.add(new HColumbusParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HTParser.sourceId)) {
                LOGGER.info("HTParser is included to index!");
                parsers.add(new HTParser(dataOperator));               
            }
//            if (idsOfTourOperatorsToIndex.contains(HottoursInParser.sourceId)) {
//                LOGGER.info("HottoursInParser is included to index!");
//                parsers.add(new HottoursInParser(dataOperator));               
//            }
            if (idsOfTourOperatorsToIndex.contains(HottoursParser.sourceId)) {
                LOGGER.info("HottoursParser is included to index!");
                parsers.add(new HottoursParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(IttourParser.sourceId)) {
                LOGGER.info("IttourParser is included to index!");
                parsers.add(new IttourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(KazkamandrivParser.sourceId)) {
                LOGGER.info("KazkamandrivParser is included to index!");
                parsers.add(new KazkamandrivParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(KenarParser.sourceId)) {
                LOGGER.info("KenarParser is included to index!");
                parsers.add(new KenarParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(MansanaParser.sourceId)) {
                LOGGER.info("MansanaParser is included to index!");
                parsers.add(new MansanaParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(NewstravelParser.sourceId)) {
                LOGGER.info("NewstravelParser is included to index!");
                parsers.add(new NewstravelParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(OrionParser.sourceId)) {
                LOGGER.info("OrionParser is included to index!");
                parsers.add(new OrionParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(PoehalisnamiParser.sourceId)) {
                LOGGER.info("PoehalisnamiParser is included to index!");
                parsers.add(new PoehalisnamiParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(ShturmanParser.sourceId)) {
                LOGGER.info("ShturmanParser is included to index!");
                parsers.add(new ShturmanParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(SilverParser.sourceId)) {
                LOGGER.info("SilverParser is included to index!");
                parsers.add(new SilverParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(SmgpParser.sourceId)) {
                LOGGER.info("SmgpParser is included to index!");
                parsers.add(new SmgpParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TEZTourParser.sourceId)) {
                LOGGER.info("TEZTourParser is included to index!");
                parsers.add(new TEZTourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TouraviaParser.sourceId)) {
                LOGGER.info("TouraviaParser is included to index!");
                parsers.add(new TouraviaParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TuiParser.sourceId)) {
                LOGGER.info("TuiParser is included to index!");
                parsers.add(new TuiParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(Tur777Parser.sourceId)) {
                LOGGER.info("Tur777Parser is included to index!");
                parsers.add(new Tur777Parser(dataOperator));               
            }
            
            if (!parsers.isEmpty()){
                while (running) {
                    for(Parser parser : parsers){
                        saveParsedTours(parser.parseTours(), parser.getSourceId());
                    }

                    LOGGER.info("Parse iteration finished, SLEEPING!");
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

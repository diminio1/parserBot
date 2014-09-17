/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.banan.data.model.Tour;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.impl.AkkordParser;

/**
 *
 * @author swat
 */
public class IndexProcessor implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(IndexProcessor.class.getName());    
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
        LOGGER.info("Parsers thread starting!");
        
        if (dataOperator != null && idsOfTourOperatorsToIndex != null && !idsOfTourOperatorsToIndex.isEmpty()){            
            
            List<Parser> parsers = new ArrayList<>();
            
            if (idsOfTourOperatorsToIndex.contains(AkkordParser.sourceId)) {
                parsers.add(new AkkordParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(ApltravelParser.sourceId)) {
                parsers.add(new ApltravelParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(CandytourParser.sourceId)) {
                parsers.add(new CandytourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HColumbusParser.sourceId)) {
                parsers.add(new HColumbusParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HTParser.sourceId)) {
                parsers.add(new HTParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HottoursInParser.sourceId)) {
                parsers.add(new HottoursInParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(HottoursParser.sourceId)) {
                parsers.add(new HottoursParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(IttourParser.sourceId)) {
                parsers.add(new IttourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(KazkamandrivParser.sourceId)) {
                parsers.add(new KazkamandrivParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(KenarParser.sourceId)) {
                parsers.add(new KenarParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(MansanaParser.sourceId)) {
                parsers.add(new MansanaParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(NewstravelParser.sourceId)) {
                parsers.add(new NewstravelParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(OrionParser.sourceId)) {
                parsers.add(new OrionParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(PoehalisnamiParser.sourceId)) {
                parsers.add(new PoehalisnamiParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(ShturmanParser.sourceId)) {
                parsers.add(new ShturmanParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(SilverParser.sourceId)) {
                parsers.add(new SilverParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(SmgpParser.sourceId)) {
                parsers.add(new SmgpParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TEZTourParser.sourceId)) {
                parsers.add(new TEZTourParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TouraviaParser.sourceId)) {
                parsers.add(new TouraviaParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(TuiParser.sourceId)) {
                parsers.add(new TuiParser(dataOperator));               
            }
            if (idsOfTourOperatorsToIndex.contains(Tur777Parser.sourceId)) {
                parsers.add(new Tur777Parser(dataOperator));               
            }
            
            if (!parsers.isEmpty()){
                while (running) {
                    for(Parser parser : parsers){
                        saveParsedTours(parser.parseTours(), parser.getSourceId());
                    }

                    LOGGER.info("Parse iteration finished, SLEEPING!");
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

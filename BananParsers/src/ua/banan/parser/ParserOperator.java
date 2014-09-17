/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.impl.AkkordParser;

/**
 *
 * @author swat
 */
public class ParserOperator {
    private static final Logger LOGGER = LogManager.getLogger(ParserOperator.class.getName());    

    
    public void startIndexing(DataOperator dataOperator, List<Integer> idsOfTourOperatorsToIndex){
        if (dataOperator != null && idsOfTourOperatorsToIndex != null && !idsOfTourOperatorsToIndex.isEmpty()){
            
            Parser parser;
            
            if (idsOfTourOperatorsToIndex.contains(AkkordParser.sourceId)) {
                
                parser = new AkkordParser(dataOperator);
                
                
            }
        }
    }
    
    public void stopIndexing(List<Integer> idsOfTourOperatorsToParse){
        throw new UnsupportedOperationException("Haven't been implemented yet");
    }
}

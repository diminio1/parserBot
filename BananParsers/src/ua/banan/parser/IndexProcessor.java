/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser;

import java.util.List;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.impl.AkkordParser;

/**
 *
 * @author swat
 */
public class IndexProcessor implements Runnable {

    /*
        7200 000 milliseconds = 2hours
    */
    private static final int SLEEP_BETWEEN_INDEXATIONS = 7200000;
    
    
    private final DataOperator dataOperator;
    private final List<Integer> idsOfTourOperatorsToIndex;

    public IndexProcessor(DataOperator dataOperator, List<Integer> idsOfTourOperatorsToIndex) {
        this.dataOperator = dataOperator;
        this.idsOfTourOperatorsToIndex = idsOfTourOperatorsToIndex;
    }        
    
    @Override
    public void run() {
        if (dataOperator != null && idsOfTourOperatorsToIndex != null && !idsOfTourOperatorsToIndex.isEmpty()){
            
            Parser parser;
            
            if (idsOfTourOperatorsToIndex.contains(AkkordParser.sourceId)) {
                
                parser = new AkkordParser(dataOperator);
                
                //TODO ::
            }
        }
    }
    
}

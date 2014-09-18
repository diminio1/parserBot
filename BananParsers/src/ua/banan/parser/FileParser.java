/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser;

import java.io.File;
import ua.banan.data.model.FileParsingResult;
import ua.banan.data.model.TourOperator;

/**
 *
 * @author swat
 */
public interface FileParser {
    public FileParsingResult parseTours(File file, TourOperator tourOperator);        
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser;

import java.util.List;
import ua.banan.data.model.Tour;

/**
 *
 * @author swat
 */
public interface Parser {
    public List<Tour> parseTours();
}

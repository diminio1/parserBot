/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ua.banan.data.model.City;
import ua.banan.data.model.FileParsingResult;
import ua.banan.data.model.Tour;
import ua.banan.data.model.TourOperator;
import ua.banan.data.model.common.Utils;
import ua.banan.data.provider.DataOperator;
import ua.banan.parser.FileParser;

/**
 *
 * @author swat
 */
public class ExcelParser extends AbstractParser implements FileParser {
    private static final Logger LOGGER = LogManager.getLogger(ExcelParser.class.getName());    

    public ExcelParser(DataOperator dataOperator) {
        super(dataOperator);
    }

    @Override
    protected Date parseDate(String inputString) {
        return null;//already parsed by Excel
    }

    @Override
    protected String parseHotelName(String nameContainer) {
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {
        return parseInt(starsContainer);
    }
      
    
    @Override
    public FileParsingResult parseTours(File file, TourOperator tourOperator) {
        FileParsingResult parsingResult = new FileParsingResult();
        
        if (file != null && tourOperator != null) {
            try {
                List<Tour> res = new ArrayList<>();

                Workbook wb;
                Row row;

                if (file.getAbsolutePath().toLowerCase().contains(".xlsx")) {
                    try (OPCPackage pkg = OPCPackage.open(file)) {
                        wb = new XSSFWorkbook(pkg);
                    }
                } else if (file.getAbsolutePath().toLowerCase().contains(".xls")) {
                    POIFSFileSystem fileSystem = new POIFSFileSystem(new FileInputStream(file));
                    wb = new HSSFWorkbook(fileSystem);
                } else {                    
                    parsingResult.setErrors("Неправильный формат документа");
                    
                    return parsingResult;
                }

                if (wb.getNumberOfSheets() == 0) {                                        
                    parsingResult.setErrors("Не удалось загрузить документ");
                    
                    return parsingResult;
                }  

                String errorsString = "";
                String warningsString = "";

                Sheet sheet = wb.getSheetAt(0);

                for(int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                    row = sheet.getRow(rowIndex);                
                    if (!isEmpty(row)) {                                                                  
                        String country = safeToString(safeGetCell(row, 0));
                        String town = safeToString(safeGetCell(row, 1));
                        String hotel = safeToString(safeGetCell(row, 2));
                        String stars = safeToString(safeGetCell(row, 3));
                        String roomType = safeToString(safeGetCell(row, 4));
                        String nutrition = safeToString(safeGetCell(row, 5));
                        String oldPrice = safeToString(safeGetCell(row, 6));
                        String price = safeToString(safeGetCell(row, 7));
                        Date departDate = safeGetCell(row, 8).getDateCellValue();
                        String duration = safeToString(safeGetCell(row, 9));
                        String departCity = safeToString(safeGetCell(row, 10));
                        String description = safeToString(safeGetCell(row, 11));


                        Tour tour = new Tour();

                        tour.setPrice(parsePrice(price));
                        tour.setPreviousPrice(parsePrice(oldPrice));
                        tour.setFeedPlan(parseFeedPlan(nutrition));
                        tour.setRoomType(parseRoomType(roomType));
                        tour.setNightsCount(parseNightCount(duration));
                        tour.setFlightDate(departDate);
                        tour.setDescription(description);
                        tour.setCountries(parseCountries(country));
                        tour.setCities(parseCities(town, Utils.getIds(tour.getCountries())));

                        List<City> cities = tour.getCities();
                        if (cities != null && cities.size() == 1){
                            tour.setHotel(parseHotel(hotel, stars, cities.get(0).getId()));
                        }

                        List<City> departCities = parseCities(departCity, Arrays.asList(new Integer[]{112}));//ID OF UKRAINE == 112
                        if (departCities != null && !departCities.isEmpty()){
                            tour.setDepartCity(departCities.get(0));                    
                        }

                        tour.setTourOperator(tourOperator);  

                        String tourWarnings = tour.getWarningsString();
                        if (tourWarnings != null && !tourWarnings.isEmpty()){
                            warningsString += "Строка " + rowIndex + ": " + tourWarnings + "\n";
                        }

                        if(tour.isValid()){
                            res.add(tour);                           
                        } else {
                            errorsString += "Строка " + rowIndex + ": " + tour.getErrorsString() + "\n";
                        }              
                    }
                }                         

                parsingResult.setErrors(errorsString);
                parsingResult.setWarnings(warningsString);
                parsingResult.setTours(res);
                
                return parsingResult;

            } catch(Exception e) {
                LOGGER.error("Error parsing Excel file", e);
                
                parsingResult.setErrors("Не удалось выгрузить туры! Попробуйте еще раз, а в случае неудачи проверьте заполнение файла!");
                
                return parsingResult;
            }    
        }
        
        parsingResult.setErrors("Не удалось выгрузить туры! Не указан файл или туроператор!");
        
        return parsingResult;
    }
    
    private static boolean isEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++){
            Cell cell = row.getCell(i);

            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK){
                return false;
            }
        }
        
        return true;
    }       

    private static Cell safeGetCell(Row row, int i){
        if (row != null) {
            Cell cell = row.getCell(i);
            String tmp = cell != null ? cell.toString() : null;
            if (tmp != null && !tmp.isEmpty() && !tmp.equals("-")) {
                return cell;
            }            
        }
        
        return null;        
    }
    
    private static String safeToString(Object object) {
        return object != null ? object.toString() : "";
    }
}

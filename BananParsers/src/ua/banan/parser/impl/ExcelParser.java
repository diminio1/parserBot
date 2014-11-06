/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ua.banan.data.provider.impl.DataOperatorImpl;
import ua.banan.parser.FileParser;

/**
 *
 * @author swat
 */
public class ExcelParser extends AbstractParser implements FileParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelParser.class.getName());    

    public ExcelParser(DataOperator dataOperator) {
        super(dataOperator);
    }   

    @Override
    protected String parseHotelName(String nameContainer) {
        return nameContainer;
    }

    @Override
    protected Integer parseHotelStars(String starsContainer) {        
        starsContainer = starsContainer != null ? starsContainer.replace("*", "") : null;
        
        Integer res = parseIntFromDouble(starsContainer);                
        
        return res != null && res <= 5 ? res : null;
    }
      
    @Override
    protected Integer parseNightCount(String inputString){
        return parseIntFromDouble(inputString);
    }
    
    private Integer parseIntFromDouble(String intString) {
        if (intString != null && !intString.isEmpty()){
            int pointIndex = intString.indexOf('.');
            Integer res;
            
            if (pointIndex != -1){
                res = parseInt(intString.substring(0, pointIndex));
            } else {
                res = parseInt(intString);
            }
            
            return (res != null && res > 0) ? res : null;
        }
        
        return null;
    }
    
    @Override
    protected Integer parsePrice(String inputString){
        if (inputString != null && !inputString.isEmpty()){
            int pointIndex = inputString.indexOf('.');
            Integer res;
            
            if (pointIndex != -1){
                res = super.parsePrice(inputString.substring(0, pointIndex));
            } else {
                res = super.parsePrice(inputString);
            }
            
            return (res != null && res > 0) ? res : null;
        }
        
        return null;        
    }
    
    
    
    @Override
    public FileParsingResult parseTours(File file, String extension, TourOperator tourOperator) {
        FileParsingResult parsingResult = new FileParsingResult();
        
        if (file != null && tourOperator != null) {
            try {
                List<Tour> res = new ArrayList<>();

                Workbook wb;
                Row row;
                                
                if (extension != null && extension.equalsIgnoreCase("xlsx")) {
                    try (OPCPackage pkg = OPCPackage.open(file)) {
                        wb = new XSSFWorkbook(pkg);
                    }
                } else if (extension != null && extension.equalsIgnoreCase("xls")){
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
                    try{
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
                            
                            List<Date> departDates;
                            
                            try {
                                departDates = Arrays.asList(row.getCell(8).getDateCellValue());
                            } catch (Exception e){
                                departDates = parseDates(safeToString(safeGetCell(row, 8)));
                            }
                            String duration = safeToString(safeGetCell(row, 9));
                            String departCity = safeToString(safeGetCell(row, 10));
                            String description = safeToString(safeGetCell(row, 11));
                            String url = safeToString(safeGetCell(row, 12));
                            String types = safeToString(safeGetCell(row, 13));

                            if(departDates != null && !departDates.isEmpty()){
                            for(Date departDate : departDates){
                                    Tour tour = new Tour();                       

                                    if(url != null){
                                        if(!url.contains("http")){
                                            url = "http://" + url;
                                        }

                                        tour.setUrl(url);
                                    } else {
                                        tour.setUrl(Utils.prepandHttpIfNotExists(tourOperator.getUrl()));
                                    }
                                    tour.setPrice(parsePrice(price));
                                    tour.setPreviousPrice(parsePrice(oldPrice));
                                    tour.setFeedPlan(parseFeedPlan(nutrition));
                                    tour.setRoomType(parseRoomType(roomType));
                                    tour.setNightsCount(parseNightCount(duration));
                                    tour.setFlightDate(departDate);
                                    tour.setDescription(description);
                                    tour.setCountries(parseCountries(country));
                                    tour.setCities(parseCities(town, Utils.getIds(tour.getCountries())));
                                    tour.setTypes(parseTypes(types));

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
                                        warningsString += "Строка " + (rowIndex + 1) + ". " + tourWarnings + "\n";
                                    }

                                    if(tour.isValid()){
                                        int indexOfTheSameTour = res.indexOf(tour);

                                        if (indexOfTheSameTour == -1){
                                            if(tour.isActual()) {

                                                if(tour.getTypes() == null || tour.getTypes().isEmpty()){
                                                    List<Integer> typesIds = new ArrayList<>();

                                                    if(((DataOperatorImpl) dataOperator).isBusTour(tour)){
                                                        typesIds.add(Tour.TYPE_EXCURSION);
                                                    }

                                                    if(((DataOperatorImpl) dataOperator).isSeaTour(tour)){
                                                        typesIds.add(Tour.TYPE_SEA);
                                                    }

                                                    tour.setTypes(typesIds);
                                                }

                                                res.add(tour);       
                                            } else {
                                                warningsString += "Строка " + (rowIndex + 1) + ". Дата выезда завтра или раньше - турист на этот тур не успеет! \n";
                                            }
                                        } else {
                                            warningsString += "Строка " + (rowIndex + 1) + ". " + "Такой тур уже есть в этом документе! Где-то в районе строки " + (indexOfTheSameTour + 2) + " :) \n";
                                        }
                                    } else {
                                        errorsString += "Строка " + (rowIndex + 1) + ". " + tour.getErrorsString() + "\n";
                                    } 
                                }
                            } else {
                                errorsString += "Строка " + (rowIndex + 1) + ". Неправильно заполнены даты выезда  \n";
                            }
                        }
                    } catch (Exception e){                        
                        LOGGER.error("Error parsing Excel file", e);
                        
                        errorsString += "Строка " + (rowIndex + 1) + ". Ошибка. Проверьте правильность заполнения\n";
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
            Cell cell = i >= 0 ? row.getCell(i) : null;

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

    private List<Integer> parseTypes(String types) {
        if(types != null && !types.isEmpty()){
            List<Integer> res = new ArrayList<>();
            
            types = types.toLowerCase();
            
            if(types.contains("пляжный")){
                res.add(Tour.TYPE_SEA);
            }
            
            if(types.contains("экскурсионный") || types.contains("автобусный")){
                res.add(Tour.TYPE_EXCURSION);
            }
            
            if(types.contains("детский")){
                res.add(Tour.TYPE_CHILDREN);
            }
            
            if(types.contains("гастрономический")){
                res.add(Tour.TYPE_GASTRONOMIC);
            }
            
            if(types.contains("шоп")){
                res.add(Tour.TYPE_SHOP);
            }
            
            if(types.contains("индивидуальный")){
                res.add(Tour.TYPE_INDIVIDUAL);
            }
            
            if(types.contains("выходного")){
                res.add(Tour.TYPE_WEEKEND);
            }
            
            if(types.contains("экстрим")){
                res.add(Tour.TYPE_EXTREME);
            }
            
            return res;
        }
        
        return null;
    }

    private List<Date> parseDates(String datesString) {
        if(datesString != null && !datesString.isEmpty()){
            String[] split = datesString.split(";");
            
            if(split != null && split.length > 0){
                List<Date> res = new ArrayList<>();
                
                for(String dateStr : split){
                    Date d = parseDate(dateStr.trim());
                    if(d != null){
                        res.add(d);
                    }
                }
                
                return res.isEmpty() ? null : res;
            } else {
                return Arrays.asList(parseDate(datesString));
            }                    
        }
        
        return null;
    }
    
    @Override
    protected Date parseDate(String inputString) {        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return dateFormat.parse(inputString);
        } catch (ParseException ex) {
            LOGGER.error("Parsing date error " + ex.getMessage(), ex);
            return null;
        }
    }
                    
}

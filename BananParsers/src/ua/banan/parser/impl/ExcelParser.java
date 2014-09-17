/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.banan.parser.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.banan.data.model.CurrencyExchanger;
import ua.banan.data.model.Tour;
import ua.banan.data.model.common.Pair;
import ua.banan.data.provider.DataOperator;

/**
 *
 * @author swat
 */
public class ExcelParser extends AbstractParser {
    private static final Logger LOGGER = LogManager.getLogger(ExcelParser.class.getName());    

    public ExcelParser(DataOperator dataOperator, CurrencyExchanger currencyExchanger) {
        super(dataOperator, currencyExchanger);
    }
    
    public static Pair<List<Tour>, String> parseFile(File file) {
        try {
            List<Tour> resArray = new ArrayList<>();
            
            POIFSFileSystem fileSystem;
            OPCPackage pkg = null;
            Workbook wb;
            Row row;
            boolean format = false; // false -> .xls, true -> .xlsx
            
            if (file.getAbsolutePath().contains(".xlsx")) {
                format = true;
                pkg = OPCPackage.open(file);
                wb = new XSSFWorkbook(pkg);
         
            } else if (file.getAbsolutePath().contains(".xls")) {
                fileSystem = new POIFSFileSystem(new FileInputStream(file));
                wb = new HSSFWorkbook(fileSystem);
            }
            else {
                return new Pair(null, "Неправильный формат документа\n");
            }

            boolean errors = false;
            boolean correct = true;
            String errorsStr = "Ошибки:\n";

            if (wb == null || wb.getNumberOfSheets() == 0) {
                return new Pair(null, "Не удалось загрузить документ\n");
            }
            int r = 1;
            
            Iterator rowItr = wb.getSheetAt(0).rowIterator();
            
            if (format) {
                row = (XSSFRow)rowItr.next();
            } else {
                row = (HSSFRow)rowItr.next();
            }

            
            while((!isEmpty(row))) {
                
                if (format) {
                    row = (XSSFRow)rowItr.next();
                } else {
                    row = (HSSFRow)rowItr.next();
                }
                
                if(isEmpty(row)) {
                    break;
                }

                String country = safeToString(safeGetCell(row, 0));
                String town = safeToString(safeGetCell(row, 1));
                String hotel = safeToString(safeGetCell(row, 2));
                String stars = safeToString(safeGetCell(row, 3));
                String roomType = safeToString(safeGetCell(row, 4));
                String nutrition = safeToString(safeGetCell(row, 5));
                String oldPrice = safeToString(safeGetCell(row, 6));
                String price = safeToString(safeGetCell(row, 7));
                String duration = safeToString(safeGetCell(row, 9));
                String departCity = safeToString(safeGetCell(row, 10));
                String description = safeToString(safeGetCell(row, 11));
                    
                Tour tourObject = new Tour();
                
                tourObject.setCountry(country.toUpperCase(), countryStand, bananLog);
                if (!country.equals("") && tourObject.country != null && !tourObject.country.isEmpty()) {
                    List<Integer> countriesIds = new ArrayList();
                    List<String> countriesNames = new ArrayList();
                    for (Pair p: tourObject.country) {
                        Integer id = (Integer)p.getFirst();
                        countriesIds.add(id);
                        countriesNames.add(getCountryNameById(id));
                    }                        
                    tourFullContainer.setCountryIds(countriesIds);
                    tourFullContainer.setCountryNames(countriesNames);
                }
                else {
                        errors = true;
                        correct = false;
                        errorsStr += "    Строка №" + r + " : нет страны в туре\n";
                        bananLog.write(null, "No country\n");
                }
                    
                tourObject.setTown(town.toUpperCase(), cityStand, "\nexel: ", bananLog);
                if (tourObject.town != null && !tourObject.town.isEmpty()) {
                    List<Integer> citiesIds = new ArrayList();
                    List<String> citiesNames = new ArrayList();
                    for (Pair p: tourObject.town) {
                        Integer id = (Integer)p.getFirst();
                        citiesIds.add((id));
                        citiesNames.add(getCityNameById(id));
                    }
                    tourFullContainer.setCityIds(citiesIds);
                    tourFullContainer.setCityNames(citiesNames);
                }
                else {
                        bananLog.write(null, "No town\n");
                }
                
                try {
                    java.util.Date departDate = safeGetCell(row, 8).getDateCellValue();
                    tourFullContainer.setFlightDate(/*dateFormat.parse(*/departDate);
                }
                catch (IllegalStateException | NumberFormatException ex ) {
                    errors = true;
                    correct = false;
                    errorsStr = errorsStr + "    Строка №" + r + " : неправильный формат даты\n";
                }
                    
                tourFullContainer.setDescription(description);
                tourFullContainer.setFeedPlan(nutrition);
                tourFullContainer.setHotelName(hotel);
                tourFullContainer.setRoomType(roomType);
                    
                Currency currency = new Currency();
                    
                double k1 = 1; //currency of current price
                double k2 = 1; //currency of previous price
                    
                if (price.contains("$"))
                    k1 = currency.dollar;
                else if (price.contains("€"))
                    k1 = currency.euro;
                tourFullContainer.setPrice((int)(findInteger(price) * k1));
                if (tourFullContainer.getPrice() == 0){
                    errors = true;
                    correct = false;
                    errorsStr = errorsStr + "    Строка №" + r + " : нет цены\n";   
                }
                    
                if (oldPrice.contains("$"))
                    k2 = currency.dollar;
                else if (oldPrice.contains("€"))
                    k2 = currency.euro;
                    tourFullContainer.setPreviousPrice((int)(findInteger(oldPrice) * k2));
                if (tourFullContainer.getPreviousPrice() == 0) {
                    tourFullContainer.setPreviousPrice(null);
                }
                    
                tourFullContainer.setStars(findInteger(stars));
                tourFullContainer.setNightsCount(findInteger(duration));
                
                Tour tObj = new Tour();
                     
                try {
                    tObj.setTown(departCity.toUpperCase(), cityStand, "exel depart: ", bananLog);
                    int id = tObj.town.get(0).getFirst();
                    tourFullContainer.setDepartCityId(id);
                    tourFullContainer.setDepartCityName(getCityNameById(id));
                }
                catch (NullPointerException ex) {
                    bananLog.write(null, "\nCaught NullPointerException in depart city id\n");
                    tourFullContainer.setDepartCityId(null);
                }
                catch (IndexOutOfBoundsException ex) {
                    bananLog.write(null, "\nCaught IndexOutOfBoundsException in depart city id\n");
                    tourFullContainer.setDepartCityId(null);
                }
                
     
                if (correct) {
                     resArray.add(tourFullContainer);
                }
                correct = true;
                r++;
            }
         
            if (!errors) {
                errorsStr = "";
            }
            String result = "Успешно загружено: " + resArray.size() + ".\n" + errorsStr;
            if (pkg != null)
                pkg.close();               
            
            return new Pair(resArray, result);

        } catch(InvalidFormatException | IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        
    }
}

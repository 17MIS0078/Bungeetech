/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bungeetech;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author sivan
 */
public class Bungeetech {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        List<CSVData> csvData = new ArrayList<>();
        Scanner sc = null;
        URL url = null;
        try {
            url = new URL("https://raw.githubusercontent.com/17MIS0078/Bungeetech/master/input/main.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            sc = new Scanner(inputStreamReader);
            int lineNo = 0;
            while (sc.hasNext()) {
                String eachLine = sc.nextLine();
                prepareInputCSVData(lineNo, csvData, eachLine);
                lineNo++;
            }
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (sc != null) {
                sc.close();
            }
        }

        if (csvData != null && !csvData.isEmpty()) {
            prepareCountryOutputFile(csvData);
        }
        
        
    }

    private static void prepareCountryOutputFile(List<CSVData> csvData) {

        List<CSVData> usaCountryCSVData = csvData.stream()
                .filter(p -> p.getCountry().contains("USA"))
                .collect(Collectors.toList());

        writeUSACountryCSVFile(usaCountryCSVData);
    }

    private static void prepareInputCSVData(int lineNo, List<CSVData> csvData, String eachLine) {

        if (lineNo > 0) {
            String[] eachLineColumns = eachLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            String sku = eachLineColumns[0];
            String description = eachLineColumns[1];
            String year = eachLineColumns[2];
            String capacity = eachLineColumns[3];
            String url = eachLineColumns[4];
            String price = eachLineColumns[5];
            String sellerInformation = eachLineColumns[6];
            String offerDescription = eachLineColumns[7];
            String country = eachLineColumns[8];

            CSVData eachCSVData = new CSVData();
            eachCSVData.setSku(sku);
            eachCSVData.setDescription(description);
            eachCSVData.setYear(year);
            eachCSVData.setCapacity(capacity);
            eachCSVData.setUrl(url);
            eachCSVData.setPrice(price);
            eachCSVData.setSellerInformation(sellerInformation);
            eachCSVData.setOfferDescription(offerDescription);
            eachCSVData.setCountry(country);

            csvData.add(eachCSVData);
        }
    }

    private static void writeUSACountryCSVFile(List<CSVData> usaCountryCSVData) {

        List<String[]> csvData = new ArrayList<>();

        List<String[]> csvHeaderData = new ArrayList<>();
        List<String[]> csvBodyData = new ArrayList<>();

        csvHeaderData.add(new String[]{"SKU", "DESCRIPTION", "YEAR", "CAPACITY", "URL", "PRICE", "SELLER_INFORMATION", "OFFER_DESCRIPTION", "COUNTRY"});
        for (CSVData eachCSVData : usaCountryCSVData) {
            csvBodyData.add(new String[]{
                eachCSVData.getSku(),
                eachCSVData.getDescription(),
                eachCSVData.getYear(),
                eachCSVData.getCapacity(),
                eachCSVData.getUrl(),
                eachCSVData.getPrice(), eachCSVData.getSellerInformation(), eachCSVData.getOfferDescription(), eachCSVData.getCountry()
            });
        }

        csvData.addAll(csvHeaderData);
        csvData.addAll(csvBodyData);

        File csvOutputFile = null;
        try {
            URI fileUri = new URI("https://github.com/17MIS0078/Bungeetech/tree/master/output/filteredCountry.csv");
            int startIndex = fileUri.toString().lastIndexOf('/');
            String fileName = fileUri.toString().substring(startIndex + 1);
            System.out.println(fileName);
            csvOutputFile = new File(fileName);
            PrintWriter pw = new PrintWriter(csvOutputFile);
            csvData.stream()
                    .map(eachCsvData -> convertDataToCSV(eachCsvData))
                    .forEach(pw::println);
        } catch (FileNotFoundException | URISyntaxException ex) {
            Logger.getLogger(Bungeetech.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String convertDataToCSV(String[] data) {
        return Stream.of(data)
                .map(eachData -> escapeDataSpecialCharacters(eachData))
                .collect(Collectors.joining(","));
    }

    private static String escapeDataSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}

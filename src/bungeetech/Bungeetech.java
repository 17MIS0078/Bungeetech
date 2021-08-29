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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        List<CSVData> usaCountryCSVData = null;

        if (!csvData.isEmpty()) {
            usaCountryCSVData = prepareCountryOutputFile(csvData);
        }

        if (usaCountryCSVData != null && !usaCountryCSVData.isEmpty()) {
            prepareMinimumPriceFile(usaCountryCSVData);
        }
    }

    private static List<CSVData> prepareCountryOutputFile(List<CSVData> csvData) {

        List<CSVData> usaCountryCSVData = csvData.stream()
                .filter(p -> p.getCountry().contains("USA"))
                .collect(Collectors.toList());

        writeUSACountryCSVFile(usaCountryCSVData);

        return usaCountryCSVData;
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
        String fileLocation = "https://github.com/17MIS0078/Bungeetech/tree/master/output/filteredCountry.csv";
        writeOutputFile(csvData, fileLocation);
    }

    private static void writeOutputFile(List<String[]> csvData, String fileLocation) {

        File csvOutputFile = null;
        URI fileUri = null;
        PrintWriter pw =null;
        try {
            fileUri = new URI(fileLocation);
            int startIndex = fileUri.toString().lastIndexOf('/');
            String fileName = fileUri.toString().substring(startIndex + 1);
            csvOutputFile = new File(fileName);
            pw = new PrintWriter(csvOutputFile);
            csvData.stream()
                    .map(eachCsvData -> convertDataToCSV(eachCsvData))
                    .forEach(pw::println);

        } catch (FileNotFoundException | URISyntaxException ex) {
            System.out.println(ex.getMessage());
        }
        finally{
            if(pw!=null){
                pw.close();
            }
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

    private static void prepareMinimumPriceFile(List<CSVData> usaCountryCSVData) {

        Map<String, List<CSVData>> countryDataBySKU = usaCountryCSVData.stream().collect(Collectors.groupingBy(CSVData::getSku));

        List<String[]> priceCSVData = new ArrayList<>();
        List<String[]> priceCSVHeaderData = new ArrayList<>();
        List<String[]> priceCSVBodyData = new ArrayList<>();

        priceCSVHeaderData.add(new String[]{"SKU", "FIRST_MINIMUM_PRICE", "FIRST_SECOND_PRICE"});

        for (Map.Entry<String, List<CSVData>> entry : countryDataBySKU.entrySet()) {

            List<CSVData> skuData = entry.getValue();

            List<String> priceList = null;

            String firstMinPrice = "";
            String secondMinPrice = "";

            if (skuData != null && !skuData.isEmpty()) {

                priceList = skuData.stream().map(CSVData::getPrice).collect(Collectors.toList());
            }

            if (priceList != null && !priceList.isEmpty()) {

                Collections.sort(priceList);

                firstMinPrice = priceList.get(0);

                if (priceList.size() > 1) {
                    secondMinPrice = priceList.get(1);
                }
            }

            priceCSVBodyData.add(new String[]{entry.getKey(), firstMinPrice.replace("$", ""), secondMinPrice.replace("$", "")});
        }

        priceCSVData.addAll(priceCSVHeaderData);
        priceCSVData.addAll(priceCSVBodyData);

        String fileLocation = "https://github.com/17MIS0078/Bungeetech/tree/master/output/lowestPrice.csv";

        writeOutputFile(priceCSVData, fileLocation);
    }
}

package beam.utils;

import beam.utils.coordmodel.*;
import com.google.gson.GsonBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Fetch Parking data from Coord API
 * access_key may change after a period of time
 */
public class CoordParkingData {
    public static void main(String[] args) throws IOException {
        String filename = "";
        long totalCurbLength = 0;
        double minPricePerHour = 0;
        double maxPricePerHour = 0;
        double maxParkingDuration = 0;

        StringBuilder result = new StringBuilder();

        /**
         * this will be used after discussing the final format
         */
//        result=getDataFromAPI();


        /**
         * using file for test purpose
         */
        result = getDataFromFile();

        CoordDetail coordDetail = new GsonBuilder().create().fromJson(result.toString(), CoordDetail.class);

        StringBuilder stringBuilder = new StringBuilder();

        Map<String, OutputFormat> csvMap = new HashMap<>();
        /*
        String header = "curbId,startLongitude,startLatitude,endLongitude,feePerHour,endLatitude,totalCurbLength,totalNoParkingLength,totalNoStoppingLength,totalFreeParkingLength,totalPaidParkingLength,totalLoadingZoneLength,totalPassengerLoadingZoneLength,maxParkingDuration"
        stringBuilder.append(header);
        */

        List<Feature> features = coordDetail.getFeatures();
        if (features == null || features.isEmpty()) {
            throw new IOException("File is Empty or Invalid");
        }
        Double startLong = features.get(0).getGeometry().getCoordinates().get(0).get(0);
        Double startLat = features.get(0).getGeometry().getCoordinates().get(0).get(1);
        Double endLong = features.get(features.size() - 1).getGeometry().getCoordinates().get(features.get(features.size() - 1).getGeometry().getCoordinates().size() - 1).get(0);
        Double endLat = features.get(features.size() - 1).getGeometry().getCoordinates().get(features.get(features.size() - 1).getGeometry().getCoordinates().size() - 1).get(1);
        stringBuilder.append("StartLong:").append(startLong).append(",StartLat:").append(startLat).append("EndLong:").append(endLong).append(",EndLat:").append(endLat);


        for (Feature feature : coordDetail.getFeatures()) {
            String curbId = feature.getProperties().getMetadata().getCurbId();
            OutputFormat outputFormat = null;
            if (!csvMap.isEmpty())
                outputFormat = csvMap.get(curbId);

            if (outputFormat == null) {
                totalCurbLength = 0;
                minPricePerHour = maxPricePerHour = 0;
                maxParkingDuration = 0;
                outputFormat = new OutputFormat();
                outputFormat.setCurbId(curbId);
                outputFormat.setStartLatitude("" + startLat);
                outputFormat.setStartLongitude("" + startLong);
                outputFormat.setEndLatitude("" + endLat);
                outputFormat.setEndLongitude("" + endLong);
            }

            double startMeters = feature.getProperties().getMetadata().getDistanceStartMeters() == null ? 0.0 : feature.getProperties().getMetadata().getDistanceStartMeters();
            double endMeters = feature.getProperties().getMetadata().getDistanceEndMeters();
            long curbLength = Math.round((endMeters - startMeters) * 3.280839895013123);
            totalCurbLength = totalCurbLength + curbLength;

            System.out.print("\n" + curbLength + " FT ");

            Set<String> permittedSet = new HashSet<>();
            for (Rule rule : feature.getProperties().getRules()) {

                switch (rule.getPrimary()) {
                    case NONE:
                        if (rule.getPermitted().isEmpty()) {
                            permittedSet.add(OutputFormat.LengthHeading.NO_STOPPING.toString());
                            addLengthToMap(curbLength, outputFormat, OutputFormat.LengthHeading.NO_STOPPING);
                        } else {
                            permittedSet.add(OutputFormat.LengthHeading.NO_PARKING.toString());
                            addLengthToMap(curbLength, outputFormat, OutputFormat.LengthHeading.NO_PARKING);
                        }
                        break;
                    case PARK:
                        if (rule.getMaxDurationH() != null && maxParkingDuration < rule.getMaxDurationH()) {
                            maxParkingDuration = rule.getMaxDurationH();
                        }
                        List<Price> prices = rule.getPrice();
                        Double pricePerHour = prices.get(0).getPricePerHour().getAmount();
                        if (pricePerHour == null || pricePerHour == 0) {
                            permittedSet.add(OutputFormat.LengthHeading.FREE_PARKING.toString());
                            addLengthToMap(curbLength, outputFormat, OutputFormat.LengthHeading.FREE_PARKING);
                        } else if (pricePerHour > 0) {
                            pricePerHour = pricePerHour / 100;
                            if (minPricePerHour > pricePerHour) {
                                minPricePerHour = pricePerHour;
                            }
                            if (maxPricePerHour < pricePerHour) {
                                maxPricePerHour = pricePerHour;
                            }
                            permittedSet.add(OutputFormat.LengthHeading.PAID_PARKING.toString());
                            addLengthToMap(curbLength, outputFormat, OutputFormat.LengthHeading.PAID_PARKING);
                        }
                        break;
                    case LOAD_GOODS:
                        permittedSet.add(OutputFormat.LengthHeading.LOADING_ZONE.toString());
                        addLengthToMap(curbLength, outputFormat, OutputFormat.LengthHeading.LOADING_ZONE);
                        break;
                    case LOAD_PASSENGERS:
                        permittedSet.add(OutputFormat.LengthHeading.PASSENGER_LOADING_ZONE.toString());
                        addLengthToMap(curbLength, outputFormat, OutputFormat.LengthHeading.PASSENGER_LOADING_ZONE);
                        break;

                }
            }

            outputFormat.setTotalCurbLength(totalCurbLength);
            for (OutputFormat.LengthHeading item : outputFormat.getPermittedTypeLengthMap().keySet()) {
                long val = outputFormat.getPermittedTypeLengthMap().get(item);
                switch (item) {
                    case NO_STOPPING:
                        outputFormat.setTotalNoStoppingLength(val);
                        break;
                    case FREE_PARKING:
                        outputFormat.setTotalFreeParkingLength(val);
                        break;
                    case PAID_PARKING:
                        outputFormat.setTotalPaidParkingLength(val);
                        break;
                    case LOADING_ZONE:
                        outputFormat.setTotalLoadingZoneLength(val);
                        break;
                    case NO_PARKING:
                        outputFormat.setTotalNoParkingLength(val);
                        break;
                    case PASSENGER_LOADING_ZONE:
                        outputFormat.setTotalPassengerLoadingZoneLength(val);
                        break;
                    default:
                        continue;
                }
            }

            for (String item : permittedSet) {
                System.out.print(" " + item);
            }

            String feePerHour = (minPricePerHour == maxPricePerHour) ? "" + minPricePerHour : minPricePerHour + "-" + maxPricePerHour;

            outputFormat.setMaxParkingDuration(maxParkingDuration);
            outputFormat.setFeePerHour(feePerHour);
            csvMap.put(curbId, outputFormat);
        }

        System.out.println("\n\nFIELDS:\n" + csvMap.get("c2Y6MTQzODY")); //hardconding the name of single curb for now only
        System.out.println("\n\nCSV OUTPUT:\n" + csvMap.get("c2Y6MTQzODY").getCsvString()); //hardconding the name of single curb for now only
        /*CSVWriter csv = new CSVWriter("sample.csv");
        csv.getBufferedWriter().append(stringBuilder.toString());
        csv.getBufferedWriter().flush();
        csv.closeFile();*/
    }

    //using file for testing only
    private static StringBuilder getDataFromFile() {
        try (FileReader fileReader = new FileReader(new File("test/input/beamville/c2Y6MTQzODY.json")); BufferedReader rdr = new BufferedReader(fileReader)) {
            StringBuilder result = new StringBuilder();
            String line = null;

            while ((line = rdr.readLine()) != null) {
                result.append(line);
            }
            return result;
        } catch (IOException e) {
            System.err.println("ERROR:" + e);
            return null;
        }
    }

    private static StringBuilder getDataFromAPI() throws IOException {
        StringBuilder result = new StringBuilder();
        String latitude = "37.761479";
        String longitude = "-122.448245";
        String radius = "7.8";
//        String duration = "1";
        String accessKey = "Lnkj2-HUkevsvbTSYXA8Hg6FhtMFgeJJRYYMF_Fboio";
        String api = "https://api.sandbox.coord.co/v1/search/curbs/bylocation/all_rules?latitude=" + latitude + "&longitude=" + longitude + "&radius_km=" + radius + "&access_key=" + accessKey;
        String line;
        URL url = new URL(api);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result;
    }

    private static void addLengthToMap(long length, OutputFormat outputFormat, OutputFormat.LengthHeading lengthHeading) {
        Long prevLength = outputFormat.getPermittedTypeLengthMap().get(lengthHeading);
        if (prevLength == null) {
            outputFormat.getPermittedTypeLengthMap().put(lengthHeading, length);
        } else {
            outputFormat.getPermittedTypeLengthMap().put(lengthHeading, prevLength + length);
        }
    }

}
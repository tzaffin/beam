package beam.utils;

import beam.analysis.via.CSVWriter;
import beam.utils.coordmodel.*;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Fetch Parking data from Coord API
 * access_key may change after a period of time
 */
public class CoordParkingData {
    private static final String JSON_FILE = "output/beamville/coordData.json";
    private static final String CSV_FILE = "output/beamville/coordData.csv";
    public static void main(String[] args) throws IOException {
        int segementNumber = 1;

        StringBuilder result = new StringBuilder();
        result = getDataFromAPI();
//        result = getDataFromFile(); //used for testing

        CoordDetail coordDetail = new GsonBuilder().create().fromJson(result.toString(), CoordDetail.class);

        StringBuilder stringBuilder = new StringBuilder();
        String header="curbId,startLongitude,startLatitude,endLongitude,endLatitude,segmentId,ruleNumber,segmentLength_Meters,primaryRule,days,startTime,endTime,feePerHour,permittedVehicle,otherVehiclesPermitted,maxDuration_Hours,vehicleType,parkingType";
        stringBuilder.append(header).append("\n");
        Map<String, List<OutputFormat>> curbDataMap = new HashMap<>();
        List<Feature> features = coordDetail.getFeatures();
        if (features == null || features.isEmpty()) {
            throw new IOException("File is Empty or Invalid");
        }

        for (Feature feature : coordDetail.getFeatures()) {
            List<OutputFormat> newRowsCollection = null;
            String curbId = feature.getProperties().getMetadata().getCurbId();

            if (!curbDataMap.isEmpty()) {
                newRowsCollection = curbDataMap.get(curbId);
            }

            if (newRowsCollection == null || newRowsCollection.isEmpty()) {
                newRowsCollection = new ArrayList<>();
                segementNumber = 0;
            }

            double startMeters = feature.getProperties().getMetadata().getDistanceStartMeters() == null ? 0.0 : feature.getProperties().getMetadata().getDistanceStartMeters();
            double endMeters = feature.getProperties().getMetadata().getDistanceEndMeters();
            double segmentLength = endMeters - startMeters;
            segementNumber++;

            int ruleNum = 1;
            for (Rule rule : feature.getProperties().getRules()) {

                String feePerHour = "";
                String parkingType = getParkingType(rule);
                if (parkingType.equalsIgnoreCase(OutputFormat.LengthHeading.PAID_PARKING.toString())) {
                    feePerHour = "" + rule.getPrice().get(0).getPricePerHour().getAmount();
                }
                for (Time time : rule.getTimes()) {
                    OutputFormat csvRow = new OutputFormat();
                    csvRow.setStartLongitude("" + feature.getGeometry().getCoordinates().get(0).get(0));
                    csvRow.setStartLatitude("" + feature.getGeometry().getCoordinates().get(0).get(1));
                    csvRow.setEndLatitude("" + feature.getGeometry().getCoordinates().get(feature.getGeometry().getCoordinates().size() - 1).get(0));
                    csvRow.setEndLongitude("" + feature.getGeometry().getCoordinates().get(feature.getGeometry().getCoordinates().size() - 1).get(1));
                    csvRow.setSegmentId(curbId + " " + segmentLength);
                    csvRow.setCurbId(curbId);
                    csvRow.setFeePerHour(feePerHour);
                    csvRow.setSegmentId(curbId + segementNumber);
                    csvRow.setRuleNumber(ruleNum);
                    csvRow.setPermittedVehicle(StringUtils.join(rule.getPermitted(), "|"));
                    csvRow.setOtherVehiclesPermitted(StringUtils.join(rule.getOtherVehiclesPermitted(), "|"));
                    csvRow.setDays(StringUtils.join(time.getDays(), "|"));
                    csvRow.setPrimaryRule(rule.getPrimary().toString());
                    csvRow.setStartTime(time.getTimeOfDayStart());
                    csvRow.setEndTime(time.getTimeOfDayEnd());
                    csvRow.setSegmentLength("" + segmentLength);
                    csvRow.setMaxDuration(rule.getMaxDurationH() == null ? "" : "" + rule.getMaxDurationH());
                    csvRow.setVehicleType(rule.getVehicleType().toString());
                    csvRow.setParkingType(parkingType);
                    newRowsCollection.add(csvRow);
                    stringBuilder.append(csvRow.printValues()).append("\n");
                }
                ruleNum++;
            }
            curbDataMap.put(curbId, newRowsCollection);
        }

        /*for (List<OutputFormat> list : newMap.values()) {
            for (OutputFormat item : list) {
                System.out.println("" + item.toString());
            }
        }*/
        CSVWriter csv = new CSVWriter(CSV_FILE);
        csv.getBufferedWriter().append(stringBuilder.toString());
        csv.getBufferedWriter().flush();
        csv.closeFile();
    }

    //using file for testing only
    private static StringBuilder getDataFromFile() {
        try (FileReader fileReader = new FileReader(new File(JSON_FILE)); BufferedReader rdr = new BufferedReader(fileReader)) {
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

        CSVWriter csv = new CSVWriter(JSON_FILE);
        csv.getBufferedWriter().append(result);
        csv.getBufferedWriter().flush();
        csv.closeFile();
        return result;
    }

    /**
     * Poopulates parkingType field in CSV
     * @param rule Given rule for curb
     * @return parking type e.g. "No Stopping"
     */
    private static String getParkingType(Rule rule) {
        String parkingType = "";
        switch (rule.getPrimary()) {
            case NONE:
                if (rule.getPermitted().isEmpty()) {
                    parkingType = OutputFormat.LengthHeading.NO_STOPPING.toString();
                } else {
                    parkingType = OutputFormat.LengthHeading.NO_PARKING.toString();
                }
                break;
            case PARK:
                Double pricePerHour = rule.getPrice().get(0).getPricePerHour().getAmount();
                if (pricePerHour == null || pricePerHour == 0) {
                    parkingType = OutputFormat.LengthHeading.FREE_PARKING.toString();
                } else if (pricePerHour > 0) {
                    parkingType = OutputFormat.LengthHeading.PAID_PARKING.toString();
                }
                break;
            case LOAD_GOODS:
                parkingType = OutputFormat.LengthHeading.LOADING_ZONE.toString();
                break;
            case LOAD_PASSENGERS:
                parkingType = OutputFormat.LengthHeading.PASSENGER_LOADING_ZONE.toString();
                break;
            default:
                parkingType = "";
        }
        return parkingType;
    }

}
package beam.utils.coordmodel;

import sun.security.util.Length;

import java.util.*;

/**
 * Includes the columns for CSV
 *
 * @author abid
 */
public class OutputFormat {
    private String curbId;
    private String startLongitude;
    private String startLatitude;
    private String endLongitude;
    private String feePerHour;
    private String endLatitude;
    private long totalCurbLength;
    private long totalNoParkingLength;
    private long totalNoStoppingLength;
    private long totalFreeParkingLength;
    private long totalPaidParkingLength;
    private long totalLoadingZoneLength;
    private long totalPassengerLoadingZoneLength;
    private double maxParkingDuration;

    private OutputFormat.LengthHeading lengthHeading;
    //    private Map<Integer, EnumMap<Permitted, String>> mapDayData;
    private Map<LengthHeading, Long> permittedTypeLengthMap;

    public String getCurbId() {
        return curbId;
    }

    public void setCurbId(String curbId) {
        this.curbId = curbId;
    }

    public OutputFormat() {
        permittedTypeLengthMap = new EnumMap<LengthHeading, Long>(LengthHeading.class);
    }

    public String getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(String startLongitude) {
        this.startLongitude = startLongitude;
    }

    public String getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(String startLatitude) {
        this.startLatitude = startLatitude;
    }

    public String getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(String endLongitude) {
        this.endLongitude = endLongitude;
    }

    public String getFeePerHour() {
        return feePerHour;
    }

    public void setFeePerHour(String feePerHour) {
        this.feePerHour = feePerHour;
    }

    public String getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(String endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getTotalCurbLength() {
        return totalCurbLength;
    }

    public void setTotalCurbLength(long totalCurbLength) {
        this.totalCurbLength = totalCurbLength;
    }

    public double getTotalNoParkingLength() {
        return totalNoParkingLength;
    }

    public void setTotalNoParkingLength(long totalNoParkingLength) {
        this.totalNoParkingLength = totalNoParkingLength;
    }

    public double getTotalNoStoppingLength() {
        return totalNoStoppingLength;
    }

    public void setTotalNoStoppingLength(long totalNoStoppingLength) {
        this.totalNoStoppingLength = totalNoStoppingLength;
    }

    public long getTotalFreeParkingLength() {
        return totalFreeParkingLength;
    }

    public void setTotalFreeParkingLength(long totalFreeParkingLength) {
        this.totalFreeParkingLength = totalFreeParkingLength;
    }

    public double getTotalPaidParkingLength() {
        return totalPaidParkingLength;
    }

    public void setTotalPaidParkingLength(long totalPaidParkingLength) {
        this.totalPaidParkingLength = totalPaidParkingLength;
    }

    public long getTotalLoadingZoneLength() {
        return totalLoadingZoneLength;
    }

    public void setTotalLoadingZoneLength(long totalLoadingZoneLength) {
        this.totalLoadingZoneLength = totalLoadingZoneLength;
    }

    public long getTotalPassengerLoadingZoneLength() {
        return totalPassengerLoadingZoneLength;
    }

    public void setTotalPassengerLoadingZoneLength(long totalPassengerLoadingZoneLength) {
        this.totalPassengerLoadingZoneLength = totalPassengerLoadingZoneLength;
    }

    public double getMaxParkingDuration() {
        return maxParkingDuration;
    }

    public void setMaxParkingDuration(double maxParkingDuration) {
        this.maxParkingDuration = maxParkingDuration;
    }

//    public EnumMap<Permitted, String> getDayData(Integer day) {
//        return mapDayData.get(day);
//    }

//    public void setDaydata(Integer dayNum, EnumMap<Permitted, String> val) {
//        this.mapDayData.put(dayNum, val);
//    }

    public Map<LengthHeading, Long> getPermittedTypeLengthMap() {
        return this.permittedTypeLengthMap;
    }


    public OutputFormat.LengthHeading getLengthHeading() {
        return this.lengthHeading;
    }

    public void setLengthHeading(OutputFormat.LengthHeading lengthHeading) {
        this.lengthHeading = lengthHeading;
    }

    @Override
    public String toString() {
        return "OutputFormat{" +
                "curbId=" + curbId +
                ", startLongitude=" + startLongitude +
                ", startLatitude=" + startLatitude +
                ", endLongitude=" + endLongitude +
                ", feePerHour=" + feePerHour +
                ", endLatitude=" + endLatitude +
                ", totalCurbLength=" + totalCurbLength +
                ", totalNoParkingLength=" + totalNoParkingLength +
                ", totalNoStoppingLength=" + totalNoStoppingLength +
                ", totalFreeParkingLength=" + totalFreeParkingLength +
                ", totalPaidParkingLength=" + totalPaidParkingLength +
                ", totalLoadingZoneLength=" + totalLoadingZoneLength +
                ", totalPassengerLoadingZoneLength=" + totalPassengerLoadingZoneLength +
                ", maxParkingDuration=" + maxParkingDuration +
                '}';
    }

    public String getCsvString() {
        return curbId +
                "," + startLongitude +
                "," + startLatitude +
                "," + endLongitude +
                "," + feePerHour +
                "," + endLatitude +
                "," + totalCurbLength +
                "," + totalNoParkingLength +
                "," + totalNoStoppingLength +
                "," + totalFreeParkingLength +
                "," + totalPaidParkingLength +
                "," + totalLoadingZoneLength +
                "," + totalPassengerLoadingZoneLength +
                "," + maxParkingDuration;
    }

    public enum LengthHeading {

        NO_STOPPING("No Stopping"),
        NO_PARKING("No Parking"),
        PASSENGER_LOADING_ZONE("Passenger Loading Zone"),
        LOADING_ZONE("Loading Zone"),
        PAID_PARKING("Paid Parking"),
        FREE_PARKING("Free Parking");
        private final String value;
        private static final Map<String, OutputFormat.LengthHeading> CONSTANTS = new HashMap<>();

        static {
            for (OutputFormat.LengthHeading c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LengthHeading(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static OutputFormat.LengthHeading fromValue(String value) {
            OutputFormat.LengthHeading constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }
}

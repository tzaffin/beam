package beam.utils.coordmodel;

import java.util.HashMap;
import java.util.Map;

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
    private String endLatitude;
    private String segmentId;
    private Integer ruleNumber;
    private String segmentLength;
    private String primaryRule;
    private String days;
    private String startTime;
    private String endTime;
    private String feePerHour;
    private String permittedVehicle;
    private String otherVehiclesPermitted;
    private String maxDuration;
    private String vehicleType;
    private String parkingType;

    public String getCurbId() {
        return curbId;
    }

    public void setCurbId(String curbId) {
        this.curbId = curbId;
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

    public String getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(String endLatitude) {
        this.endLatitude = endLatitude;
    }

    /**
     * This is the unique identifier for each segment inside a curb
     *
     * @return segmentId
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * Sets the unique identifier for each segment inside a curb. Better way is to concatenate the curbId with segment number
     */
    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    /**
     * Serial number for any rule inside a curb segment
     *
     * @return number of rule
     */
    public Integer getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(Integer ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    /**
     * Length of segment inside the curb.
     *
     * @return length of segment (meters)
     */
    public String getSegmentLength() {
        return segmentLength;
    }

    /**
     * length of segment inside the curb
     *
     * @param segmentLength length of segment (meters)
     */
    public void setSegmentLength(String segmentLength) {
        this.segmentLength = segmentLength;
    }

    public String getPrimaryRule() {
        return primaryRule;
    }

    public void setPrimaryRule(String primaryRule) {
        this.primaryRule = primaryRule;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFeePerHour() {
        return feePerHour;
    }

    public void setFeePerHour(String feePerHour) {
        this.feePerHour = feePerHour;
    }

    public String getPermittedVehicle() {
        return permittedVehicle;
    }

    public void setPermittedVehicle(String permittedVehicle) {
        this.permittedVehicle = permittedVehicle;
    }

    public String getOtherVehiclesPermitted() {
        return otherVehiclesPermitted;
    }

    public void setOtherVehiclesPermitted(String otherVehiclesPermitted) {
        this.otherVehiclesPermitted = otherVehiclesPermitted;
    }

    /**
     * Maximum duration of any rule
     *
     * @return duration (hours)
     */
    public String getMaxDuration() {
        return maxDuration;
    }

    /**
     * Set the maximum duration of any rule
     *
     * @param maxDuration duration (hours)
     */
    public void setMaxDuration(String maxDuration) {
        this.maxDuration = maxDuration;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getParkingType() {
        return parkingType;
    }

    public void setParkingType(String parkingType) {
        this.parkingType = parkingType;
    }

    @Override
    public String toString() {
        return "{" +
                "curbId='" + curbId + '\'' +
                ", startLongitude='" + startLongitude + '\'' +
                ", startLatitude='" + startLatitude + '\'' +
                ", endLongitude='" + endLongitude + '\'' +
                ", endLatitude='" + endLatitude + '\'' +
                ", segmentId='" + segmentId + '\'' +
                ", ruleNumber=" + ruleNumber +
                ", segmentLength='" + segmentLength + '\'' +
                ", primaryRule='" + primaryRule + '\'' +
                ", days='" + days + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", feePerHour='" + feePerHour + '\'' +
                ", permittedVehicle='" + permittedVehicle + '\'' +
                ", otherVehiclesPermitted='" + otherVehiclesPermitted + '\'' +
                ", maxDuration='" + maxDuration + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", parkingType='" + parkingType + '\'' +
                '}';
    }

    public String printValues() {
        return curbId +
                "," + startLongitude +
                "," + startLatitude +
                "," + endLongitude +
                "," + endLatitude +
                "," + segmentId +
                "," + ruleNumber +
                "," + segmentLength +
                "," + primaryRule +
                "," + days +
                "," + startTime +
                "," + endTime +
                "," + feePerHour +
                "," + permittedVehicle +
                "," + otherVehiclesPermitted +
                "," + maxDuration +
                "," + vehicleType +
                "," + parkingType;
    }

    public enum LengthHeading {

        NO_STOPPING("No Stopping"),
        NO_PARKING("No Parking"),
        PASSENGER_LOADING_ZONE("Passenger Loading Zone"),
        LOADING_ZONE("Loading Zone"),
        PAID_PARKING("Paid Parking"),
        FREE_PARKING("Free Parking");
        private final String value;
        private static final Map<String, LengthHeading> CONSTANTS = new HashMap<>();

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


package beam.utils.coordmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The rules that apply along a particular segment of a curb during certain time periods.
 *
 * @author abid
 */
public class Rule {

    /**
     * The longest a vehicle may remain at this curb while engaged in a permitted use, in
     * hours.
     * <p>
     * If a new rule starts applying before `max_duration_h` has elapsed, the new rule's
     * max_duration_h takes effect, but counting from when the vehicle first arrived.
     * For instance, If a curb had 2-hour parking until 5pm but 3-hour parking thereafter, and
     * a vehicle arrived at 4pm, they could continue parking until 7pm.
     */
    @SerializedName("max_duration_h")
    @Expose
    private Double maxDurationH;
    /**
     * The uses that are permitted for vehicles not of this segment's primary vehicle type.
     */
    @SerializedName("other_vehicles_permitted")
    @Expose
    private List<OtherVehiclesPermitted> otherVehiclesPermitted = null;
    /**
     * All the uses that are permitted, including the primary use.
     */
    @SerializedName("permitted")
    @Expose
    private List<Permitted> permitted = null;
    /**
     * The price a vehicle must pay while on this segment. In general, this price applies regardless
     * of use or vehicle type.
     * <p>
     * If a new rule starts applying, that rule's prices take effect, but counting from when the
     * vehicle first arrived. For instance, if a curb had:
     * * Parking at ${1} an hour until 8am;
     * * Parking at ${4} for the first hour and ${5} for the second hour thereafter,
     * A vehicle arriving at 7am would pay ${1} for the first hour and ${5} for the second.
     */
    @SerializedName("price")
    @Expose
    private List<Price> price = null;
    @SerializedName("primary")
    @Expose
    private Rule.Primary primary;
    /**
     * The days and times of day when this rule applies.
     */
    @SerializedName("times")
    @Expose
    private List<Time> times = null;
    @SerializedName("vehicle_type")
    @Expose
    private Rule.VehicleType vehicleType = Rule.VehicleType.fromValue("all");

    /**
     * The longest a vehicle may remain at this curb while engaged in a permitted use, in
     * hours.
     * <p>
     * If a new rule starts applying before `max_duration_h` has elapsed, the new rule's
     * max_duration_h takes effect, but counting from when the vehicle first arrived.
     * For instance, If a curb had 2-hour parking until 5pm but 3-hour parking thereafter, and
     * a vehicle arrived at 4pm, they could continue parking until 7pm.
     */
    public Double getMaxDurationH() {
        return maxDurationH;
    }

    /**
     * The longest a vehicle may remain at this curb while engaged in a permitted use, in
     * hours.
     * <p>
     * If a new rule starts applying before `max_duration_h` has elapsed, the new rule's
     * max_duration_h takes effect, but counting from when the vehicle first arrived.
     * For instance, If a curb had 2-hour parking until 5pm but 3-hour parking thereafter, and
     * a vehicle arrived at 4pm, they could continue parking until 7pm.
     */
    public void setMaxDurationH(Double maxDurationH) {
        this.maxDurationH = maxDurationH;
    }

    /**
     * The uses that are permitted for vehicles not of this segment's primary vehicle type.
     */
    public List<OtherVehiclesPermitted> getOtherVehiclesPermitted() {
        return otherVehiclesPermitted;
    }

    /**
     * The uses that are permitted for vehicles not of this segment's primary vehicle type.
     *
     *
     */
    public void setOtherVehiclesPermitted(List<OtherVehiclesPermitted> otherVehiclesPermitted) {
        this.otherVehiclesPermitted = otherVehiclesPermitted;
    }

    /**
     * All the uses that are permitted, including the primary use.
     *
     */
    public List<Permitted> getPermitted() {
        return permitted;
    }

    /**
     * All the uses that are permitted, including the primary use.
     *
     */
    public void setPermitted(List<Permitted> permitted) {
        this.permitted = permitted;
    }

    /**
     * The price a vehicle must pay while on this segment. In general, this price applies regardless
     * of use or vehicle type.
     *
     * If a new rule starts applying, that rule's prices take effect, but counting from when the
     * vehicle first arrived. For instance, if a curb had:
     *   * Parking at ${1} an hour until 8am;
     *   * Parking at ${4} for the first hour and ${5} for the second hour thereafter,
     * A vehicle arriving at 7am would pay ${1} for the first hour and ${5} for the second.
     *
     *
     */
    public List<Price> getPrice() {
        return price;
    }

    /**
     * The price a vehicle must pay while on this segment. In general, this price applies regardless
     * of use or vehicle type.
     *
     * If a new rule starts applying, that rule's prices take effect, but counting from when the
     * vehicle first arrived. For instance, if a curb had:
     *   * Parking at ${1} an hour until 8am;
     *   * Parking at ${4} for the first hour and ${5} for the second hour thereafter,
     * A vehicle arriving at 7am would pay ${1} for the first hour and ${5} for the second.
     *
     *
     */
    public void setPrice(List<Price> price) {
        this.price = price;
    }

    public Rule.Primary getPrimary() {
        return primary;
    }

    public void setPrimary(Rule.Primary primary) {
        this.primary = primary;
    }

    /**
     * The days and times of day when this rule applies.
     *
     */
    public List<Time> getTimes() {
        return times;
    }

    /**
     * The days and times of day when this rule applies.
     *
     */
    public void setTimes(List<Time> times) {
        this.times = times;
    }

    public Rule.VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(Rule.VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public enum Primary {

        @SerializedName("park")
        PARK("park"),
        @SerializedName("load_goods")
        LOAD_GOODS("load_goods"),
        @SerializedName("load_passengers")
        LOAD_PASSENGERS("load_passengers"),
        @SerializedName("none")
        NONE("none");
        private final String value;
        private static final Map<String, Rule.Primary> CONSTANTS = new HashMap<>();

        static {
            for (Rule.Primary c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Primary(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Rule.Primary fromValue(String value) {
            Rule.Primary constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum VehicleType {

        @SerializedName("all")
        ALL("all"),
        @SerializedName("taxi")
        TAXI("taxi"),
        @SerializedName("commercial")
        COMMERCIAL("commercial"),
        @SerializedName("truck")
        TRUCK("truck"),
        @SerializedName("motorcycle")
        MOTORCYCLE("motorcycle");
        private final String value;
        private static final Map<String, Rule.VehicleType> CONSTANTS = new HashMap<>();

        static {
            for (Rule.VehicleType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private VehicleType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Rule.VehicleType fromValue(String value) {
            Rule.VehicleType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}

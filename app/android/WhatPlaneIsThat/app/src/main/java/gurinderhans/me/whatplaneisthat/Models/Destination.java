package gurinderhans.me.whatplaneisthat.Models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ghans on 6/24/15.
 */
public class Destination {

    public final String toShort, fromShort;
    public final String toFullCity, fromFullCity;
    public final String toFullAirport, fromFullAirport;
    public final LatLng destTo, destFrom;
    private final long departureTime, arrivalTime;

    private Destination(Builder destBuilder) {
        this.toShort = destBuilder.toShort;
        this.fromShort = destBuilder.fromShort;


        // split to get city and airport name

        String[] toDestSplit = destBuilder.toFull.split(",");
        if (toDestSplit.length == 2) {
            this.toFullCity = toDestSplit[0].trim();
            this.toFullAirport = toDestSplit[1].trim();
        } else {
            this.toFullCity = "N/a";
            this.toFullAirport = "N/a";
        }

        String[] fromDestSplit = destBuilder.fromFull.split(",");
        if (fromDestSplit.length == 2) {
            this.fromFullCity = fromDestSplit[0].trim();
            this.fromFullAirport = fromDestSplit[1].trim();
        } else {
            this.fromFullCity = "N/a";
            this.fromFullAirport = "N/a";
        }

        this.departureTime = destBuilder.departureTime;
        this.arrivalTime = destBuilder.arrivalTime;

        this.destTo = destBuilder.destTo;
        this.destFrom = destBuilder.destFrom;
    }

    public String getArrivalTime() {
        return new SimpleDateFormat("h:mma").format(new Date(arrivalTime * 1000l)).toLowerCase();
    }

    public String getDepartureTime() {
        if (departureTime != 0l)
            return new SimpleDateFormat("h:mma").format(new Date(arrivalTime * 1000l)).toLowerCase();
        return "N/a";
    }

    public static class Builder {
        private String fromShort = "";
        private String toShort = "";

        private String fromFull = "";
        private String toFull = "";

        private long departureTime;
        private long arrivalTime;

        private LatLng destTo;
        private LatLng destFrom;


        public Builder toShortName(String name) {
            this.toShort = name;
            return this;
        }

        public Builder fromShortName(String name) {
            this.fromShort = name;
            return this;
        }

        public Builder toFullName(String name) {
            this.toFull = name;
            return this;
        }

        public Builder fromFullName(String name) {
            this.fromFull = name;
            return this;
        }

        public Builder departureTime(long time) {
            this.departureTime = time;
            return this;
        }

        public Builder arrivalTime(long time) {
            this.arrivalTime = time;
            return this;
        }

        public Builder toCoords(LatLng pos) {
            this.destTo = pos;
            return this;
        }

        public Builder fromCoords(LatLng pos) {
            this.destFrom = pos;
            return this;
        }

        public Destination build() {
            return new Destination(this);
        }
    }
}

package gurinderhans.me.whatplaneisthat.Models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ghans on 6/24/15.
 */
public class Destination {

    public final String toShort, fromShort;
    public final String toFull, fromFull;
    public final LatLng destTo, destFrom;
    private final long departureTime, arrivalTime;

    private Destination(Builder destBuilder) {
        this.toShort = destBuilder.toShort;
        this.fromShort = destBuilder.fromShort;

        this.toFull = destBuilder.toFull;
        this.fromFull = destBuilder.fromFull;

        this.departureTime = destBuilder.departureTime;
        this.arrivalTime = destBuilder.arrivalTime;

        this.destTo = destBuilder.destTo;
        this.destFrom = destBuilder.destFrom;
    }

    public String getArrivalTime() {
        if (arrivalTime != 0l)
            return new SimpleDateFormat("h:mma").format(new Date(arrivalTime * 1000l)).toLowerCase();
        return "N/a";
    }

    public String getDepartureTime() {
        if (departureTime != 0l)
            return new SimpleDateFormat("h:mma").format(new Date(arrivalTime * 1000l)).toLowerCase();
        return "N/a";
    }

    public static class Builder {
        private String fromShort;
        private String toShort;

        private String fromFull;
        private String toFull;

        private long departureTime;
        private long arrivalTime;

        private LatLng destTo;
        private LatLng destFrom;


        public Builder toShortName(String name) {
            this.toShort = !name.isEmpty() ? name : "N/a";
            return this;
        }

        public Builder fromShortName(String name) {
            this.fromShort = !name.isEmpty() ? name : "N/a";
            return this;
        }

        public Builder toFullName(String name) {
            this.toFull = !name.isEmpty() ? name : "N/a";
            return this;
        }

        public Builder fromFullName(String name) {
            this.fromFull = !name.isEmpty() ? name : "N/a";
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

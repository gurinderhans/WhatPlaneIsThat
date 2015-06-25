package gurinderhans.me.whatplaneisthat.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ghans on 6/24/15.
 */
public class Destination {

    public final String toShort, fromShort;
    final String toFull, fromFull;
    final LatLng destTo, destFrom;

    private Destination(Builder destBuilder) {
        this.toShort = destBuilder.toShort;
        this.fromShort = destBuilder.fromShort;

        this.toFull = destBuilder.toFull;
        this.fromFull = destBuilder.fromFull;

        this.destTo = destBuilder.destTo;
        this.destFrom = destBuilder.destFrom;
    }

    public static class Builder {
        private String fromShort = "";
        private String toShort = "";

        private String fromFull = "";
        private String toFull = "";

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

package gurinderhans.me.whatplaneisthat.Models;

import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    protected static final String TAG = Plane.class.getSimpleName();

    // plane's unique identifier (used for fetching plane data from web and searching for plane in mem)
    final String keyIdentifier;

    final String shortName;

    private LatLng planePos;


    private Plane(Builder planeBuilder) {
        // copy plane values to plane
        this.keyIdentifier = planeBuilder.key;
        this.shortName = planeBuilder.shortName;
        this.planePos = planeBuilder.planePos;
    }

    public static class Builder {
        private String key, shortName;
        private LatLng planePos;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder shortName(String name) {
            this.shortName = name;
            return this;
        }

        public Builder position(LatLng pos) {
            this.planePos = pos;
            return this;
        }

        public Plane build() {
            return new Plane(this);
        }

    }


    public LatLng getPlanePos() {
        return planePos;
    }

    public void setPlanePos(LatLng planePos) {
        this.planePos = planePos;
    }

}

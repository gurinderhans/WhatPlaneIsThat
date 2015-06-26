package gurinderhans.me.whatplaneisthat.Models;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    protected static final String TAG = Plane.class.getSimpleName();

    // plane's unique identifier (used for fetching plane data from web and searching for plane in mem)
    public final String keyIdentifier;

    public final String shortName;

    private String fullName;
    private String airlineName;

    private LatLng planePos;

    private float rotation, altitude, speed;

    private Destination destination;

    private Pair<Bitmap, Integer> planeImage;


    private Plane(Builder planeBuilder) {
        // copy plane values to plane
        this.keyIdentifier = planeBuilder.key;
        this.shortName = planeBuilder.shortName;
        this.planePos = planeBuilder.planePos;
        this.rotation = planeBuilder.rotation;
        this.destination = planeBuilder.dest;
    }


    //
    // setters & getters
    //

    public LatLng getPlanePos() {
        return planePos;
    }

    public void setPlanePos(LatLng planePos) {
        this.planePos = planePos;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getFullName() {
        return !fullName.isEmpty() ? fullName : !shortName.isEmpty() ? shortName : "No CallSign";
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAirlineName() {
        return !airlineName.isEmpty() ? airlineName : "Unknown Airlines";
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public Pair<Bitmap, Integer> getPlaneImage() {
        return planeImage;
    }

    public void setPlaneImage(Pair<Bitmap, Integer> planeImage) {
        this.planeImage = planeImage;
    }

    public static class Builder {
        private String key = "", shortName = "";
        private LatLng planePos;
        private float rotation;
        private Destination dest;

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

        public Builder rotation(float val) {
            this.rotation = val;
            return this;
        }

        public Builder shortDestinationNames(Destination destination) {
            this.dest = destination;
            return this;
        }

        public Plane build() {
            return new Plane(this);
        }

    }
}

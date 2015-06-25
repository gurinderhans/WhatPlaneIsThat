package gurinderhans.me.whatplaneisthat.Models;

import android.graphics.drawable.Drawable;
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
    public final String keyIdentifier;

    public final String shortName;

    private String fullName;
    private String airlineName;

    private LatLng planePos;

    private float rotation, altitude, speed;

    private Destination destination;

    private boolean isCached;

    private String smallImageUrl, largeImageUrl;

    private Pair<Drawable, Integer> planeImage;


    private Plane(Builder planeBuilder) {
        // copy plane values to plane
        this.keyIdentifier = planeBuilder.key;
        this.shortName = planeBuilder.shortName;
        this.planePos = planeBuilder.planePos;
        this.rotation = planeBuilder.rotation;
        this.destination = planeBuilder.dest;
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

    public boolean isCached() {
        return isCached;
    }

    public void setIsCached(boolean isCached) {
        this.isCached = isCached;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String url) {
        this.smallImageUrl = url;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String url) {
        this.largeImageUrl = url;
    }

    public Pair<Drawable, Integer> getPlaneImage() {
        return planeImage;
    }

    public void setPlaneImage(Pair<Drawable, Integer> planeImage) {
        this.planeImage = planeImage;
    }
}

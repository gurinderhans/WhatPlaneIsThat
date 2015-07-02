package gurinderhans.me.whatplaneisthat.Models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    protected static final String TAG = Plane.class.getSimpleName();

    // plane's unique identifier (used for fetching plane data from web and searching for plane in mem)
    public final String keyIdentifier;

    public final String shortName;

    @Nullable
    private String fullName;

    @Nullable
    private String airlineName;

    @Nullable
    private LatLng planePos;

    private float rotation;

    private ArrayList<Float> altitude = new ArrayList<>();
    private ArrayList<Float> speed = new ArrayList<>();


    @Nullable
    private Destination destination;

    @Nullable
    private Pair<Drawable, Integer> planeImage;


    private Plane(Builder planeBuilder) {
        // copy plane values to plane
        this.keyIdentifier = planeBuilder.key;
        this.shortName = planeBuilder.shortName;
        this.planePos = planeBuilder.planePos;
        this.rotation = planeBuilder.rotation;
        this.destination = planeBuilder.dest;
    }

    @Nullable
    public LatLng getPlanePos() {
        return planePos;
    }

    //
    // setters & getters
    //

    public void setPlanePos(@Nullable LatLng planePos) {
        this.planePos = planePos;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Nullable
    public Destination getDestination() {
        return destination;
    }

    public void setDestination(@Nullable Destination destination) {
        this.destination = destination;
    }

    public String getFullName() {
        return fullName != null && !fullName.isEmpty() ? fullName : (!shortName.isEmpty() ? shortName : "No CallSign");
    }

    public void setFullName(@Nullable String fullName) {
        this.fullName = fullName;
    }

    public String getAirlineName() {
        return airlineName != null && !airlineName.isEmpty() ? airlineName : "Unknown Airlines";
    }

    public void setAirlineName(@Nullable String airlineName) {
        this.airlineName = airlineName;
    }

    @Nullable
    public Pair<Drawable, Integer> getPlaneImage() {
        return planeImage;
    }

    public void setPlaneImage(@Nullable Pair<Drawable, Integer> planeImage) {
        this.planeImage = planeImage;
    }

    public float getSpeed() {
        // TODO: index out of bounds exception ?
        return speed.get(speed.size() - 1);
    }

    public void setSpeed(float speed) {
        this.speed.add(speed);
    }

    public ArrayList<Float> getSpeedDataSet() {
        return speed;
    }

    public float getAltitude() {
        // TODO: index out of bounds exception ?
        return altitude.get(altitude.size() - 1);
    }

    public void setAltitude(float altitude) {
        this.altitude.add(altitude);
    }

    public ArrayList<Float> getAltitudeDataSet() {
        return altitude;
    }

    public static class Builder {
        private String key = "";
        private String shortName = "";
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

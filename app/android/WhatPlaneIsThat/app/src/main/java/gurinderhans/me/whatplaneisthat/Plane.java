package gurinderhans.me.whatplaneisthat;

import android.graphics.drawable.Drawable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {


    final String keyIdentifier; // plane's unique identifier (used for fetching plane data and searching in lists)
    final String shortName;
    final String fullName;

    final String airlineName; // plane airlines name

    // destination FROM
    final Pair<String, LatLng> destinationFromFull;
    final String destinationFromShort;
    private final Long departureTime;

    // destination TO
    final Pair<String, LatLng> destinationToFull;
    final String destinationToShort;
    private final Long arrivalTime;

    // Pair.first = image drawable, Pair.second = image average color
    private Pair<Drawable, Integer> planeImage;

    // true when the plane object is cached in mem.
    final boolean isCached;

    // plane's position and speed
    private LatLng planePos; // plane position currently
    private float
            rotation = 0f,
            altitude = 0f,
            speed = 0f;

    private JsonArray planeTrail; // plane path trail

    public Plane(String key, String shortName, float rotation, float altitude, float speed, LatLng planePos, String destFromShort, String destToShort) {
        // copy plane values to plane
        this.keyIdentifier = key;
        this.shortName = shortName;

        this.rotation = rotation;
        this.altitude = altitude;
        this.speed = speed;
        this.planePos = planePos;

        this.destinationFromShort = destFromShort;
        this.destinationToShort = destToShort;

        // unknown values as of now
        this.fullName = Constants.UNKNOWN_VALUE;
        this.airlineName = Constants.UNKNOWN_VALUE;
        this.destinationFromFull = null;
        this.departureTime = Long.MIN_VALUE;
        this.destinationToFull = null;
        this.arrivalTime = Long.MIN_VALUE;
        this.planeImage = null;
        this.isCached = false;

    }

    public Plane(Plane oldPlane, String fullName, String airlineName,
                 Pair<String, LatLng> destFromFull, String destFromShort, long departureTime,
                 Pair<String, LatLng> destToFull, String destToShort, long arrivalTime) {
        // copy plane values to plane
        this.keyIdentifier = oldPlane.keyIdentifier;
        this.shortName = oldPlane.shortName;

        this.rotation = oldPlane.rotation;
        this.altitude = oldPlane.altitude;
        this.speed = oldPlane.speed;
        this.planePos = oldPlane.planePos;

        this.destinationFromShort = destFromShort != null ? destFromShort : oldPlane.destinationFromShort;
        this.destinationToShort = destToShort != null ? destToShort : oldPlane.destinationToShort;

        this.fullName = fullName;
        this.airlineName = airlineName;
        this.destinationFromFull = destFromFull;
        this.departureTime = departureTime;
        this.destinationToFull = destToFull;
        this.arrivalTime = arrivalTime;
        this.isCached = true;
    }

    // class getters & setters

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public void setPosition(LatLng planePos) {
        this.planePos = planePos;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public LatLng getPlanePos() {
        return planePos;
    }

    public String getArrivalTime() {
        if (arrivalTime == Long.MIN_VALUE) return Constants.UNKNOWN_VALUE;
        Date date = new Date(arrivalTime * 1000l);
        return new SimpleDateFormat("h:mma").format(date).toLowerCase();
    }

    public void setPlaneImage(Pair<Drawable, Integer> planeImage) {
        this.planeImage = planeImage;
    }

    public Pair<Drawable, Integer> getPlaneImage() {
        return planeImage;
    }
}

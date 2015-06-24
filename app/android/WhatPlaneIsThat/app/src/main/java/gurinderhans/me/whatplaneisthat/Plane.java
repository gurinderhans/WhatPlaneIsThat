package gurinderhans.me.whatplaneisthat;

import android.graphics.drawable.Drawable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {
    protected static final String TAG = Plane.class.getSimpleName();

    // plane's unique identifier (used for fetching plane data from web and searching for plane in mem)
    final String keyIdentifier;

    final String shortName;
    final String fullName;
    final String airlineName;
    final String imageUrlSmall;
    final String imageUrlLarge;

    // destination FROM
    final Pair<String, LatLng> destinationFromFull;
    final String destinationFromShort;
    private final Long departureTime; // unix timestamp
    // destination TO
    final Pair<String, LatLng> destinationToFull;
    final String destinationToShort;
    private final Long arrivalTime;

    // true when the plane object has been cached in mem.
    private boolean isCached;

    // Pair.first = image drawable, Pair.second = image average color
    private Pair<Drawable, Integer> planeImage;

    // plane's position and speed
    private LatLng planePos; // plane position currently
    private float
            rotation = 0f,
            altitude = 0f,
            speed = 0f;

    private JsonArray planeTrail; // plane path trail

    private Plane(String key, String shortName, String destFromShort, String destToShort) {
        // copy plane values to plane
        this.keyIdentifier = key;
        this.shortName = shortName;
        this.destinationFromShort = destFromShort;
        this.destinationToShort = destToShort;

        // unknown values as of now
        this.fullName = null;
        this.airlineName = null;
        this.destinationFromFull = null;
        this.departureTime = null;
        this.destinationToFull = null;
        this.arrivalTime = null;
        this.planeImage = null;
        this.imageUrlSmall = null;
        this.imageUrlLarge = null;
    }

    private Plane(Plane oldPlane, String fullName, String airlineName,
                  Pair<String, LatLng> destFromFull, String destFromShort, long departureTime,
                  Pair<String, LatLng> destToFull, String destToShort, long arrivalTime, Pair<String, String> imageUrl) {
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

        this.imageUrlSmall = imageUrl.first;
        this.imageUrlLarge = imageUrl.second;

        isCached = true;
    }


    //
    // MARK: class getters & setters
    //

    public void setRotation(Float rotation) {
        this.rotation = rotation;
    }

    public void setAltitude(Float altitude) {
        this.altitude = altitude;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public void setPosition(LatLng planePos) {
        this.planePos = planePos;
    }

    public void setPlaneImage(Pair<Drawable, Integer> planeImage) {
        this.planeImage = planeImage;
    }

    public void setPlaneTrail(JsonArray planeTrail) {
        this.planeTrail = planeTrail;
    }

    synchronized public void setIsCached(boolean isCached) {
        this.isCached = isCached;
    }

    public float getRotation() {
        return rotation;
    }

    public LatLng getPlanePos() {
        return planePos;
    }

    public String getArrivalTime() {
        if (arrivalTime != null)
            return new SimpleDateFormat("h:mma").format(new Date(arrivalTime * 1000l)).toLowerCase();
        return null;
    }

    public Pair<Drawable, Integer> getPlaneImage() {
        return planeImage;
    }

    public JsonArray getPlaneTrail() {
        return planeTrail;
    }

    synchronized public boolean isCached() {
        return isCached;
    }

    //
    // MARK: plane creators
    //

    public static Plane makePlane(Map.Entry<String, JsonElement> entry) {
        JsonArray dataArr = entry.getValue().getAsJsonArray();
        JsonElement
                elCallSign = dataArr.get(16),
                elDestFromShort = dataArr.get(11),
                elDestToShort = dataArr.get(12),
                elPlaneRotation = dataArr.get(3),
                elPlaneAltitude = dataArr.get(4),
                elPlaneSpeed = dataArr.get(5),
                elPlanePosLat = dataArr.get(1),
                elPlanePosLng = dataArr.get(2);

        Plane plane = new Plane(
                entry.getKey(),
                elCallSign != null && !elCallSign.getAsString().isEmpty() ? elCallSign.getAsString() : null,
                elDestFromShort != null && !elDestFromShort.getAsString().isEmpty() ? elDestFromShort.getAsString() : null,
                elDestToShort != null && !elDestToShort.getAsString().isEmpty() ? elDestToShort.getAsString() : null);

        plane.setRotation(elPlaneRotation != null ? elPlaneRotation.getAsFloat() : null);
        plane.setAltitude(elPlaneAltitude != null ? elPlaneAltitude.getAsFloat() : null);
        plane.setSpeed(elPlaneSpeed != null ? elPlaneSpeed.getAsFloat() : null);
        plane.setPosition(new LatLng(elPlanePosLat.getAsDouble(), elPlanePosLng.getAsDouble()));

        return plane;
    }

    public static Plane updatePlane(Plane oldPlane, JsonObject oData) {

        String
                planeName = Tools.jsonElToString(oData.get(Constants.KEY_AIRCRAFT_NAME)),
                airlineName = Tools.jsonElToString(oData.get(Constants.KEY_AIRLINE_NAME)),
                planeFromFull = Tools.jsonElToString(oData.get(Constants.KEY_PLANE_FROM)),
                planeFromShort = Tools.jsonElToString(oData.get(Constants.KEY_PLANE_SHORT_FROM)),
                planeToFull = Tools.jsonElToString(oData.get(Constants.KEY_PLANE_TO)),
                planeToShort = Tools.jsonElToString(oData.get(Constants.KEY_PLANE_SHORT_TO)),
                planeImageUrl = Tools.jsonElToString(oData.get(Constants.KEY_PLANE_IMAGE_URL)),
                planeImageLargeUrl = Tools.jsonElToString(oData.get(Constants.KEY_PLANE_IMAGE_LARGE_URL));

        // plane from -> to place coordinates
        JsonElement
                elPlaneFromPos = oData.get(Constants.KEY_PLANE_POS_FROM),
                elPlaneToPos = oData.get(Constants.KEY_PLANE_POS_TO);
        JsonArray
                posFromCoords = (elPlaneFromPos != null) ? elPlaneFromPos.getAsJsonArray() : new JsonArray(),
                posToCoords = (elPlaneToPos != null) ? elPlaneToPos.getAsJsonArray() : new JsonArray();
        LatLng posTo = (posFromCoords.size() == 2)
                ? new LatLng(posFromCoords.get(0).getAsDouble(), posFromCoords.get(1).getAsDouble())
                : new LatLng(oldPlane.getPlanePos().latitude, oldPlane.getPlanePos().longitude);
        LatLng posFrom = (posToCoords.size() == 2)
                ? new LatLng(posToCoords.get(0).getAsDouble(), posToCoords.get(1).getAsDouble())
                : new LatLng(oldPlane.getPlanePos().latitude, oldPlane.getPlanePos().longitude);

        Long planeArrival = Tools.jsonElToLong(oData.get(Constants.KEY_PLANE_ARRIVAL_TIME));
        Long planeDeparture = Tools.jsonElToLong(oData.get(Constants.KEY_PLANE_DEPARTURE_TIME));

        return new Plane(oldPlane, planeName, airlineName,
                Pair.create(planeFromFull, posFrom), planeFromShort, planeDeparture,
                Pair.create(planeToFull, posTo), planeToShort, planeArrival, Pair.create(planeImageUrl, planeImageLargeUrl));

    }


    //
    // MARK: Custom methods for searching through array lists
    //

    /**
     * Checks if given plane name is in mPlaneMarkers
     *
     * @param markersList
     * @param name        - plane name
     * @return - index of the plane in the list, -1 if not found
     */
    public static int getPlaneMarkerIndex(List<Pair<Plane, Marker>> markersList, String name) {
        for (int i = 0; i < markersList.size(); i++) {
            if (markersList.get(i).first.keyIdentifier.equals(name))
                return i;
        }
        return -1;
    }

    /**
     * Checks if given plane name is in mPlaneMarkers
     *
     * @param markersList
     * @param planeMarker - plane marker
     * @return - index of the plane in the list, -1 if not found
     */
    public static int getPlaneMarkerIndex(List<Pair<Plane, Marker>> markersList, Marker planeMarker) {
        for (int i = 0; i < markersList.size(); i++) {
            if (markersList.get(i).second.equals(planeMarker))
                return i;
        }
        return -1;
    }
}

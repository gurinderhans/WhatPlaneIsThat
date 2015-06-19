package gurinderhans.me.whatplaneisthat;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ghans on 6/17/15.
 */

public class GeoLocation {

    private GeoLocation() {
    }

    /**
     * Calculates LatLng of some point at a distance from given latitude, longitude at an angle
     *
     * @param location - given location
     * @param bearing  - give bearing / angle (in degrees)
     * @param distance - distance in Km
     * @return - new LatLng that is distance away from current point at some angle
     */
    public static LatLng boundingBox(LatLng location, double bearing, double distance) {

        float radius = 6378.1f;
        double latitude = location.latitude;
        double longitude = location.longitude;

        // new latitude
        double nLat = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(latitude)) * Math.cos(distance / radius) + Math.cos(Math.toRadians(latitude)) * Math.sin(distance / radius) * Math.cos(Math.toRadians(bearing))));
        double nLng = Math.toDegrees(Math.toRadians(longitude) + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(distance / radius) * Math.cos(Math.toRadians(latitude)), Math.cos(distance / radius) - Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(nLat))));

        return new LatLng(nLat, nLng);
    }

}
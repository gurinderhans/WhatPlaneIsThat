package gurinderhans.me.whatplaneisthat;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    String name, landingAt, name2;
    double latitude, longitude;

    public Plane(String name, String landingAt, String name2, double lat, double lng) {
        this.name = name;
        this.landingAt = landingAt;
        this.name2 = name2;

        this.latitude = lat;
        this.longitude = lng;
    }
}

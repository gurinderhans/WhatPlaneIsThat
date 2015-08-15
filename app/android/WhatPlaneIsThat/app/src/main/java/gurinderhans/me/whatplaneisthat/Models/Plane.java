package gurinderhans.me.whatplaneisthat.Models;

import android.graphics.drawable.Drawable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

	protected static final String TAG = Plane.class.getSimpleName();

	// plane's unique identifier (used for fetching plane data from web and searching for plane in mem.)
	public final String keyIdentifier;

	private String shortName;
	private String fullName;
	private String airlineName;

	private LatLng planePos;

	private double rotation;

	private ArrayList<Double> altitude = new ArrayList<>();
	private ArrayList<Double> speed = new ArrayList<>();

	private Destination destination;

	private Pair<Drawable, Integer> planeImage;


	public Plane(String keyIdentifier) {
		this.keyIdentifier = keyIdentifier;
	}

	//
	// setters & getters
	//

	public LatLng getPosition() {
		return planePos;
	}

	public void setPosition(LatLng planePos) {
		this.planePos = planePos;
	}

	public void setPlanePos(LatLng planePos) {
		this.planePos = planePos;
	}

	public String getShortName() {
		return shortName != null && !shortName.isEmpty() ? shortName : "No Callsign";
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public String getFullName() {
		return fullName != null && !fullName.isEmpty() ? fullName : getShortName();
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAirlineName() {
		return airlineName != null && !airlineName.isEmpty() ? airlineName : "Unknown Airlines";
	}

	public void setAirlineName(String airlineName) {
		this.airlineName = airlineName;
	}

	public Pair<Drawable, Integer> getPlaneImage() {
		return planeImage;
	}

	public void setPlaneImage(Pair<Drawable, Integer> planeImage) {
		this.planeImage = planeImage;
	}

	public double getSpeed() {
		// TODO: index out of bounds exception ?
		return speed.get(speed.size() - 1);
	}

	public void setSpeed(double speed) {
		this.speed.add(speed);
	}

	public ArrayList<Double> getSpeedDataSet() {
		return speed;
	}

	public double getAltitude() {
		// TODO: index out of bounds exception ?
		return altitude.get(altitude.size() - 1);
	}

	public void setAltitude(double altitude) {
		this.altitude.add(altitude);
	}

	public ArrayList<Double> getAltitudeDataSet() {
		return altitude;
	}
}

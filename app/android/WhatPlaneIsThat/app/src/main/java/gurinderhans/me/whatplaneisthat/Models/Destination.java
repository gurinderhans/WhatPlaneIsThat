package gurinderhans.me.whatplaneisthat.Models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ghans on 6/24/15.
 */
public class Destination {

	public final String toShort;
	public final String fromShort;
	public String toFullCity;
	public String fromFullCity;
	public String toFullAirport;
	public String fromFullAirport;

	public LatLng destTo;
	public LatLng destFrom;

	private long departureTime;
	private long arrivalTime;

	public Destination(String fromShort, String toShort) {
		this.fromShort = fromShort;
		this.toShort = toShort;
	}

	public String getToFullCity() {
		return toFullCity;
	}

	public void setToFullCity(String toFullCity) {
		this.toFullCity = toFullCity;
	}

	public String getFromFullCity() {
		return fromFullCity;
	}

	public void setFromFullCity(String fromFullCity) {
		this.fromFullCity = fromFullCity;
	}

	public String getToFullAirport() {
		return toFullAirport;
	}

	public void setToFullAirport(String toFullAirport) {
		this.toFullAirport = toFullAirport;
	}

	public String getFromFullAirport() {
		return fromFullAirport;
	}

	public void setFromFullAirport(String fromFullAirport) {
		this.fromFullAirport = fromFullAirport;
	}

	public LatLng getDestTo() {
		return destTo;
	}

	public void setDestTo(LatLng destTo) {
		this.destTo = destTo;
	}

	public LatLng getDestFrom() {
		return destFrom;
	}

	public void setDestFrom(LatLng destFrom) {
		this.destFrom = destFrom;
	}

	public String getArrivalTime() {
		if (arrivalTime != Long.MIN_VALUE)
			return new SimpleDateFormat("h:mma", Locale.getDefault()).format(new Date(arrivalTime * 1000l)).toLowerCase();

		return "N/a";
	}

	public String getDepartureTime() {
		if (departureTime != Long.MIN_VALUE)
			return new SimpleDateFormat("h:mma", Locale.getDefault()).format(new Date(arrivalTime * 1000l)).toLowerCase();

		return "N/a";
	}
}

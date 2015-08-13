package gurinderhans.me.whatplaneisthat.Controllers;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gurinderhans.me.whatplaneisthat.Constants;
import gurinderhans.me.whatplaneisthat.Models.Destination;
import gurinderhans.me.whatplaneisthat.Models.Plane;

import static gurinderhans.me.whatplaneisthat.Tools.getJsonDoubleFromArr;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonLong;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonString;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonStringFromArr;
import static gurinderhans.me.whatplaneisthat.Tools.getPlaneIndex;

/**
 * Created by ghans on 15-08-12.
 */
public class PlaneController {

	public static final String TAG = PlaneController.class.getSimpleName();

	private List<Plane> allPlanes = new ArrayList<>();

	public PlaneController() {
		/* @empty constructor */
	}

	public List<Plane> addPlanes(JSONObject data) {

		Iterator<String> iterator = data.keys();

		while (iterator.hasNext()) {
			String planeKey = iterator.next();

			try {

				int planeIndex = getPlaneIndex(allPlanes, planeKey);

				if (planeIndex == -1) {
					Plane newPlane = _createPlane(planeKey, data.getJSONArray(planeKey));
					allPlanes.add(newPlane);
					continue;
				}

				// else update
				allPlanes.set(planeIndex,
						_updatePlane(allPlanes.get(planeIndex), data.getJSONArray(planeKey)));


			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// return the updated list
		return allPlanes;
	}

	private Plane _createPlane(String key, JSONArray data) {

		Plane plane = new Plane(key);

		plane.setShortName(getJsonStringFromArr(data, 16));
		plane.setRotation(getJsonDoubleFromArr(data, 3));
		plane.setAltitude(getJsonDoubleFromArr(data, 4));
		plane.setSpeed(getJsonDoubleFromArr(data, 5));

		try {
			plane.setPosition(new LatLng(data.getDouble(1), data.getDouble(2)));
		} catch (Exception e) {
			// unable to set plane position, oh well!
			e.printStackTrace();
		}

		plane.setDestination(
				new Destination(getJsonStringFromArr(data, 11), getJsonStringFromArr(data, 12))
		);

		return plane;
	}

	private Plane _updatePlane(Plane plane, JSONArray newData) {

		plane.setRotation(getJsonDoubleFromArr(newData, 3));
		plane.setAltitude(getJsonDoubleFromArr(newData, 4));
		plane.setSpeed(getJsonDoubleFromArr(newData, 5));

		// set position
		try {
			plane.setPosition(new LatLng(newData.getDouble(1), newData.getDouble(2)));
		} catch (Exception e) {
			// unable to set plane position, oh well!
			e.printStackTrace();
		}

		// return new plane
		return plane;
	}

	public List<Plane> getAllPlanes() {
		return allPlanes;
	}

	public void setMorePlaneInfo(int planeIndex, JSONObject data) {
		Plane plane = allPlanes.get(planeIndex);

		plane.setFullName(getJsonString(data, Constants.KEY_AIRCRAFT_NAME));
		plane.setAirlineName(getJsonString(data, Constants.KEY_AIRLINE_NAME));

		Destination planeDestination = plane.getDestination();
		if (planeDestination != null) {

			planeDestination.setFromShort(getJsonString(data, Constants.KEY_PLANE_FROM_SHORT));
			planeDestination.setToShort(getJsonString(data, Constants.KEY_PLANE_TO_SHORT));

			planeDestination.setToFullCity(getJsonString(data, Constants.KEY_PLANE_TO_CITY));
			planeDestination.setFromFullCity(getJsonString(data, Constants.KEY_PLANE_FROM_CITY));

			planeDestination.setDepartureTime(getJsonLong(data, Constants.KEY_PLANE_DEPARTURE_TIME));
			planeDestination.setArrivalTime(getJsonLong(data, Constants.KEY_PLANE_ARRIVAL_TIME));

			try {
				JSONArray coordsArr = data.getJSONArray(Constants.KEY_PLANE_POS_FROM);
				if (coordsArr.length() == 2)
					planeDestination.setDestFrom(new LatLng(coordsArr.getDouble(0), coordsArr.getDouble(1)));
			} catch (Exception e) {
				// unable to set dest from coordinates, oh well!
			}

			try {
				JSONArray coordsArr = data.getJSONArray(Constants.KEY_PLANE_POS_TO);
				if (coordsArr.length() == 2)
					planeDestination.setDestTo(new LatLng(coordsArr.getDouble(0), coordsArr.getDouble(1)));
			} catch (Exception e) {
				// unable to set dest to coordinates, oh well!
			}

			plane.setDestination(planeDestination);
		}

		// update list with 'new' plane
		allPlanes.set(planeIndex, plane);
	}
}
package gurinderhans.me.whatplaneisthat.Controllers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Pair;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gurinderhans.me.whatplaneisthat.Constants;
import gurinderhans.me.whatplaneisthat.MainActivity;
import gurinderhans.me.whatplaneisthat.Models.Destination;
import gurinderhans.me.whatplaneisthat.Models.Plane;
import gurinderhans.me.whatplaneisthat.PlaneApplication;
import gurinderhans.me.whatplaneisthat.R;
import gurinderhans.me.whatplaneisthat.Tools;

import static gurinderhans.me.whatplaneisthat.Constants.MIN_GRAPH_POINTS;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonDoubleFromArr;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonLong;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonString;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonStringFromArr;
import static gurinderhans.me.whatplaneisthat.Tools.getPlaneIndex;

/**
 * Created by ghans on 15-08-12.
 */
public class PlaneController {

	protected static final String TAG = PlaneController.class.getSimpleName();

	public final GoogleMap mMap;

	private int mSelectedPlaneIndex = -1;

	private Polyline mCurrentDrawnPolyline; // current drawn polyline

	private List<Plane> mPlanes = new ArrayList<>();
	private List<Marker> mPlaneMarkers = new ArrayList<>();


	public PlaneController(GoogleMap map) {
		this.mMap = map;
	}

	public void updatePlanes(JSONObject data) {

		Iterator<String> iterator = data.keys();

		while (iterator.hasNext()) {
			String planeKey = iterator.next();

			try {

				int planeIndex = getPlaneIndex(mPlanes, planeKey);

				if (planeIndex == Constants.INVALID_ARRAY_INDEX) {
					Plane newPlane = _createPlane(planeKey, data.getJSONArray(planeKey));
					mPlanes.add(newPlane);

					Marker planeMarker = mMap.addMarker(new MarkerOptions()
							.position(newPlane.getPosition())
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
							.rotation((float) newPlane.getRotation())
							.flat(true));
					mPlaneMarkers.add(planeMarker);
					continue;
				}

				// ELSE: update or remove
				LatLngBounds mapSearchBounds = new LatLngBounds(
						Tools.boundingBox(MainActivity.mUserLocation, 225, Constants.SEARCH_RADIUS),
						Tools.boundingBox(MainActivity.mUserLocation, 45, Constants.SEARCH_RADIUS));

				Plane updatedPlane = _updatePlane(mPlanes.get(planeIndex),
						data.getJSONArray(planeKey));

				if (mapSearchBounds.contains(updatedPlane.getPosition())) {
					mPlanes.set(planeIndex, updatedPlane);

					// update maker and replace the old one in list
					Marker oldMarker = mPlaneMarkers.get(planeIndex);
					oldMarker.setPosition(updatedPlane.getPosition());
					oldMarker.setRotation((float) updatedPlane.getRotation());
					mPlaneMarkers.set(planeIndex, oldMarker);
				} else {
					// remove marker from map
					mPlaneMarkers.get(planeIndex).remove();
				}

			} catch (JSONException e) {
				/* This occurs when we try to convert a JSON node to a JSONArray,
				which we use to check if the node contains plane data
				*/
			}
		}
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

	public void setMorePlaneInfo(JSONObject data, final ImageView planeImageView, final Handler mainHandler) {

		// few resets
		if (mCurrentDrawnPolyline != null)
			mCurrentDrawnPolyline.remove();

		for (Marker marker : mPlaneMarkers)
			marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon));
		planeImageView.setImageResource(R.drawable.transparent);


		// update old content
		final Plane plane = mPlanes.get(mSelectedPlaneIndex);

		plane.setFullName(getJsonString(data, Constants.KEY_AIRCRAFT_NAME));
		plane.setAirlineName(getJsonString(data, Constants.KEY_AIRLINE_NAME));

		Destination planeDestination = plane.getDestination();
		if (planeDestination != null) {

			planeDestination.setFromShort(getJsonString(data, Constants.KEY_PLANE_FROM_SHORT));
			planeDestination.setToShort(getJsonString(data, Constants.KEY_PLANE_TO_SHORT));


			String placeTo = getJsonString(data, Constants.KEY_PLANE_TO_CITY);
			String placeFrom = getJsonString(data, Constants.KEY_PLANE_FROM_CITY);

			planeDestination.setToFullCity(placeTo != null ? placeTo.split(",")[0] : Constants.UNKNOWN_VALUE);
			planeDestination.setFromFullCity(placeFrom != null ? placeFrom.split(",")[0] : Constants.UNKNOWN_VALUE);

			planeDestination.setToAirport(placeTo != null ? placeTo.split(",")[1] : Constants.UNKNOWN_VALUE);
			planeDestination.setFromAirport(placeFrom != null ? placeFrom.split(",")[1] : Constants.UNKNOWN_VALUE);

			planeDestination.setDepartureTime(getJsonLong(data, Constants.KEY_PLANE_DEPARTURE_TIME));
			planeDestination.setArrivalTime(getJsonLong(data, Constants.KEY_PLANE_ARRIVAL_TIME));

			try {
				JSONArray coordinatesArr = data.getJSONArray(Constants.KEY_PLANE_POS_FROM);
				if (coordinatesArr.length() == 2)
					planeDestination.setDestFrom(new LatLng(coordinatesArr.getDouble(0), coordinatesArr.getDouble(1)));
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
		mPlanes.set(mSelectedPlaneIndex, plane);

		// mark this plane marker as selected
		mPlaneMarkers.get(mSelectedPlaneIndex).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plane_icon_selected));

		// fetch the plane image

		String largeImageUrl = getJsonString(data, Constants.KEY_PLANE_IMAGE_LARGE_URL);
		String smallImageUrl = getJsonString(data, Constants.KEY_PLANE_IMAGE_URL);

		// fallback to small image if there is no big image
		String imgUrl = largeImageUrl != null && !largeImageUrl.isEmpty()
				? largeImageUrl
				: smallImageUrl != null && !smallImageUrl.isEmpty()
				? smallImageUrl : "";

		ImageLoader imgLoader = PlaneApplication.getInstance().getImageLoader();
		imgLoader.get(imgUrl, new ImageLoader.ImageListener() {
			@Override
			public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
				final Bitmap bmp = response.getBitmap();
				if (bmp != null) {

					final Drawable image = new BitmapDrawable(null, bmp);

					// TODO: fade in animation
					mPlanes.get(mSelectedPlaneIndex).setPlaneImage(Pair.create(image, Tools.getBitmapColor(bmp)));

					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							planeImageView.setImageDrawable(image);
						}
					});
				}
			}

			@Override
			public void onErrorResponse(VolleyError error) {
				planeImageView.setImageResource(R.drawable.transparent);
			}
		});

		// draw polyline
		try {
			PolylineOptions line = new PolylineOptions().width(15).color(0xFF00AEEF);
			JSONArray trailArr = data.getJSONArray(Constants.KEY_PLANE_MAP_TRAIL);

			for (int i = 0; i < trailArr.length(); i += 5)
				line.add(new LatLng(trailArr.getDouble(i), trailArr.getDouble(i + 1)));

			mCurrentDrawnPolyline = mMap.addPolyline(line);

		} catch (JSONException e) {
			// unable to parse polyline json data, oh well!
		}

	}

	public void setupChart(LineChart chart, LineData data, int color) {

		// no description text
		chart.setDescription("");
		chart.setNoDataTextDescription("You need to provide data for the chart.");

		// enable / disable grid background
		chart.setDrawGridBackground(false);

		// enable touch gestures
		chart.setTouchEnabled(true);

		// enable scaling and dragging
		chart.setDragEnabled(true);
		chart.setScaleEnabled(true);

		// if disabled, scaling can be done on x- and y-axis separately
		chart.setPinchZoom(false);

		chart.setBackgroundColor(color);

		chart.setViewPortOffsets(50, 20, 50, 0);

		// add data
		chart.setData(data);

		if (data.getDataSetByIndex(0).getValueCount() >= MIN_GRAPH_POINTS) {
			chart.setVisibleXRange(MIN_GRAPH_POINTS);
			chart.moveViewToX(data.getDataSetByIndex(0).getValueCount() - MIN_GRAPH_POINTS);
		}

		// get the legend (only possible after setting data)
		Legend l = chart.getLegend();
		l.setEnabled(false);

		chart.getAxisLeft().setEnabled(false);
		chart.getAxisRight().setEnabled(false);
		chart.getXAxis().setEnabled(false);

		chart.getAxisLeft().setStartAtZero(false);

		// animate calls invalidate()...
		chart.animateX(2500);
	}

	public LineData getPlaneAltitudeData() {

		if (mSelectedPlaneIndex == -1)
			return null;

		ArrayList<Double> altitudeSet = getPlanes().get(mSelectedPlaneIndex).getAltitudeDataSet();

		ArrayList<String> xVals = new ArrayList<>();

		for (int i = 0; i < altitudeSet.size(); i++)
			xVals.add(i + "");

		ArrayList<Entry> yVals = new ArrayList<>();

		for (int i = 0; i < altitudeSet.size(); i++) {
			float val = Float.parseFloat(altitudeSet.get(i).toString()); // FIXME: WTF??
			yVals.add(new Entry(val, i));
		}

		LineDataSet set = new LineDataSet(yVals, "Altitude");

		set.setLineWidth(1.75f);
		set.setCircleSize(3f);
		set.setColor(Color.WHITE);
		set.setCircleColor(Color.WHITE);
		set.setHighLightColor(Color.WHITE);
		set.setValueTextColor(Color.WHITE);
		set.setDrawValues(true);

		ArrayList<LineDataSet> dataSets = new ArrayList<>();
		dataSets.add(set); // add the datasets

		// create a data object with the datasets
		return new LineData(xVals, dataSets);
	}

	public LineData getPlaneSpeedData() {

		if (mSelectedPlaneIndex == -1)
			return null;

		ArrayList<Double> speedDataSet = getPlanes().get(mSelectedPlaneIndex).getSpeedDataSet();

		ArrayList<String> xVals = new ArrayList<>();

		for (int i = 0; i < speedDataSet.size(); i++)
			xVals.add(i + "");

		ArrayList<Entry> yVals = new ArrayList<>();

		for (int i = 0; i < speedDataSet.size(); i++) {
			float val = Float.parseFloat(speedDataSet.get(i).toString());
			yVals.add(new Entry(val, i));
		}

		LineDataSet set = new LineDataSet(yVals, "Speed");

		set.setLineWidth(1.75f);
		set.setCircleSize(3f);
		set.setColor(Color.WHITE);
		set.setCircleColor(Color.WHITE);
		set.setHighLightColor(Color.WHITE);
		set.setValueTextColor(Color.WHITE);
		set.setDrawValues(true);

		ArrayList<LineDataSet> dataSets = new ArrayList<>();
		dataSets.add(set); // add the datasets

		// create a data object with the datasets
		return new LineData(xVals, dataSets);

	}

	public List<Plane> getPlanes() {
		return mPlanes;
	}

	public List<Marker> getPlaneMarkers() {
		return mPlaneMarkers;
	}

	public Polyline getPlanePathPolyline() {
		return mCurrentDrawnPolyline;
	}

	public int getSelectedPlaneIndex() {
		return mSelectedPlaneIndex;
	}

	public void setSelectedPlaneIndex(int mCurrentFocusedPlaneMarkerIndex) {
		this.mSelectedPlaneIndex = mCurrentFocusedPlaneMarkerIndex;
	}
}
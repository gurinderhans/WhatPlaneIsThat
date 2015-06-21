package gurinderhans.me.whatplaneisthat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements LocationListener, SensorEventListener, OnMarkerClickListener {

    // TODO: Estimate plane location and make it move in "realtime"
    // TODO: use plane speed to make planes move in real-time and then adjust location on new HTTP req.
    // TODO: guess which planes "I" might be able to see
    // TODO: get current visibility and user location & rotation to create the user marker
    //       that simulates it virtually, like a torch effect that would show planes maybe
    //       in user's view


    protected static final String TAG = MainActivity.class.getSimpleName();

    Handler mHandler = new Handler();
    OkHttpWrapper mWrapper;
    List<Pair<Plane, Marker>> mPlaneMarkers;
    Pair<Plane, Marker> mCurrentFocusedPlane;
    boolean followUser = false;
    boolean cameraAnimationFinished = false;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    SensorManager mSensorManager;
    LocationManager mLocationManager;
    LatLng mUserLocation;
    Marker mUserMarker;
    GroundOverlay visibilityCircle;
    Polyline mCurrentDrawnPolyline; // only one polyline at a time

    ImageButton mLockCameraLocation;
    SlidingUpPanelLayout mSlidingUpPanelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlaneMarkers = new ArrayList<>();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mWrapper = new OkHttpWrapper(this);

        // get cached location
        Location cachedLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mUserLocation = new LatLng(cachedLocation.getLatitude(), cachedLocation.getLongitude());

        mLockCameraLocation = (ImageButton) findViewById(R.id.lockToLocation);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);


        mLockCameraLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUser = true;
                cameraAnimationFinished = false;
                mLockCameraLocation.setImageResource(R.drawable.ic_gps_fixed_blue_24dp);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, Constants.MAP_CAMERA_LOCK_MIN_ZOOM), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        cameraAnimationFinished = true;
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });


        setUpMapIfNeeded();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            // TODO: maybe wanna track distance and set to true if past a certain dist?
            followUser = false;
            mLockCameraLocation.setImageResource(R.drawable.ic_gps_fixed_black_24dp);
        }
        return super.dispatchTouchEvent(ev);
    }

    Runnable fetchData = new Runnable() {
        @Override
        public void run() {
            LatLng north_west = GeoLocation.boundingBox(mUserLocation, 315, 100);
            LatLng south_east = GeoLocation.boundingBox(mUserLocation, 135, 100);
            mWrapper.getJson(Constants.BASE_URL + String.format(Constants.OPTIONS_FORMAT,
                            // map bounds
                            north_west.latitude + "",
                            south_east.latitude + "",
                            north_west.longitude + "",
                            south_east.longitude) + "",
                    new OkHttpWrapper.HttpCallback() {
                        @Override
                        public void onFailure(Response response, Throwable throwable) {
                            //
                        }

                        @Override
                        public void onSuccess(Object data) {
                            JsonObject jsonData = (JsonObject) data;
                            for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {

                                if (entry.getValue() instanceof JsonArray) { // this data is about a plane and not other json info

                                    JsonArray dataArr = entry.getValue().getAsJsonArray();

                                    Plane plane = new Plane(dataArr.get(16).getAsString(),
                                            dataArr.get(12).getAsString(),
                                            dataArr.get(13).getAsString(),
                                            entry.getKey(),
                                            dataArr.get(3).getAsFloat(),
                                            dataArr.get(4).getAsFloat(),
                                            dataArr.get(5).getAsFloat(),
                                            dataArr.get(1).getAsDouble(),
                                            dataArr.get(2).getAsDouble());

//                                    String planeName = plane.name + Constants.PLANE_NAME_SPLITTER + plane.name2;

                                    LatLngBounds searchBounds = new LatLngBounds(GeoLocation.boundingBox(mUserLocation, 225, 100), GeoLocation.boundingBox(mUserLocation, 45, 100));

                                    Log.i(TAG, "bounds: " + searchBounds.toString());

                                    int markerIndex = getPlaneMarkerIndex(plane.idName);

                                    // add to list if not already
                                    if (markerIndex == -1) {
                                        Pair<Plane, Marker> planeMarker = Pair.create(plane, mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(plane.latitude, plane.longitude))
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
                                                .rotation(plane.rotation)
                                                .flat(true)));
                                        mPlaneMarkers.add(planeMarker);

                                    } else {

                                        if (!searchBounds.contains(new LatLng(plane.latitude, plane.longitude))) {
                                            // remove the marker
                                            mPlaneMarkers.get(markerIndex).second.remove();
                                            Log.i(TAG, "removed plane:" + mPlaneMarkers.get(markerIndex).first.idName);
                                            mPlaneMarkers.remove(markerIndex);
                                        } else {
                                            // update its location
                                            Marker marker = mPlaneMarkers.get(markerIndex).second;
                                            marker.setPosition(new LatLng(plane.latitude, plane.longitude));
                                            marker.setRotation(plane.rotation);

                                            // update polyline
                                            if (mCurrentDrawnPolyline != null && mCurrentFocusedPlane == mPlaneMarkers.get(markerIndex)) {
                                                List<LatLng> points = mCurrentDrawnPolyline.getPoints();
                                                points.add(0, new LatLng(plane.latitude, plane.longitude));
                                                mCurrentDrawnPolyline.setPoints(points);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFinished() {
                            // loop with delay
                            mHandler.postDelayed(fetchData, Constants.REFRESH_INTERVAL);
                        }
                    });
        }
    };


    /**
     * Checks if given plane name is in mPlaneMarkers
     *
     * @param name - plane name
     * @return - index of the plane in the list, -1 if not found
     */
    public int getPlaneMarkerIndex(String name) {

        for (int i = 0; i < mPlaneMarkers.size(); i++) {
            if (mPlaneMarkers.get(i).first.idName.equals(name))
                return i;
        }

        return -1;
    }

    /**
     * Checks if given plane name is in mPlaneMarkers
     *
     * @param planeMarker - plane marker
     * @return - index of the plane in the list, -1 if not found
     */
    public int getPlaneMarkerIndex(Marker planeMarker) {
        for (int i = 0; i < mPlaneMarkers.size(); i++) {
            if (mPlaneMarkers.get(i).second.equals(planeMarker))
                return i;
        }

        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // fech data
        mHandler.postDelayed(fetchData, 0);

        // register different types of listeners
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0f, this);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();

        // remove runnable
        mHandler.removeCallbacks(fetchData);

        // remove listeners
        mLocationManager.removeUpdates(this);
        mSensorManager.unregisterListener(this);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        // user marker
        mUserMarker = mMap.addMarker(new MarkerOptions().position(mUserLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(Tools.getSVGBitmap(this, R.drawable.user_marker, -1, -1)))
                        .rotation(0f)
                        .flat(true)
                        .anchor(0.5f, 0.5f)
        );
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 12f));

        // user visibility circle
        visibilityCircle = mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(Tools.getSVGBitmap(this, R.drawable.user_visibility, -1, -1)))
                .anchor(0.5f, 0.5f)
                .position(mUserLocation, 500000f));

        // hide the marker toolbar - the two buttons on the bottom right that go to google maps
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // marker click listener
        mMap.setOnMarkerClickListener(this);

    }


    //
    // MARK: location change methods
    //


    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "location now: " + location.toString());

        // update user location
        mUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mUserMarker.setPosition(mUserLocation);

        // plane visibiity circle - radius will depend on the actual visibilty retreived from some weather API ( TODO )
        visibilityCircle.setPosition(mUserLocation);
        visibilityCircle.setDimensions(5000f);

        Log.i(TAG, "follow user: " + followUser);

        // follow user maker
        if (followUser && cameraAnimationFinished) {
            cameraAnimationFinished = false;
            // FIXME: zoom value not corrected properly
            float zoom = (mMap.getCameraPosition().zoom < Constants.MAP_CAMERA_LOCK_MIN_ZOOM) ? Constants.MAP_CAMERA_LOCK_MIN_ZOOM : mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, zoom), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    cameraAnimationFinished = true;
                }

                @Override
                public void onCancel() {

                }
            });
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "status changed of: " + provider + " to: " + status + ", extras: " + extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "provider disabled: " + provider);
    }


    //
    // MARK: sensor events
    //


    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        mUserMarker.setRotation(degree);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //
    // MARK: marker click listener
    //

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i(TAG, "normal marker clicked");

        if (marker != mUserMarker) {
            int index = getPlaneMarkerIndex(marker);

            if (index != -1) {
                mCurrentFocusedPlane = mPlaneMarkers.get(index);

                mWrapper.getJson(String.format(Constants.PLANE_DATA_URL, mCurrentFocusedPlane.first.planekey), new OkHttpWrapper.HttpCallback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(Object jsonData) {

                        // TODO: make sure all views get filled and they aren't empty

                        JsonObject data = (JsonObject) jsonData;

                        JsonElement planeName = data.get(Constants.KEY_AIRCRAFT_NAME),
                                airlineName = data.get(Constants.KEY_AIRLINE_NAME),
                                planeFrom = data.get(Constants.KEY_PLANE_FROM),
                                planeTo = data.get(Constants.KEY_PLANE_TO);

                        // plane name and airline name
                        ((TextView) findViewById(R.id.planeName)).setText((planeName != null) ? planeName.getAsString() : (!mCurrentFocusedPlane.first.name.isEmpty()) ? mCurrentFocusedPlane.first.name : "No Callsign");
                        ((TextView) findViewById(R.id.airlineName)).setText((airlineName != null) ? airlineName.getAsString() : "Unknown Airlines");


                        // plane from -> to airports
                        ((TextView) findViewById(R.id.planeFrom)).setText((planeFrom != null) ? planeFrom.getAsString() : "N/a");
                        ((TextView) findViewById(R.id.planeTo)).setText((planeTo != null) ? planeTo.getAsString() : "N/a");
                        ((ImageView) findViewById(R.id.plane_from_to_img)).setImageResource(R.drawable.plane_from_to);


                        // remove polyline if exists
                        if (mCurrentDrawnPolyline != null) mCurrentDrawnPolyline.remove();

                        JsonArray trailArray = data.getAsJsonArray(Constants.KEY_PLANE_MAP_TRAIL);

                        PolylineOptions line = new PolylineOptions().width(10).color(R.color.visibility_circle_color);
                        line.add(mCurrentFocusedPlane.second.getPosition());

                        for (int i = 0; i < trailArray.size(); i += 5) { // ignore +3,4...
                            line.add(new LatLng(trailArray.get(i).getAsDouble(), trailArray.get(i + 1).getAsDouble()));
                        }

                        mCurrentDrawnPolyline = mMap.addPolyline(line);
                    }

                    @Override
                    public void onFinished() {

                    }
                });
            }
        }

        return true;
    }
}

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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gurinderhans.me.whatplaneisthat.Models.Plane;

public class MainActivity extends FragmentActivity implements LocationListener, SensorEventListener, OnMarkerClickListener, SlidingUpPanelLayout.PanelSlideListener {

    // TODO: Estimate plane location and make it move in "realtime"
    // TODO: use plane speed to make planes move in real-time and then adjust location on new HTTP req.
    // TODO: guess which planes "I" might be able to see
    // TODO: make sure all views get filled and they aren't empty
    // TODO: get current visibility and user location & rotation to create the user marker
    //       that simulates it virtually, like a torch effect that would show planes maybe
    //       in user's view


    protected static final String TAG = MainActivity.class.getSimpleName();

    // max dp that the image will go up
    private static final float MAX_TRANSLATE_DP = -269.08267f;

    Handler mHandler = new Handler();

    int mCurrentFocusedPlaneMarkerIndex = -1;
    List<Pair<Plane, Marker>> mPlaneMarkers = new ArrayList<>();


    boolean followUser = false;
    boolean cameraAnimationFinished = false;

    SensorManager mSensorManager;
    LocationManager mLocationManager;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    LatLng mUserLocation;
    Marker mUserMarker;
    GroundOverlay mPlaneVisibilityCircle;
    Polyline mCurrentDrawnPolyline; // only one polyline at a time

    // main activity views
    ImageButton mLockCameraLocation;
    SlidingUpPanelLayout mSlidingUpPanelLayout;
    ImageView mPlaneImage;

    // panel views
    View mCollapsedView;
    View mAnchoredView;
    View mExpandedView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mLockCameraLocation = (ImageButton) findViewById(R.id.lockToLocation);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mPlaneImage = (ImageView) findViewById(R.id.planeImage);

        // sliding panel views for different states
        mCollapsedView = findViewById(R.id.panelCollapsedView);
        mAnchoredView = findViewById(R.id.panelAnchoredView);

        // hide all other panel views so only collapsed shows initially
        mAnchoredView.setVisibility(View.INVISIBLE);

        // get cached location
        Location cachedLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mUserLocation = new LatLng(cachedLocation.getLatitude(), cachedLocation.getLongitude());

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

        // panel slide listener for layout stuff
        mSlidingUpPanelLayout.setPanelSlideListener(this);

        // set image initial position so it hides behind the panel
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (230 * scale + 0.5f);
        mPlaneImage.setTranslationY(pixels);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // fetch data
        mHandler.postDelayed(fetchData, 0);

        // register different types of listeners
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0f, this);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
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

            LatLng north_west = GeoLocation.boundingBox(mUserLocation, 315, Constants.SEARCH_RADIUS);
            LatLng south_east = GeoLocation.boundingBox(mUserLocation, 135, Constants.SEARCH_RADIUS);
            String reqUrl = Constants.BASE_URL + String.format(
                    Constants.OPTIONS_FORMAT,
                    north_west.latitude + "",
                    south_east.latitude + "",
                    north_west.longitude + "",
                    south_east.longitude + "");

            JsonObjectRequest request = new JsonObjectRequest(reqUrl, null, new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    Iterator<String> dataIterator = response.keys();

                    while (dataIterator.hasNext()) {
                        String key = dataIterator.next();

                        try { // it's plane data if we can convert to a JSONArray

                            JSONArray planeDataArr = response.getJSONArray(key);

                            Plane.Builder planeBuilder = new Plane.Builder().key(key);

                            if (!planeDataArr.isNull(16))
                                planeBuilder.shortName(planeDataArr.getString(16));

                            if (!planeDataArr.isNull(1) && !planeDataArr.isNull(2))
                                planeBuilder.position(new LatLng(planeDataArr.getDouble(1), planeDataArr.getDouble(2)));

                            Plane myPlane = planeBuilder.build();

                            mMap.addMarker(new MarkerOptions().position(myPlane.getPlanePos())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
                                    .rotation(0f)
                                    .flat(true));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

            PlaneApplication.getInstance().getRequestQueue().add(request);
        }
    };

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
        mPlaneVisibilityCircle = mMap.addGroundOverlay(new GroundOverlayOptions()
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
        mPlaneVisibilityCircle.setPosition(mUserLocation);
        mPlaneVisibilityCircle.setDimensions(5000f);

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

        /*if (marker != mUserMarker) {
            mCurrentFocusedPlaneMarkerIndex = Plane.getPlaneMarkerIndex(mPlaneMarkers, marker);

            if (mCurrentFocusedPlaneMarkerIndex != -1) {
                Plane selectedPlane = mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first;

                // FIXME: cache is broken
                Log.i(TAG, "cached: " + selectedPlane.isCached());
                if (selectedPlane.isCached()) {
                    switch (mSlidingUpPanelLayout.getPanelState()) {
                        case COLLAPSED:
                            setCollapsedPanelData();
                            break;
                        case ANCHORED:
                            setAnchoredPanelData();
                            break;
                        case EXPANDED:
                            setExpandedPanelData();
                            break;
                        default:
                            break;
                    }
                } else {
                    mOkHttpWrapper.getJson(String.format(Constants.PLANE_DATA_URL, selectedPlane.keyIdentifier), onFetchedPlaneInfo);
                }
            }
        }*/

        return true;
    }


    //
    // MARK: Panel slide listener
    //

    @Override
    public void onPanelSlide(View view, float v) {
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (230 * scale + 0.5f);

        float transitionPixel = (-(v * 100 * 16.65f) + pixels);
        float transitiondp = (transitionPixel - 0.5f) / scale;
        Log.i(TAG, "transitiondp: " + transitiondp);

        mPlaneImage.setTranslationY(((transitiondp <= MAX_TRANSLATE_DP) ? (MAX_TRANSLATE_DP * scale + 0.5f) : transitionPixel));
    }

    @Override
    public void onPanelCollapsed(View view) {
        // bring back the panel short view
        anchoredToCollapsed(100l);
//        setCollapsedPanelData();
    }

    @Override
    public void onPanelExpanded(View view) {
    }

    @Override
    public void onPanelAnchored(View view) {
        collapsedToAnchored(100l);
//        setAnchoredPanelData();

    }

    @Override
    public void onPanelHidden(View view) {
    }


    //
    // MARK: panel view data setters
    //

    /*public void setCollapsedPanelData() {

        if (mCurrentFocusedPlaneMarkerIndex == -1) return;

        Plane plane = mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first;

        // plane name
        ((TextView) findViewById(R.id.planeName)).setText(plane.shortName != null ? plane.shortName : "No Callsign");

        // plane from -> to airports
        ((TextView) findViewById(R.id.planeFrom)).setText(plane.destinationFromShort != null ? plane.destinationFromShort : "N/a");
        ((TextView) findViewById(R.id.planeTo)).setText(plane.destinationToShort != null ? plane.destinationToShort : "N/a");

        ((TextView) findViewById(R.id.arrivalTime)).setText(plane.getArrivalTime() != null ? plane.getArrivalTime() : "N/a");

        if (plane.getPlaneImage() != null)
            mPlaneImage.setImageDrawable(plane.getPlaneImage().first);
    }

    public void setAnchoredPanelData() {

        if (mCurrentFocusedPlaneMarkerIndex == -1) return;

        Plane plane = mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first;

        // plane name and airline name
        ((TextView) findViewById(R.id.anchoredPanelPlaneName)).setText(plane.fullName != null ? plane.fullName : plane.shortName != null ? plane.shortName : "No Plane name");
        ((TextView) findViewById(R.id.anchoredPanelAirlineName)).setText(plane.airlineName != null ? plane.airlineName : "Unknown Airlines");

        // plane from -> to airports
        ((TextView) findViewById(R.id.anchoredPanelDestFrom)).setText(plane.destinationFromShort != null ? plane.destinationFromShort : "N/a");
        ((TextView) findViewById(R.id.anchoredPanelDestTo)).setText(plane.destinationToShort != null ? plane.destinationToShort : "N/a");

    }*/

    public void setExpandedPanelData() {
    }


    //
    // MARK: panel view transitions
    //

    private void collapsedToAnchored(final long duration) {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(duration);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // show anchored view
                Animation animation1 = new AlphaAnimation(0, 1);
                mAnchoredView.setVisibility(View.VISIBLE);
                animation1.setInterpolator(new AccelerateInterpolator());
                animation1.setDuration(duration);
                mAnchoredView.startAnimation(animation1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCollapsedView.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mCollapsedView.startAnimation(animation);
    }

    private void anchoredToCollapsed(final long duration) {

        Animation animation = new AlphaAnimation(1, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(duration);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // show anchored view
                Animation animation1 = new AlphaAnimation(0, 1);
                mCollapsedView.setVisibility(View.VISIBLE);
                animation1.setInterpolator(new AccelerateInterpolator());
                animation1.setDuration(duration);
                mCollapsedView.startAnimation(animation1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnchoredView.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mAnchoredView.startAnimation(animation);
    }


}

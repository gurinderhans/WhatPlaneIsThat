package gurinderhans.me.whatplaneisthat;

import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gurinderhans.me.whatplaneisthat.Models.Destination;
import gurinderhans.me.whatplaneisthat.Models.Plane;

public class MainActivity extends FragmentActivity implements LocationListener, SensorEventListener, OnMarkerClickListener, SlidingUpPanelLayout.PanelSlideListener {

    // TODO: Estimate plane location and make it move in "realtime"
    // TODO: use plane speed to make planes move in real-time and then adjust location on new HTTP req.
    // TODO: guess which planes "I" might be able to see
    // TODO: make sure all views get filled and they aren't empty


    protected static final String TAG = MainActivity.class.getSimpleName();

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
    Polyline mCurrentDrawnPolyline; // current drawn polyline

    // main activity views
    ImageButton mLockCameraToUserLocation;
    SlidingUpPanelLayout mSlidingUpPanelLayout;
    ImageView mPlaneImage;

    // sliding panel views for different states
    View mCollapsedView;
    View mAnchoredView;
    View mExpandedView;

    int blueColor = Color.rgb(89, 199, 250);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mLockCameraToUserLocation = (ImageButton) findViewById(R.id.lockToLocation);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mPlaneImage = (ImageView) findViewById(R.id.planeImage);

        mCollapsedView = findViewById(R.id.panelCollapsedView);
        mAnchoredView = findViewById(R.id.panelAnchoredView);

        // hide all other panel views so only collapsed shows initially
        mAnchoredView.setVisibility(View.INVISIBLE);

        // get cached location
        Location cachedLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mUserLocation = new LatLng(cachedLocation.getLatitude(), cachedLocation.getLongitude());

        mLockCameraToUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUser = true;
                cameraAnimationFinished = false;
                mLockCameraToUserLocation.setImageResource(R.drawable.ic_gps_fixed_blue_24dp);
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

        mSlidingUpPanelLayout.setPanelSlideListener(this);

        // set image initial position so it hides behind the panel
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (230 * scale + 0.5f);
        mPlaneImage.setTranslationY(pixels);


        LineData data = getData(36, 100);

        chart = (LineChart) findViewById(R.id.chart1);
        setupChart(chart, data, blueColor);

    }

    LineChart chart;

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
    protected void onPause() {
        super.onPause();

        // remove runnable
        mHandler.removeCallbacks(fetchData);

        // remove listeners
        mLocationManager.removeUpdates(this);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            // TODO: maybe wanna track distance and set to true if past a certain dist?
            followUser = false;
            mLockCameraToUserLocation.setImageResource(R.drawable.ic_gps_fixed_black_24dp);
        }
        return super.dispatchTouchEvent(ev);
    }


    //
    // MARK: location manager methods
    //


    @Override
    public void onLocationChanged(Location location) {

        // update user location
        mUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mUserMarker.setPosition(mUserLocation);

        // plane visibiity circle - radius will depend on the actual visibilty retreived from some weather API ( TODO )
        mPlaneVisibilityCircle.setPosition(mUserLocation);
        mPlaneVisibilityCircle.setDimensions(5000f);

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

        Log.i(TAG, "id: " + marker.getId());

        mCurrentFocusedPlaneMarkerIndex = Tools.getPlaneMarkerIdIndex(mPlaneMarkers, marker.getId());

        if (mCurrentFocusedPlaneMarkerIndex != -1) {

            Plane oSelectedPlane = mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first;

            // call to network to fetch data
            String reqUrl = String.format(Constants.PLANE_DATA_URL,
                    oSelectedPlane.keyIdentifier);

            // TODO: add error listener
            JsonObjectRequest request = new JsonObjectRequest(reqUrl, null,
                    onFetchedPlaneInfo, null);
            PlaneApplication.getInstance().getRequestQueue().add(request);

        }

        return true;
    }


    //
    // MARK: Pane slide listener
    //


    @Override
    public void onPanelSlide(View view, float v) {
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (230 * scale + 0.5f);

        float transitionPixel = (-(v * 100 * 16.65f) + pixels);
        float transitiondp = (transitionPixel - 0.5f) / scale;

        mPlaneImage.setTranslationY(((transitiondp <= Constants.MAX_TRANSLATE_DP) ? (Constants.MAX_TRANSLATE_DP * scale + 0.5f) : transitionPixel));
    }

    @Override
    public void onPanelCollapsed(View view) {
        // bring back the panel short view
        openedToCollapsed(100l);
        setCollapsedPanelData();
    }

    @Override
    public void onPanelExpanded(View view) {
        collapsedToOpened(100l);
        setOpenedPanelData();
    }

    @Override
    public void onPanelAnchored(View view) {
        collapsedToOpened(100l);
        setOpenedPanelData();
    }

    @Override
    public void onPanelHidden(View view) {
    }


    //
    // MARK: panel view transitions
    //

    private void openedToCollapsed(final long duration) {

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

    private void collapsedToOpened(final long duration) {
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


    //
    // MARK: custom methods
    //


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

    public void updateSlidingPane() {
        switch (mSlidingUpPanelLayout.getPanelState()) {
            case COLLAPSED:
                setCollapsedPanelData();
                break;
            case ANCHORED:
                setOpenedPanelData();
                break;
            case EXPANDED:
                setOpenedPanelData();
                break;
            default:
                break;
        }
    }

    public void setCollapsedPanelData() {

        if (mCurrentFocusedPlaneMarkerIndex == -1) return;

        Plane plane = mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first;

        // plane name
        ((TextView) findViewById(R.id.planeName)).setText(!plane.shortName.isEmpty() ? plane.shortName : "No Callsign");

        // plane from -> to airports
        ((TextView) findViewById(R.id.planeFrom)).setText(!plane.getDestination().fromShort.isEmpty() ? plane.getDestination().fromShort : "N/a");
        ((TextView) findViewById(R.id.planeTo)).setText(!plane.getDestination().toShort.isEmpty() ? plane.getDestination().toShort : "N/a");

        ((TextView) findViewById(R.id.arrivalTime)).setText(plane.getDestination().getArrivalTime());

    }

    public void setOpenedPanelData() {
        if (mCurrentFocusedPlaneMarkerIndex == -1) return;

        Plane plane = mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first;

        // plane name and airline name
        ((TextView) findViewById(R.id.anchoredPanelPlaneName)).setText(plane.getFullName());
        ((TextView) findViewById(R.id.anchoredPanelAirlineName)).setText(plane.getAirlineName());

        // plane from -> to airports
        ((TextView) findViewById(R.id.anchoredPanelFromCity)).setText(plane.getDestination().fromFullCity);
        ((TextView) findViewById(R.id.anchoredPanelToCity)).setText(plane.getDestination().toFullCity);

        ((TextView) findViewById(R.id.anchoredPanelFromAirport)).setText(plane.getDestination().fromFullAirport);
        ((TextView) findViewById(R.id.anchoredPanelToAirport)).setText(plane.getDestination().toFullAirport);

        if (plane.getPlaneImage() != null && plane.getPlaneImage().first != null)
            mPlaneImage.setImageBitmap(plane.getPlaneImage().first);

    }


    //
    // MARK: line chart
    //

    protected String[] mMonths = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private void setupChart(LineChart chart, LineData data, int color) {

        // no description text
        chart.setDescription("");
        chart.setNoDataTextDescription("You need to provide data for the chart.");

        // mChart.setDrawHorizontalGrid(false);
        //
        // enable / disable grid background
        chart.setDrawGridBackground(false);
//        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setBackgroundColor(color);

        // set custom chart offsets (automatic offset calculation is hereby disabled)
        chart.setViewPortOffsets(10, 0, 10, 0);

        // add data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        chart.getXAxis().setEnabled(false);

        // animate calls invalidate()...
        chart.animateX(2500);
    }

    private LineData getData(int count, float range) {

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            xVals.add(mMonths[i % 12]);
        }

        ArrayList<Entry> yVals = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range) + 3;
            yVals.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setFillAlpha(110);
        set1.setFillColor(Color.RED);

        set1.setLineWidth(1.75f);
        set1.setCircleSize(3f);
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.WHITE);
        set1.setHighLightColor(Color.WHITE);
        set1.setDrawValues(false);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        return data;
    }


    //
    // MARK: volley response listeners
    //


    Response.Listener<JSONObject> onFetchedPlaneInfo = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            // reset
            for (Pair<Plane, Marker> markerPair : mPlaneMarkers)
                markerPair.second.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon));
            mPlaneImage.setImageResource(R.drawable.transparent);
            if (mCurrentDrawnPolyline != null)
                mCurrentDrawnPolyline.remove();


            /* update plane object */

            mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first.setFullName(Tools.getJsonString(response, Constants.KEY_AIRCRAFT_NAME));
            mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first.setAirlineName(Tools.getJsonString(response, Constants.KEY_AIRLINE_NAME));

            // build destination object
            Destination.Builder destBuilder = new Destination.Builder()
                    .fromShortName(mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first.getDestination().fromShort)
                    .toShortName(mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first.getDestination().toShort);


            if (!response.isNull(Constants.KEY_PLANE_FROM_SHORT))
                destBuilder.fromShortName(Tools.getJsonString(response, Constants.KEY_PLANE_FROM_SHORT));

            if (!response.isNull(Constants.KEY_PLANE_TO_SHORT))
                destBuilder.toShortName(Tools.getJsonString(response, Constants.KEY_PLANE_TO_SHORT));

            // set destination full name
            destBuilder.fromFullName(Tools.getJsonString(response, Constants.KEY_PLANE_FROM))
                    .toFullName(Tools.getJsonString(response, Constants.KEY_PLANE_TO));

            // destination arrival and departure times
            if (!response.isNull(Constants.KEY_PLANE_DEPARTURE_TIME)) {
                try {
                    destBuilder.departureTime(response.getLong(Constants.KEY_PLANE_DEPARTURE_TIME));
                } catch (Exception e) {
                }
            }

            if (!response.isNull(Constants.KEY_PLANE_ARRIVAL_TIME)) {
                try {
                    destBuilder.arrivalTime(response.getLong(Constants.KEY_PLANE_ARRIVAL_TIME));
                } catch (JSONException e) {
                }
            }

            // NOTE: catch blocks can be empty as the values for these variables have previously
            // been set and not setting them now won't matter

            if (!response.isNull(Constants.KEY_PLANE_POS_FROM)) {
                try {
                    JSONArray coordsArr = response.getJSONArray(Constants.KEY_PLANE_POS_FROM);
                    if (coordsArr.length() == 2) {
                        destBuilder.fromCoords(new LatLng(coordsArr.getDouble(0), coordsArr.getDouble(1)));
                    }
                } catch (JSONException e) {
                }
            }
            if (!response.isNull(Constants.KEY_PLANE_POS_TO)) {
                try {
                    JSONArray coordsArr = response.getJSONArray(Constants.KEY_PLANE_POS_TO);
                    if (coordsArr.length() == 2) {
                        destBuilder.toCoords(new LatLng(coordsArr.getDouble(0), coordsArr.getDouble(1)));
                    }
                } catch (JSONException e) {
                }
            }

            Destination planeDest = destBuilder.build();
            mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first.setDestination(planeDest);


            String largeImageUrl = Tools.getJsonString(response, Constants.KEY_PLANE_IMAGE_LARGE_URL);
            String smallImageUrl = Tools.getJsonString(response, Constants.KEY_PLANE_IMAGE_URL);

            String imgUrl = !largeImageUrl.isEmpty() ? largeImageUrl : smallImageUrl;

            // fetch new image
            ImageLoader imgLoader = PlaneApplication.getInstance().getImageLoader();
            imgLoader.get(imgUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bmp = response.getBitmap();
                    if (bmp != null) {
                        // TODO: fade in animation
                        mPlaneImage.setImageBitmap(bmp);
                        mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).first.setPlaneImage(Pair.create(bmp, Tools.getBitmapColor(bmp)));
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    mPlaneImage.setImageResource(R.drawable.transparent);
                }
            });

            // set marker icon to SELECTED
            mPlaneMarkers.get(mCurrentFocusedPlaneMarkerIndex).second.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plane_icon_selected));

            // draw new polyline
            try {
                PolylineOptions line = new PolylineOptions().width(10).color(R.color.visibility_circle_color);
                JSONArray trailArr = response.getJSONArray(Constants.KEY_PLANE_MAP_TRAIL);
                for (int i = 0; i < trailArr.length(); i += 5) {
                    line.add(new LatLng(trailArr.getDouble(i), trailArr.getDouble(i + 1)));
                }
                mCurrentDrawnPolyline = mMap.addPolyline(line);
            } catch (JSONException e) {
            }

            updateSlidingPane();
        }
    };

    Response.Listener<JSONObject> onFetchedAllPlanes = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            Iterator<String> jsonIterator = response.keys();

            while (jsonIterator.hasNext()) {
                String key = jsonIterator.next();

                try { // it's plane data if we can convert to a JSONArray

                    JSONArray planeDataArr = response.getJSONArray(key);

                    Plane.Builder planeBuilder = new Plane.Builder().key(key);

                    planeBuilder.shortName(Tools.getJsonStringFromArr(planeDataArr, 16));

                    if (!planeDataArr.isNull(1) && !planeDataArr.isNull(2))
                        planeBuilder.position(new LatLng(planeDataArr.getDouble(1), planeDataArr.getDouble(2)));
                    if (!planeDataArr.isNull(3))
                        planeBuilder.rotation((float) planeDataArr.getDouble(3));

                    // build plane destination object
                    Destination.Builder destBuilder = new Destination.Builder();
                    destBuilder.fromShortName(Tools.getJsonStringFromArr(planeDataArr, 11))
                            .toShortName(Tools.getJsonStringFromArr(planeDataArr, 12));

                    // build destination
                    planeBuilder.shortDestinationNames(destBuilder.build());

                    Plane tmpPlane = planeBuilder.build();

                    int markerIndex = Tools.getPlaneMarkerIndex(mPlaneMarkers, key);

                    // add plane to markers list if not
                    if (markerIndex == -1) {
                        Marker planeMarker = mMap.addMarker(new MarkerOptions().position(tmpPlane.getPlanePos())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
                                .rotation(tmpPlane.getRotation())
                                .flat(true));
                        mPlaneMarkers.add(Pair.create(tmpPlane, planeMarker));
                    } else {

                        /* directly update plane and marker objects */

                        mPlaneMarkers.get(markerIndex).first.setPlanePos(tmpPlane.getPlanePos());
                        mPlaneMarkers.get(markerIndex).first.setRotation(tmpPlane.getRotation());

                        // only set destination if it's null, otherwise other destination
                        // variables are set to empty, ex. `toFullCity` as this information
                        // isn't available at this time
                        if (mPlaneMarkers.get(markerIndex).first.getDestination() == null)
                            mPlaneMarkers.get(markerIndex).first.setDestination(tmpPlane.getDestination());

                        mPlaneMarkers.get(markerIndex).second.setPosition(tmpPlane.getPlanePos());
                        mPlaneMarkers.get(markerIndex).second.setRotation(tmpPlane.getRotation());
                    }

                } catch (JSONException e) {
                    // this can be ignored because only time this exception occurs is when we try
                    // to convert a json node to an node array but it's not. That is used as a
                    // detection to check if the node value contains plane data or not
                }

            }

            // fetch again after 10 seconds
            mHandler.postDelayed(fetchData, 10000);
        }
    };


    //
    // MARK: runnables
    //

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

            // TODO: add error listener
            JsonObjectRequest request = new JsonObjectRequest(reqUrl, null, onFetchedAllPlanes, null);
            PlaneApplication.getInstance().getRequestQueue().add(request);
        }
    };


}

package gurinderhans.me.whatplaneisthat;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONObject;

import gurinderhans.me.whatplaneisthat.Controllers.PlaneController;
import gurinderhans.me.whatplaneisthat.Models.Plane;

import static gurinderhans.me.whatplaneisthat.Constants.BASE_URL;
import static gurinderhans.me.whatplaneisthat.Constants.INVALID_ARRAY_INDEX;
import static gurinderhans.me.whatplaneisthat.Constants.MAP_CAMERA_LOCK_MIN_ZOOM;
import static gurinderhans.me.whatplaneisthat.Constants.OPTIONS_FORMAT;
import static gurinderhans.me.whatplaneisthat.Constants.PLANE_DATA_URL;
import static gurinderhans.me.whatplaneisthat.Constants.REFRESH_INTERVAL;
import static gurinderhans.me.whatplaneisthat.Constants.SEARCH_RADIUS;

public class MainActivity extends FragmentActivity implements LocationListener, SensorEventListener,
		OnMarkerClickListener, SlidingUpPanelLayout.PanelSlideListener, GoogleMap.OnMapClickListener {

	/**
	 * Possible future [TODOS]
	 */
	// TODO: Estimate plane location and make it move in "realtime"
	// TODO: use plane speed to make planes move in real-time and then adjust location on new HTTP req.
	// TODO: guess which planes "I" might be able to see
	// TODO: make sure all views get filled and they aren't empty


	protected static final String TAG = MainActivity.class.getSimpleName();

	public static LatLng mUserLocation = new LatLng(0, 0); // default

	Handler mHandler = new Handler();

	boolean followUser = false;
	boolean cameraAnimationFinished = false;

	SlidingUpPanelLayout.PanelState mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;

	float mPanelPrevSlideValue = 0;

	SensorManager mSensorManager;
	LocationManager mLocationManager;
	GoogleMap mMap;
	Marker mUserMarker;
	GroundOverlay mUserVisibilityCircle;


	// main activity views
	ImageButton mLockCameraToUserLocation;
	SlidingUpPanelLayout mSlidingUpPanelLayout;
	ImageView mPlaneImage;

	// sliding panel views for different states
	View mCollapsedView;
	View mAnchoredView;
	View mNoPlaneSelectedView;

	// graph charts
	LineChart mAltitudeLineChart;
	LineChart mSpeedLineChart;

	// Controllers
	PlaneController mPlanesController;

	Runnable fetchData = new Runnable() {
		@Override
		public void run() {
			LatLng north_west = Tools.boundingBox(mUserLocation, 315, SEARCH_RADIUS);
			LatLng south_east = Tools.boundingBox(mUserLocation, 135, SEARCH_RADIUS);
			String reqUrl = BASE_URL + String.format(
					OPTIONS_FORMAT,
					String.valueOf(north_west.latitude),
					String.valueOf(south_east.latitude),
					String.valueOf(north_west.longitude),
					String.valueOf(south_east.longitude)
			);

			// TODO: add error listener to json object request
			JsonObjectRequest request = new JsonObjectRequest(reqUrl, null, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					// add new planes to the list
					mPlanesController.updatePlanes(response);

					// fetch again after 10 seconds
					mHandler.postDelayed(fetchData, REFRESH_INTERVAL);
				}
			}, null);

			PlaneApplication.getInstance().getRequestQueue().add(request);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setting the status bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(getResources().getColor(R.color.transparent_status_bar_color));

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mLockCameraToUserLocation = (ImageButton) findViewById(R.id.lockToLocation);
		mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mPlaneImage = (ImageView) findViewById(R.id.planeImage);

		mAltitudeLineChart = (LineChart) findViewById(R.id.planeAltitudeChart);
		mSpeedLineChart = (LineChart) findViewById(R.id.planeSpeedChart);

		mCollapsedView = findViewById(R.id.panelCollapsedView);
		mAnchoredView = findViewById(R.id.panelAnchoredView);
		mNoPlaneSelectedView = findViewById(R.id.noPlaneSelectedView);


		// store cached location to have something until GPS is available
		Location cachedLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (cachedLocation != null)
			mUserLocation = new LatLng(cachedLocation.getLatitude(), cachedLocation.getLongitude());

		managePanel(-1f);
		setUpMapIfNeeded();

		// initialize plane controller with google maps
		mPlanesController = new PlaneController(mMap);

		mLockCameraToUserLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				followUser = true;
				cameraAnimationFinished = false;
				mLockCameraToUserLocation.setImageResource(R.drawable.ic_gps_fixed_blue_24dp);
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, MAP_CAMERA_LOCK_MIN_ZOOM), new GoogleMap.CancelableCallback() {
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
		mSlidingUpPanelLayout.setPanelSlideListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setUpMapIfNeeded();
		mHandler.postDelayed(fetchData, 0);
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
			// TODO: set dist threshold before setting follow user to false
			followUser = false;
			mLockCameraToUserLocation.setImageResource(R.drawable.ic_gps_fixed_black_24dp);
		}
		return super.dispatchTouchEvent(ev);
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
	// MARK: location manager methods
	//


	@Override
	public void onLocationChanged(Location location) {

		// update user location
		mUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mUserMarker.setPosition(mUserLocation);

		// user visibility circle - radius will depend on the actual visibility retrieved from a weather API // TODO: 15-08-14
		mUserVisibilityCircle.setPosition(mUserLocation);
		mUserVisibilityCircle.setDimensions(5000f);

		// follow user maker
		if (followUser && cameraAnimationFinished) {
			cameraAnimationFinished = false;
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, MAP_CAMERA_LOCK_MIN_ZOOM), new GoogleMap.CancelableCallback() {
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
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}


	//
	// MARK: marker click listener
	//

	@Override
	public boolean onMarkerClick(Marker marker) {

		// set selected index on plane controller
		mPlanesController.setSelectedPlaneIndex(
				Tools.getPlaneMarkerIdIndex(mPlanesController.getPlaneMarkers(), marker.getId()));

		if (mPlanesController.getSelectedPlaneIndex() != INVALID_ARRAY_INDEX) {

			LineData planeAltitudeData = mPlanesController.getPlaneAltitudeData();
			LineData planeSpeedData = mPlanesController.getPlaneSpeedData();

			if (planeAltitudeData != null)
				mPlanesController.setupChart(mAltitudeLineChart, planeAltitudeData, Color.rgb(89, 199, 250));

			if (planeSpeedData != null)
				mPlanesController.setupChart(mSpeedLineChart, planeSpeedData, Color.rgb(250, 104, 104));

			Plane selectedPlane = mPlanesController
					.getPlanes().get(mPlanesController.getSelectedPlaneIndex());

			String reqUrl = String.format(PLANE_DATA_URL,
					selectedPlane.keyIdentifier);

			// TODO: add error listener
			JsonObjectRequest request = new JsonObjectRequest(reqUrl, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							// update this plane
							mPlanesController.setMorePlaneInfo(response, mPlaneImage, mHandler);

							// hide no plane selected image
							mNoPlaneSelectedView.setVisibility(View.INVISIBLE);

							updateSlidingPane();
						}
					}, null);
			PlaneApplication.getInstance().getRequestQueue().add(request);
		}

		return true;
	}


	//
	// MARK: map click listener
	//

	@Override
	public void onMapClick(LatLng latLng) {

		// TODO: 15-08-15 plane controller should handle this ?
		// TODO: 15-08-15 managePanel() should handle this ?

		// show the no plane selected panel image
		mNoPlaneSelectedView.setVisibility(View.VISIBLE);

		// remove polyline
		if (mPlanesController.getPlanePathPolyline() != null)
			mPlanesController.getPlanePathPolyline().remove();

		// reset markers
		for (Marker marker : mPlanesController.getPlaneMarkers())
			marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon));

		// clear image
		mPlaneImage.setImageResource(R.drawable.transparent);
		if (mPlanesController.getSelectedPlaneIndex() != -1)
			mPlanesController.getPlanes().get(mPlanesController.getSelectedPlaneIndex()).setPlaneImage(null);
	}


	//
	// MARK: Pane slide listener
	//

	@Override
	public void onPanelSlide(View view, float v) {
		managePanel(v);
	}

	@Override
	public void onPanelCollapsed(View view) {
		mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
	}

	@Override
	public void onPanelAnchored(View view) {
		mPanelState = SlidingUpPanelLayout.PanelState.ANCHORED;
	}

	@Override
	public void onPanelExpanded(View view) {
	}

	@Override
	public void onPanelHidden(View view) {
	}


	//
	// MARK: custom methods
	//

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			if (mMap != null)
				setUpMap();
		}
	}

	private void setUpMap() {

		// map settings
		mMap.getUiSettings().setMapToolbarEnabled(false);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnMapClickListener(this);

		// user marker
		mUserMarker = mMap.addMarker(new MarkerOptions().position(mUserLocation)
						.icon(BitmapDescriptorFactory.fromBitmap(Tools.getSVGBitmap(this, R.drawable.user_marker, -1, -1)))
						.rotation(0f)
						.flat(true)
						.anchor(0.5f, 0.5f)
		);
		// user visibility circle
		mUserVisibilityCircle = mMap.addGroundOverlay(new GroundOverlayOptions()
				.image(BitmapDescriptorFactory.fromBitmap(Tools.getSVGBitmap(this, R.drawable.user_visibility, -1, -1)))
				.anchor(0.5f, 0.5f)
				.position(mUserLocation, 500000f));

		// animate camera to user location
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 12f));
	}

	/**
	 * Manages the slide panel
	 *
	 * @param slideValue - how far up the panel is form the top (in percent %)
	 */
	private void managePanel(float slideValue) {

		// used for initial setup stuff
		if (slideValue == -1) {
			// hide all other panel views so only prompt view shows
			mCollapsedView.setVisibility(View.INVISIBLE);
			mAnchoredView.setVisibility(View.INVISIBLE);

			// set image initial position so it hides behind the panel
			float translationVal = getResources().getDimensionPixelSize(R.dimen.plane_image_height);
			mPlaneImage.setTranslationY(translationVal);

			return;
		}

		// grab the screen height and do things relative to it
		Point screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);

		// calculate a transition value for the image
		float translationVal = -((screenSize.y * slideValue) * 1.44f) + getResources().getDimensionPixelSize(R.dimen.plane_image_height);

		// compute the transition value limiter
		TypedValue outValue = new TypedValue();
		getResources().getValue(R.integer.sliding_panel_anchor_point, outValue, true);
		float maxVal = -(screenSize.y * outValue.getFloat() * 1.44f) + getResources().getDimensionPixelSize(R.dimen.plane_image_height);

		// limit the transition value using the computed limiter
		translationVal = translationVal < maxVal ? maxVal : translationVal;

		// set translation value
		mPlaneImage.setTranslationY(translationVal);


		// going up and state hasn't been changed
		if (slideValue - mPanelPrevSlideValue > 0 && mPanelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
			mPanelState = SlidingUpPanelLayout.PanelState.ANCHORED;
			mPanelPrevSlideValue = slideValue;
			collapsedToOpened(100l);
			setOpenedPanelData();
		}

		// if coming down and state not collapsed, set to collapsed to the collapsed layout doesn't show until
		// its actually collapsed
		if (slideValue - mPanelPrevSlideValue < 0 && mPanelState != SlidingUpPanelLayout.PanelState.COLLAPSED) {
			mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
			mPanelPrevSlideValue = slideValue;
			openedToCollapsed(100l);
			setCollapsedPanelData();
		}
	}


	// MARK: panel view transitions

	private void openedToCollapsed(final long duration) {
		Animation animation = new AlphaAnimation(1, 0);
		animation.setInterpolator(new AccelerateInterpolator());
		animation.setDuration(duration);

		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// show anchored view
				Animation alphaAnimation = new AlphaAnimation(0, 1);
				mCollapsedView.setVisibility(View.VISIBLE);
				alphaAnimation.setInterpolator(new AccelerateInterpolator());
				alphaAnimation.setDuration(duration);
				mCollapsedView.startAnimation(alphaAnimation);
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
				Animation alphaAnimation = new AlphaAnimation(0, 1);
				mAnchoredView.setVisibility(View.VISIBLE);
				alphaAnimation.setInterpolator(new AccelerateInterpolator());
				alphaAnimation.setDuration(duration);
				mAnchoredView.startAnimation(alphaAnimation);
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

	public void setCollapsedPanelData() {

		if (mPlanesController.getSelectedPlaneIndex() == -1) return;

		Plane plane = mPlanesController.getPlanes().get(mPlanesController.getSelectedPlaneIndex());

		// plane name
		((TextView) findViewById(R.id.planeName)).setText(plane.getShortName());

		// plane from -> to airports
		((TextView) findViewById(R.id.planeFrom)).setText(plane.getDestination().getFromShort());
		((TextView) findViewById(R.id.planeTo)).setText(plane.getDestination().getToShort());

		((TextView) findViewById(R.id.arrivalTime)).setText(plane.getDestination().getArrivalTime());

		mCollapsedView.setVisibility(View.VISIBLE);
	}

	public void setOpenedPanelData() {
		if (mPlanesController.getSelectedPlaneIndex() == -1) return;

		Plane plane = mPlanesController.getPlanes().get(mPlanesController.getSelectedPlaneIndex());

		// plane name and airline name
		((TextView) findViewById(R.id.anchoredPanelPlaneName)).setText(plane.getFullName());
		((TextView) findViewById(R.id.anchoredPanelAirlineName)).setText(plane.getAirlineName());

		// plane from -> to airports
		((TextView) findViewById(R.id.anchoredPanelFromCity)).setText(plane.getDestination().getFromFullCity());
		((TextView) findViewById(R.id.anchoredPanelToCity)).setText(plane.getDestination().getToFullCity());

		((TextView) findViewById(R.id.anchoredPanelFromAirport)).setText(plane.getDestination().getFromAirport());
		((TextView) findViewById(R.id.anchoredPanelToAirport)).setText(plane.getDestination().getToAirport());


		Pair<Drawable, Integer> planeImage = plane.getPlaneImage();
		if (planeImage != null && planeImage.first != null)
			mPlaneImage.setImageDrawable(planeImage.first);

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

}

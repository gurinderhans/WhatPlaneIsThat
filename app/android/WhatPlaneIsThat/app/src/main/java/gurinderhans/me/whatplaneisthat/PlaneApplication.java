package gurinderhans.me.whatplaneisthat;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by ghans on 6/23/15.
 */
public class PlaneApplication extends Application {

	private static PlaneApplication sInstance;

	private RequestQueue mRequestQueue;

	private ImageLoader mImageLoader;

	public synchronized static PlaneApplication getInstance() {
		return sInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mRequestQueue = Volley.newRequestQueue(this);

		mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache());

		sInstance = this;
	}

	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}
}

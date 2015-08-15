package gurinderhans.me.whatplaneisthat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by ghans on 7/1/15.
 */
public class SplashScreen extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		int secondsDelayed = 1;
		new Handler().postDelayed(new Runnable() {
			public void run() {
				startActivity(new Intent(SplashScreen.this, MainActivity.class));
				finish(); // remove activity from stack
			}
		}, secondsDelayed * 1000);
	}
}

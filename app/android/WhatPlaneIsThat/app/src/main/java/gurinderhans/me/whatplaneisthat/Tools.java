package gurinderhans.me.whatplaneisthat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.larvalabs.svgandroid.SVGBuilder;
import com.squareup.okhttp.ResponseBody;

/**
 * Created by ghans on 6/15/15.
 */
public class Tools {

    static final Gson gson = new Gson();

    private Tools() {
        //
    }

    public static JsonObject stringToJsonObject(ResponseBody data) {

        try {
            JsonElement json = gson.fromJson(data.string(), JsonElement.class);
            return json.getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JsonObject();
    }

    public static Bitmap getMarker(Context c, int visibility) {


        Picture picture = new SVGBuilder().readFromResource(c.getResources(), R.drawable.user_visibility).build().getPicture();


        int height = (visibility != 0) ? visibility : picture.getHeight();

        Bitmap bmp = Bitmap.createBitmap(picture.getWidth(), height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        canvas.drawPicture(picture, new Rect(0, 0, picture.getWidth(), height));

        return bmp;
    }

}

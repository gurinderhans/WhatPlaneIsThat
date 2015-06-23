package gurinderhans.me.whatplaneisthat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

    public static Bitmap getSVGBitmap(Context c, int rId, int width, int height) {

        Picture picture = new SVGBuilder().readFromResource(c.getResources(), rId).build().getPicture();

        int w = (width < 1) ? picture.getWidth() : width;
        int h = (height < 1) ? picture.getWidth() : height;

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        canvas.drawPicture(picture, new Rect(0, 0, w, h));

        return bmp;
    }

    public static int getBitmapColor(Bitmap bitmap) {

        int redColors = 0;
        int greenColors = 0;
        int blueColors = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redColors += Color.red(c);
                greenColors += Color.green(c);
                blueColors += Color.blue(c);
            }
        }
        // calculate average of bitmap r,g,b values
        int red = (redColors / pixelCount);
        int green = (greenColors / pixelCount);
        int blue = (blueColors / pixelCount);

        return Color.rgb(red, green, blue);
    }

    // json parsing helper functions
    public static String jsonElToString(JsonElement el) {
        return el != null && !el.getAsString().isEmpty() ? el.getAsString() : null;
    }

    public static Long jsonElToLong(JsonElement el) {
        try {
            return el.getAsLong();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

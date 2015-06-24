package gurinderhans.me.whatplaneisthat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Rect;

import com.larvalabs.svgandroid.SVGBuilder;

/**
 * Created by ghans on 6/15/15.
 */
public class Tools {

    private Tools() {
        //
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

//    /**
//     * Checks if given plane name is in mPlaneMarkers
//     *
//     * @param markersList
//     * @param name        - plane name
//     * @return - index of the plane in the list, -1 if not found
//     */
//    public static int getPlaneMarkerIndex(List<Pair<Plane, Marker>> markersList, String name) {
//        for (int i = 0; i < markersList.size(); i++) {
//            if (markersList.get(i).first.keyIdentifier.equals(name))
//                return i;
//        }
//        return -1;
//    }
//
//    /**
//     * Checks if given plane name is in mPlaneMarkers
//     *
//     * @param markersList
//     * @param planeMarker - plane marker
//     * @return - index of the plane in the list, -1 if not found
//     */
//    public static int getPlaneMarkerIndex(List<Pair<Plane, Marker>> markersList, Marker planeMarker) {
//        for (int i = 0; i < markersList.size(); i++) {
//            if (markersList.get(i).second.equals(planeMarker))
//                return i;
//        }
//        return -1;
//    }
}

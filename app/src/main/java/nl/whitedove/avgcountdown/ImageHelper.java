package nl.whitedove.avgcountdown;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Display;

import java.io.FileOutputStream;
import java.io.InputStream;

class ImageHelper {

    static void saveImage(Context cxt, InputStream input) {
        FileOutputStream out = null;
        String dir = getDirectory(cxt);
        String filename = dir + "avgcdbg.png";

        try {
            out = new FileOutputStream(filename);
            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            Helper.ShowMessage(cxt, e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                Helper.ShowMessage(cxt, e.getMessage());
            }
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("background", filename);
        editor.apply();
    }

    static void saveImage(Context cxt, Uri imageUri) {
        try {
            InputStream input = cxt.getContentResolver().openInputStream(imageUri);
            saveImage(cxt, input);
        } catch (Exception e) {
            Helper.ShowMessage(cxt, e.getMessage());
        }
    }

    private static String getDirectory(Context cxt) {
        String state = Environment.getExternalStorageState();
        // Return external storage folder
        if (Environment.MEDIA_MOUNTED.equals(state))
            //noinspection ConstantConditions
            return cxt.getExternalFilesDir(null).getAbsolutePath();
        // Return internal storage folder
        return cxt.getFilesDir().getAbsolutePath();
    }

    static Bitmap decodeSampledBitmapFromFile(String fileUri, Context context, Display display) {

        // Decode bitmap with inSampleSize set
        final BitmapFactory.Options options = getImageSize(fileUri, context, display);
        Bitmap bmp = null;
        try {
            options.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeFile(fileUri, options);
        } catch (Exception e) {
            Helper.ShowMessage(context, e.getMessage(), true);
        }
        return bmp;
    }


    static boolean imageSmallerThanScreen(String fileUri, Context context, Display display) {
        // Display size
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Decode with inJustDecodeBounds=true to check dimensions
        try {
            BitmapFactory.decodeFile(fileUri, options);
        } catch (Exception e) {
            Helper.ShowMessage(context, e.getMessage(), true);
        }
        return (options.outHeight < height && options.outWidth < width);
    }

    static BitmapFactory.Options getImageSize(String fileUri, Context context, Display display) {
        // Display size,
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Decode with inJustDecodeBounds=true to check dimensions
        try {
            BitmapFactory.decodeFile(fileUri, options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, width, height);
        } catch (Exception e) {
            Helper.ShowMessage(context, e.getMessage(), true);
        }
        return options;
    }

    static private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}

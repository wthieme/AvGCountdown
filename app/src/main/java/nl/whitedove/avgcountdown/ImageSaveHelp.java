package nl.whitedove.avgcountdown;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageSaveHelp {

    public static void saveImage(Context cxt, InputStream input) {
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
            Melding(cxt, e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                Melding(cxt, e.getMessage());
            }
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("background", filename);
        editor.apply();
    }

    public static void saveImage(Context cxt, Uri imageUri) {
        try {
            InputStream input = cxt.getContentResolver().openInputStream(imageUri);
            saveImage(cxt,input);
        } catch (Exception e) {
            Melding(cxt, e.getMessage());
        }
    }

    private static String getDirectory(Context cxt) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Return external storage folder
            return cxt.getExternalFilesDir(null).getAbsolutePath();
        }
        // Return internal storage folder
        return cxt.getFilesDir().getAbsolutePath();
    }

    private static void Melding(Context cxt, String log) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(cxt, log, duration);
        toast.show();
    }
}

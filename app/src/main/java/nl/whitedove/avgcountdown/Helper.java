package nl.whitedove.avgcountdown;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;


class Helper {

    private static final boolean DEBUG = false;

    static void Log(String log) {
        if (Helper.DEBUG) {
            System.out.println(log);
        }
    }

    static Boolean TestInternet(Context ctx) {
        Boolean result;

        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            result = false;
        } else {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            result = netInfo != null && netInfo.isConnectedOrConnecting();
        }

        if (!result) {
            Helper.ShowMessage(ctx, "Geen internet connectie");
        }

        return result;
    }

    static void ShowMessage(Context cxt, String melding) {
        Helper.ShowMessage(cxt, melding, false);
    }

    static void ShowMessage(Context cxt, String melding, boolean durationLong) {
        Helper.Log(melding);
        int duration;
        if (durationLong)
            duration = Toast.LENGTH_LONG;
        else
            duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(cxt, melding, duration);
        toast.show();
    }
}

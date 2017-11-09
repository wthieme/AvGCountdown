package nl.whitedove.avgcountdown;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.joda.time.DateTime;


class Helper {

    private static final boolean DEBUG = false;
    static final DateTime DEFAULT_EVENT_DATE = new DateTime(2018, 5, 29, 19, 0);

    static void Log(String log) {
        if (Helper.DEBUG) {
            System.out.println(log);
        }
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

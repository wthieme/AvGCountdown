package nl.whitedove.avgcountdown;

import android.content.Context;
import android.widget.Toast;

class Melding {

    static void Log(String log) {
        //System.out.println(log);
    }

    static void ShowMessage(Context cxt, String melding, boolean durationLong) {
        Melding.Log(melding);
        int duration;
        if (durationLong)
            duration = Toast.LENGTH_LONG;
        else
            duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(cxt, melding, duration);
        toast.show();
    }
}

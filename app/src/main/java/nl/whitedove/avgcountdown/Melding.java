package nl.whitedove.avgcountdown;

import android.content.Context;
import android.widget.Toast;

public class Melding {

    public static void Log(String log) {
        //System.out.println(log);
    }

    public static void ShowMessage(Context cxt, String melding) {
        Melding.Log(melding);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(cxt, melding, duration);
        toast.show();
    }
}

package nl.whitedove.avgcountdown;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetRandomFileActivity extends Activity {
    ProgressDialog mProgress;

    private static final String url = "https://wthieme.github.io/AvgCountDown/";
    // Avg, Nature, Planes
    private static final String[] DirCats = {"A3kzhxf5har9wubhfvnbzps9u", "N67xke6cg6kqjpntb53bm978t", "Pzf99r5w3y433ug8mrawapkub"};
    private static final int[] NrCats = {83, 20, 30};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting new image ...");
        mProgress.show();
        GetRandomFile();
    }

    private void GetRandomFile() {
        if (!Helper.TestInternet(this)) {
            return;
        }
        new AsyncgetRandomFile().execute();
    }

    private class AsyncgetRandomFile extends AsyncTask<Object, Object, Object> {

        @Override
        protected Void doInBackground(Object... params) {
            GetRandomFileFromInternet();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            returnToSender();
        }
    }

    private void GetRandomFileFromInternet() {
        Random rnd = new Random();
        int mapNr;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String bgCat = preferences.getString("bgCat", "All");

        switch (bgCat) {
            case "AvG":
                mapNr = 0;
                break;
            case "Nature":
                mapNr = 1;
                break;
            case "Planes":
                mapNr = 2;
                break;
            default:
                mapNr = rnd.nextInt(3);
        }

        String map = DirCats[mapNr];

        Helper.Log(String.format("Geslecteerde map: %s", map));
        int aantal = NrCats[mapNr];
        Helper.Log(String.format("Aantal files: %d", aantal));
        int nr = rnd.nextInt(aantal);
        String fileName = String.format("%04d.jpg",nr);
        String fullName = url + map + "/" + fileName;

        Helper.Log("Random file: " + fullName);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(fullName)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            InputStream input = response.body().byteStream();
            ImageSaveHelp.saveImage(getApplicationContext(), input);
        } catch (IOException ignored) {
        }
    }

    private void returnToSender() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        mProgress.dismiss();
        Helper.Log("ReturnToSender");
    }
}
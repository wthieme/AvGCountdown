package nl.whitedove.avgcountdown;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static DateTime peildatum = new DateTime(2016, 12, 16, 12, 0);
    private DateTimeFormatter dtFormat = DateTimeFormat.shortDateTime().withLocale(Locale.getDefault());
    private ScheduledExecutorService executer = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture future;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDefaults();
        initTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        initTimer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setDefaults();
    }

    public void moreClick(View oView) {
        Melding.Log("More clicked");
        PopupMenu popup = new PopupMenu(this, oView);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.settings, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_settings:
                        Intent intent1 = new Intent();
                        intent1.setClass(MainActivity.this, SetPreferenceActivity.class);
                        startActivityForResult(intent1, 0);
                        return true;

                    case R.id.select_background:
                        Intent intent2 = new Intent();
                        intent2.setClass(MainActivity.this, ImageSelectActivity.class);
                        startActivityForResult(intent2, 1);
                        return true;
                }
                return true;
            }
        });

        popup.show();
    }

    private void updateScreen() {
        Melding.Log("Update screen");

        DateTime today = DateTime.now();
        DateTime thatDay = peildatum;

        Period period = new Period(thatDay, today);

        TextView tvCD = (TextView) findViewById(R.id.tvCD);
        if (today.isAfter(thatDay))
            tvCD.setText(getString(R.string.Counter));
        else
            tvCD.setText(getString(R.string.CountDown));

        TextView tvY = (TextView) findViewById(R.id.tvY);
        tvY.setText(String.format("%dy ", Math.abs(period.getYears())));

        TextView tvM = (TextView) findViewById(R.id.tvM);
        tvM.setText(String.format("%dm ", Math.abs(period.getMonths())));

        TextView tvW = (TextView) findViewById(R.id.tvW);
        tvW.setText(String.format("%dw ", Math.abs(period.getWeeks())));

        TextView tvD = (TextView) findViewById(R.id.tvD);
        tvD.setText(String.format("%dd ", Math.abs(period.getDays())));

        TextView tvH = (TextView) findViewById(R.id.tvH);
        tvH.setText(String.format("%dh ", Math.abs(period.getHours())));

        TextView tvMi = (TextView) findViewById(R.id.tvMi);
        tvMi.setText(String.format("%dm ", Math.abs(period.getMinutes())));

        TextView tvS = (TextView) findViewById(R.id.tvS);
        tvS.setText(String.format("%ds", Math.abs(period.getSeconds())));

        Locale locale = Locale.getDefault();
        NumberFormat nFormat = NumberFormat.getNumberInstance(locale);

        long totalYears = Math.abs(Years.yearsBetween(today, thatDay).getYears());
        TextView tvYears = (TextView) findViewById(R.id.tvYears);
        tvYears.setText(nFormat.format(totalYears));

        long totalMonths = Math.abs(Months.monthsBetween(today, thatDay).getMonths());
        TextView tvMonths = (TextView) findViewById(R.id.tvMonths);
        tvMonths.setText(nFormat.format(totalMonths));

        long totalWeeks = Math.abs(Weeks.weeksBetween(today, thatDay).getWeeks());
        TextView tvWeeks = (TextView) findViewById(R.id.tvWeeks);
        tvWeeks.setText(nFormat.format(totalWeeks));

        long totalDays = Math.abs(Days.daysBetween(today, thatDay).getDays());
        TextView tvDays = (TextView) findViewById(R.id.tvDays);
        tvDays.setText(nFormat.format(totalDays));

        long totalHours = Math.abs(Hours.hoursBetween(today, thatDay).getHours());
        TextView tvHours = (TextView) findViewById(R.id.tvHours);
        tvHours.setText(nFormat.format(totalHours));

        long totalMinutes = Math.abs(Minutes.minutesBetween(today, thatDay).getMinutes());
        TextView tvMinutes = (TextView) findViewById(R.id.tvMinutes);
        tvMinutes.setText(nFormat.format(totalMinutes));

        int totalSeconds = Math.abs(Seconds.secondsBetween(today, thatDay).getSeconds());
        TextView tvSeconds = (TextView) findViewById(R.id.tvSeconds);
        tvSeconds.setText(nFormat.format(totalSeconds));
        setBackground();
    }

    private void stopTimer() {
        if (executer != null && future != null) {
            future.cancel(false);
            Melding.Log("Task gestopt");
        }
    }

    private void initTimer() {
        if (executer == null) {
            executer = Executors.newSingleThreadScheduledExecutor();
            Melding.Log("Executer gemaakt");
        }

        final MyHandler mHandler = new MyHandler(this);

        Runnable task = new Runnable() {
            public void run() {
                Context cxt = getApplicationContext();
                try {
                    mHandler.obtainMessage(1).sendToTarget();
                } catch (Exception e) {
                    Melding.ShowMessage(cxt, e.getMessage(), true);
                }
            }
        };

        if (future != null) {
            future.cancel(false);
            Melding.Log("Task gestopt");
        }

        future = executer.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        Melding.Log("Task geactiveerd");
    }

    private void setDefaults() {
        setBackground();
        setLayout();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String avgEvent = preferences.getString("eventname", "VUUR try out show");
        TextView tvAvgFw = (TextView) findViewById(R.id.tvAvgFw);
        Typeface font = Typeface.createFromAsset(getAssets(), "Curly.ttf");
        tvAvgFw.setTypeface(font);
        tvAvgFw.setText(avgEvent);

        long ldate1 = peildatum.getMillis();
        long l1 = preferences.getLong("eventdate", ldate1);
        DateTime dt1 = new DateTime(l1);
        long ldate2 = peildatum.getMillis();
        long l2 = preferences.getLong("eventtime", ldate2);
        DateTime dt2 = new DateTime(l2);

        peildatum = new DateTime(dt1.getYear(), dt1.getMonthOfYear(), dt1.getDayOfMonth(), dt2.getHourOfDay(), dt2.getMinuteOfHour());
        TextView tvCD = (TextView) findViewById(R.id.tvCD);
        tvCD.setTypeface(font);

        TextView tvPD = (TextView) findViewById(R.id.tvPeildatum);
        tvPD.setText(dtFormat.print(peildatum));

        String kleur = preferences.getString("textcolor", "White");
        int zwart = Color.parseColor("#000000");
        int wit = Color.parseColor("#FFFFFF");
        if (kleur.equals("Black")) {

            tvAvgFw.setTextColor(zwart);
            tvAvgFw.setShadowLayer(2f, 2f, 2f, wit);
            tvCD.setTextColor(zwart);
            tvCD.setShadowLayer(2f, 2f, 2f, wit);
            tvPD.setTextColor(zwart);
            ((TextView) findViewById(R.id.tvY)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvM)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvW)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvD)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvH)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvMi)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvS)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvTyears)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvYears)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvTmonths)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvMonths)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvTweeks)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvWeeks)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvTdays)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvDays)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvThours)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvHours)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvTminutes)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvMinutes)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvTseconds)).setTextColor(zwart);
            ((TextView) findViewById(R.id.tvSeconds)).setTextColor(zwart);
        }

        String imgPath = preferences.getString("background", "");
        Melding.Log("ImgPath = " + imgPath);
        if (!imgPath.isEmpty()) {

            ImageView ivBg = (ImageView) findViewById(R.id.imgBg);
            File file = new File(imgPath);
            if (file.exists()) {
                // If image is smaller than the screen adjes view bounds
                boolean small = imageSmallerThanScreen(imgPath);
                if (small) {
                    ivBg.setAdjustViewBounds(true);
                }
                Bitmap bmp = decodeSampledBitmapFromFile(imgPath);
                ivBg.setImageBitmap(bmp);
            } else {
                Melding.ShowMessage(this, "Background image not found", false);
            }
        }
        initTimer();
    }

    private Bitmap decodeSampledBitmapFromFile(String fileUri) {

        // Decode bitmap with inSampleSize set
        final BitmapFactory.Options options = getImageSize(fileUri);

        Context cxt = getApplicationContext();
        Bitmap bmp = null;
        try {
            options.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeFile(fileUri, options);
        } catch (Exception e) {
            Melding.ShowMessage(cxt, e.getMessage(), true);
        }
        return bmp;
    }

    private boolean imageSmallerThanScreen(String fileUri) {
        // Display size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Decode with inJustDecodeBounds=true to check dimensions
        Context cxt = getApplicationContext();
        try {
            BitmapFactory.decodeFile(fileUri, options);
        } catch (Exception e) {
            Melding.ShowMessage(cxt, e.getMessage(), true);
        }
        return (options.outHeight < height && options.outWidth < width);
    }

    private BitmapFactory.Options getImageSize(String fileUri) {
        // Display size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Decode with inJustDecodeBounds=true to check dimensions
        Context cxt = getApplicationContext();
        try {
            BitmapFactory.decodeFile(fileUri, options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, width, height);
        } catch (Exception e) {
            Melding.ShowMessage(cxt, e.getMessage(), true);
        }
        return options;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;

        MyHandler(MainActivity myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity ma = myClassWeakReference.get();
            if (ma != null) {
                ma.updateScreen();
            }
        }
    }

    private void setLayout() {
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Context cxt = getApplicationContext();

        String hor = preferences.getString("textlochor", "2");
        String ver = preferences.getString("textlocver", "1");
        String rowname = "r" + ver;

        int rowid = getResources().getIdentifier(rowname, "id", cxt.getPackageName());
        TableRow tr = (TableRow) findViewById(rowid);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0f));

        int cell = (Integer.parseInt(ver) - 1) * 3 + Integer.parseInt(hor);
        String cellname = String.format("c%d", cell);
        int cellid = getResources().getIdentifier(cellname, "id", cxt.getPackageName());
        TextView tv = (TextView) findViewById(cellid);
        ViewGroup parent = (ViewGroup) tv.getParent();

        TableLayout tab = (TableLayout) findViewById(R.id.ttxt);
        ViewGroup parenttab = (ViewGroup) tab.getParent();
        parenttab.removeView(tab);

        final int index = parent.indexOfChild(tv);
        parent.removeView(tv);
        parent.addView(tab, index, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        tv.setVisibility(View.INVISIBLE);
    }

    private void setBackground() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String manauto = preferences.getString("bgManAuto", "Manual");

        if (manauto.equals("Manual")) {
            return;
        }

        long last = preferences.getLong("lastdate", 0L);
        DateTime lastDate = new DateTime(last);
        DateTime nu = DateTime.now();

        String bgFreq = preferences.getString("bgFreq", "5");

        int minutesExtra = Integer.parseInt(bgFreq);
        if (lastDate.plusMinutes(minutesExtra).isBefore(nu)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("lastdate", nu.getMillis());
            editor.apply();
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, DriveGetRandomFileActivity.class);
            startActivity(intent);
        }
    }
}
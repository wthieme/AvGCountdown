package nl.whitedove.avgcountdown;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class TimePreference extends DialogPreference {
    private DateTime tijd = Helper.DEFAULT_EVENT_DATE;
    private TimePicker picker = null;

    public TimePreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            picker.setHour(tijd.getHourOfDay());
            picker.setMinute(tijd.getMinuteOfHour());
        } else {
            //noinspection deprecation
            picker.setCurrentHour(tijd.getHourOfDay());
            //noinspection deprecation
            picker.setCurrentMinute(tijd.getMinuteOfHour());
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                tijd = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(), picker.getHour(), picker.getMinute());
            else
                //noinspection deprecation
                tijd = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(), picker.getCurrentHour(), picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(tijd.getMillis())) {
                persistLong(tijd.getMillis());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                tijd = new DateTime(getPersistedLong(System.currentTimeMillis()));
            } else {
                tijd = new DateTime(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                tijd = new DateTime(System.currentTimeMillis());
            } else {
                tijd = new DateTime(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (tijd == null) {
            return null;
        }
        DateTimeFormatter tFormat = DateTimeFormat.shortTime().withLocale(Locale.getDefault());
        return tFormat.print(tijd);
    }
}
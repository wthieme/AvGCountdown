package nl.whitedove.avgcountdown;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class DatePreference extends DialogPreference {
    private DateTime datum = new DateTime(2017, 6, 9, 19, 0);
    private DatePicker picker = null;

    public DatePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public DatePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new DatePicker(getContext());
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.init(datum.getYear(), datum.getMonthOfYear() - 1, datum.getDayOfMonth(), null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            datum = new DateTime(picker.getYear(), picker.getMonth() + 1, picker.getDayOfMonth(), 0, 0, 0);

            setSummary(getSummary());
            if (callChangeListener(datum)) {
                persistLong(datum.getMillis());
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
                datum = new DateTime(getPersistedLong(System.currentTimeMillis()));
            } else {
                datum = new DateTime(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                datum = new DateTime(getPersistedLong(System.currentTimeMillis()));
            } else {
                datum = new DateTime(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (datum == null) {
            return null;
        }
        DateTimeFormatter dFormat = DateTimeFormat.shortDate().withLocale(Locale.getDefault());
        return dFormat.print(datum);
    }
}
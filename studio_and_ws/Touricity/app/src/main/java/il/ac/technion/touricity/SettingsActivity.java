package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import il.ac.technion.touricity.service.LocationService;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private AppCompatDelegate mDelegate;
    private static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Context is the settings activity
        mContext = this;

        // Inflate action bar
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getDelegate().getSupportActionBar().show();

        // Add 'general' preferences, defined in the XML file
        // TODO: refactor activity to use preference fragments instead of deprecated methods
        // See <a href="http://developer.android.com/reference/android/preference/PreferenceActivity.html">
        // PreferenceActivity</a>
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sync_interval_key)));
    }



    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {

        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        }
        else if(preference instanceof EditTextPreference) {
            // Location setting
            if (preference.getKey().equals(mContext.getString(R.string.pref_location_key))) {
                // TODO: complete code using notes from location service
                Intent intent = new Intent(mContext, LocationService.class);
                intent.putExtra(Intent.EXTRA_TEXT, stringValue);
                mContext.startService(intent);
                // TODO: set location display from OSM data
                preference.setSummary(stringValue);
            }

            // Sync interval setting
            else if (preference.getKey().equals(mContext.getString(R.string.pref_sync_interval_key))) {
                // 5 minutes
                final int MIN_SYNC_INTERVAL = 5;
                // 1440 minutes are 24 hours
                final int MAX_SYNC_INTERVAL = 1440;

                // Restrict interval to certain range.
                if (Integer.valueOf(stringValue) < MIN_SYNC_INTERVAL) {
                    stringValue = String.valueOf(MIN_SYNC_INTERVAL);
                    Toast.makeText(mContext,
                            "Minimal value is " + stringValue,
                            Toast.LENGTH_SHORT).show();
                }
                else if (Integer.valueOf(stringValue) > MAX_SYNC_INTERVAL) {
                    stringValue = String.valueOf(MAX_SYNC_INTERVAL);

                    Toast.makeText(mContext,
                            "Maximal value is " + stringValue,
                            Toast.LENGTH_SHORT).show();
                }

                // Save interval value.
                preference.getEditor().putInt(preference.getKey(),
                        Integer.valueOf(stringValue));
                preference.getEditor().apply();

                // Set the summary to reflect the new value.
                int formatId = R.string.pref_sync_interval_summary;
                preference.setSummary(mContext.getString(formatId, stringValue));
            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
        }
        return true;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        if (super.getParentActivityIntent() != null) {
            return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        return null;
    }
}

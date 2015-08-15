package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import il.ac.technion.touricity.data.ToursContract;
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

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    private static final int LOCATIONS_LOADER = 0;

    // 5 minutes
    private static final int MIN_SYNC_INTERVAL = 5;
    // 1440 minutes are 24 hours
    private static final int MAX_SYNC_INTERVAL = 1440;

    private Context mContext;
    private AppCompatDelegate mDelegate;

    private LocationListPreference mLocationQueryPreference;
    private LocationAdapter mLocationAdapter;

    private static final String[] OSM_COLUMNS = {
            // Used for projection.
            // _ID must be used in every projection
            ToursContract.OSMEntry._ID,
            ToursContract.OSMEntry.COLUMN_LOCATION_TYPE,
            ToursContract.OSMEntry.COLUMN_LOCATION_NAME
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    // The following variables are package-private.
    // Package-private is stricter than protected and public scopes, but more permissive
    // than private scope.
    static final int COL_ID = 0;
    static final int COL_LOCATION_TYPE = 1;
    static final int COL_LOCATION_NAME = 2;

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

        mLocationAdapter = new LocationAdapter(mContext, null, 0);
        mLocationQueryPreference = (LocationListPreference)
                findPreference(getString(R.string.pref_location_query_key));
        // Register the adapter associated with the location list preference
        mLocationQueryPreference.registerAdapter(mLocationAdapter);

        // Location query preference is generally invisible to the user. Only pop up after
        // typing a requested location.
        getPreferenceScreen().removePreference(mLocationQueryPreference);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sync_interval_key)));

        // TODO: remove later
//        // Set the listener to watch for value changes.
//        mLocationQueryPreference.setOnPreferenceChangeListener(this);
//
//        // Trigger the listener immediately with the preference's
//        // current value.
//        onPreferenceChange(mLocationQueryPreference, mLocationQueryPreference.getValue());
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
            //preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            Log.v(LOG_TAG, "Clicked:" + stringValue);
        }
        else if(preference instanceof EditTextPreference) {

            // Location setting
            if (preference.getKey().equals(mContext.getString(R.string.pref_location_key))) {
                // TODO: only contact OSM database if the location is not in the OSM table
                updateLocation(mContext, stringValue);
                // Only update location summary if and when OSM returns an existing location and
                // the user certifies that this IS the requested location. There can be multiple
                // places with the same name. For now, don't save anything.
                return false;
            }

            // Sync interval setting
            else if (preference.getKey().equals(mContext.getString(R.string.pref_sync_interval_key))) {
                // Restrict interval to certain range.
                if (Integer.valueOf(stringValue) < MIN_SYNC_INTERVAL) {
                    Toast.makeText(mContext,
                            "Minimal value is " + stringValue,
                            Toast.LENGTH_SHORT).show();
                    // Don't save the entered value.
                    return false;
                }
                else if (Integer.valueOf(stringValue) > MAX_SYNC_INTERVAL) {
                    Toast.makeText(mContext,
                            "Maximal value is " + stringValue,
                            Toast.LENGTH_SHORT).show();
                    // Don't save the entered value.
                    return false;
                }
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

        // Save preference change.
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

    private void updateLocation(Context context, String requestedLocation) {
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra(Intent.EXTRA_TEXT, requestedLocation);
        context.startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mQueryReceiver,
                new IntentFilter(LocationService.BROADCAST_EVENT));
    }

    // Handler for received intents for the "BROADCAST_EVENT" event
    private BroadcastReceiver mQueryReceiver = new BroadcastReceiver() {

        private final String LOG_TAG = BroadcastReceiver.class.getSimpleName();
        @Override
        public void onReceive(Context context, Intent intent) {
            // Intent has no data attached to it, nothing to extract
            Log.d(LOG_TAG, "Got message from location service.");

            String sortOrder = ToursContract.OSMEntry.COLUMN_QUERY_RELEVANCE + " ASC";

            Uri queryOSMUri = ToursContract.OSMEntry.CONTENT_URI;

            Cursor cursor = context.getContentResolver().query(
                    queryOSMUri,
                    OSM_COLUMNS,
                    null,
                    null,
                    sortOrder
            );

            // Put a new cursor and close the old one.
            mLocationAdapter.changeCursor(cursor);

            mLocationQueryPreference.show();
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mQueryReceiver);
        super.onPause();
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

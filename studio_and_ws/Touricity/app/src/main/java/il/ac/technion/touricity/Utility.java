package il.ac.technion.touricity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

public class Utility {

    private static final String LOGIN_TAG = "login_tag";

    public class ServerConfig {

        public static final String PROTOCOL = "http://";
        // TODO: change to server final IP
        public static final String SERVER_IP = "192.168.1.145";
//        public static final String SERVER_IP = "10.0.0.3";
//        public static final String SERVER_IP = "10.0.0.1";
        public static final String SERVER_PORT = "8080";
        public static final String SERVER_APP = "ToursAppServer";
        public static final String SERVER_SERVLET = "tours_slet";

        public static final String SERVER_BASE_URL = PROTOCOL + SERVER_IP + ":" + SERVER_PORT +
                "/" + SERVER_APP + "/" + SERVER_SERVLET;

        // TODO: implement what happens when there's a timeout
        private static final int READ_TIMEOUT = 10000;
        private static final int CONNECTION_TIMEOUT = 15000;
    }

    public static void setupHttpUrlConnection(HttpURLConnection urlConnection) {
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-type", "application/json");
            urlConnection.setReadTimeout(ServerConfig.READ_TIMEOUT);
            urlConnection.setConnectTimeout(ServerConfig.CONNECTION_TIMEOUT);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        }
    }

    public static Long getPreferredLocationId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getLong(context.getString(R.string.pref_location_id_key), -1);
    }

    public static String getPreferredLocationName(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.pref_location_name_key), "");
    }

    public static double getPreferredLocationLatitude(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return (double)sharedPref.getFloat(context.getString(R.string.pref_location_lat_key), 0);
    }

    public static double getPreferredLocationLongitude(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return (double)sharedPref.getFloat(context.getString(R.string.pref_location_long_key), 0);
    }

    public static void saveLocationToPreferences(Context context, long id, String name,
                                                   float latitude, float longitude) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.pref_location_id_key), id);
        editor.putString(context.getString(R.string.pref_location_name_key), name);
        editor.putFloat(context.getString(R.string.pref_location_lat_key), latitude);
        editor.putFloat(context.getString(R.string.pref_location_long_key), longitude);
        editor.commit();
    }

    public static boolean getIsLoggedIn(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.pref_user_is_logged_in_key), false);
    }

    public static String getLoggedInUsername(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.pref_user_name_key), "");
    }

    public static String getLoggedInUserPassword(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.pref_user_password_key), "");
    }

    public static void saveLoginSession(Context context, String username, String password) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.pref_user_is_logged_in_key), true);
        editor.putString(context.getString(R.string.pref_user_name_key), username);
        editor.putString(context.getString(R.string.pref_user_password_key), password);
        editor.commit();
    }

    public static void saveLogoutState(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.pref_user_is_logged_in_key), false);
        editor.remove(context.getString(R.string.pref_user_name_key));
        editor.remove(context.getString(R.string.pref_user_password_key));
        editor.commit();
    }

    /**
     * Helper method to tell if the location returned by Open Street Map service is a city, town,
     * village or hamlet. Generally, places can be administrative boundaries, counties and even
     * continents. Hence, we need to filter the results.
     * @param locationType from OpenStreetMap API response
     * @return true if the of the place is a city, town, village or hamlet, false otherwise.
     */
    public static boolean isPlace(String locationType) {
        if (locationType.equals("city") || locationType.equals("town")
                || locationType.equals("village") || locationType.equals("hamlet")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Helper method to provide the icon resource drawable according to the location type returned
     * by the OpenStreetMap call.
     * @param context Context to use for resource localization
     * @param locationType from OpenStreetMap API response
     * @return Drawable for the corresponding icon. null if no relation is found.
     * @see <a href="https://www.google.com/design/icons/index.html">Material icons site</a>.
     */
    public static int getIconResourceIdForLocationType(Context context, String locationType) {
        Drawable drawable = null;

        if (locationType.equals("city")) {
            return R.drawable.ic_city_24dp;
        } else if (locationType.equals("town")) {
            return R.drawable.ic_town_24dp;
        } else if (locationType.equals("village")) {
            return R.drawable.ic_village_24dp;
        } else if (locationType.equals("hamlet")) {
            return R.drawable.ic_hamlet_24dp;
        }

        return -1;
    }

    public static void showLoginDialog(FragmentActivity context) {
        // Create an instance of the dialog fragment and show it
        DialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.show(context.getSupportFragmentManager(), LOGIN_TAG);
    }
}

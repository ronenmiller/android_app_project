package il.ac.technion.touricity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.text.SimpleDateFormat;

public class Utility {

    // Package-shared
    static final String ABOUT_TAG = "about_tag";
    static final String LOGIN_TAG = "login_tag";
    static final String LOGOUT_TAG = "logout_tag";
    static final String DELETE_TOUR_TAG = "delete_tour_tag";
    static final String RESERVE_SLOT_TAG = "reserve_slot_tag";
    static final String DELETE_SLOT_TAG = "delete_slot_tag";
    static final String DELETE_RESERVATION_TAG = "delete_reservation_tag";
    static final String TIME_PICKER_TAG = "time_picker_tag";
    static final String DATE_PICKER_TAG = "date_picker_tag";
    static final String RATE_TAG = "rate_tag";

    public class ServerConfig {

        public static final String PROTOCOL = "http://";
        // TODO: change to server final IP
        // Liron's parents home
//        public static final String SERVER_IP = "192.168.1.145";
        // Ori's home
//        public static final String SERVER_IP = "10.100.102.8";
        // Liron's apartment
        public static final String SERVER_IP = "10.0.0.6";
        // Ronen's apartment
//        public static final String SERVER_IP = "10.0.0.1";
        // Technion
//        public static final String SERVER_IP = "132.68.50.169";
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

    public static long getPreferredLocationId(Context context) {
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
        editor.apply();
    }

    public static boolean getIsLoggedIn(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.pref_user_is_logged_in_key), false);
    }

    public static String getLoggedInUserId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.pref_user_id_key), null);
    }

    public static boolean getLoggedInUserIsGuide(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.pref_user_is_guide_key), false);
    }

    public static void saveLoginSession(Context context, String userId, boolean isGuide) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.pref_user_is_logged_in_key), true);
        editor.putString(context.getString(R.string.pref_user_id_key), userId);
        editor.putBoolean(context.getString(R.string.pref_user_is_guide_key), isGuide);
        editor.apply();
    }

    public static void saveLogoutState(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.pref_user_is_logged_in_key), false);
        editor.remove(context.getString(R.string.pref_user_id_key));
        editor.remove(context.getString(R.string.pref_user_is_guide_key));
        editor.apply();
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
     * @param locationType from OpenStreetMap API response
     * @return Drawable id for the corresponding icon. -1 if no relation is found.
     * @see <a href="https://www.google.com/design/icons/index.html">Material icons site</a>.
     */
    public static int getIconResourceIdForLocationType(String locationType, boolean isLocationInHistory) {
        if (isLocationInHistory) {
            if (locationType.equals("city")) {
                return R.drawable.ic_city_teal_24dp;
            } else if (locationType.equals("town")) {
                return R.drawable.ic_town_teal_24dp;
            } else if (locationType.equals("village")) {
                return R.drawable.ic_village_teal_24dp;
            } else if (locationType.equals("hamlet")) {
                return R.drawable.ic_hamlet_teal_24dp;
            }
        }
        else {
            if (locationType.equals("city")) {
                return R.drawable.ic_city_24dp;
            } else if (locationType.equals("town")) {
                return R.drawable.ic_town_24dp;
            } else if (locationType.equals("village")) {
                return R.drawable.ic_village_24dp;
            } else if (locationType.equals("hamlet")) {
                return R.drawable.ic_hamlet_24dp;
            }
        }

        return -1;
    }

    public static String convertFirstLetterToUppercase(String s) {
        s = s.toUpperCase().charAt(0) + s.toLowerCase().substring(1, s.length());
        return s;
    }

    public static int getLanguageIconIdForLanguageId(int languageId) {
        if (languageId == 1) {
            return R.drawable.flag_usa;
        } else if (languageId == 2) {
            return R.drawable.flag_spain;
        } else if (languageId == 3) {
            return R.drawable.flag_france;
        } else if (languageId == 4) {
            return R.drawable.flag_germany;
        } else if (languageId == 5) {
            return R.drawable.flag_italy;
        } else if (languageId == 6) {
            return R.drawable.flag_portugal;
        } else if (languageId == 7) {
            return R.drawable.flag_china;
        } else if (languageId == 8) {
            return R.drawable.flag_israel;
        }

        return -1;
    }

    public static void showAboutDialog(FragmentActivity context) {
        // Create an instance of the dialog fragment and show it
        DialogFragment aboutDialogFragment = new AboutDialogFragment();
        aboutDialogFragment.show(context.getSupportFragmentManager(), ABOUT_TAG);
    }

    public static void showLoginDialog(FragmentActivity context) {
        // Create an instance of the dialog fragment and show it
        DialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.show(context.getSupportFragmentManager(), LOGIN_TAG);
    }

    public static void showLogoutDialog(FragmentActivity context) {
        // Create an instance of the dialog fragment and show it
        DialogFragment logoutDialogFragment = new LogoutDialogFragment();
        logoutDialogFragment.show(context.getSupportFragmentManager(), LOGOUT_TAG);
    }

    public static void showDeleteTourDialog(FragmentActivity context, Uri uri) {
        // Create an instance of the dialog fragment and show it
        DialogFragment deleteTourDialogFragment = DeleteTourDialogFragment.newInstance(uri);
        deleteTourDialogFragment.show(context.getSupportFragmentManager(), DELETE_TOUR_TAG);
    }

    public static void showReserveSlotDialog(FragmentActivity context, Uri uri) {
        // Create an instance of the dialog fragment and show it
        DialogFragment reserveSlotDialogFragment = ReserveSlotDialogFragment.newInstance(uri);
        reserveSlotDialogFragment.show(context.getSupportFragmentManager(), RESERVE_SLOT_TAG);
    }

    public static void showDeleteSlotDialog(FragmentActivity context, Uri uri) {
        // Create an instance of the dialog fragment and show it
        DialogFragment deleteSlotDialogFragment = DeleteSlotDialogFragment.newInstance(uri);
        deleteSlotDialogFragment.show(context.getSupportFragmentManager(), DELETE_SLOT_TAG);
    }

    public static void showDeleteReservationDialog(FragmentActivity context, Uri uri) {
        // Create an instance of the dialog fragment and show it
        DialogFragment deleteReservationDialogFragment = DeleteReservationDialogFragment.newInstance(uri);
        deleteReservationDialogFragment.show(context.getSupportFragmentManager(), DELETE_RESERVATION_TAG);
    }

    public static void showDatePickerDialog(FragmentActivity context) {
        // Create an instance of the dialog fragment and show it
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(context.getSupportFragmentManager(), DATE_PICKER_TAG);
    }

    public static void showTimePickerDialog(FragmentActivity context) {
        // Create an instance of the dialog fragment and show it
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(context.getSupportFragmentManager(), TIME_PICKER_TAG);
    }

    public static void showRatingDialog(FragmentActivity context, int tourId, String guideId) {
        // Create an instance of the dialog fragment and show it
        DialogFragment ratingDialogFragment = RatingDialogFragment.newInstance(tourId, guideId);
        ratingDialogFragment.show(context.getSupportFragmentManager(), RATE_TAG);
    }

    public static String getFriendlyTimeString(long timeInMillis) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("HH:mm");
        return shortenedDateFormat.format(timeInMillis);
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon, Jun 8, 2015"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay < currentJulianDay) {
            // For past dates, use the form "Mon Jun 3, 2015"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
            return shortenedDateFormat.format(dateInMillis);
        } else if (julianDay < currentJulianDay + 7) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3, 2015"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }
}

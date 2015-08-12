package il.ac.technion.touricity.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import il.ac.technion.touricity.data.ToursContract;

public class LocationService extends IntentService {

    private final String LOG_TAG = LocationService.class.getSimpleName();

    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String locationQuery = intent.getStringExtra(Intent.EXTRA_TEXT);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String locationsJsonStr = null;

        String format = "json";
        String featureType = "city";
        int resultsLimit = 10;
        String language = "en";
        // TODO: consider removing this parameter on production
        String email = "sesami.open@gmail.com";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://nominatim.openstreetmap.org/search?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "format";
            final String FEATURE_PARAM = "featureType";
            final String LIMIT_PARAM = "limit";
            final String LANGUAGE_PARAM = "accept-language";
            final String EMAIL_PARAM = "email";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(FEATURE_PARAM, featureType)
                    .appendQueryParameter(LIMIT_PARAM, Integer.toString(resultsLimit))
                    .appendQueryParameter(LANGUAGE_PARAM, language)
                    .appendQueryParameter(EMAIL_PARAM, email)
                            .build();

            Log.v(LOG_TAG, "URL: " + builtUri.toString());

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            locationsJsonStr = buffer.toString();

            Log.v(LOG_TAG, "Json: " + locationsJsonStr);
            getLocationsDataFromJson(locationsJsonStr, locationQuery);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public void getLocationsDataFromJson(String locationsJsonStr,
                                         String locationQuery  )
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OSM_ID = "osm_id";
        final String OSM_NAME = "display_name";
        final String OSM_CLASS = "class";

        // Location coordinate
        final String OSM_LATITUDE = "lat";
        final String OSM_LONGITUDE = "lon";

        try {
            JSONArray locationsArray = new JSONArray(locationsJsonStr);

            // Store new locations in order for the user to select his desired location
            ArrayList<ContentValues> locationsList = new ArrayList<>();

            for(int i = 0; i < locationsArray.length(); i++) {
                // These are the values that will be collected.
                long locationId;
                String locationName;
                String locationClass;
                double locationLatitude;
                double locationLongitude;

                // Get the JSON object representing the location
                JSONObject locationObject = locationsArray.getJSONObject(i);

                locationClass = locationObject.getString(OSM_CLASS);
                if (locationClass.equals("place")) {
                    // get requested elements from json
                    locationId = locationObject.getLong(OSM_ID);
                    locationName = locationObject.getString(OSM_NAME);
                    locationLatitude = locationObject.getDouble(OSM_LATITUDE);
                    locationLongitude = locationObject.getDouble(OSM_LONGITUDE);

                    ContentValues locationValues = new ContentValues();

                    locationValues.put(ToursContract.LocationEntry._ID, locationId);
                    locationValues.put(ToursContract.LocationEntry.COLUMN_LOCATION_NAME, locationName);
                    locationValues.put(ToursContract.LocationEntry.COLUMN_COORD_LAT, locationLatitude);
                    locationValues.put(ToursContract.LocationEntry.COLUMN_COORD_LONG, locationLongitude);

                    locationsList.add(locationValues);
                }
            }

            // TODO: let the user choose which location does he mean from ArrayList<ContentV. Implement the next
            // TODO: lines in a function after returning an ArrayList<ContentValues> to the user
            // TODO: uncomment after content resolver implementation
            // add to database
//            long locationId = addLocation(locationQuery, cityName, cityLatitude, cityLongitude);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    // TODO: fix this function
//    long addLocation(String locationSetting, String cityName, double lat, double lon) {
//        // Students: First, check if the location with this city name exists in the db
//        // If it exists, return the current ID
//        // Otherwise, insert it using the content resolver and the base URI
//        final String locationSettingSelection =
//                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
//        final String[] locationSelectionArgs = {locationSetting};
//        long locationId;
//
//        ContentResolver resolver = this.getContentResolver();
//        Cursor cursor = null;
//        try {
//            cursor = resolver.query(
//                    WeatherContract.LocationEntry.CONTENT_URI,
//                    null,
//                    locationSettingSelection,
//                    locationSelectionArgs,
//                    null);
//
//            if (cursor.moveToFirst()) {
//                // the location already exists in the database
//                locationId = cursor.getLong(cursor.getColumnIndex(WeatherContract.LocationEntry._ID));
//            } else {
//                ContentValues values = new ContentValues();
//                values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
//                values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
//                values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
//                values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
//                Uri returnUri = resolver.insert(WeatherContract.LocationEntry.CONTENT_URI, values);
//                locationId = Long.valueOf
//                        (WeatherContract.WeatherEntry.getLocationSettingFromUri(returnUri));
//            }
//        }
//        finally {
//            cursor.close();
//        }
//
//        return locationId;
//    }
}
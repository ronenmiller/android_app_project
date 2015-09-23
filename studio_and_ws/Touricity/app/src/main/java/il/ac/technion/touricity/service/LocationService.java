package il.ac.technion.touricity.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
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

import il.ac.technion.touricity.MainFragment;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class LocationService extends IntentService {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

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

        // Liron's key. Limited to 15,000 transactions a month.
        String key = "uhAfC4lns0tussoDZNMrdrGJaNp17WAR";
        String format = "json";
        int resultsLimit = 6;
        String language = "en";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String LOCATION_BASE_URL =
                    "http://open.mapquestapi.com/nominatim/v1/search.php?";
            final String KEY_PARAM = "key";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "format";
            final String LIMIT_PARAM = "limit";
            final String LANGUAGE_PARAM = "accept-language";

            Uri builtUri = Uri.parse(LOCATION_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, key)
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(LIMIT_PARAM, Integer.toString(resultsLimit))
                    .appendQueryParameter(LANGUAGE_PARAM, language)
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
        final String OSM_TYPE = "type";

        // Location coordinate
        final String OSM_LATITUDE = "lat";
        final String OSM_LONGITUDE = "lon";

        try {
            JSONArray locationsArray = new JSONArray(locationsJsonStr);

            // Store new locations in order for the user to select his desired location
            ArrayList<ContentValues> cvArrayList = new ArrayList<>();

            for(int i = 0; i < locationsArray.length(); i++) {
                // These are the values that will be collected.
                long locationId;
                String locationName;
                String locationType;
                double locationLatitude;
                double locationLongitude;

                // Get the JSON object representing the location
                JSONObject locationObject = locationsArray.getJSONObject(i);

                locationType = locationObject.getString(OSM_TYPE);
                if (Utility.isPlace(locationType)) {
                    // get requested elements from json
                    locationId = locationObject.getLong(OSM_ID);
                    locationName = locationObject.getString(OSM_NAME);
                    locationLatitude = locationObject.getDouble(OSM_LATITUDE);
                    locationLongitude = locationObject.getDouble(OSM_LONGITUDE);

                    ContentValues locationValues = new ContentValues();

                    locationValues.put(ToursContract.OSMEntry.COLUMN_LOCATION_ID, locationId);
                    locationValues.put(ToursContract.OSMEntry.COLUMN_LOCATION_NAME, locationName);
                    locationValues.put(ToursContract.OSMEntry.COLUMN_LOCATION_TYPE, locationType);
                    locationValues.put(ToursContract.OSMEntry.COLUMN_COORD_LAT, locationLatitude);
                    locationValues.put(ToursContract.OSMEntry.COLUMN_COORD_LONG, locationLongitude);

                    cvArrayList.add(locationValues);
                }
            }

            // Add to the database
            int inserted = 0;
            if ( cvArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvArrayList.size()];
                cvArrayList.toArray(cvArray);
                inserted = this.getContentResolver().bulkInsert(
                        ToursContract.OSMEntry.CONTENT_URI,
                        cvArray
                );
            }

            sendBroadcast();

            Log.d(LOG_TAG, "Location service complete. " + inserted + " rows inserted to OSM table.");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // Send an Intent with an action named BROADCAST_LOCATION_SERVICE_DONE.
    private void sendBroadcast() {
        Intent intent = new Intent(MainFragment.BROADCAST_LOCATION_SERVICE_DONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
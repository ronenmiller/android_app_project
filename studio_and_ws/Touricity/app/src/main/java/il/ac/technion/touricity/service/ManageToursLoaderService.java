package il.ac.technion.touricity.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import il.ac.technion.touricity.ManageToursFragment;
import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class ManageToursLoaderService extends IntentService {

    private static final String LOG_TAG = ManageToursLoaderService.class.getSimpleName();

    private static final String[] TOUR_ID_COLUMNS = new String[] {
            ToursContract.TourEntry.TABLE_NAME + "." + ToursContract.TourEntry._ID
    };

    private static final int COL_TOUR_ID = 0;

    public ManageToursLoaderService() {
        super("ManageToursLoaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String managerId = Utility.getLoggedInUserId(getApplicationContext());

        // Bail out.
        if (managerId == null) {
            return;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String responseMessageJsonStr = null;

        try {
            // Construct the URL for the touricity server query
            URL url = new URL(Utility.ServerConfig.SERVER_BASE_URL);

            // Open and configure the connection.
            urlConnection = (HttpURLConnection) url.openConnection();
            Utility.setupHttpUrlConnection(urlConnection);

            // Create a message to be delivered to the server.
            Map<String, String> map = new HashMap<>();
            map.put(Message.MessageKeys.TOUR_MANAGER_KEY, managerId);
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.QUERY_TOURS_BY_MANAGER_ID, jsonObject.toString());
            Gson gson = new Gson();
            String requestMessageJsonStr = gson.toJson(message);

            Log.v(LOG_TAG, requestMessageJsonStr);

            // Attach the message to the connection.
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(requestMessageJsonStr);
            writer.flush();
            writer.close();
            outputStream.close();

            // Transmit request to the server.
            urlConnection.connect();

            // Receive response from the server, according to the request, in the form of a message.
            InputStream inputStream = urlConnection.getInputStream();
            // Read the response into a String
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                throw new NullPointerException("Something went wrong");
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

            // The response message in a string format.
            responseMessageJsonStr = buffer.toString();
            Log.v(LOG_TAG, responseMessageJsonStr);

            // Extract tours from message
            Message responseMessage = gson.fromJson(responseMessageJsonStr, Message.class);
            String toursJsonStr = responseMessage.getMessageJson();
            getTourDataFromJson(toursJsonStr, managerId);
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
     * Take the String representing the matching tours in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public void getTourDataFromJson(String toursJsonStr, String managerId) throws JSONException {

        // Now we have a String representing the matching tours in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        try {
            JSONArray toursArray = new JSONArray(toursJsonStr);

            // Avoid crash.
            if (toursArray.isNull(0)) {
                Log.d(LOG_TAG, "ManageToursLoaderService complete. No tours found for this manager.");
                sendBroadcast();
                return;
            }

            // Insert the new tours information into the database.
            ArrayList<ContentValues> cvToursArrayList = new ArrayList<>(toursArray.length());
            ArrayList<ContentValues> cvLocationsArrayList = new ArrayList<>();
            for(int i = 0; i < toursArray.length(); i++) {
                // These are the values that will be collected.
                long osmId;
                String locationName;
                String locationType;
                double locationLatitude;
                double locationLongitude;

                int tourId;

                String title;
                // Duration is in minutes.
                int duration;
                int language;
                String location;
                double rating;
                String description;

                // TODO: figure these out.
                Object photos;
                String comments;

                // Get the JSON object representing the day
                JSONObject tourObject = toursArray.getJSONObject(i);
                osmId = tourObject.getLong(Message.MessageKeys.TOUR_OSM_ID);
                locationName = tourObject.getString(Message.MessageKeys.LOCATION_OSM_NAME_KEY);
                locationType = tourObject.getString(Message.MessageKeys.LOCATION_OSM_TYPE_KEY);
                locationLatitude = tourObject.getDouble(Message.MessageKeys.LOCATION_OSM_LATITUDE_KEY);
                locationLongitude = tourObject.getDouble(Message.MessageKeys.LOCATION_OSM_LONGITUDE_KEY);

                ContentValues locationValues = new ContentValues();

                locationValues.put(ToursContract.LocationEntry.COLUMN_LOCATION_ID, osmId);
                locationValues.put(ToursContract.LocationEntry.COLUMN_LOCATION_NAME, locationName);
                locationValues.put(ToursContract.LocationEntry.COLUMN_LOCATION_TYPE, locationType);
                locationValues.put(ToursContract.LocationEntry.COLUMN_COORD_LAT, locationLatitude);
                locationValues.put(ToursContract.LocationEntry.COLUMN_COORD_LONG, locationLongitude);

                cvLocationsArrayList.add(locationValues);

                tourId = tourObject.getInt(Message.MessageKeys.TOUR_ID_KEY);
                title = tourObject.getString(Message.MessageKeys.TOUR_TITLE_KEY);
                duration = tourObject.getInt(Message.MessageKeys.TOUR_DURATION_KEY);
                language = tourObject.getInt(Message.MessageKeys.TOUR_LANGUAGE_KEY);
                location = tourObject.getString(Message.MessageKeys.TOUR_LOCATION_KEY);
                rating = tourObject.getDouble(Message.MessageKeys.TOUR_RATING_KEY);
                description = tourObject.getString(Message.MessageKeys.TOUR_DESCRIPTION_KEY);

                ContentValues tourValues = new ContentValues();

                tourValues.put(ToursContract.TourEntry._ID, tourId);
                tourValues.put(ToursContract.TourEntry.COLUMN_OSM_ID, osmId);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_MANAGER_ID, managerId);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_TITLE, title);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_DURATION, duration);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE, language);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_LOCATION, location);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_RATING, rating);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_AVAILABLE, 1);
                // TODO: retrieve tour description together with photos and comments only when clicking on a specific tour
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_DESCRIPTION, description);

                cvToursArrayList.add(tourValues);
            }

            // Add locations to the database.
            int inserted = 0;
            if ( cvLocationsArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvLocationsArrayList.size()];
                cvLocationsArrayList.toArray(cvArray);
                inserted = getContentResolver().bulkInsert(
                        ToursContract.LocationEntry.CONTENT_URI,
                        cvArray
                );

            }

            Log.d(LOG_TAG, inserted + " locations inserted to the local database.");

            // Delete tours which have no open slots, and are marked as unavailable in the server,
            // so we don't build up an endless history.
            // That way, old tours will be deleted, and new tours, for which we didn't query
            // the slots table yet (and therefore don't have any slots open) will be deleted
            // and added again next (that's why the insertion is after the deletion).
            ContentResolver resolver = getContentResolver();

            Cursor toursCursor = null;
            Cursor slotsCursor = null;
            try {
                // Find tours
                toursCursor = resolver.query(
                        ToursContract.TourEntry.buildTourManagerUri(managerId),
                        TOUR_ID_COLUMNS,
                        null,
                        null,
                        null
                );
                if (toursCursor != null) {
                    while (toursCursor.moveToNext()) {
                        int tourID = toursCursor.getInt(COL_TOUR_ID);
                        String slotSelection = ToursContract.SlotEntry.TABLE_NAME +
                                "." + ToursContract.SlotEntry.COLUMN_SLOT_TOUR_ID +
                                " = ?";
                        String[] slotSelectionArgs = new String[]{Integer.toString(tourID)};

                        slotsCursor = resolver.query(
                                ToursContract.SlotEntry.CONTENT_URI,
                                null,
                                slotSelection,
                                slotSelectionArgs,
                                null
                        );

                        // If the cursor is empty, i.e. there are no open slots for this tour,
                        // then delete the tour from the database.
                        if (slotsCursor != null && !slotsCursor.moveToFirst()) {
                            String tourSelection = ToursContract.TourEntry.TABLE_NAME +
                                    "." + ToursContract.TourEntry._ID +
                                    " = ?";

                            resolver.delete(
                                    ToursContract.TourEntry.CONTENT_URI,
                                    tourSelection,
                                    slotSelectionArgs
                            );
                        }
                    }
                }
            }
            finally {
                if (slotsCursor != null) {
                    slotsCursor.close();
                }
                if (toursCursor != null) {
                    toursCursor.close();
                }
            }

            // Add tours to the database.
            inserted = 0;
            if ( cvToursArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvToursArrayList.size()];
                cvToursArrayList.toArray(cvArray);
                inserted = getContentResolver().bulkInsert(
                        ToursContract.TourEntry.CONTENT_URI,
                        cvArray
                );

            }

            Log.d(LOG_TAG, "ManageToursLoaderService complete. " + inserted + " tours inserted to the local database.");

            sendBroadcast();

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // Send an Intent with an action named BROADCAST_MANAGE_TOURS_LOADER_SERVICE_DONE.
    private void sendBroadcast() {
        Intent intent = new Intent(ManageToursFragment.BROADCAST_MANAGE_TOURS_LOADER_SERVICE_DONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
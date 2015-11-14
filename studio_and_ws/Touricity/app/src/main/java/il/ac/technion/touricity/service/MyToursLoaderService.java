package il.ac.technion.touricity.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
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

import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.MyToursFragment;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class MyToursLoaderService extends IntentService {

    private static final String LOG_TAG = MyToursLoaderService.class.getSimpleName();

    public MyToursLoaderService() {
        super("MyToursLoaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String userId = Utility.getLoggedInUserId(getApplicationContext());

        // Bail out.
        if (userId == null) {
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
            map.put(Message.MessageKeys.USER_ID_KEY, userId);
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.QUERY_MY_RESERVATIONS, jsonObject.toString());
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
            String reservationsJsonStr = responseMessage.getMessageJson();
            getReservationDataFromJson(reservationsJsonStr, userId);
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
    public void getReservationDataFromJson(String slotsJsonStr, String userId) throws JSONException {

        // Now we have a String representing the matching tours in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        try {
            JSONArray reservationsArray = new JSONArray(slotsJsonStr);

            // Avoid crash.
            if (reservationsArray.isNull(0)) {
                Log.d(LOG_TAG, "MyReservationsLoaderService complete. No reservations found for this user.");
                sendBroadcast();
                return;
            }

            // Insert the new tours information into the database.
            ArrayList<ContentValues> cvReservationsArrayList = new ArrayList<>(reservationsArray.length());
            ArrayList<ContentValues> cvSlotsArrayList = new ArrayList<>(reservationsArray.length());
            ArrayList<ContentValues> cvToursArrayList = new ArrayList<>();
            ArrayList<ContentValues> cvLocationsArrayList = new ArrayList<>();
            ArrayList<ContentValues> cvGuidesArrayList = new ArrayList<>();
            for(int i = 0; i < reservationsArray.length(); i++) {
                // These are the values that will be collected.
                String guideId;
                String guideName;
                String guideEmail;
                float guideRating;

                long osmId;
                String locationName;
                String locationType;
                double locationLatitude;
                double locationLongitude;

                int tourId;
                String managerId;
                String title;
                // Duration is in minutes.
                int duration;
                int language;
                String location;
                float rating;
                boolean available;
                String description;
                // TODO: figure these out.
                Object photos;
                String comments;

                long slotId;
                int slotDate;
                long slotTime;
                int currentCapacity;
                int totalCapacity;
                boolean slotActive;
                boolean slotCanceled;

                int reservationParticipants;
                boolean reservationActive;

                // Get the JSON object representing the day
                JSONObject reservationObject = reservationsArray.getJSONObject(i);

                guideId = reservationObject.getString(Message.MessageKeys.SLOT_GUIDE_ID_KEY);
                guideName = reservationObject.getString(Message.MessageKeys.USER_NAME_KEY);
                guideEmail = reservationObject.getString(Message.MessageKeys.USER_EMAIL_KEY);
                if (reservationObject.isNull(Message.MessageKeys.USER_RATING_KEY)) {
                    guideRating = 0;
                }
                else {
                    guideRating = (float)reservationObject.getDouble(Message.MessageKeys.USER_RATING_KEY);
                }

                ContentValues guideValues = new ContentValues();

                guideValues.put(ToursContract.UserEntry._ID, guideId);
                guideValues.put(ToursContract.UserEntry.COLUMN_USER_NAME, guideName);
                guideValues.put(ToursContract.UserEntry.COLUMN_USER_EMAIL, guideEmail);
                guideValues.put(ToursContract.UserEntry.COLUMN_USER_RATING, guideRating);

                cvGuidesArrayList.add(guideValues);

                osmId = reservationObject.getLong(Message.MessageKeys.TOUR_OSM_ID);
                locationName = reservationObject.getString(Message.MessageKeys.LOCATION_OSM_NAME_KEY);
                locationType = reservationObject.getString(Message.MessageKeys.LOCATION_OSM_TYPE_KEY);
                locationLatitude = reservationObject.getDouble(Message.MessageKeys.LOCATION_OSM_LATITUDE_KEY);
                locationLongitude = reservationObject.getDouble(Message.MessageKeys.LOCATION_OSM_LONGITUDE_KEY);

                ContentValues locationValues = new ContentValues();

                locationValues.put(ToursContract.LocationEntry.COLUMN_LOCATION_ID, osmId);
                locationValues.put(ToursContract.LocationEntry.COLUMN_LOCATION_NAME, locationName);
                locationValues.put(ToursContract.LocationEntry.COLUMN_LOCATION_TYPE, locationType);
                locationValues.put(ToursContract.LocationEntry.COLUMN_COORD_LAT, locationLatitude);
                locationValues.put(ToursContract.LocationEntry.COLUMN_COORD_LONG, locationLongitude);

                cvLocationsArrayList.add(locationValues);

                tourId = reservationObject.getInt(Message.MessageKeys.TOUR_ID_KEY);
                managerId = reservationObject.getString(Message.MessageKeys.TOUR_MANAGER_KEY);
                title = reservationObject.getString(Message.MessageKeys.TOUR_TITLE_KEY);
                duration = reservationObject.getInt(Message.MessageKeys.TOUR_DURATION_KEY);
                language = reservationObject.getInt(Message.MessageKeys.TOUR_LANGUAGE_KEY);
                location = reservationObject.getString(Message.MessageKeys.TOUR_LOCATION_KEY);
                rating = (float)reservationObject.getDouble(Message.MessageKeys.TOUR_RATING_KEY);
                available = reservationObject.getBoolean(Message.MessageKeys.TOUR_AVAILABLE_KEY);
                description = reservationObject.getString(Message.MessageKeys.TOUR_DESCRIPTION_KEY);

                ContentValues tourValues = new ContentValues();

                tourValues.put(ToursContract.TourEntry._ID, tourId);
                tourValues.put(ToursContract.TourEntry.COLUMN_OSM_ID, osmId);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_MANAGER_ID, managerId);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_TITLE, title);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_DURATION, duration);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE, language);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_LOCATION, location);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_RATING, rating);
                if (available) {
                    tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_AVAILABLE, 1);
                }
                else {
                    tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_AVAILABLE, 0);
                }
                // TODO: retrieve tour description together with photos and comments only when clicking on a specific tour
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_DESCRIPTION, description);

                cvToursArrayList.add(tourValues);

                slotId = reservationObject.getLong(Message.MessageKeys.SLOT_ID_KEY);
                slotDate = reservationObject.getInt(Message.MessageKeys.SLOT_DATE_KEY);
                slotTime = reservationObject.getLong(Message.MessageKeys.SLOT_TIME_KEY);
                currentCapacity = reservationObject.getInt(Message.MessageKeys.SLOT_CURRENT_CAPACITY_KEY);
                totalCapacity = reservationObject.getInt(Message.MessageKeys.SLOT_TOTAL_CAPACITY_KEY);
                slotActive = reservationObject.getBoolean(Message.MessageKeys.SLOT_ACTIVE_KEY);
                slotCanceled = reservationObject.getBoolean(Message.MessageKeys.SLOT_CANCELED_KEY);

                ContentValues slotValues = new ContentValues();

                slotValues.put(ToursContract.SlotEntry._ID, slotId);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_GUIDE_ID, guideId);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_TOUR_ID, tourId);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_DATE, slotDate);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_TIME, slotTime);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_CURRENT_CAPACITY, currentCapacity);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_TOTAL_CAPACITY, totalCapacity);
                if (slotActive) {
                    slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_ACTIVE, 1);
                }
                else {
                    slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_ACTIVE, 0);
                }
                if (slotCanceled) {
                    slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_CANCELED, 1);
                }
                else {
                    slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_CANCELED, 0);
                }


                cvSlotsArrayList.add(slotValues);

                reservationParticipants = reservationObject.getInt(Message.MessageKeys.RESERVATION_OCCUPIED_KEY);
                reservationActive = reservationObject.getBoolean(Message.MessageKeys.RESERVATION_ACTIVE_KEY);

                ContentValues reservationValues = new ContentValues();

                reservationValues.put(ToursContract.ReservationEntry._ID, slotId);
                reservationValues.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_USER_ID, userId);
                reservationValues.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_PARTICIPANTS, reservationParticipants);
                if (reservationActive) {
                    reservationValues.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_ACTIVE, 1);
                }
                else {
                    reservationValues.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_ACTIVE, 0);
                }

                cvReservationsArrayList.add(reservationValues);
            }

            // Add guides to the database.
            int inserted = 0;
            if ( cvGuidesArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvGuidesArrayList.size()];
                cvGuidesArrayList.toArray(cvArray);
                inserted = getContentResolver().bulkInsert(
                        ToursContract.UserEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, inserted + " guides inserted to the local database.");

            // Add locations to the database.
            inserted = 0;
            if ( cvLocationsArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvLocationsArrayList.size()];
                cvLocationsArrayList.toArray(cvArray);
                inserted = getContentResolver().bulkInsert(
                        ToursContract.LocationEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, inserted + " locations inserted to the local database.");

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

            Log.d(LOG_TAG, inserted + " tours inserted to the local database.");

            // Add slots to the database.
            inserted = 0;
            if ( cvSlotsArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvSlotsArrayList.size()];
                cvSlotsArrayList.toArray(cvArray);
                inserted = getContentResolver().bulkInsert(
                        ToursContract.SlotEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, inserted + " slots inserted to the local database.");

            // Add reservations to the database.
            inserted = 0;
            if ( cvReservationsArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvReservationsArrayList.size()];
                cvReservationsArrayList.toArray(cvArray);
                inserted = getContentResolver().bulkInsert(
                        ToursContract.ReservationEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, "MyToursLoaderService complete. " + inserted + " reservations inserted to the local database.");

            sendBroadcast();

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // Send an Intent with an action named BROADCAST_MY_TOURS_LOADER_SERVICE_DONE.
    private void sendBroadcast() {
        Intent intent = new Intent(MyToursFragment.BROADCAST_MY_TOURS_LOADER_SERVICE_DONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
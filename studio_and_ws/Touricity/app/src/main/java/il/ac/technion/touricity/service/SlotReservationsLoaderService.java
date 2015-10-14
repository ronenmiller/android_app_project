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
import il.ac.technion.touricity.SlotReservationsFragment;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class SlotReservationsLoaderService extends IntentService {

    private static final String LOG_TAG = SlotReservationsLoaderService.class.getSimpleName();

    public SlotReservationsLoaderService() {
        super("SlotReservationsLoaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        long slotId = intent.getLongExtra(SlotReservationsFragment.INTENT_EXTRA_SLOT_ID, -1L);

        // Bail out.
        if (slotId == -1L) {
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
            Map<String, Long> map = new HashMap<>();
            map.put(Message.MessageKeys.SLOT_ID_KEY, slotId);
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.QUERY_RESERVATIONS_BY_SLOT_ID, jsonObject.toString());
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
            getReservationsDataFromJson(reservationsJsonStr, slotId);
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
    public void getReservationsDataFromJson(String reservationsJsonStr, long slotId) throws JSONException {

        // Now we have a String representing the matching tours in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        try {
            JSONArray reservationsArray = new JSONArray(reservationsJsonStr);

            // Avoid crash.
            if (reservationsArray.isNull(0)) {
                Log.d(LOG_TAG, "SlotReservationsLoaderService complete. No reservations found for this slot.");
                sendBroadcast();
                return;
            }

            // Insert the new tours information into the database.
            ArrayList<ContentValues> cvReservationsArrayList = new ArrayList<>(reservationsArray.length());
            ArrayList<ContentValues> cvUsersArrayList = new ArrayList<>(reservationsArray.length());
            for(int i = 0; i < reservationsArray.length(); i++) {
                // These are the values that will be collected.
                String userId;
                String username;
                String email;
                String userRating;

                int numReserved;

                // Get the JSON object representing the day
                JSONObject reservationObject = reservationsArray.getJSONObject(i);
                userId = reservationObject.getString(Message.MessageKeys.USER_ID_KEY);
                username = reservationObject.getString(Message.MessageKeys.USER_NAME_KEY);
                email = reservationObject.getString(Message.MessageKeys.USER_EMAIL_KEY);
                // Rating can be null for non-guides.
                userRating = reservationObject.getString(Message.MessageKeys.USER_RATING_KEY);

                ContentValues userValues = new ContentValues();

                userValues.put(ToursContract.UserEntry._ID, userId);
                userValues.put(ToursContract.UserEntry.COLUMN_USER_NAME, username);
                userValues.put(ToursContract.UserEntry.COLUMN_USER_EMAIL, email);
                if (!reservationObject.isNull(Message.MessageKeys.USER_RATING_KEY)) {
                    userRating = reservationObject.getString(Message.MessageKeys.USER_RATING_KEY);
                    userValues.put(ToursContract.UserEntry.COLUMN_USER_RATING, userRating);
                }

                cvUsersArrayList.add(userValues);

                numReserved = reservationObject.getInt(Message.MessageKeys.RESERVATION_OCCUPIED_KEY);

                ContentValues reservationValues = new ContentValues();

                reservationValues.put(ToursContract.ReservationEntry._ID, slotId);
                reservationValues.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_USER_ID, userId);
                reservationValues.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_PARTICIPANTS, numReserved);
                reservationValues.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_ACTIVE, 1);

                cvReservationsArrayList.add(reservationValues);
            }

            // Add users to the database.
            int usersInserted = 0;
            if ( cvUsersArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvUsersArrayList.size()];
                cvUsersArrayList.toArray(cvArray);
                usersInserted = getContentResolver().bulkInsert(
                        ToursContract.UserEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, usersInserted + " users inserted to the local database.");

            // Add reservations to the database.
            int reservationsInserted = 0;
            if ( cvReservationsArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvReservationsArrayList.size()];
                cvReservationsArrayList.toArray(cvArray);
                reservationsInserted = getContentResolver().bulkInsert(
                        ToursContract.ReservationEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, reservationsInserted + " reservations inserted to the local database.");

            if (usersInserted == reservationsInserted) {
                Log.d(LOG_TAG, "SlotReservationsLoaderService complete.");
            }
            else {
                Log.d(LOG_TAG, "SlotReservationsLoaderService failed! " +
                        "The number of users inserted is different than the number of reservations inserted.");
            }

            sendBroadcast();

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // Send an Intent with an action named BROADCAST_SLOT_RESERVATIONS_LOADER_SERVICE_DONE.
    private void sendBroadcast() {
        Intent intent = new Intent(SlotReservationsFragment.BROADCAST_SLOT_RESERVATIONS_LOADER_SERVICE_DONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
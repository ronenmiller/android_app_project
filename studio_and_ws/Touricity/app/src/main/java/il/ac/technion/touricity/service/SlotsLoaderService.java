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

import il.ac.technion.touricity.DetailFragment;
import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class SlotsLoaderService extends IntentService {

    private static final String LOG_TAG = SlotsLoaderService.class.getSimpleName();

    private int mButtonClicked;

    public SlotsLoaderService() {
        super("SlotsLoaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        int tourId = intent.getIntExtra(DetailFragment.INTENT_EXTRA_TOUR_ID, -1);
        mButtonClicked = intent.getIntExtra(DetailFragment.INTENT_EXTRA_BTN_ID, -1);

        if (tourId == -1 || mButtonClicked == -1) {
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
            Map<String, Integer> map = new HashMap<>();
            map.put(Message.MessageKeys.TOUR_ID_KEY, tourId);
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.QUERY_SLOTS_BY_TOUR_ID, jsonObject.toString());
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
            String slotsJsonStr = responseMessage.getMessageJson();
            getSlotsDataFromJson(slotsJsonStr, tourId);

            buffer.toString();

            Log.v(LOG_TAG, "Json: " + slotsJsonStr);

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
     * Take the String representing the matching slots in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public void getSlotsDataFromJson(String slotsJsonStr,
                                     int tourId)
            throws JSONException {

        // Now we have a String representing the matching slots in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        try {
            JSONArray slotsArray = new JSONArray(slotsJsonStr);

            // Avoid crash.
            if (slotsArray.isNull(0)) {
                Log.d(LOG_TAG, "Slots service complete. No slots found for this tour.");
                sendBroadcast();
                return;
            }

            // Store new locations in order for the user to select his desired location
            ArrayList<ContentValues> cvGuidesArrayList = new ArrayList<>();
            ArrayList<ContentValues> cvSlotsArrayList = new ArrayList<>();

            for(int i = 0; i < slotsArray.length(); i++) {
                // These are the values that will be collected.
                String guideId;
                String guideName;
                String guideEmail;
                double guideRating;

                long slotId;
                // Date is stored as a julian date.
                int slotDate;
                long slotTime;
                int slotVacant;

                // Get the JSON object representing the location
                JSONObject slotObject = slotsArray.getJSONObject(i);

                // get requested elements from json
                guideId = slotObject.getString(Message.MessageKeys.SLOT_GUIDE_ID_KEY);
                guideName = slotObject.getString(Message.MessageKeys.USER_NAME_KEY);
                guideEmail = slotObject.getString(Message.MessageKeys.USER_EMAIL_KEY);
                try {
                    // Might be null if no user has rated this guide yet.
                    guideRating = slotObject.getDouble(Message.MessageKeys.USER_RATING_KEY);
                }
                catch (JSONException e) {
                    guideRating = 0;
                }

                ContentValues guideValues = new ContentValues();

                guideValues.put(ToursContract.UserEntry._ID, guideId);
                guideValues.put(ToursContract.UserEntry.COLUMN_USER_NAME, guideName);
                guideValues.put(ToursContract.UserEntry.COLUMN_USER_EMAIL, guideEmail);
                guideValues.put(ToursContract.UserEntry.COLUMN_USER_RATING, guideRating);

                cvGuidesArrayList.add(guideValues);

                slotId = slotObject.getLong(Message.MessageKeys.SLOT_ID_KEY);
                slotDate = slotObject.getInt(Message.MessageKeys.SLOT_DATE_KEY);
                slotTime = slotObject.getLong(Message.MessageKeys.SLOT_TIME_KEY);
                slotVacant = slotObject.getInt(Message.MessageKeys.SLOT_CAPACITY_KEY);

                ContentValues slotValues = new ContentValues();

                slotValues.put(ToursContract.SlotEntry._ID, slotId);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_GUIDE_ID, guideId);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_TOUR_ID, tourId);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_DATE, slotDate);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_TIME, slotTime);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_CAPACITY, slotVacant);
                slotValues.put(ToursContract.SlotEntry.COLUMN_SLOT_ACTIVE, 1);

                cvSlotsArrayList.add(slotValues);
            }

            // Add guides to the database.
            int guidesInserted = 0;
            if ( cvGuidesArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvGuidesArrayList.size()];
                cvGuidesArrayList.toArray(cvArray);
                guidesInserted = this.getContentResolver().bulkInsert(
                        ToursContract.UserEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, guidesInserted + " guides inserted to users table.");

            // Add slots to the database.
            int slotsInserted = 0;
            if ( cvSlotsArrayList.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cvSlotsArrayList.size()];
                cvSlotsArrayList.toArray(cvArray);
                slotsInserted = this.getContentResolver().bulkInsert(
                        ToursContract.SlotEntry.CONTENT_URI,
                        cvArray
                );
            }

            Log.d(LOG_TAG, "Slots loader service complete. " + slotsInserted + " slots inserted to slots table.");

            sendBroadcast();

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // Send an Intent with an action named BROADCAST_SLOTS_LOADER_SERVICE_DONE.
    private void sendBroadcast() {
        Intent intent = new Intent(DetailFragment.BROADCAST_SLOTS_LOADER_SERVICE_DONE);
        intent.putExtra(DetailFragment.INTENT_EXTRA_BTN_ID, mButtonClicked);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
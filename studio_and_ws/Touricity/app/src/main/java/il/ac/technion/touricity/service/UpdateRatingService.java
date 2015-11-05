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
import java.util.HashMap;
import java.util.Map;

import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.RatingDialogFragment;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class UpdateRatingService extends IntentService {

    private static final String LOG_TAG = UpdateRatingService.class.getSimpleName();

    public UpdateRatingService() { super("UpdateRatingService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String userId = Utility.getLoggedInUserId(getApplicationContext());
        int tourId = intent.getIntExtra(RatingDialogFragment.INTENT_EXTRA_TOUR_ID, -1);
        String guideId = intent.getStringExtra(RatingDialogFragment.INTENT_EXTRA_GUIDE_ID);
        float tourRating = intent.getFloatExtra(RatingDialogFragment.INTENT_EXTRA_TOUR_RATING, -1);
        float guideRating = intent.getFloatExtra(RatingDialogFragment.INTENT_EXTRA_GUIDE_RATING, -1);

        // Sanity check.
        if (userId == null || tourId == -1 || guideId == null || tourRating == -1 || guideRating == -1) {
            sendBroadcast(false);
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
            map.put(Message.MessageKeys.TOUR_ID_KEY, Integer.toString(tourId));
            map.put(Message.MessageKeys.SLOT_GUIDE_ID_KEY, guideId);
            map.put(Message.MessageKeys.TOUR_RATING_KEY, Float.toString(tourRating));
            map.put(Message.MessageKeys.USER_RATING_KEY, Float.toString(guideRating));
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.UPDATE_RATINGS, jsonObject.toString());
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
                throw new IOException("Something went wrong");
            }

            // The response message in a string format.
            responseMessageJsonStr = buffer.toString();
            Log.v(LOG_TAG, responseMessageJsonStr);

            // We know the message type to be CREATE_SLOT.
            Message responseMessage = gson.fromJson(responseMessageJsonStr, Message.class);
            JSONArray ratingsArray = new JSONArray(responseMessage.getMessageJson());

            // A single row is returned with the (possibly null) ratings.
            if (ratingsArray.isNull(0)) {
                sendBroadcast(false);
            }

            JSONObject ratingsObject = ratingsArray.getJSONObject(0);
            float averageTourRating = (float)ratingsObject.getDouble(Message.MessageKeys.TOUR_RATING_KEY);
            float averageGuideRating = (float)ratingsObject.getDouble(Message.MessageKeys.USER_RATING_KEY);

            // Add the tour rating to the tours table.
            String selection = ToursContract.TourEntry.TABLE_NAME + "." +
                    ToursContract.TourEntry._ID + " = ?";
            ContentValues cv = new ContentValues();
            cv.put(ToursContract.TourEntry.COLUMN_TOUR_RATING, averageTourRating);
            getContentResolver().update(
                    ToursContract.TourEntry.CONTENT_URI,
                    cv,
                    selection,
                    new String[]{Integer.toString(tourId)}
            );

            Log.d(LOG_TAG, "Tour rating update is done.");

            // Add the guide rating to the users table.
            selection = ToursContract.UserEntry.TABLE_NAME+ "." +
                    ToursContract.UserEntry._ID + " = ?";
            cv.clear();
            cv.put(ToursContract.UserEntry.COLUMN_USER_RATING, averageGuideRating);
            getContentResolver().update(
                    ToursContract.UserEntry.CONTENT_URI,
                    cv,
                    selection,
                    new String[]{guideId}
            );

            Log.d(LOG_TAG, "Guide rating update is done.");

            sendBroadcast(true);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attempting
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
                    // Nothing is done if the reader was already closed.
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    // Send an Intent with an action named BROADCAST_UPDATE_RATING_SERVICE_DONE.
    private void sendBroadcast(boolean success) {
        Intent broadcastIntent = new Intent(RatingDialogFragment.BROADCAST_UPDATE_RATING_SERVICE_DONE);
        broadcastIntent.putExtra(RatingDialogFragment.BROADCAST_INTENT_RESULT, success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}

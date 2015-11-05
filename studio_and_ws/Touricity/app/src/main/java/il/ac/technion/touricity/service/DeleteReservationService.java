package il.ac.technion.touricity.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

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

import il.ac.technion.touricity.DeleteReservationDialogFragment;
import il.ac.technion.touricity.DeleteSlotDialogFragment;
import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class DeleteReservationService extends IntentService {

    private static final String LOG_TAG = DeleteReservationService.class.getSimpleName();

    public DeleteReservationService() { super("DeleteReservationService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        long slotId = intent.getLongExtra(DeleteSlotDialogFragment.INTENT_EXTRA_SLOT_ID, -1L);
        String userId = Utility.getLoggedInUserId(getApplicationContext());
        if (slotId == -1L || userId == null) {
            return;
        }

        boolean success = deleteReservationFromServerDb(slotId, userId);

        if (success) {
            String selection = ToursContract.ReservationEntry.TABLE_NAME + "." +
                    ToursContract.ReservationEntry._ID + " = ? AND " +
                    ToursContract.ReservationEntry.TABLE_NAME + "." +
                    ToursContract.ReservationEntry.COLUMN_RESERVATION_USER_ID + " = ?";
            ContentValues cv = new ContentValues();
            cv.put(ToursContract.ReservationEntry.COLUMN_RESERVATION_ACTIVE, 0);

            int updated = getContentResolver().update(
                    ToursContract.ReservationEntry.CONTENT_URI,
                    cv,
                    selection,
                    new String[]{Long.toString(slotId), userId}
            );

            if (updated == 1) {
                Log.d(LOG_TAG, "Reservation was marked canceled in the local database.");
            }
        }

        sendBroadcast(success);
    }

    public boolean deleteReservationFromServerDb(long slotId, String userId) {
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
            map.put(Message.MessageKeys.SLOT_ID_KEY, Long.toString(slotId));
            map.put(Message.MessageKeys.USER_ID_KEY, userId);
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.DELETE_RESERVATION, jsonObject.toString());
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

            // We know the message type to be DELETE_RESERVATION.
            Message responseMessage = gson.fromJson(responseMessageJsonStr, Message.class);
            JSONObject responseJSON = new JSONObject(responseMessage.getMessageJson());
            String isModified = responseJSON.getString(Message.MessageKeys.IS_MODIFIED);
            return Boolean.valueOf(isModified);

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

        return false;
    }

    // Send an Intent with an action named BROADCAST_DELETE_RESERVATION_SERVICE_DONE.
    private void sendBroadcast(boolean success) {
        Intent broadcastIntent = new Intent(DeleteReservationDialogFragment.BROADCAST_DELETE_RESERVATION_SERVICE_DONE);
        broadcastIntent.putExtra(DeleteReservationDialogFragment.BROADCAST_INTENT_RESULT, success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

}

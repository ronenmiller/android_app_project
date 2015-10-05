package il.ac.technion.touricity.service;

import android.app.IntentService;
import android.content.Intent;
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

import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.SearchFragment;
import il.ac.technion.touricity.Utility;

public class AddLocationService extends IntentService {

    private static final String LOG_TAG = AddLocationService.class.getSimpleName();

    public AddLocationService() {
        super("AddLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        long osmId = intent.getLongExtra(SearchFragment.INTENT_EXTRA_LOCATION_ID, -1L);
        String locationName = intent.getStringExtra(SearchFragment.INTENT_EXTRA_LOCATION_NAME);
        String locationType = intent.getStringExtra(SearchFragment.INTENT_EXTRA_LOCATION_TYPE);
        double latitude = intent.getDoubleExtra(SearchFragment.INTENT_EXTRA_LOCATION_LATITUDE, -1);
        double longitude = intent.getDoubleExtra(SearchFragment.INTENT_EXTRA_LOCATION_LONGITUDE, -1);

        if (osmId == -1L || locationName == null || locationType == null || latitude == -1 || longitude == -1) {
            return;
        }

        boolean success = addLocationToServerDb(osmId, locationName, locationType, latitude, longitude);
        if (success) {
            Log.d(LOG_TAG, "AddLocationService completed successfully.");
        }
        else {
            Log.d(LOG_TAG, "AddLocationService failed to insert the location to the server database.");
        }
    }

    public boolean addLocationToServerDb(long osmId, String locationName, String locationType,
                                         double latitude, double longitude) {
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
            map.put(Message.MessageKeys.LOCATION_OSM_ID_KEY, Long.toString(osmId));
            map.put(Message.MessageKeys.LOCATION_OSM_NAME_KEY, locationName);
            map.put(Message.MessageKeys.LOCATION_OSM_TYPE_KEY, locationType);
            map.put(Message.MessageKeys.LOCATION_OSM_LATITUDE_KEY, Double.toString(latitude));
            map.put(Message.MessageKeys.LOCATION_OSM_LONGITUDE_KEY, Double.toString(longitude));
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.ADD_LOCATION, jsonObject.toString());
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

            // We know the message type to be ADD_USER.
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
}

package il.ac.technion.touricity.service;

import android.app.IntentService;
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

import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.SignUpActivity;
import il.ac.technion.touricity.Utility;

public class SignUpService extends IntentService {

    private static final String LOG_TAG = SignUpService.class.getSimpleName();

    public SignUpService() { super("SignUpService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        boolean cancelEmail  = false;
        boolean cancelNickname = false;

        String email = intent.getStringExtra(SignUpActivity.INTENT_EXTRA_EMAIL);
        String nickname = intent.getStringExtra(SignUpActivity.INTENT_EXTRA_NICKNAME);
        String phone = intent.getStringExtra(SignUpActivity.INTENT_EXTRA_PHONE);
        String password = intent.getStringExtra(SignUpActivity.INTENT_EXTRA_PASSWORD);
        boolean isGuide = intent.getBooleanExtra(SignUpActivity.INTENT_EXTRA_GUIDE, false);

        if (phone.equals("")) {
            phone = null;
        }

        try {
            // TODO: in SQL, change constraints regarding the phone number in the 'users' and 'people' table
            if (!isUnique(Message.MessageTypes.VALIDATE_UNIQUE_EMAIL,
                    Message.MessageKeys.USER_EMAIL_KEY,
                    email)) {
                cancelEmail = true;
            }

            if (!isUnique(Message.MessageTypes.VALIDATE_UNIQUE_USERNAME,
                    Message.MessageKeys.USER_NAME_KEY,
                    nickname)) {

                cancelNickname = true;
            }

            boolean cancel = cancelEmail || cancelNickname;

            boolean success = false;
            if (!cancel) {
                success = addUserToServerDb(email, nickname, phone, password, isGuide);
            }

            sendBroadcast(cancelEmail, cancelNickname, success);
        }
        catch (NullPointerException e) {
            // Nothing we can do.
        }


    }

    private boolean isUnique(int type, String key, String value) throws NullPointerException {
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
            map.put(key, value);
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(type, jsonObject.toString());
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

            // We know the message type to be VALIDATE_UNIQUE_USERNAME.
            Message responseMessage = gson.fromJson(responseMessageJsonStr, Message.class);
            JSONObject responseJSON = new JSONObject(responseMessage.getMessageJson());
            String isUnique = responseJSON.getString(Message.MessageKeys.IS_EXISTS);
            return Boolean.valueOf(isUnique);

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

    public boolean addUserToServerDb(String email, String nickname, String phone,
                                  String password, boolean isGuide) {
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
            map.put(Message.MessageKeys.USER_EMAIL_KEY, email);
            map.put(Message.MessageKeys.USER_NAME_KEY, nickname);
            map.put(Message.MessageKeys.USER_PHONE_KEY, phone);
            map.put(Message.MessageKeys.USER_PASSWORD_KEY, password);
            map.put(Message.MessageKeys.USER_TYPE_KEY, Boolean.toString(isGuide));
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.ADD_USER, jsonObject.toString());
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

            // We know the message type to be VALIDATE_UNIQUE_USERNAME.
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

    // Send an Intent with an action named BROADCAST_SIGNUP_SERVICE_DONE.
    private void sendBroadcast(boolean cancelEmail, boolean cancelNickname, boolean success) {
        Intent broadcastIntent = new Intent(SignUpActivity.BROADCAST_SIGNUP_SERVICE_DONE);
        broadcastIntent.putExtra(SignUpActivity.BROADCAST_INTENT_CANCEL_EMAIL, cancelEmail);
        broadcastIntent.putExtra(SignUpActivity.BROADCAST_INTENT_CANCEL_NICKNAME, cancelNickname);
        broadcastIntent.putExtra(SignUpActivity.BROADCAST_INTENT_RESULT_SUCCESS, success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

}

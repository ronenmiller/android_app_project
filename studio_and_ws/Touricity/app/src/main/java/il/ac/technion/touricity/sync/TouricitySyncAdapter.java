package il.ac.technion.touricity.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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

import il.ac.technion.touricity.MainActivity;
import il.ac.technion.touricity.Message;
import il.ac.technion.touricity.R;
import il.ac.technion.touricity.Utility;
import il.ac.technion.touricity.data.ToursContract;

public class TouricitySyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = TouricitySyncAdapter.class.getSimpleName();

    private static final int SECONDS_IN_MINUTE = 60;

    private static final String[] TOUR_ID_COLUMNS = new String[] {
            ToursContract.TourEntry.TABLE_NAME + "." + ToursContract.TourEntry._ID
    };

    private static final int COL_TOUR_ID = 0;

    private static final int TOURS_NOTIFICATION_ID = 3004;

    private int mInserted = 0;
    private long mLocationId = -1L;

    public TouricitySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d(LOG_TAG, "onPerformSync");

        long locationId = Utility.getPreferredLocationId(getContext().getApplicationContext());
        Log.d(LOG_TAG, "Location ID of sync adapter: " + locationId);

        if (locationId == -1L) {
            return;
        }

        // Don't notify about new tours when the user searches a location inside the app for the
        // first time.
        if (locationId != mLocationId) {
            mInserted = 0;
            mLocationId = locationId;
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
            map.put(Message.MessageKeys.LOCATION_OSM_ID_KEY, locationId);
            JSONObject jsonObject = new JSONObject(map);
            Message message = new Message(Message.MessageTypes.QUERY_TOURS_BY_OSM_ID, jsonObject.toString());
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

            // Extract tours from message
            Message responseMessage = gson.fromJson(responseMessageJsonStr, Message.class);
            String ToursJsonStr = responseMessage.getMessageJson();
            getTourDataFromJson(ToursJsonStr, locationId);
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
    public void getTourDataFromJson(String toursJsonStr, long osmID) throws JSONException {

        // Now we have a String representing the matching tours in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        try {
            JSONArray toursArray = new JSONArray(toursJsonStr);

            // Avoid crash.
            if (toursArray.isNull(0)) {
                Log.d(LOG_TAG, "SyncAdapter complete. No tours found for this location.");
                return;
            }

            // Insert the new tours information into the database.
            ArrayList<ContentValues> cvArrayList = new ArrayList<>(toursArray.length());
            for(int i = 0; i < toursArray.length(); i++) {
                // These are the values that will be collected.
                int tourID;
                String managerID;
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

                tourID = tourObject.getInt(Message.MessageKeys.TOUR_ID_KEY);
                managerID = tourObject.getString(Message.MessageKeys.TOUR_MANAGER_KEY);
                title = tourObject.getString(Message.MessageKeys.TOUR_TITLE_KEY);
                duration = tourObject.getInt(Message.MessageKeys.TOUR_DURATION_KEY);
                language = tourObject.getInt(Message.MessageKeys.TOUR_LANGUAGE_KEY);
                location = tourObject.getString(Message.MessageKeys.TOUR_LOCATION_KEY);
                rating = tourObject.getDouble(Message.MessageKeys.TOUR_RATING_KEY);
                description = tourObject.getString(Message.MessageKeys.TOUR_DESCRIPTION_KEY);


                ContentValues tourValues = new ContentValues();

                tourValues.put(ToursContract.TourEntry._ID, tourID);
                tourValues.put(ToursContract.TourEntry.COLUMN_OSM_ID, osmID);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_MANAGER_ID, managerID);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_TITLE, title);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_DURATION, duration);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE, language);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_LOCATION, location);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_RATING, rating);
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_AVAILABLE, 1);
                // TODO: retrieve tour description together with photos and comments only when clicking on a specific tour
                tourValues.put(ToursContract.TourEntry.COLUMN_TOUR_DESCRIPTION, description);

                cvArrayList.add(tourValues);
            }

            // Delete tours which have no open slots, so we don't build up an endless history.
            // That way, old tours will be deleted, and new tours, for which we didn't query
            // the slots table yet (and therefore don't have any slots open) will be deleted
            // and added again next (that's why the insertion is after the deletion).
            ContentResolver resolver = getContext().getContentResolver();

            Cursor toursCursor = null;
            Cursor slotsCursor = null;
            try {
                // Find tours
                toursCursor = resolver.query(
                        ToursContract.TourEntry.buildTourLocationUri(osmID),
                        TOUR_ID_COLUMNS,
                        null,
                        null,
                        null
                );
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
                    if (!slotsCursor.moveToFirst()) {
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
            finally {
                 if (slotsCursor != null) {
                     slotsCursor.close();
                 }
                if (toursCursor != null) {
                    toursCursor.close();
                }
            }

            // Add tours to the database.
            int inserted = 0;
            if ( cvArrayList.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] cvArray = new ContentValues[cvArrayList.size()];
                cvArrayList.toArray(cvArray);
                inserted = getContext().getContentResolver().bulkInsert(
                        ToursContract.TourEntry.CONTENT_URI,
                        cvArray
                );

                // New tours are available, and this is not the first time the sync is made.
                if (inserted > mInserted && mInserted != 0) {
                    notifyTours();
                }
                mInserted = inserted;
            }

            Log.d(LOG_TAG, "SyncAdapter complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        // The following parameters are in seconds.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int syncInterval = Integer.parseInt(prefs.getString(
                context.getString(R.string.pref_sync_interval_key), "180")) * SECONDS_IN_MINUTE;
        int syncFlextime = syncInterval / 3 ;
        configurePeriodicSync(context, syncInterval, syncFlextime);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        boolean autoSync = prefs.getBoolean(context.getString(R.string.pref_sync_data_key), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), autoSync);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyTours() {
        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify = prefs.getBoolean(context.getString(R.string.pref_notifications_key), true);

        if (!notify) {
            return;
        }

        // Let's send a notification with the availability of new tours.
        String preferredLocation = Utility.getPreferredLocationName(context.getApplicationContext());

        int iconId = R.mipmap.ic_launcher;
        String title = context.getString(R.string.app_name);

        // Define the text of the forecast.
        String contentText = String.format(context.getString(R.string.format_notification),
                preferredLocation
        );

        // build your notification here.
        // see: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(contentText);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(TOURS_NOTIFICATION_ID, mBuilder.build());
    }
}
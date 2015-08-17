package il.ac.technion.touricity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.LocationService;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOCATIONS_LOADER = 0;

    public static final String BROADCAST_SERVICE_DONE = "broadcast_service_done";

    private LocationAdapter mLocationAdapter;

    private static final String[] OSM_COLUMNS = {
            // Used for projection.
            // _ID must be used in every projection
            ToursContract.OSMEntry._ID,
            ToursContract.OSMEntry.COLUMN_OSM_ID,
            ToursContract.OSMEntry.COLUMN_LOCATION_NAME,
            ToursContract.OSMEntry.COLUMN_LOCATION_TYPE,
            ToursContract.OSMEntry.COLUMN_COORD_LAT,
            ToursContract.OSMEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to OSM_COLUMNS.  If OSM_COLUMNS changes, these
    // must change.
    // The following variables are package-private.
    // Package-private is stricter than protected and public scopes, but more permissive
    // than private scope.
    static final int COL_ID = 0;
    static final int COL_OSM_ID = 1;
    static final int COL_LOCATION_NAME = 2;
    static final int COL_LOCATION_TYPE = 3;
    static final int COL_COORD_LAT = 4;
    static final int COL_COORD_LONG = 5;


    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

//        onLocationChanged(requestedLocation);

        // The LocationAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mLocationAdapter = new LocationAdapter(getActivity(), null, 0);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_search);
        // TODO: later decide between the LocationAdapter and the SlotsAdapter using a boolean
        listView.setAdapter(mLocationAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor)adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    addLocation(cursor);
                }
            }
        });

        return rootView;
    }

    private void addLocation(Cursor cursor) {
        // read contents from cursor
        long osmID = cursor.getLong(COL_OSM_ID);
        String locationName = cursor.getString(COL_LOCATION_NAME);
        String locationType = cursor.getString(COL_LOCATION_TYPE);
        double latitude = cursor.getDouble(COL_COORD_LAT);
        double longitude = cursor.getDouble(COL_COORD_LONG);

        // insert contents into location table
        ContentValues cv = new ContentValues();
        cv.put(ToursContract.LocationEntry.COLUMN_OSM_ID, osmID);
        cv.put(ToursContract.LocationEntry.COLUMN_LOCATION_NAME, locationName);
        cv.put(ToursContract.LocationEntry.COLUMN_LOCATION_TYPE, locationType);
        cv.put(ToursContract.LocationEntry.COLUMN_COORD_LAT, latitude);
        cv.put(ToursContract.LocationEntry.COLUMN_COORD_LONG, longitude);

        getActivity().getContentResolver().insert(
                ToursContract.LocationEntry.CONTENT_URI,
                cv
        );

        // Release resources.
        cursor.close();

        // Return to main activity.
        locationSelected(osmID, locationName);
    }

    private void locationSelected(long id, String name) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_FOUND);
        intent.putExtra(MainActivity.EXTRA_LOC_ID, id);
        intent.putExtra(MainActivity.EXTRA_LOC_NAME, name);
        getActivity().startActivity(intent);
    }

    // package-shared
    void onLocationChanged(String requestedLocation) {
        updateLocation(requestedLocation);
    }

    private void updateLocation(String requestedLocation) {
        Intent intent = new Intent(getActivity(), LocationService.class);
        intent.putExtra(Intent.EXTRA_TEXT, requestedLocation);
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.
        String sortOrder = ToursContract.OSMEntry._ID + " ASC";

        Uri queryOSMUri = ToursContract.OSMEntry.CONTENT_URI;
        return new CursorLoader(
                getActivity(),
                queryOSMUri,
                OSM_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        // returns false if the cursor is empty
        if (cursor.moveToFirst() == false) {
            // Release resources.
            cursor.close();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setAction(MainActivity.ACTION_NOT_FOUND);
            getActivity().startActivity(intent);
        }
        // returns false if the cursor move failed, i.e. there is no second row
        else if (cursor.moveToNext() == false) {
            cursor.moveToFirst();
            addLocation(cursor);
        }
        // there are at least two rows in the cursor
        else {
            cursor.moveToFirst();
            // move before the first row
            cursor.moveToPrevious();
            mLocationAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mLocationAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_SERVICE_DONE));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            SearchFragment sf = (SearchFragment)getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_search);
            getLoaderManager().restartLoader(LOCATIONS_LOADER, null, sf);
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}

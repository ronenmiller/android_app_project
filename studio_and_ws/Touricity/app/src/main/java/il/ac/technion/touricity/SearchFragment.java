package il.ac.technion.touricity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import il.ac.technion.touricity.data.ToursContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private LocationAdapter mLocationAdapter;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // The LocationAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mLocationAdapter = new LocationAdapter(getActivity(), null, 0);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_search);
        listView.setAdapter(mLocationAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    addLocation(cursor);
                }
            }
        });

        return rootView;
    }

    private void addLocation(Cursor cursor) {
        // read contents from cursor
        long osmID = cursor.getLong(MainFragment.COL_OSM_ID);
        String locationName = cursor.getString(MainFragment.COL_LOCATION_NAME);
        String locationType = cursor.getString(MainFragment.COL_LOCATION_TYPE);
        double latitude = cursor.getDouble(MainFragment.COL_COORD_LAT);
        double longitude = cursor.getDouble(MainFragment.COL_COORD_LONG);

        // Save values to preferences file to be used later on.
        // Type is used just to display the correct icon in the list view
        // and therefore doesn't need to be saved.
        Context context = getActivity().getApplicationContext();
        Utility.saveLocationToPreferences(
                context,
                osmID,
                locationName,
                (float) latitude,
                (float) longitude
                );

        // Insert contents into the location table.
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
        locationSelected();
    }

    private void locationSelected() {
        Intent returnIntent = new Intent();
        getActivity().setResult(MainFragment.RESULT_OK, returnIntent);
        getActivity().finish();
    }

    void onLocationChanged() {
        getLoaderManager().initLoader(MainFragment.LOCATIONS_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(MainFragment.LOCATIONS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.
        String sortOrder = ToursContract.OSMEntry._ID + " ASC";

        Uri queryOSMUri = ToursContract.OSMEntry.CONTENT_URI;
        return new CursorLoader(
                getActivity(),
                queryOSMUri,
                MainFragment.OSM_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mLocationAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mLocationAdapter.swapCursor(null);
    }

}

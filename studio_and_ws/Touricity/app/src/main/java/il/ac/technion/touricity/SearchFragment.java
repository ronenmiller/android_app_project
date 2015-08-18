package il.ac.technion.touricity;

import android.content.ContentValues;
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
        // TODO: check if can use shared preferences instead
        intent.putExtra(MainActivity.EXTRA_LOC_ID, id);
        intent.putExtra(MainActivity.EXTRA_LOC_NAME, name);
        getActivity().startActivity(intent);
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
        mLocationAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mLocationAdapter.swapCursor(null);
    }

}

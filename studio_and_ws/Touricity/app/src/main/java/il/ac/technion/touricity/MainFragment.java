package il.ac.technion.touricity;

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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.LocationService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOCATIONS_LOADER = 0;
    private static final int SLOTS_LOADER = 1;

    private EditText mEditText;
    private ImageView mImageView;
    private ListView mListView;
    private LocationAdapter mLocationAdapter;

    private static final String[] OSM_COLUMNS = {
            // Used for projection.
            // _ID must be used in every projection
            ToursContract.OSMEntry._ID,
            ToursContract.OSMEntry.COLUMN_LOCATION_TYPE,
            ToursContract.OSMEntry.COLUMN_LOCATION_NAME
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    // The following variables are package-private.
    // Package-private is stricter than protected and public scopes, but more permissive
    // than private scope.
    static final int COL_ID = 0;
    static final int COL_LOCATION_TYPE = 1;
    static final int COL_LOCATION_NAME = 2;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mEditText = (EditText)rootView.findViewById(R.id.edittext_location);
        mImageView = (ImageView)rootView.findViewById(R.id.imagebtn_search_location);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set animation while loading results
                RotateAnimation animation = new RotateAnimation(0f, 350f, 15f, 15f);
                animation.setInterpolator(new LinearInterpolator());
                animation.setRepeatCount(Animation.INFINITE);
                animation.setDuration(700);
                mImageView.startAnimation(animation);

                String requestedLocation = mEditText.getText().toString();
                onLocationChanged(requestedLocation);
            }
        });

        // The LocationAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mLocationAdapter = new LocationAdapter(getActivity(), null, 0);

        mListView = (ListView)rootView.findViewById(R.id.listview_main);
        // TODO: later decide between the LocationAdapter and the SlotsAdapter using a boolean
        mListView.setAdapter(mLocationAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {

//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor cursor = (Cursor)adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                            locationSetting, cursor.getLong(COL_WEATHER_DATE));
//                    ((Callback) getActivity()).onItemSelected(dateUri);
//                }
//                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
//        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
//            // The listview probably hasn't even been populated yet.  Actually perform the
//            // swapout in onLoadFinished.
//            mPosition = savedInstanceState.getInt(SELECTED_KEY);
//        }

        return rootView;
    }

    private void onLocationChanged(String requestedLocation ) {
        updateLocation(requestedLocation);
        getLoaderManager().restartLoader(LOCATIONS_LOADER, null, this);
    }

    private void updateLocation(String requestedLocation) {
        Intent intent = new Intent(getActivity(), LocationService.class);
        intent.putExtra(Intent.EXTRA_TEXT, requestedLocation);
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.

        if (i == LOCATIONS_LOADER) {
            // Sort order:  Ascending, by relevance.
            String sortOrder = ToursContract.OSMEntry.COLUMN_QUERY_RELEVANCE + " ASC";

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
        else if(i == SLOTS_LOADER) {
            // TODO: complete later
            return null;
        }
        else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        int loaderId = cursorLoader.getId();
        if (loaderId == LOCATIONS_LOADER) {
            mLocationAdapter.swapCursor(cursor);
            mImageView.setAnimation(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        int loaderId = loader.getId();
        if (loaderId == LOCATIONS_LOADER) {
            mLocationAdapter.swapCursor(null);
        }
    }
}

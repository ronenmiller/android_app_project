package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.LocationsLoaderService;
import il.ac.technion.touricity.sync.TouricitySyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    public final String LOG_TAG = MainFragment.class.getSimpleName();

    // package-shared
    static final int OSM_LOADER = 0;
    static final int TOURS_LOADER = 1;

    public static final String BROADCAST_LOCATIONS_LOADER_SERVICE_DONE = "broadcast_locations_loader_service_done";
    public static final String BROADCAST_TOURS_LOADER_SERVICE_DONE = "broadcast_tours_loader_service_done";

    // package-shared
    static final String[] OSM_COLUMNS = {
            // Used for projection.
            // _ID must be used in every projection
            ToursContract.OSMEntry._ID,
            ToursContract.OSMEntry.COLUMN_LOCATION_ID,
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
    static final int COL_OSM_ID = 0;
    static final int COL_OSM_LOCATION_ID = 1;
    static final int COL_LOCATION_NAME = 2;
    static final int COL_LOCATION_TYPE = 3;
    static final int COL_COORD_LAT = 4;
    static final int COL_COORD_LONG = 5;

    // package-shared
    static final String[] TOUR_COLUMNS = {
            // Used for projection.
            // _ID must be used in every projection
            ToursContract.TourEntry.TABLE_NAME + "." + ToursContract.TourEntry._ID,
            ToursContract.TourEntry.COLUMN_TOUR_TITLE,
            ToursContract.TourEntry.COLUMN_TOUR_DURATION,
            ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE,
            ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME,
            ToursContract.TourEntry.COLUMN_TOUR_RATING,
    };

    static final int COL_TOUR_ID = 0;
    static final int COL_TOUR_TITLE = 1;
    static final int COL_TOUR_DURATION = 2;
    static final int COL_TOUR_LANGUAGE = 3;
    static final int COL_TOUR_LANGUAGE_NAME = 4;
    static final int COL_TOUR_RATING = 5;

    static final int LOCATIONS_REQUEST = 0;

    private ToursAdapter mToursAdapter;

    private View mHeaderView;
    private TextView mHeaderText;
    private View mFooterView;
    private ListView mToursListView;
    private SwipeRefreshLayout mSwipeLayout;
    private FrameLayout mProgressBarLayout;
    private ImageView mProgressBarView;

    private static final String SELECTED_KEY = "selected_position";
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SHOW_KEY = "show_location";
    private boolean mShowLocation = false;

    private MenuItem mCreateTourMenuItem;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri tourUri);
        void onCreateTour();
    }


    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mToursAdapter = new ToursAdapter(getActivity(), null, 0);

        mToursListView = (ListView)rootView.findViewById(R.id.listview_main);

        mSwipeLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorScheme(R.color.touricity_teal,
                R.color.touricity_light_teal,
                R.color.touricity_light_grey);

        mProgressBarLayout = (FrameLayout) rootView.findViewById(R.id.framelayout_main);
        mProgressBarView = (ImageView)rootView.findViewById(R.id.imageview_main);

        mProgressBarLayout.setVisibility(View.GONE);

        mHeaderView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_header, null, false);
        mHeaderText = (TextView)mHeaderView.findViewById(R.id.list_item_header);
        mFooterView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_create_tour, null, false);

        boolean isUserLoggedIn = Utility.getIsLoggedIn(getActivity().getApplicationContext());
        boolean isUserGuide = Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext());
        boolean isLocationSelected = Utility.getPreferredLocationId
                (getActivity().getApplicationContext()) != -1L;

        if (!isLocationSelected) {
            mHeaderText.setText(getResources().getString(R.string.find_destination));
            mToursListView.addHeaderView(mHeaderView);
            mHeaderView.setClickable(false);
        }
        else {
            updateLocationViews(true);
        }

        if (isUserLoggedIn && isUserGuide && isLocationSelected) {
            mToursListView.addFooterView(mFooterView);
        }
        else {
            mToursListView.removeFooterView(mFooterView);
        }

        mToursListView.setAdapter(mToursAdapter);
        mToursListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor)adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int tourId = cursor.getInt(COL_TOUR_ID);
                    Uri tourUri = ToursContract.TourEntry.buildTourIdUri(tourId);
                    ((Callback)getActivity()).onItemSelected(tourUri);
                }
                else {
                    if (Utility.getIsLoggedIn(getActivity().getApplicationContext()) &&
                            Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext())) {
                        if (mToursListView.getLastVisiblePosition() == position) {
                            ((Callback)getActivity()).onCreateTour();
                        }
                    }
                }
                mPosition = position;

            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_KEY)) {
                // The listview probably hasn't even been populated yet.  Actually perform the
                // swapout in onLoadFinished.
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }

            if (savedInstanceState.containsKey(SHOW_KEY)) {
                mShowLocation = savedInstanceState.getBoolean(SHOW_KEY);
            }
        }

        return rootView;
    }

    @Override
    public void onRefresh() {
        updateTours();
    }

    // Called on login or logout.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void showGuideOptions(boolean show) {
        if (show) {
            if (mToursListView != null) {
                mToursListView.addFooterView(mFooterView);
            }
            if (mCreateTourMenuItem != null) {
                mCreateTourMenuItem.setVisible(true);
            }
        }
        else {
            if (mToursListView != null) {
                mToursListView.removeFooterView(mFooterView);
            }
            if (mCreateTourMenuItem != null) {
                mCreateTourMenuItem.setVisible(false);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putBoolean(SHOW_KEY, mShowLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_main, menu);

        mCreateTourMenuItem = menu.findItem(R.id.action_create_tour);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_create_tour) {
            ((Callback)getActivity()).onCreateTour();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void performLocationSearch(String query) {
        // Delete previous results
        getActivity().getContentResolver().delete(
                ToursContract.OSMEntry.CONTENT_URI,
                null,
                null
        );
        // Arrange visibility of views.
        updateLocationViews(false);
        mProgressBarLayout.setVisibility(View.VISIBLE);
        mToursListView.setVisibility(View.GONE);

        // Set animation while loading results
        RotateAnimation animation = new RotateAnimation
                (0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(1500);
        mProgressBarView.startAnimation(animation);

        Intent intent = new Intent(getActivity(), LocationsLoaderService.class);
        intent.putExtra(Intent.EXTRA_TEXT, query);
        getActivity().startService(intent);
    }

    private void updateTours() {
        TouricitySyncAdapter.syncImmediately(getActivity());
    }

    // package-shared to be called from main activity
    void updateLocationViews(boolean locationFound) {
        MainActivity mainActivity = (MainActivity)getActivity();
        if (locationFound) {
            mShowLocation = true;
            mProgressBarLayout.setVisibility(View.GONE);
            mainActivity.showLocationLinearLayout(true);
        }
        else {
            mShowLocation = false;
            mainActivity.showLocationLinearLayout(false);
        }
    }

    public void addLocation(Cursor cursor, boolean isOsm) {
        long osmId;
        String locationName;
        String locationType;
        double latitude;
        double longitude;
        // Read contents from cursor.
        if (isOsm) {
            osmId = cursor.getLong(COL_OSM_LOCATION_ID);
            locationName = cursor.getString(COL_LOCATION_NAME);
            locationType = cursor.getString(COL_LOCATION_TYPE);
            latitude = cursor.getDouble(COL_COORD_LAT);
            longitude = cursor.getDouble(COL_COORD_LONG);
        }
        else {
            // Read contents from cursor.
            osmId = cursor.getLong(MainActivity.COL_RECENT_LOCATION_ID);
            locationName = cursor.getString(MainActivity.COL_RECENT_LOCATION_NAME);
            locationType = cursor.getString(MainActivity.COL_RECENT_LOCATION_TYPE);
            latitude = cursor.getDouble(MainActivity.COL_RECENT_COORD_LAT);
            longitude = cursor.getDouble(MainActivity.COL_RECENT_COORD_LONG);
        }

        // Save values to preferences file to be used later on.
        // Type is used just to display the correct icon in the list view
        // and therefore doesn't need to be saved.
        Context context = getActivity().getApplicationContext();
        Utility.saveLocationToPreferences(
                context,
                osmId,
                locationName,
                (float)latitude,
                (float)longitude
        );

        // Insert contents into location table.
        ContentValues cv = new ContentValues();
        cv.put(ToursContract.LocationEntry.COLUMN_LOCATION_ID, osmId);
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

        // Load tours from the server and populate the list view.
        updateTours();
        // Update views.
        updateLocationViews(true);
    }

    private void launchSearchFragment() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        startActivityForResult(intent, LOCATIONS_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATIONS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // Load tours from the server and populate the list view.
                updateTours();
                // Update views.
                updateLocationViews(true);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Arrange visibility of views.
        updateLocationViews(mShowLocation);

        // The only loader that has a visible list view in this activity.
        getLoaderManager().initLoader(TOURS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.
        if (i == OSM_LOADER) {
            // Sort order:  By most relevant locations, the most relevant is first.
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
        else if (i == TOURS_LOADER) {
            // Sort order:  By tour rating, highest is first.
            String sortOrder = ToursContract.TourEntry.COLUMN_TOUR_RATING + " DESC";

            long locationId = Utility.getPreferredLocationId(getActivity().getApplicationContext());
            if (locationId == -1L) {
                return null;
            }

            Log.d(LOG_TAG, "Location ID: " + locationId);
            Uri tourForLocationUri = ToursContract.TourEntry.buildTourLocationUri(locationId);

            return new CursorLoader(
                    getActivity(),
                    tourForLocationUri,
                    TOUR_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (cursorLoader.getId() == OSM_LOADER) {
            // returns false if the cursor is empty
            if (!cursor.moveToFirst()) {
                // Release resources.
                cursor.close();
                String locationDisplay = getString(R.string.search_not_found);
                Toast.makeText(getActivity(), locationDisplay, Toast.LENGTH_LONG).show();
                if (Utility.getPreferredLocationId(getActivity().getApplicationContext()) == -1L) {
                    // There is no location to show.
                    updateLocationViews(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        // The list header is already visible.
                        mHeaderText.setText(getString(R.string.find_destination));
                    }
                }
                else {
                    // Show the last location found.
                    updateLocationViews(true);
                }
            }
            // Returns false if the cursor move failed, i.e. there is no second row -
            // a single result was found.
            else if (!cursor.moveToNext()) {
                cursor.moveToFirst();
                addLocation(cursor, true);
            }
            // There are at least two rows in the cursor, show results in search fragment.
            else {
                cursor.close();
                launchSearchFragment();
            }
        }
        else if (cursorLoader.getId() == TOURS_LOADER) {
            Log.d(LOG_TAG, "Tours cursor returned " + cursor.getCount() + " rows.");
            mToursAdapter.swapCursor(cursor);
            // Only show the create tour menu item when a location is selected.
            if (Utility.getIsLoggedIn(getActivity().getApplicationContext()) &&
                    Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext())) {
                if (mCreateTourMenuItem != null) {
                    mCreateTourMenuItem.setVisible(true);
                }
            }

            if (cursor.getCount() > 0) {
                mToursListView.removeHeaderView(mHeaderView);
                mToursListView.setClickable(true);
                if (mPosition != ListView.INVALID_POSITION) {
                    // If we don't need to restart the loader, and there's a desired position to restore
                    // to, do so now.
                    mToursListView.smoothScrollToPosition(mPosition);
                }
            }
            else {
                mToursListView.removeHeaderView(mHeaderView);
                String toursNotFound = getString(R.string.tours_not_found);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mHeaderText.setText(toursNotFound);
                    mToursListView.addHeaderView(mHeaderView);
                    mToursListView.setClickable(false);
                }
                else {
                    Toast.makeText(getActivity(), toursNotFound, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (loader.getId() == TOURS_LOADER) {
            mToursAdapter.swapCursor(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mLocationReceiver,
                new IntentFilter(BROADCAST_LOCATIONS_LOADER_SERVICE_DONE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mToursReceiver,
                new IntentFilter(BROADCAST_TOURS_LOADER_SERVICE_DONE));

        updateTours();
    }

    // handler for received Intents for the BROADCAST_LOCATIONS_LOADER_SERVICE_DONE event
    private BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and set visibility attribute
            Log.d(LOG_TAG, "Location broadcast received.");
            mProgressBarView.setAnimation(null);
            mProgressBarLayout.setVisibility(View.GONE);
            mToursListView.setVisibility(View.VISIBLE);
            MainFragment mf = (MainFragment)getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            getActivity().getSupportLoaderManager().restartLoader(OSM_LOADER, null, mf);
        }
    };

    // handler for received Intents for the BROADCAST_LOCATIONS_LOADER_SERVICE_DONE event
    private BroadcastReceiver mToursReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and set visibility attribute
            Log.d(LOG_TAG, "Tours broadcast received.");
            mSwipeLayout.setRefreshing(false);
            MainFragment mf = (MainFragment)getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            getActivity().getSupportLoaderManager().restartLoader(TOURS_LOADER, null, mf);
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLocationReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mToursReceiver);
        super.onPause();
    }
}

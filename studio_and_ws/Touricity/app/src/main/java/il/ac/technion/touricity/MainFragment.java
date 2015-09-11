package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.app.SearchManager;
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
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.LocationService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // package-shared
    static final int LOCATIONS_LOADER = 0;
    static final int RECENT_LOC_LOADER = 1;
    static final int SLOTS_LOADER = 2;

    private static final int HISTORY_ADAPTER_RESET_IN_SECONDS = 2;

    public static final String BROADCAST_LOCATION_SERVICE_DONE = "broadcast_location_service_done";

    // package-shared
    static final String[] OSM_COLUMNS = {
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

    static final int LOCATIONS_REQUEST = 0;
    static final int RESULT_OK = 0;
    static final int RESULT_CANCELED = 1;

    private RecentLocationAdapter mRecentLocationAdapter;
    private String mHistoryQuery = "";

    private ListView mToursListView;
    private FrameLayout mProgressBarLayout;
    private ImageView mProgressBarView;

    public MainFragment() {
    }

    // TODO: add support for saving state (Bundle savedInstanceState) when the device is rotated
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mHistoryQuery = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecentLocationAdapter = new RecentLocationAdapter(getActivity(), null, 0);

        // TODO: add touch selectors to all the lists views in the app
        mToursListView = (ListView)rootView.findViewById(R.id.listview_main);
        mProgressBarLayout = (FrameLayout) rootView.findViewById(R.id.framelayout_main);
        mProgressBarView = (ImageView)rootView.findViewById(R.id.imageview_main);

        mProgressBarLayout.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_main, menu);

        // Associate searchable configuration with the SearchView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager)getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView)menu
                    .findItem(R.id.action_search).getActionView();
            if (searchView != null) {
                searchView.setSearchableInfo(searchManager.
                        getSearchableInfo(getActivity().getComponentName()));
                searchView.setIconifiedByDefault(true);
                searchView.setSubmitButtonEnabled(false);
                searchView.setQueryHint(getResources().getString(R.string.search_hint));
                searchView.setSuggestionsAdapter(mRecentLocationAdapter);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        // TODO: stop searching if internet connection is not available
                        performLocationSearch(s);
                        // return true if the query has been handled by the listener
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        if (!s.equals("")) {
                            mHistoryQuery = s;
                            MainFragment mf = (MainFragment) getActivity().getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_main);
                            getLoaderManager().restartLoader(RECENT_LOC_LOADER, null, mf);
                        }
                        // return true if the action was handled by the listener
                        return true;
                    }
                });

                searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                    @Override
                    public boolean onSuggestionSelect(int position) {
                        return false;
                    }

                    @Override
                    public boolean onSuggestionClick(int position) {
                        Cursor cursor = (Cursor)searchView.getSuggestionsAdapter().getItem(position);
                        addLocation(cursor);
                        // true if the listener handles the event and wants to override the default
                        // behavior of launching any intent or submitting a search query specified
                        // on that item.
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search) {
            getActivity().onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void performLocationSearch(String query) {
        // Delete previous results
        getActivity().getContentResolver().delete(
                ToursContract.OSMEntry.CONTENT_URI,
                null,
                null
        );
        // Arrange visibility of views.
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.showLocationRelativeLayout(false);
        mToursListView.setVisibility(View.GONE);
        mProgressBarLayout.setVisibility(View.VISIBLE);

        // Set animation while loading results
        RotateAnimation animation = new RotateAnimation
                (0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(1500);
        mProgressBarView.startAnimation(animation);

        // do something with s, the entered string
        onLocationChanged(query);
    }

    public void onLocationChanged(String requestedLocation) {
        Intent intent = new Intent(getActivity(), LocationService.class);
        intent.putExtra(Intent.EXTRA_TEXT, requestedLocation);
        getActivity().startService(intent);
    }

    // package-shared to be called from main activity
    void updateLocationViews(boolean locationFound) {
        String locationDisplay;
        if (locationFound) {
            locationDisplay = Utility.getPreferredLocationName(getActivity().getApplicationContext());
        }
        else {
            locationDisplay = getString(R.string.search_not_found);
            Toast.makeText(getActivity(), locationDisplay, Toast.LENGTH_LONG).show();
        }

        // Stop the rotating animation and set visibility attribute
        mProgressBarView.setAnimation(null);
        mProgressBarLayout.setVisibility(View.GONE);
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.showLocationRelativeLayout(true);
        mToursListView.setVisibility(View.VISIBLE);
    }

    private void addLocation(Cursor cursor) {
        // Read contents from cursor.
        long osmID = cursor.getLong(COL_OSM_ID);
        String locationName = cursor.getString(COL_LOCATION_NAME);
        String locationType = cursor.getString(COL_LOCATION_TYPE);
        double latitude = cursor.getDouble(COL_COORD_LAT);
        double longitude = cursor.getDouble(COL_COORD_LONG);

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

        // Insert contents into location table.
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

        if (requestCode == LOCATIONS_REQUEST) {
            if(resultCode == RESULT_OK) {
                updateLocationViews(true);
            }
            if (resultCode == RESULT_CANCELED) {
                updateLocationViews(false);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // The only loader that has a visible list view in this activity.
        getLoaderManager().initLoader(SLOTS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.
        if (i == LOCATIONS_LOADER) {
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
        else if (i == RECENT_LOC_LOADER) {
            String sortOrder = ToursContract.LocationEntry._ID + " DESC";
            Uri queryLocationUri = ToursContract.LocationEntry.CONTENT_URI;
            String selection = null;
            if (!mHistoryQuery.equals("")) {
                selection = ToursContract.LocationEntry.TABLE_NAME +
                        "." + ToursContract.LocationEntry.COLUMN_LOCATION_NAME +
                        " LIKE '%" + mHistoryQuery + "%'";
                return new CursorLoader(
                        getActivity(),
                        queryLocationUri,
                        OSM_COLUMNS,
                        selection,
                        null,
                        sortOrder
                );
            }
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (cursorLoader.getId() == LOCATIONS_LOADER) {
            // returns false if the cursor is empty
            if (!cursor.moveToFirst()) {
                // Release resources.
                cursor.close();
                updateLocationViews(false);
            }
            // returns false if the cursor move failed, i.e. there is no second row -
            // a single result was found
            else if (!cursor.moveToNext()) {
                cursor.moveToFirst();
                addLocation(cursor);
            }
            // there are at least two rows in the cursor, show results in search fragment
            else {
                cursor.close();
                launchSearchFragment();
            }
        }
        else if (cursorLoader.getId() == RECENT_LOC_LOADER) {
            mRecentLocationAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (loader.getId() == RECENT_LOC_LOADER) {

            // Create delay so the app won't crash.
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        // Close cursor after 2 seconds.
                        sleep(HISTORY_ADAPTER_RESET_IN_SECONDS * 1000);
                        mRecentLocationAdapter.swapCursor(null);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
        }
        else
        if (loader.getId() == SLOTS_LOADER) {
            // TODO: complete using adapter
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_LOCATION_SERVICE_DONE));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainFragment mf = (MainFragment)getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            getLoaderManager().restartLoader(LOCATIONS_LOADER, null, mf);
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}

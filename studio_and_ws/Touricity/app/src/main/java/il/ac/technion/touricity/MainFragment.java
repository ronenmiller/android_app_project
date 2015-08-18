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
import android.widget.TextView;
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

    public static final String BROADCAST_SERVICE_DONE = "broadcast_service_done";

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

    private long mLocationId = -1;
    private String mLocationName = null;

    private String mLastSearch = "";

    private RecentLocationAdapter mRecentLocationAdapter;
    private String mQuery = "";

    TextView mTextView;
    ListView mListView;
    FrameLayout mFrameLayout;
    ImageView mImageView;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mQuery = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecentLocationAdapter = new RecentLocationAdapter(getActivity(), null, 0);

        mTextView = (TextView)rootView.findViewById(R.id.textview_location_main);
        // TODO: add touch selectors to all the lists views in the app
        mListView = (ListView)rootView.findViewById(R.id.listview_main);
        mFrameLayout = (FrameLayout) rootView.findViewById(R.id.framelayout_main);
        mImageView = (ImageView)rootView.findViewById(R.id.imageview_main);

        mFrameLayout.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager)getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView)menu
                    .findItem(R.id.search_location).getActionView();
            if (searchView != null) {
                searchView.setSearchableInfo(searchManager.
                        getSearchableInfo(getActivity().getComponentName()));
                searchView.setIconifiedByDefault(true);
                searchView.setQueryHint(getResources().getString(R.string.search_hint));

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        // TODO: stop searching if internet connection is not available

                        if (!s.equals(mLastSearch)) {
                            mLastSearch = s;
                            performLocationSearch(s);
                        }
                        // return true if the query has been handled by the listener
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        loadHistory(searchView, s);
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
                        Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                        String locationName = cursor.getString(COL_LOCATION_NAME);
                        if (!locationName.equals(mLastSearch)) {
                            mLastSearch = locationName;
                            performLocationSearch(locationName);
                        }
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

        if (id == R.id.search_location) {
            getActivity().onSearchRequested();
            return true;
        }
        if (id == R.id.action_settings) {
            Context context = getActivity();
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadHistory(SearchView searchView, String query) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mQuery = query;
            searchView.setSuggestionsAdapter(mRecentLocationAdapter);
            MainFragment mf = (MainFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            getLoaderManager().restartLoader(RECENT_LOC_LOADER, null, mf);
        }

    }

    private void performLocationSearch(String query) {
        // Delete previous results
        getActivity().getContentResolver().delete(
                ToursContract.OSMEntry.CONTENT_URI,
                null,
                null
        );
        // Arrange visibility of views.
        mTextView.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);

        // Set animation while loading results
        RotateAnimation animation = new RotateAnimation
                (0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//                        RotateAnimation animation = new RotateAnimation(0f, 350f, 15f, 15f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(1500);
        mImageView.startAnimation(animation);

        // do something with s, the entered string
        onLocationChanged(query);
    }

    public void onLocationChanged(String requestedLocation) {
        Intent intent = new Intent(getActivity(), LocationService.class);
        intent.putExtra(Intent.EXTRA_TEXT, requestedLocation);
        getActivity().startService(intent);
    }

    // package-shared to be called from main activity
    void updateLocation(long id, String name) {
        // TODO: save important values to preferences
        mLocationId = id;
        mLocationName = name;
        mTextView.setText(mLocationName);

        // Stop the rotating animation and set visibility attribute
        mImageView.setAnimation(null);
        mFrameLayout.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.VISIBLE);
    }

    private void addLocation(Cursor cursor) {
        // Read contents from cursor.
        long osmID = cursor.getLong(COL_OSM_ID);
        String locationName = cursor.getString(COL_LOCATION_NAME);
        String locationType = cursor.getString(COL_LOCATION_TYPE);
        double latitude = cursor.getDouble(COL_COORD_LAT);
        double longitude = cursor.getDouble(COL_COORD_LONG);

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

        // TODO: also update preferences
        // Update views.
        updateLocation(osmID, locationName);
    }

    private void launchSearchFragment() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        getActivity().startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
            if (!mQuery.equals("")) {
                selection = ToursContract.LocationEntry.TABLE_NAME +
                        "." + ToursContract.LocationEntry.COLUMN_LOCATION_NAME +
                        " LIKE '%" + mQuery + "%'";
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
                String msg = getString(R.string.search_not_found);
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                updateLocation(-1, msg);
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
                // TODO: might need to delete
//                cursor.moveToFirst();
//                // move before the first row
//                cursor.moveToPrevious();
                launchSearchFragment();
            }
        }
        else if (cursorLoader.getId() == RECENT_LOC_LOADER) {
            mRecentLocationAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (loader.getId() == RECENT_LOC_LOADER) {
            mRecentLocationAdapter.swapCursor(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset history settings.
        mQuery = "";
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_SERVICE_DONE));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainFragment mf = (MainFragment) getActivity().getSupportFragmentManager()
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
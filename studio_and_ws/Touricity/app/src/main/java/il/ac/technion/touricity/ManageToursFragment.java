package il.ac.technion.touricity;

import android.content.BroadcastReceiver;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.ManageToursLoaderService;

/**
 * A placeholder fragment containing a simple view.
 */
public class ManageToursFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    public final String LOG_TAG = ManageToursFragment.class.getSimpleName();

    private static final int TOURS_LOADER = 0;

    public static final String BROADCAST_MANAGE_TOURS_LOADER_SERVICE_DONE = "broadcast_manage_tours_loader_service_done";

    // package-shared
    static final String[] TOUR_COLUMNS = {
            // Used for projection.
            // _ID must be used in every projection
            ToursContract.TourEntry.TABLE_NAME + "." + ToursContract.TourEntry._ID,
            ToursContract.TourEntry.COLUMN_TOUR_TITLE,
            ToursContract.LocationEntry.COLUMN_LOCATION_NAME,
            ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE,
            ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME,
            ToursContract.TourEntry.COLUMN_TOUR_RATING,
    };

    static final int COL_TOUR_ID = 0;
    static final int COL_TOUR_TITLE = 1;
    static final int COL_TOUR_LOCATION_NAME = 2;
    static final int COL_TOUR_LANGUAGE = 3;
    static final int COL_TOUR_LANGUAGE_NAME = 4;
    static final int COL_TOUR_RATING = 5;

    private ManageToursAdapter mManageToursAdapter;

    private View mHeaderView;
    private TextView mHeaderText;
    private ListView mToursListView;
    private SwipeRefreshLayout mSwipeLayout;

    private static final String SELECTED_KEY = "selected_position";
    private int mPosition = ListView.INVALID_POSITION;

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
    }


    public ManageToursFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_tours, container, false);

        mManageToursAdapter = new ManageToursAdapter(getActivity(), null, 0);

        mToursListView = (ListView)rootView.findViewById(R.id.listview_manage_tours);

        mSwipeLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorScheme(R.color.touricity_teal,
                R.color.touricity_light_teal,
                R.color.touricity_light_grey);

        mHeaderView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_header, null, false);
        mHeaderText = (TextView)mHeaderView.findViewById(R.id.list_item_header);

        mToursListView.setAdapter(mManageToursAdapter);
        mToursListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor)adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int tourId = cursor.getInt(MainFragment.COL_TOUR_ID);
                    Uri tourUri = ToursContract.TourEntry.buildTourIdUri(tourId);
                    ((Callback)getActivity()).onItemSelected(tourUri);
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
        }

        return rootView;
    }

    @Override
    public void onRefresh() {
        Intent intent = new Intent(getActivity(), ManageToursLoaderService.class);
        getActivity().startService(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
    }

    public void onDeleteTour() {
        getActivity().getSupportLoaderManager().restartLoader(TOURS_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = new Intent(getActivity(), ManageToursLoaderService.class);
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.
        // Sort order:  By tour rating, highest is first.
        String sortOrder = ToursContract.TourEntry.COLUMN_TOUR_RATING + " DESC";

        String managerId = Utility.getLoggedInUserId(getActivity().getApplicationContext());
        if (managerId == null) {
            return null;
        }

        Uri tourForManagerUri = ToursContract.TourEntry.buildTourManagerUri(managerId);

        return new CursorLoader(
                getActivity(),
                tourForManagerUri,
                TOUR_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.d(LOG_TAG, "Tours manager cursor returned " + cursor.getCount() + " rows.");
        mManageToursAdapter.swapCursor(cursor);

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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mManageToursAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mToursReceiver,
                new IntentFilter(BROADCAST_MANAGE_TOURS_LOADER_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_LOCATIONS_LOADER_SERVICE_DONE event.
    private BroadcastReceiver mToursReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and load the results.
            Log.d(LOG_TAG, "Tours manager broadcast received.");
            mSwipeLayout.setRefreshing(false);
            ManageToursFragment mtf = (ManageToursFragment)getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_manage_tours);
            getActivity().getSupportLoaderManager().restartLoader(TOURS_LOADER, null, mtf);
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mToursReceiver);
        super.onPause();
    }
}

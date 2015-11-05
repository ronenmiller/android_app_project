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
import il.ac.technion.touricity.service.MyToursLoaderService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MyToursFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    public final String LOG_TAG = MyToursFragment.class.getSimpleName();

    private static final int RESERVATIONS_LOADER = 0;

    public static final String BROADCAST_MY_TOURS_LOADER_SERVICE_DONE = "broadcast_my_tours_loader_service_done";

    // package-shared
    private static final String[] RESERVATIONS_COLUMNS = {
            ToursContract.SlotEntry.TABLE_NAME + "." + ToursContract.SlotEntry._ID,
            ToursContract.SlotEntry.COLUMN_SLOT_DATE,
            ToursContract.SlotEntry.COLUMN_SLOT_TIME,
            ToursContract.TourEntry.COLUMN_TOUR_RATING,
            ToursContract.TourEntry.COLUMN_TOUR_TITLE,
            ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE,
            ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME
    };

    // These indices are tied to RESERVATIONS_COLUMNS.  If RESERVATIONS_COLUMNS changes, these
    // must change.
    public static final int COL_SLOT_ID = 0;
    public static final int COL_SLOT_DATE = 1;
    public static final int COL_SLOT_TIME = 2;
    public static final int COL_TOUR_RATING = 3;
    public static final int COL_TOUR_TITLE = 4;
    public static final int COL_TOUR_LANGUAGE = 5;
    public static final int COL_TOUR_LANGUAGE_NAME = 6;

    private MyToursAdapter mMyToursAdapter;

    private View mHeaderView;
    private TextView mHeaderText;
    private ListView mReservationsListView;
    private SwipeRefreshLayout mSwipeLayout;

    private boolean mOnRate = false;

    private static final String SELECTED_KEY = "selected_position";
    private int mPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * MyToursFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri slotUri);
    }


    public MyToursFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_tours, container, false);

        mMyToursAdapter = new MyToursAdapter(getActivity(), null, 0);

        mReservationsListView = (ListView)rootView.findViewById(R.id.listview_my_tours);

        mSwipeLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorScheme(R.color.touricity_teal,
                R.color.touricity_light_teal,
                R.color.touricity_light_grey);

        mHeaderView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_header, null, false);
        mHeaderText = (TextView)mHeaderView.findViewById(R.id.list_item_header);

        mReservationsListView.setAdapter(mMyToursAdapter);
        mReservationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int slotId = cursor.getInt(COL_SLOT_ID);
                    Uri slotUri = ToursContract.SlotEntry.buildSlotIdUri(slotId);
                    ((Callback) getActivity()).onItemSelected(slotUri);
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
        Intent intent = new Intent(getActivity(), MyToursLoaderService.class);
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

    public void onDeleteReservation() {
        getActivity().getSupportLoaderManager().restartLoader(RESERVATIONS_LOADER, null, this);
    }

    public void onRate() {
        getActivity().getSupportLoaderManager().restartLoader(RESERVATIONS_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = new Intent(getActivity(), MyToursLoaderService.class);
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = ToursContract.SlotEntry.COLUMN_SLOT_DATE + " ASC, " +
                ToursContract.SlotEntry.COLUMN_SLOT_TIME + " ASC";

        String userId = Utility.getLoggedInUserId(getActivity().getApplicationContext());
        if (userId == null) {
            return null;
        }

        String selection = ToursContract.ReservationEntry.TABLE_NAME + "." +
                ToursContract.ReservationEntry.COLUMN_RESERVATION_USER_ID + " = ?";

        return new CursorLoader(
                getActivity(),
                ToursContract.ReservationEntry.CONTENT_URI,
                RESERVATIONS_COLUMNS,
                selection,
                new String[]{userId},
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.d(LOG_TAG, "My tours cursor returned " + cursor.getCount() + " rows.");
        mMyToursAdapter.swapCursor(cursor);

        if (cursor.getCount() > 0) {
            mReservationsListView.removeHeaderView(mHeaderView);
            mReservationsListView.setClickable(true);
            if (mPosition != ListView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mReservationsListView.smoothScrollToPosition(mPosition);
            }
        }
        else {
            mReservationsListView.removeHeaderView(mHeaderView);
            String reservationsNotFound = getString(R.string.reservations_not_found);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mHeaderText.setText(reservationsNotFound);
                mReservationsListView.addHeaderView(mHeaderView);
                mReservationsListView.setClickable(false);
            }
            else {
                Toast.makeText(getActivity(), reservationsNotFound, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mMyToursAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReservationsReceiver,
                new IntentFilter(BROADCAST_MY_TOURS_LOADER_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_MY_TOURS_LOADER_SERVICE_DONE event.
    private BroadcastReceiver mReservationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and load the results.
            Log.d(LOG_TAG, "My tours broadcast received.");
            mSwipeLayout.setRefreshing(false);
            MyToursFragment mtf = (MyToursFragment)getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_my_tours);
            getActivity().getSupportLoaderManager().restartLoader(RESERVATIONS_LOADER, null, mtf);
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReservationsReceiver);
        super.onPause();
    }
}

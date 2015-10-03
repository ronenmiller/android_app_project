package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.data.ToursContract.SlotEntry;


/**
 * A placeholder fragment containing a simple view.
 */
public class SlotsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = SlotsFragment.class.getSimpleName();

    private static final String SLOT_URI = "SLOT_URI";
    private static final int SLOTS_LOADER = 0;

    private Uri mUri = null;

    private ListView mSlotsListView;
    private SlotsAdapter mSlotsAdapter;

    private View mHeaderView;
    private TextView mHeaderText;
    private View mFooterView;

    private static final String SELECTED_KEY = "selected_position";
    private int mPosition = ListView.INVALID_POSITION;

    private static final String[] SLOTS_COLUMNS = {
            SlotEntry.TABLE_NAME + "." + SlotEntry._ID,
            ToursContract.UserEntry.COLUMN_USER_NAME,
            ToursContract.UserEntry.COLUMN_USER_RATING,
            SlotEntry.COLUMN_SLOT_DATE,
            SlotEntry.COLUMN_SLOT_TIME,
            SlotEntry.COLUMN_SLOT_VACANT,
    };

    // These indices are tied to SLOT_COLUMNS.  If SLOT_COLUMNS changes, these
    // must change.
    public static final int COL_SLOT_ID = 0;
    public static final int COL_SLOT_GUIDE_NAME = 1;
    public static final int COL_SLOT_GUIDE_RATING = 2;
    public static final int COL_SLOT_DATE = 3;
    public static final int COL_SLOT_TIME = 4;
    public static final int COL_SLOT_VACANT = 5;

    private MenuItem mCreateSlotMenuItem;

    public interface Callback {
        void onCreateSlot(Uri tourUri);
    }

    public SlotsFragment() {
    }

    public static SlotsFragment newInstance(Uri uri) {
        SlotsFragment f = new SlotsFragment();

        if (uri != null) {
            Bundle args = new Bundle();
            args.putParcelable(SLOT_URI, uri);
            f.setArguments(args);
        }

        return f;
    }

    public Uri getShownUri() {
        if (getArguments() != null)
            return getArguments().getParcelable(SLOT_URI);
        else {
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUri = getShownUri();

        View rootView = inflater.inflate(R.layout.fragment_slots, container, false);

        mSlotsAdapter = new SlotsAdapter(getActivity(), null, 0);

        mSlotsListView = (ListView)rootView.findViewById(R.id.listview_slots);

        mHeaderView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_header, null, false);
        mHeaderText = (TextView)mHeaderView.findViewById(R.id.list_item_header);
        mHeaderText.setText(getResources().getString(R.string.slots_not_found));
        mFooterView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_create_slot, null, false);

        boolean isUserLoggedIn = Utility.getIsLoggedIn(getActivity().getApplicationContext());
        boolean isUserGuide = Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext());

        if (isUserLoggedIn && isUserGuide) {
            mSlotsListView.addFooterView(mFooterView);
        }
        else {
            mSlotsListView.removeFooterView(mFooterView);
        }

        mSlotsListView.setAdapter(mSlotsAdapter);

        mSlotsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    if (!Utility.getIsLoggedIn(getActivity().getApplicationContext())) {
                        Utility.showLoginDialog(getActivity());
                        return;
                    }
                    long slotId = cursor.getLong(COL_SLOT_ID);
                    int slotVacant = cursor.getInt(COL_SLOT_VACANT);
                    Uri slotUri = SlotEntry.buildSlotIdUri(slotId, slotVacant);
                    Utility.showReserveSlotDialog(getActivity(), slotUri);
                }
                else {
                    if (Utility.getIsLoggedIn(getActivity().getApplicationContext()) &&
                            Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext())) {
                        if (mSlotsListView.getLastVisiblePosition() == position) {
                            ((Callback)getActivity()).onCreateSlot(mUri);
                        }
                    }
                }
                mPosition = position;
            }
        });

        return rootView;
    }

    // Called on login or logout.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void showGuideOptions(boolean show) {
        if (show) {
            if (mSlotsListView != null) {
                mSlotsListView.addFooterView(mFooterView);
            }
            if (mCreateSlotMenuItem != null) {
                mCreateSlotMenuItem.setVisible(true);
            }
        }
        else {
            if (mSlotsListView != null) {
                mSlotsListView.removeFooterView(mFooterView);
            }
            if (mCreateSlotMenuItem != null) {
                mCreateSlotMenuItem.setVisible(false);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_slots, menu);

        mCreateSlotMenuItem = menu.findItem(R.id.action_create_slot);

        if (Utility.getIsLoggedIn(getActivity().getApplicationContext()) &&
                Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext())) {
            mCreateSlotMenuItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_create_slot) {
            ((Callback)getActivity()).onCreateSlot(mUri);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(SLOTS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri == null) {
            return null;
        }

        int tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
        Uri uri = SlotEntry.CONTENT_URI;
        String selection = SlotEntry.TABLE_NAME +
                "." + SlotEntry.COLUMN_SLOT_TOUR_ID + " = ?";
        String sortOrder = SlotEntry.COLUMN_SLOT_DATE + " ASC, " +
                SlotEntry.COLUMN_SLOT_TIME + " ASC";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                uri,
                SLOTS_COLUMNS,
                selection,
                new String[]{Integer.toString(tourId)},
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG, "Slots cursor returned " + cursor.getCount() + " rows.");
        mSlotsAdapter.swapCursor(cursor);
        if (Utility.getIsLoggedIn(getActivity().getApplicationContext()) &&
                Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext())) {
            if (mCreateSlotMenuItem != null) {
                mCreateSlotMenuItem.setVisible(true);
            }
        }

        if (cursor.getCount() > 0) {
            mSlotsListView.removeHeaderView(mHeaderView);
            if (mPosition != ListView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mSlotsListView.smoothScrollToPosition(mPosition);
            }
        }
        else {
            // Slots not found.
            mSlotsListView.removeHeaderView(mHeaderView);
            String slotsNotFound = getString(R.string.slots_not_found);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSlotsListView.addHeaderView(mHeaderView);
                mHeaderView.setClickable(false);
            }
            else {
                Toast.makeText(getActivity(), slotsNotFound, Toast.LENGTH_LONG).show();
            }
        }


        mSlotsAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mSlotsListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSlotsAdapter.swapCursor(null);
    }
}

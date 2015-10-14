package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.data.ToursContract.TourEntry;
import il.ac.technion.touricity.service.SlotReservationsLoaderService;


/**
 * A placeholder fragment containing a simple view.
 */
public class SlotReservationsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = SlotReservationsFragment.class.getSimpleName();

    public static final String BROADCAST_SLOT_RESERVATIONS_LOADER_SERVICE_DONE = "broadcast_slot_reservations_loader_service_done";

    private static final String SLOT_URI = "URI";
    private static final int SLOT_LOADER = 0;
    private static final int RESERVATIONS_LOADER = 1;

    public static final String INTENT_EXTRA_SLOT_ID = "extra_slot_id";

    private Uri mUri = null;

    private String mLocation;
    private long mSlotId;
    private int mTourId;

    private MenuItem mMapMenuItem;
    private boolean mIsSlotVisible = false;

    private static final String[] SLOT_COLUMNS = {
            ToursContract.SlotEntry.TABLE_NAME + "." + ToursContract.SlotEntry._ID,
            TourEntry.TABLE_NAME + "." + TourEntry._ID,
            TourEntry.COLUMN_TOUR_TITLE,
            TourEntry.COLUMN_TOUR_DURATION,
            TourEntry.COLUMN_TOUR_LANGUAGE,
            ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME,
            TourEntry.COLUMN_TOUR_LOCATION,
            ToursContract.SlotEntry.COLUMN_SLOT_DATE,
            ToursContract.SlotEntry.COLUMN_SLOT_TIME,
    };

    // These indices are tied to SLOT_COLUMNS. If SLOT_COLUMNS changes, these must change.
    public static final int COL_SLOT_ID = 0;
    public static final int COL_TOUR_ID = 1;
    public static final int COL_TOUR_TITLE = 2;
    public static final int COL_TOUR_DURATION = 3;
    public static final int COL_TOUR_LANGUAGE = 4;
    public static final int COL_TOUR_LANGUAGE_NAME = 5;
    public static final int COL_TOUR_LOCATION = 6;
    public static final int COL_SLOT_DATE = 7;
    public static final int COL_SLOT_TIME = 8;

    private static final String[] SLOT_RESERVATIONS_COLUMNS = {
            ToursContract.UserEntry.TABLE_NAME + "." + ToursContract.UserEntry._ID,
            ToursContract.UserEntry.COLUMN_USER_EMAIL,
            ToursContract.ReservationEntry.COLUMN_RESERVATION_PARTICIPANTS,
    };

    // These indices are tied to SLOT_RESERVATIONS_COLUMNS. If SLOT_RESERVATIONS_COLUMNS changes,
    // these must change.
    public static final int COL_USER_ID = 0;
    public static final int COL_USER_EMAIL = 1;
    public static final int COL_RESERVATION_PARTICIPANTS = 2;

    private SlotReservationsAdapter mSlotReservationsAdapter;

    private ImageView mLanguageIconView;
    private TextView mTitleView;
    private TextView mDurationView;
    private TextView mLanguageView;
    private TextView mLocationView;
    private TextView mDateTimeView;
    private ListView mReservationsListView;
    private TextView mRightFooterView;

    private HorizontalScrollView mPhotosView;
    private LinearLayout mCommentsView;

    private Button mDeleteSlotBtn;

    private View mSlotReservationsFormView;
    private View mProgressView;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * SlotReservationsFragmentCallback for when an item has been selected.
         */
        void onEditSlot(Uri tourUri);
    }

    public SlotReservationsFragment() {
        setHasOptionsMenu(true);
    }

    public static SlotReservationsFragment newInstance(Uri uri) {
        SlotReservationsFragment f = new SlotReservationsFragment();

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

        if (savedInstanceState != null) {
            mIsSlotVisible = true;
        }
        else {
            mIsSlotVisible = false;
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUri = getShownUri();

        mSlotReservationsAdapter = new SlotReservationsAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_slot_reservations, container, false);

        mLanguageIconView = (ImageView)rootView.findViewById(R.id.slot_reservations_language_icon);
        mTitleView = (TextView)rootView.findViewById(R.id.slot_reservations_tour_title);
        mDurationView = (TextView)rootView.findViewById(R.id.slot_reservations_tour_duration);
        mLanguageView = (TextView)rootView.findViewById(R.id.slot_reservations_tour_language);
        mLocationView = (TextView)rootView.findViewById(R.id.slot_reservations_tour_location);
        mDateTimeView = (TextView)rootView.findViewById(R.id.slot_reservations_date_time);
        mReservationsListView = (ListView)rootView.findViewById(R.id.slot_reservations_listview);

       View headerView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_slot_reservations, null, false);
        TextView leftHeaderView = (TextView)headerView.findViewById(R.id.list_item_slot_reservations_left);
        TextView rightHeaderView = (TextView)headerView.findViewById(R.id.list_item_slot_reservations_right);
        leftHeaderView.setText(getString(R.string.slot_reservations_email));
        leftHeaderView.setTextColor(getResources().getColor(R.color.touricity_teal));
        rightHeaderView.setText(getString(R.string.slot_reservations_reserved));
        rightHeaderView.setTextColor(getResources().getColor(R.color.touricity_teal));
        headerView.setClickable(false);
        headerView.setEnabled(false);
        mReservationsListView.addHeaderView(headerView);

        View footerView = ((LayoutInflater)getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_slot_reservations, null, false);
        TextView leftFooterView = (TextView)footerView.findViewById(R.id.list_item_slot_reservations_left);
        mRightFooterView = (TextView)footerView.findViewById(R.id.list_item_slot_reservations_right);
        leftFooterView.setText(getString(R.string.slot_reservations_total));
        leftFooterView.setTextColor(getResources().getColor(R.color.touricity_teal));
        mRightFooterView.setTextColor(getResources().getColor(R.color.touricity_teal));
        footerView.setClickable(false);
        footerView.setEnabled(false);
        mReservationsListView.addFooterView(footerView);

        mReservationsListView.setAdapter(mSlotReservationsAdapter);

        Button editSlotBtn = (Button)rootView.findViewById(R.id.slot_reservations_edit_slot_btn);

        editSlotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    return;
                }

                Uri tourAndSlotUri = TourEntry.buildTourIdUri(mTourId)
                        .buildUpon().appendPath(Long.toString(mSlotId)).build();
                ((Callback)getActivity()).onEditSlot(tourAndSlotUri);
            }
        });

        mDeleteSlotBtn = (Button)rootView.findViewById(R.id.slot_reservations_delete_slot_btn);
        mDeleteSlotBtn.setEnabled(false);
        mDeleteSlotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    return;
                }

                Utility.showDeleteSlotDialog(getActivity(), mUri);
            }
        });

        mSlotReservationsFormView = rootView.findViewById(R.id.slot_reservations_scrollview_form);
        mProgressView = rootView.findViewById(R.id.slot_reservations_progressbar);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_detail, menu);

        mMapMenuItem = menu.findItem(R.id.action_map);
        mMapMenuItem.setVisible(mIsSlotVisible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_map) {
            openTourLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openTourLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (mLocation == null) {
            return;
        }

        Uri geoLocation = Uri.parse("geo:0,0?q=" + mLocation);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSlotReservationsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSlotReservationsFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSlotReservationsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mSlotReservationsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getSupportLoaderManager().restartLoader(SLOT_LOADER, null, this);

        if (mUri == null) {
            return;
        }

        long slotId = ToursContract.SlotEntry.getSlotIdFromUri(mUri);
        Intent intent = new Intent(getActivity(), SlotReservationsLoaderService.class);
        intent.putExtra(INTENT_EXTRA_SLOT_ID, slotId);
        showProgress(true);
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri == null) {
            return null;
        }

        long slotId = ToursContract.SlotEntry.getSlotIdFromUri(mUri);

        if (id == SLOT_LOADER) {

            Uri uri = ToursContract.SlotEntry.buildSlotIdUri(slotId);
            String selection = ToursContract.SlotEntry.TABLE_NAME + "."
                    + ToursContract.SlotEntry.COLUMN_SLOT_ACTIVE + " = 1";

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    uri,
                    SLOT_COLUMNS,
                    selection,
                    null,
                    null
            );
        }
        else if (id == RESERVATIONS_LOADER) {
            Uri uri = ToursContract.ReservationEntry.CONTENT_URI;
            String selection = ToursContract.ReservationEntry.TABLE_NAME + "." +
                    ToursContract.ReservationEntry._ID + " = ?";

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    uri,
                    SLOT_RESERVATIONS_COLUMNS,
                    selection,
                    new String[]{Long.toString(slotId)},
                    null
            );
        }
        else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == SLOT_LOADER) {
            if (data != null && data.moveToFirst()) {
                // Read language ID from cursor in order to set the icon.
                int languageId = data.getInt(COL_TOUR_LANGUAGE);
                // Set image source based on the language ID.
                mLanguageIconView.setImageResource(Utility.getLanguageIconIdForLanguageId(languageId));
                // For accessibility, add a content description to the icon field.
                String languageName = data.getString(COL_TOUR_LANGUAGE_NAME);
                mLanguageIconView.setContentDescription(languageName);

                // Read title from cursor.
                String title = data.getString(COL_TOUR_TITLE);
                // Set the tour's title.
                mTitleView.setText(title);

                // Read tour duration from cursor.
                int duration = data.getInt(COL_TOUR_DURATION);
                // Set the tour's duration.
                int durationFormatId = R.string.tour_duration;
                mDurationView.setText(getActivity().getString(durationFormatId, Integer.toString(duration)));

                // Set the tour's language.
                int languageFormatId = R.string.tour_language;
                mLanguageView.setText(getActivity().getString(languageFormatId, languageName));

                // Read tour location from cursor.
                mLocation = data.getString(COL_TOUR_LOCATION);
                // Set the tour's location.
                int locationFormatId = R.string.tour_location;
                mLocationView.setText(getActivity().getString(locationFormatId, mLocation));
                // Show the map menu item in the menu.
                mIsSlotVisible = true;
                if (mMapMenuItem != null) {
                    mMapMenuItem.setVisible(true);
                }

                Time dayTime = new Time();

                // Read julian date from cursor and translate it to a human-readable date string.
                int julianDate = data.getInt(COL_SLOT_DATE);
                long dateInMillis = dayTime.setJulianDay(julianDate);
                String formattedDate = Utility.getFriendlyDayString(getActivity(), dateInMillis);

                // Read local time string from the cursor.
                long timeInMillis = data.getLong(COL_SLOT_TIME);
                String formattedTime = Utility.getFriendlyTimeString(timeInMillis);
                int dateFormatId = R.string.format_full_friendly_date;

                // Set date and time text on the text view.
                mDateTimeView.setText(getString(
                        dateFormatId,
                        formattedDate,
                        formattedTime));

                mSlotId = data.getLong(COL_SLOT_ID);

                mTourId = data.getInt(COL_TOUR_ID);
                mDeleteSlotBtn.setEnabled(true);

                data.close();
            }
        }
        else if (loader.getId() == RESERVATIONS_LOADER) {
            mSlotReservationsAdapter.swapCursor(data);

            int totalReserved = 0;
            while (data.moveToNext()) {
                totalReserved += data.getInt(COL_RESERVATION_PARTICIPANTS);
            }
            mRightFooterView.setText(Integer.toString(totalReserved));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSlotReservationsAdapter.swapCursor(null);
        mRightFooterView.setText(0);
    }


    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSlotReservationsReceiver,
                new IntentFilter(BROADCAST_SLOT_RESERVATIONS_LOADER_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_SLOTS_LOADER_SERVICE_DONE event
    private BroadcastReceiver mSlotReservationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and set visibility attribute
            Log.d(LOG_TAG, "Slots reservations broadcast received.");
            showProgress(false);
            SlotReservationsFragment srf = (SlotReservationsFragment)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(SlotReservationsActivity.SLOT_RESERVATIONS_FRAGMENT_TAG);
            getActivity().getSupportLoaderManager().restartLoader(RESERVATIONS_LOADER, null, srf);
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSlotReservationsReceiver);
        super.onPause();
    }
}

package il.ac.technion.touricity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class ReservationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = ReservationDetailFragment.class.getSimpleName();

    private static final String DETAIL_URI = "URI";
    private static final int RESERVATION_LOADER = 1;

    private Uri mUri = null;

    private long mSlotId;
    private int mSlotCurrentCapacity;
    private String mLocation;
    private int mTourId;
    private String mGuideId;

    private MenuItem mMapMenuItem;
    private boolean mIsTourVisible = false;

    private Button mEditReservationBtn;
    private Button mDeleteReservationBtn;

    private static final String[] RESERVATION_COLUMNS = {
            ToursContract.SlotEntry.TABLE_NAME + "." + ToursContract.SlotEntry._ID,
            ToursContract.TourEntry.TABLE_NAME + "." + ToursContract.TourEntry._ID,
            ToursContract.TourEntry.COLUMN_TOUR_TITLE,
            ToursContract.SlotEntry.COLUMN_SLOT_GUIDE_ID,
            ToursContract.UserEntry.COLUMN_USER_NAME,
            ToursContract.SlotEntry.COLUMN_SLOT_DATE,
            ToursContract.SlotEntry.COLUMN_SLOT_TIME,
            ToursContract.SlotEntry.COLUMN_SLOT_CURRENT_CAPACITY,
            ToursContract.SlotEntry.COLUMN_SLOT_CANCELED,
            ToursContract.ReservationEntry.COLUMN_RESERVATION_PARTICIPANTS,
            ToursContract.TourEntry.COLUMN_TOUR_DURATION,
            ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE,
            ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME,
            ToursContract.TourEntry.COLUMN_TOUR_LOCATION,
            ToursContract.TourEntry.COLUMN_TOUR_RATING,
            ToursContract.UserEntry.COLUMN_USER_RATING,
            ToursContract.TourEntry.COLUMN_TOUR_DESCRIPTION,
            ToursContract.TourEntry.COLUMN_TOUR_PHOTOS,
            ToursContract.TourEntry.COLUMN_TOUR_COMMENTS
    };

    // These indices are tied to RESERVATION_COLUMNS.  If RESERVATION_COLUMNS changes, these
    // must change.
    public static final int COL_SLOT_ID = 0;
    public static final int COL_TOUR_ID = 1;
    public static final int COL_TOUR_TITLE = 2;
    public static final int COL_SLOT_GUIDE_ID = 3;
    public static final int COL_GUIDE_NAME = 4;
    public static final int COL_SLOT_DATE = 5;
    public static final int COL_SLOT_TIME = 6;
    public static final int COL_SLOT_CURRENT_CAPACITY = 7;
    public static final int COL_SLOT_CANCELED = 8;
    public static final int COL_RESERVATION_RESERVED = 9;
    public static final int COL_TOUR_DURATION = 10;
    public static final int COL_TOUR_LANGUAGE = 11;
    public static final int COL_TOUR_LANGUAGE_NAME = 12;
    public static final int COL_TOUR_LOCATION = 13;
    public static final int COL_TOUR_RATING = 14;
    public static final int COL_GUIDE_RATING = 15;
    public static final int COL_TOUR_DESCRIPTION = 16;
    public static final int COL_TOUR_PHOTOS = 17;
    public static final int COL_TOUR_COMMENTS = 18;


    private ImageView mLanguageIconView;
    private TextView mTitleView;
    private TextView mGuideView;
    private TextView mTimeView;
    private TextView mReservedView;
    private TextView mDurationView;
    private TextView mLanguageView;
    private TextView mLocationView;
    private RatingBar mTourRatingBar;
    private RatingBar mGuideRatingBar;
    private TextView mDescriptionView;
    private HorizontalScrollView mPhotosView;
    private LinearLayout mCommentsView;
    private TextView mCanceledView;
    private TextView mRateView;

    public ReservationDetailFragment() {
        setHasOptionsMenu(true);
    }

    public static ReservationDetailFragment newInstance(Uri uri) {
        ReservationDetailFragment f = new ReservationDetailFragment();

        if (uri != null) {
            Bundle args = new Bundle();
            args.putParcelable(DETAIL_URI, uri);
            f.setArguments(args);
        }

        return f;
    }

    public Uri getShownUri() {
        if (getArguments() != null)
            return getArguments().getParcelable(DETAIL_URI);
        else {
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mIsTourVisible = true;
        }
        else {
            mIsTourVisible = false;
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUri = getShownUri();

        View rootView = inflater.inflate(R.layout.fragment_reservation_detail, container, false);

        mLanguageIconView = (ImageView)rootView.findViewById(R.id.detail_language_icon);
        mTitleView = (TextView)rootView.findViewById(R.id.detail_tour_title);
        mGuideView = (TextView)rootView.findViewById(R.id.detail_tour_guide);
        mTimeView = (TextView)rootView.findViewById(R.id.detail_tour_time);
        mReservedView = (TextView)rootView.findViewById(R.id.detail_tour_reserved);
        mDurationView = (TextView)rootView.findViewById(R.id.detail_tour_duration);
        mLanguageView = (TextView)rootView.findViewById(R.id.detail_tour_language);
        mLocationView = (TextView)rootView.findViewById(R.id.detail_tour_location);
        mTourRatingBar = (RatingBar)rootView.findViewById(R.id.detail_tour_rating_bar);
        mGuideRatingBar = (RatingBar)rootView.findViewById(R.id.detail_guide_rating_bar);
        mDescriptionView = (TextView)rootView.findViewById(R.id.detail_tour_description);
        mCanceledView = (TextView)rootView.findViewById(R.id.detail_tour_canceled);
        mRateView = (TextView)rootView.findViewById(R.id.detail_tour_rate);

        mTourRatingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.showRatingDialog(getActivity(), mTourId, mGuideId);
                return true;
            }
        });

        mGuideRatingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.showRatingDialog(getActivity(), mTourId, mGuideId);
                return true;
            }
        });

        mEditReservationBtn = (Button)rootView.findViewById(R.id.detail_edit_reservation_btn);

        mEditReservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    return;
                }

                Uri slotUri = ToursContract.SlotEntry.buildSlotIdAndCurrentCapacityUri(mSlotId, mSlotCurrentCapacity);
                Utility.showReserveSlotDialog(getActivity(), slotUri);
            }
        });

        mDeleteReservationBtn = (Button) rootView.findViewById(R.id.detail_delete_reservation_btn);

        // On click, load slots from the server in order to check that no slots are attached
        // to this tour and deletion can occur.
        mDeleteReservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    return;
                }

                Utility.showDeleteReservationDialog(getActivity(), mUri);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_detail, menu);

        mMapMenuItem = menu.findItem(R.id.action_map);
        mMapMenuItem.setVisible(mIsTourVisible);
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

    public void onRate() {
        getActivity().getSupportLoaderManager().restartLoader(RESERVATION_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(RESERVATION_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri == null) {
            return null;
        }

        mSlotId = ToursContract.SlotEntry.getSlotIdFromUri(mUri);
        String userId = Utility.getLoggedInUserId(getActivity().getApplicationContext());
        Uri uri = ToursContract.ReservationEntry.CONTENT_URI;
        String selection = ToursContract.ReservationEntry.TABLE_NAME +
                        "." + ToursContract.ReservationEntry._ID + " = ? AND " +
                ToursContract.ReservationEntry.TABLE_NAME +
                "." + ToursContract.ReservationEntry.COLUMN_RESERVATION_USER_ID + " = ?";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                uri,
                RESERVATION_COLUMNS,
                selection,
                new String[]{Long.toString(mSlotId), userId},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Extract data for the rating dialog.
            mTourId = data.getInt(COL_TOUR_ID);
            mGuideId = data.getString(COL_SLOT_GUIDE_ID);

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

            String guide = data.getString(COL_GUIDE_NAME);
            int guideFormatId = R.string.detail_reservation_guide;
            mGuideView.setText(getString(
                    guideFormatId,
                    guide));

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
            mTimeView.setText(getString(
                    dateFormatId,
                    formattedDate,
                    formattedTime));

            int reserved = data.getInt(COL_RESERVATION_RESERVED);
            int reservedFormatId = R.string.detail_reservation_reserved;
            mReservedView.setText(getString(
                    reservedFormatId,
                    reserved));

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
            mIsTourVisible = true;
            if (mMapMenuItem != null) {
                mMapMenuItem.setVisible(true);
            }

            // Read tour rating from cursor.
            double tourRating = data.getInt(COL_TOUR_RATING);
            // Set the tour's rating.
            mTourRatingBar.setRating((float)tourRating);

            // Read guide rating from cursor.
            double guideRating = data.getInt(COL_GUIDE_RATING);
            // Set the guide's rating.
            mGuideRatingBar.setRating((float)guideRating);

            // Read description from cursor.
            String description = data.getString(COL_TOUR_DESCRIPTION);
            // Set the tour's description.
            mDescriptionView.setText(description);

            mSlotCurrentCapacity = data.getInt(COL_SLOT_CURRENT_CAPACITY);

            // Current duration is in minutes.
            long durationInMillis = duration * 60 * 1000;

            if (System.currentTimeMillis() > dateInMillis + durationInMillis) {
                mRateView.setVisibility(View.VISIBLE);
                mTourRatingBar.setEnabled(true);
                mGuideRatingBar.setEnabled(true);
                mDeleteReservationBtn.setEnabled(false);
                mEditReservationBtn.setEnabled(false);
            }
            else {
                mRateView.setVisibility(View.GONE);
                mTourRatingBar.setEnabled(false);
                mGuideRatingBar.setEnabled(false);
                mDeleteReservationBtn.setEnabled(true);
                mEditReservationBtn.setEnabled(true);
            }

            int isCanceled = data.getInt(COL_SLOT_CANCELED);
            if (isCanceled == 1) {
                mCanceledView.setVisibility(View.VISIBLE);
                mTourRatingBar.setEnabled(false);
                mGuideRatingBar.setEnabled(false);
                mDeleteReservationBtn.setEnabled(false);
                mEditReservationBtn.setEnabled(false);
            }
            else {
                mCanceledView.setVisibility(View.GONE);
            }

            data.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}

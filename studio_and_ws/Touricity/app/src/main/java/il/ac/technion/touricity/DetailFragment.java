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
import android.widget.RatingBar;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.data.ToursContract.TourEntry;
import il.ac.technion.touricity.service.SlotsLoaderService;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = DetailFragment.class.getSimpleName();

    public static final String BROADCAST_SLOTS_LOADER_SERVICE_DONE = "broadcast_slots_loader_service_done";

    private static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;

    public static final String INTENT_EXTRA_TOUR_ID = "extra_tour_id";
    public static final String INTENT_EXTRA_BTN_ID = "extra_btn_id";

    public static final int VIEW_SLOTS_BTN_ID = 0;
    public static final int DELETE_TOUR_BTN_ID = 1;

    private Uri mUri = null;

    private String mLocation;

    private MenuItem mMapMenuItem;
    private boolean mIsTourVisible = false;

    private static final String[] DETAIL_COLUMNS = {
            TourEntry.TABLE_NAME + "." + TourEntry._ID,
            TourEntry.COLUMN_TOUR_TITLE,
            TourEntry.COLUMN_TOUR_DURATION,
            TourEntry.COLUMN_TOUR_LANGUAGE,
            ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME,
            TourEntry.COLUMN_TOUR_LOCATION,
            TourEntry.COLUMN_TOUR_RATING,
            TourEntry.COLUMN_TOUR_DESCRIPTION,
            TourEntry.COLUMN_TOUR_PHOTOS,
            TourEntry.COLUMN_TOUR_COMMENTS
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_TOUR_ID = 0;
    public static final int COL_TOUR_TITLE = 1;
    public static final int COL_TOUR_DURATION = 2;
    public static final int COL_TOUR_LANGUAGE = 3;
    public static final int COL_TOUR_LANGUAGE_NAME = 4;
    public static final int COL_TOUR_LOCATION = 5;
    public static final int COL_TOUR_RATING = 6;
    public static final int COL_TOUR_DESCRIPTION = 7;
    public static final int COL_TOUR_PHOTOS = 8;
    public static final int COL_TOUR_COMMENTS = 9;

    private ImageView mLanguageIconView;
    private TextView mTitleView;
    private TextView mDurationView;
    private TextView mLanguageView;
    private TextView mLocationView;
    private RatingBar mRatingBar;
    private TextView mDescriptionView;
    private HorizontalScrollView mPhotosView;
    private LinearLayout mCommentsView;

    private Button mDeleteTourBtn;

    private View mDetailFormView;
    private View mProgressView;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onViewSlots(Uri tourUri);
    }

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(Uri uri) {
        DetailFragment f = new DetailFragment();

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

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mLanguageIconView = (ImageView)rootView.findViewById(R.id.detail_language_icon);
        mTitleView = (TextView)rootView.findViewById(R.id.detail_tour_title);
        mDurationView = (TextView)rootView.findViewById(R.id.detail_tour_duration);
        mLanguageView = (TextView)rootView.findViewById(R.id.detail_tour_language);
        mLocationView = (TextView)rootView.findViewById(R.id.detail_tour_location);
        mRatingBar = (RatingBar)rootView.findViewById(R.id.detail_tour_rating_bar);
        mDescriptionView = (TextView)rootView.findViewById(R.id.detail_tour_description);

        Button viewSlotsBtn = (Button)rootView.findViewById(R.id.detail_view_slots_btn);

        viewSlotsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    return;
                }

                int tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
                Intent intent = new Intent(getActivity(), SlotsLoaderService.class);
                intent.putExtra(INTENT_EXTRA_TOUR_ID, tourId);
                intent.putExtra(INTENT_EXTRA_BTN_ID, VIEW_SLOTS_BTN_ID);
                showProgress(true);
                getActivity().startService(intent);
            }
        });

        mDeleteTourBtn = (Button)rootView.findViewById(R.id.detail_delete_tour_btn);

        showGuideOptions();

        // On click, load slots from the server in order to check that no slots are attached
        // to this tour and deletion can occur.
        mDeleteTourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    return;
                }

                int tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
                Intent intent = new Intent(getActivity(), SlotsLoaderService.class);
                intent.putExtra(INTENT_EXTRA_TOUR_ID, tourId);
                intent.putExtra(INTENT_EXTRA_BTN_ID, DELETE_TOUR_BTN_ID);
                showProgress(true);
                getActivity().startService(intent);
            }
        });

        mDetailFormView = rootView.findViewById(R.id.detail_scrollview_form);
        mProgressView = rootView.findViewById(R.id.detail_progressbar);

        return rootView;
    }

    // Called on login or logout.
    public void showGuideOptions() {
        boolean isUserLoggedIn = Utility.getIsLoggedIn(getActivity().getApplicationContext());
        boolean isUserGuide = Utility.getLoggedInUserIsGuide(getActivity().getApplicationContext());

        if (isUserLoggedIn && isUserGuide) {
            if (mUri != null) {
                int tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
                String selection = TourEntry.TABLE_NAME + "." +
                        TourEntry._ID + " = ?";
                Cursor tourCursor = null;
                try {
                    tourCursor = getActivity().getContentResolver().query(
                            TourEntry.CONTENT_URI,
                            new String[]{TourEntry.COLUMN_TOUR_MANAGER_ID},
                            selection,
                            new String[]{Integer.toString(tourId)},
                            null
                    );

                    if (tourCursor != null && tourCursor.moveToNext()) {
                        String managerId = tourCursor.getString(0);
                        String loggedInUserId = Utility
                                .getLoggedInUserId(getActivity().getApplicationContext());
                        if (managerId.equals(loggedInUserId)) {
                            mDeleteTourBtn.setVisibility(View.VISIBLE);
                        }
                    }
                }
                finally {
                    if (tourCursor != null) {
                        tourCursor.close();
                    }
                }
            }
        }
        else {
            mDeleteTourBtn.setVisibility(View.GONE);
        }
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

            mDetailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDetailFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDetailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDetailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri == null) {
            return null;
        }

        int tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
        Uri uri = TourEntry.CONTENT_URI;
        String selection = ToursContract.TourEntry.TABLE_NAME +
                        "." + ToursContract.TourEntry._ID + " = ?";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                uri,
                DETAIL_COLUMNS,
                selection,
                new String[]{Integer.toString(tourId)},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
            mIsTourVisible = true;
            if (mMapMenuItem != null) {
                mMapMenuItem.setVisible(true);
            }

            // Read tour rating from cursor.
            double rating = data.getInt(COL_TOUR_RATING);
            // Set the tour's rating.
            mRatingBar.setRating((float) rating);

            // Read description from cursor.
            String description = data.getString(COL_TOUR_DESCRIPTION);
            // Set the tour's description.
            mDescriptionView.setText(description);

            data.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }


    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSlotReceiver,
                new IntentFilter(BROADCAST_SLOTS_LOADER_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_SLOTS_LOADER_SERVICE_DONE event
    private BroadcastReceiver mSlotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and set visibility attribute
            Log.d(LOG_TAG, "Slots broadcast received.");
            showProgress(false);
            if (intent != null) {
                if (intent.getIntExtra(INTENT_EXTRA_BTN_ID, -1) == VIEW_SLOTS_BTN_ID) {
                    ((Callback)getActivity()).onViewSlots(mUri);
                }
                else if (intent.getIntExtra(INTENT_EXTRA_BTN_ID, -1) == DELETE_TOUR_BTN_ID) {
                    Utility.showDeleteTourDialog(getActivity(), mUri);
                }
            }
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSlotReceiver);
        super.onPause();
    }
}

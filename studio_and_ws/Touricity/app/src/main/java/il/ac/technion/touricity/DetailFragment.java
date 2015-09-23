package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.content.Intent;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.data.ToursContract.TourEntry;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;

    private Uri mUri = null;

    private String mLocation;

    private MenuItem mMapMenuItem;
    private boolean mIsTourVisible = false;

    private static final String MAP_MENU_KEY = "map_menu_key";

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
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the map menu item needs to be saved.
        outState.putInt(MAP_MENU_KEY, mMapMenuItem.getItemId());
        super.onSaveInstanceState(outState);
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

        return rootView;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

        int tourId = (int)ToursContract.TourEntry.getIdFromUri(mUri);
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
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}

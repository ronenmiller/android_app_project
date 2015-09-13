package il.ac.technion.touricity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ImageView;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract.TourEntry;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;

    private Uri mUri = null;

    private static final String[] DETAIL_COLUMNS = {
            TourEntry.TABLE_NAME + "." + TourEntry._ID,
            TourEntry.COLUMN_TOUR_TITLE,
            TourEntry.COLUMN_TOUR_DURATION,
            TourEntry.COLUMN_TOUR_LOCATION,
            TourEntry.COLUMN_TOUR_RATING,
            TourEntry.COLUMN_TOUR_AVAILABLE,
            TourEntry.COLUMN_TOUR_DESCRIPTION,
            TourEntry.COLUMN_TOUR_THUMBNAIL,
            TourEntry.COLUMN_TOUR_PHOTOS,
            TourEntry.COLUMN_TOUR_LANGUAGES,
            TourEntry.COLUMN_TOUR_COMMENTS
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_TOUR_ID = 0;
    public static final int COL_TOUR_TITLE = 1;
    public static final int COL_TOUR_DURATION = 2;
    public static final int COL_TOUR_LOCATION = 3;
    public static final int COL_TOUR_RATING = 4;
    public static final int COL_TOUR_AVAILABLE = 5;
    public static final int COL_TOUR_DESCRIPTION = 6;
    public static final int COL_TOUR_THUMBNAIL = 7;
    public static final int COL_TOUR_PHOTOS = 8;
    public static final int COL_TOUR_LANGUAGES = 9;
    public static final int COL_TOUR_COMMENTS = 10;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

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

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        mUri = getShownUri();
//
//        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
//        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
//        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
//        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
//        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
//        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
//        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
//        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
//        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
//        return rootView;
//    }

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

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                mUri,
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        if (data != null && data.moveToFirst()) {
//            // Read weather condition ID from cursor
//            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
//            // Set image source based on the weather id
//            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
//
//            // Read date from cursor and update views for day of week and date
//            long date = data.getLong(COL_WEATHER_DATE);
//            String friendlyDateText = Utility.getDayName(getActivity(), date);
//            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
//            mFriendlyDateView.setText(friendlyDateText);
//            mDateView.setText(dateText);
//
//            // Read description from cursor and update view
//            String description = data.getString(COL_WEATHER_DESC);
//            mDescriptionView.setText(description);
//
//            // For accessibility, add a content description to the icon field
//            mIconView.setContentDescription(description);
//
//            // Read high temperature from cursor and update view
//            boolean isMetric = Utility.isMetric(getActivity());
//
//            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
//            String highString = Utility.formatTemperature(getActivity(), high, isMetric);
//            mHighTempView.setText(highString);
//
//            // Read low temperature from cursor and update view
//            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
//            String lowString = Utility.formatTemperature(getActivity(), low, isMetric);
//            mLowTempView.setText(lowString);
//
//            // Read humidity from cursor and update view
//            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
//            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
//
//            // Read wind speed and direction from cursor and update view
//            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
//            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
//            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
//
//            // Read pressure from cursor and update view
//            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
//            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
//
//            // We still need this for the share intent
//            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);
//        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}

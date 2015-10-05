package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.sync.TouricitySyncAdapter;

public class MainActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        LoginDialogFragment.LoginDialogListener,
        LogoutDialogFragment.LogoutDialogListener,
        DeleteTourDialogFragment.DeleteTourDialogListener,
        MainFragment.Callback,
        DetailFragment.Callback,
        SlotsFragment.Callback,
        DatePickerFragment.DatePickerDialogListener,
        TimePickerFragment.TimePickerDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String CREATE_TOUR_FRAGMENT_TAG = "CTFTAG";
    private static final String SLOTS_FRAGMENT_TAG = "SFTAG";
    private static final String CREATE_SLOT_FRAGMENT_TAG = "CSFTAG";

    static final int RECENT_LOCATIONS_LOADER = 0;

    private static final String RECENT_BUNDLE_KEY = "recent_bundle_key";

    // package-shared
    static final String[] RECENT_LOCATION_COLUMNS = {
            // Used for projection.
            // _ID must be used in every projection
            ToursContract.LocationEntry._ID,
            ToursContract.LocationEntry.COLUMN_LOCATION_ID,
            ToursContract.LocationEntry.COLUMN_LOCATION_NAME,
            ToursContract.LocationEntry.COLUMN_LOCATION_TYPE,
            ToursContract.LocationEntry.COLUMN_COORD_LAT,
            ToursContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to RECENT_LOCATION_COLUMNS.  If RECENT_LOCATION_COLUMNS changes, these
    // must change.
    static final int COL_RECENT_ID = 0;
    static final int COL_RECENT_LOCATION_ID = 1;
    static final int COL_RECENT_LOCATION_NAME = 2;
    static final int COL_RECENT_LOCATION_TYPE = 3;
    static final int COL_RECENT_COORD_LAT = 4;
    static final int COL_RECENT_COORD_LONG = 5;

    private boolean mTwoPane;

    private FrameLayout mToursSlotsDetailContainer;

    private LinearLayout mLocationLinearLayout;
    private TextView mLocationNameTextView;

    private MenuItem mLoginMenuItem;
    private MenuItem mSignupMenuItem;
    private MenuItem mLogoutMenuItem;
    private MenuItem mMyToursMenuItem;
    private MenuItem mManageToursMenuItem;
    private MenuItem mManageSlotsMenuItem;


    private RecentLocationAdapter mRecentLocationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeDatabase();

        setContentView(R.layout.activity_main);

        mRecentLocationAdapter = new RecentLocationAdapter(this, null, 0);

        mLocationLinearLayout = (LinearLayout)findViewById(R.id.linearlayout_location_main);
        mLocationNameTextView = (TextView)findViewById(R.id.textview_location_main);
        showLocationLinearLayout(false);
        Button locationMapButton = (Button)findViewById(R.id.location_map_btn);
        // Avoid making the map button's text with capital letters.
        locationMapButton.setTransformationMethod(null);
        locationMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreferredLocationInMap();
            }
        });

        mToursSlotsDetailContainer = (FrameLayout)findViewById(R.id.tours_slots_detail_container);
        if (mToursSlotsDetailContainer != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                mToursSlotsDetailContainer.setVisibility(View.INVISIBLE);
            }
        } else {
            mTwoPane = false;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0f);
            }
        }

        TouricitySyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mLoginMenuItem = menu.findItem(R.id.action_login);
        mSignupMenuItem = menu.findItem(R.id.action_signup);
        mLogoutMenuItem = menu.findItem(R.id.action_logout);
        mMyToursMenuItem = menu.findItem(R.id.action_my_tours);
        mManageToursMenuItem = menu.findItem(R.id.action_manage_tours);
        mManageSlotsMenuItem = menu.findItem(R.id.action_manage_slots);

        addSearchView(menu);

        if (Utility.getIsLoggedIn(getApplicationContext()))
        {
            showSignInMenuItems(false);
        }
        else {
            showSignInMenuItems(true);
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void addSearchView(Menu menu) {
        // Associate searchable configuration with the SearchView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView)menu
                    .findItem(R.id.action_search).getActionView();
            if (searchView != null) {
                searchView.setSearchableInfo(searchManager.
                        getSearchableInfo(getComponentName()));
                searchView.setIconifiedByDefault(true);
                searchView.setSubmitButtonEnabled(false);
                searchView.setQueryHint(getResources().getString(R.string.search_hint));
                searchView.setSuggestionsAdapter(mRecentLocationAdapter);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        // TODO: stop searching if internet connection is not available
                        MainFragment mf = (MainFragment)getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_main);
                        mf.performLocationSearch(s);
                        // return true if the query has been handled by the listener
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        if (!s.equals("")) {
                            if (s.length() >= 3) {
                                showSuggestions(s);
                            }
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
                        if (cursor != null) {
                            MainFragment mf = (MainFragment)getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_main);
                            mf.addLocation(cursor, false);
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

    private void showSuggestions(String s) {
        Bundle bundle = new Bundle();
        bundle.putString(RECENT_BUNDLE_KEY, s);
        getSupportLoaderManager().restartLoader(RECENT_LOCATIONS_LOADER, bundle, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Context context = this;
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else if (id == R.id.action_about) {
            Utility.showAboutDialog(this);
        }
        else if (id == R.id.action_search) {
            onSearchRequested();
            return true;
        }
        else if (id == R.id.action_signup) {
            Intent intent = new Intent(this, SignUpActivity.class);
            this.startActivity(intent);
            return true;
        }
        else if (id == R.id.action_login) {
            Utility.showLoginDialog(this);
        }
        else if (id == R.id.action_logout) {
            Utility.showLogoutDialog(this);
        }
        else if (id == R.id.action_my_tours) {
            // TODO: uncomment when activity is ready
//            Intent intent = new Intent(this, MyToursActivity.class);
//            this.startActivity(intent);
        }
        else if (id == R.id.action_manage_tours) {
            Intent intent = new Intent(this, ManageToursActivity.class);
            this.startActivity(intent);
            return true;
        }
        else if (id == R.id.action_manage_slots) {
            // TODO: uncomment when activity is ready
//            Intent intent = new Intent(this, ManageSlotsActivity.class);
//            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri tourUri) {
        if (tourUri == null) {
            return;
        }

        // two-pane mode
        if (mTwoPane) {
            mToursSlotsDetailContainer.setVisibility(View.VISIBLE);
            DetailFragment fragment = DetailFragment.newInstance(tourUri);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tours_slots_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
        // one-pane mode
        else {
            Intent intent = new Intent(this, DetailActivity.class).setData(tourUri);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateTour() {
        // two-pane mode
        if (mTwoPane) {
            mToursSlotsDetailContainer.setVisibility(View.VISIBLE);
            CreateTourFragment fragment = new CreateTourFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tours_slots_detail_container, fragment, CREATE_TOUR_FRAGMENT_TAG)
                    .commit();
        }
        // one-pane mode
        else {
            Intent intent = new Intent(this, CreateTourActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateSlot(Uri tourUri) {
        // always two-pane mode, otherwise pressing the create slot button will lead to
        // slots activity.
        CreateSlotFragment fragment = CreateSlotFragment.newInstance(tourUri);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tours_slots_detail_container, fragment, CREATE_SLOT_FRAGMENT_TAG)
                .commit();
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the LoginDialogFragment.LoginDialogListener interface
    @Override
    public void onDateSelected(int julianDate) {
        CreateSlotFragment csf = (CreateSlotFragment)getSupportFragmentManager().findFragmentByTag(CREATE_SLOT_FRAGMENT_TAG);
        csf.applyDate(julianDate);
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the LoginDialogFragment.LoginDialogListener interface
    @Override
    public void onTimeSelected(long timeInMillis) {
        CreateSlotFragment csf = (CreateSlotFragment)getSupportFragmentManager().findFragmentByTag(CREATE_SLOT_FRAGMENT_TAG);
        csf.applyTime(timeInMillis);
    }

    @Override
    public void onViewSlots(Uri tourUri) {
        if (tourUri == null) {
            return;
        }

        // always two-pane mode, otherwise pressing the view slots button will lead to
        // detail activity.
        SlotsFragment fragment = SlotsFragment.newInstance(tourUri);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tours_slots_detail_container, fragment, SLOTS_FRAGMENT_TAG)
                .commit();
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        double posLat = Utility.getPreferredLocationLatitude(this.getApplicationContext());
        double posLong = Utility.getPreferredLocationLongitude(this.getApplicationContext());

        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }

    public void showLocationLinearLayout(boolean show) {
        if (mLocationLinearLayout != null) {
            if (show) {
                mLocationLinearLayout.setVisibility(View.VISIBLE);
                if (mLocationNameTextView != null) {
                    mLocationNameTextView.setText(Utility.getPreferredLocationName(this));
                }
            }
            else {
                mLocationLinearLayout.setVisibility(View.GONE);
            }
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the LoginDialogFragment.LoginDialogListener interface
    @Override
    public void onLogin(DialogFragment dialog, String userId, boolean isGuide) {
        // User touched the dialog's login button
        String loginSuccess = getString(R.string.login_success);
        Toast.makeText(this, loginSuccess, Toast.LENGTH_LONG).show();
        Utility.saveLoginSession(this.getApplicationContext(), userId, isGuide);
        dialog.dismiss();

        showSignInMenuItems(false);

        MainFragment mf = (MainFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main);
        mf.showGuideOptions();
        DetailFragment df = (DetailFragment)getSupportFragmentManager()
                .findFragmentByTag(DETAIL_FRAGMENT_TAG);
        if (df != null) {
            df.showGuideOptions();
        }
        SlotsFragment sf = (SlotsFragment)getSupportFragmentManager()
                .findFragmentByTag(SLOTS_FRAGMENT_TAG);
        if (sf != null) {
            sf.showGuideOptions();
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the LogoutDialogFragment.LogoutDialogListener interface
    @Override
    public void onLogout(DialogFragment dialog) {
        // User touched the dialog's login button
        String logoutSuccess = getString(R.string.logout_success);
        Toast.makeText(this, logoutSuccess, Toast.LENGTH_LONG).show();
        Utility.saveLogoutState(this.getApplicationContext());
        dialog.dismiss();

        showSignInMenuItems(true);

        if (mTwoPane) {
            CreateSlotFragment csf = (CreateSlotFragment)getSupportFragmentManager()
                    .findFragmentByTag(CREATE_SLOT_FRAGMENT_TAG);
            ReserveSlotDialogFragment rsdf = (ReserveSlotDialogFragment)getSupportFragmentManager()
                    .findFragmentByTag(Utility.RESERVE_SLOT_TAG);
            if (rsdf != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(rsdf).commit();
            }
            if (csf != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(csf).commit();
            }
            getSupportFragmentManager().executePendingTransactions();
        }

        MainFragment mf = (MainFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main);
        mf.showGuideOptions();
        DetailFragment df = (DetailFragment)getSupportFragmentManager()
                .findFragmentByTag(DETAIL_FRAGMENT_TAG);
        if (df != null) {
            df.showGuideOptions();
        }
        SlotsFragment sf = (SlotsFragment)getSupportFragmentManager()
                .findFragmentByTag(SLOTS_FRAGMENT_TAG);
        if (sf != null) {
            sf.showGuideOptions();
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the DeleteTourDialogFragment.DeleteTourDialogListener interface
    @Override
    public void onDeleteTour(DialogFragment dialog) {
        dialog.dismiss();

        // Always two-pane mode, otherwise detail activity will be called.
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager()
                        .findFragmentById(R.id.tours_slots_detail_container)).commit();
        getSupportFragmentManager().executePendingTransactions();

        MainFragment mf = (MainFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main);
        mf.onDeleteTour();
    }

    private void showSignInMenuItems(boolean show) {
        if (mLoginMenuItem != null) {
            mLoginMenuItem.setVisible(show);
        }
        if (mSignupMenuItem != null) {
            mSignupMenuItem.setVisible(show);
        }
        if (mLogoutMenuItem != null) {
            mLogoutMenuItem.setVisible(!show);
        }
        if (mMyToursMenuItem != null) {
            mMyToursMenuItem.setVisible(!show);
        }
        if (mManageToursMenuItem != null) {
            mManageToursMenuItem.setVisible(!show);
        }
        if (mManageSlotsMenuItem != null) {
            mManageSlotsMenuItem.setVisible(!show);
        }
    }

    private void initializeDatabase() {
        // Delete previous contents.
        int deleted = getContentResolver().delete(ToursContract.LanguageEntry.CONTENT_URI, null, null);

        Log.d(LOG_TAG, "ToursDbHelper deleted " + deleted + " rows from the languages table.");

        if (deleted == 0) {
            // This is the first time the user is running the app, or deliberately deleted the data
            // from the android settings.
            Context context = getApplicationContext();
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(context.getString(R.string.pref_location_id_key));
            editor.remove(context.getString(R.string.pref_location_name_key));
            editor.remove(context.getString(R.string.pref_location_lat_key));
            editor.remove(context.getString(R.string.pref_location_long_key));
            editor.remove(context.getString(R.string.pref_user_is_logged_in_key));
            editor.remove(context.getString(R.string.pref_user_id_key));
            editor.remove(context.getString(R.string.pref_user_is_guide_key));
            editor.apply();

            showSignInMenuItems(true);
        }

        String[] languages = new String[]
                {getString(R.string.language_english),
                        getString(R.string.language_spanish),
                        getString(R.string.language_french),
                        getString(R.string.language_german),
                        getString(R.string.language_italian),
                        getString(R.string.language_portuguese),
                        getString(R.string.language_chinese),
                        getString(R.string.language_hebrew)};

        ArrayList<ContentValues> cvArrayList = new ArrayList<>();
        for (String language : languages) {
            ContentValues cv = new ContentValues();
            cv.put(ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME, language);
            cvArrayList.add(cv);
        }

        ContentValues[] cvArray = new ContentValues[cvArrayList.size()];
        cvArrayList.toArray(cvArray);
        int inserted = getContentResolver().bulkInsert(
                ToursContract.LanguageEntry.CONTENT_URI,
                cvArray
        );
        Log.d(LOG_TAG, "ToursDbHelper inserted " + inserted + " rows into the languages table.");
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.

        // Sort order:  By recently used locations, the most recent is first.
        String sortOrder = ToursContract.LocationEntry._ID + " DESC";
        Uri queryLocationUri = ToursContract.LocationEntry.CONTENT_URI;
        String selection;
        String location = bundle.getString(RECENT_BUNDLE_KEY);
        if (location != null && !location.equals("")) {
            selection = ToursContract.LocationEntry.TABLE_NAME +
                    "." + ToursContract.LocationEntry.COLUMN_LOCATION_NAME +
                    " LIKE '%" + location + "%'";
            return new CursorLoader(
                    this,
                    queryLocationUri,
                    RECENT_LOCATION_COLUMNS,
                    selection,
                    null,
                    sortOrder
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mRecentLocationAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mRecentLocationAdapter.swapCursor(null);
    }
}

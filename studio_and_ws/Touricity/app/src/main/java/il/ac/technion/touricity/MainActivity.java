package il.ac.technion.touricity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
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
        implements LoginDialogFragment.LoginDialogListener,
        LogoutDialogFragment.LogoutDialogListener,
        MainFragment.Callback,
        DetailFragment.Callback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String CREATE_TOUR_FRAGMENT_TAG = "CTFTAG";
    private static final String SLOTS_FRAGMENT_TAG = "SFTAG";

    private boolean mTwoPane;

    private FrameLayout mToursSlotsDetailContainer;

    private LinearLayout mLocationLinearLayout;
    private TextView mLocationNameTextView;

    private MenuItem mLoginMenuItem;
    private MenuItem mSignupMenuItem;
    private MenuItem mLogoutMenuItem;
    private MenuItem mMyToursMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeDatabase();

        setContentView(R.layout.activity_main);

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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.tours_slots_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
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

        if (Utility.getIsLoggedIn(getApplicationContext()))
        {
            showSignInMenuItems(false);
        }
        else {
            showSignInMenuItems(true);
        }

        return true;
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
        else if (id == R.id.action_signup) {
            Intent intent = new Intent(this, SignUpActivity.class);
            this.startActivity(intent);
        }
        else if (id == R.id.action_login) {
            Utility.showLoginDialog(this);
        }
        else if (id == R.id.action_logout) {
            Utility.showLogoutDialog(this);
        }
        else if (id == R.id.action_my_tours) {
            // TODO: complete code
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
    public void onViewSlots(Uri tourUri) {
        if (tourUri == null) {
            return;
        }

        // always two-pane mode, otherwise pressing the view slots button will lead to
        // detail activity.
        mToursSlotsDetailContainer.setVisibility(View.VISIBLE);
        SlotsFragment fragment = SlotsFragment.newInstance(tourUri);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.tours_slots_detail_container, fragment, SLOTS_FRAGMENT_TAG)
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
        // We need to implement this function in this activity, and not in the
        // LoginDialogActivity, because here one can call getApplicationContext().
        Utility.saveLoginSession(this.getApplicationContext(), userId, isGuide);
        dialog.dismiss();

        showSignInMenuItems(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Utility.getLoggedInUserIsGuide(getApplicationContext())) {
                MainFragment mf = (MainFragment)getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_main);
                mf.showGuideOptions(true);
            }
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
        // We need to implement this function in this activity, and not in the
        // LogoutDialogActivity, because here one can call getApplicationContext().
        Utility.saveLogoutState(this.getApplicationContext());
        dialog.dismiss();

        showSignInMenuItems(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MainFragment mf = (MainFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            mf.showGuideOptions(false);
        }
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
            editor.commit();

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
        for (int i = 0; i < languages.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME, languages[i]);
            cvArrayList.add(cv);
        }

        ContentValues[] cvArray = new ContentValues[cvArrayList.size()];
        cvArrayList.toArray(cvArray);
        int inserted = 0;
        inserted = getContentResolver().bulkInsert(
                ToursContract.LanguageEntry.CONTENT_URI,
                cvArray
        );
        Log.d(LOG_TAG, "ToursDbHelper inserted " + inserted + " rows into the languages table.");
    }
}

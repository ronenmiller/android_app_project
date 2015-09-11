package il.ac.technion.touricity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
        implements LoginDialogFragment.LoginDialogListener,
        LogoutDialogFragment.LogoutDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private LinearLayout mLocationLinearLayout;
    private TextView mLocationNameTextView;

    private MenuItem mLoginMenuItem;
    private MenuItem mSignupMenuItem;
    private MenuItem mLogoutMenuItem;
    private MenuItem mMyToursMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationLinearLayout = (LinearLayout)findViewById(R.id.linearlayout_location_main);
        mLocationNameTextView = (TextView)findViewById(R.id.textview_location_main);
        showLocationRelativeLayout(false);
        Button locationMapButton = (Button)findViewById(R.id.location_map_btn);
        // Avoid making the map button's text with capital letters.
        locationMapButton.setTransformationMethod(null);
        locationMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreferredLocationInMap();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mLoginMenuItem = (MenuItem)menu.findItem(R.id.action_login);
        mSignupMenuItem = (MenuItem)menu.findItem(R.id.action_signup);
        mLogoutMenuItem = (MenuItem)menu.findItem(R.id.action_logout);
        mMyToursMenuItem = (MenuItem)menu.findItem(R.id.action_my_tours);

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

    public void showLocationRelativeLayout(boolean show) {
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
    public void onLogin(DialogFragment dialog, String username, String password) {
        // User touched the dialog's login button
        String loginSuccess = getString(R.string.login_success);
        Toast.makeText(this, loginSuccess, Toast.LENGTH_LONG).show();
        // We need to implement this function in this activity, and not in the
        // LoginDialogActivity, because here one can call getApplicationContext().
        Utility.saveLoginSession(this.getApplicationContext(), username, password);
        dialog.dismiss();

        showSignInMenuItems(false);
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

}

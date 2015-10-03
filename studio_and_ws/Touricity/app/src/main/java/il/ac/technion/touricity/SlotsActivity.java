package il.ac.technion.touricity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SlotsActivity extends FragmentActivity
        implements LoginDialogFragment.LoginDialogListener,
        SlotsFragment.Callback {

    public static final String SLOTS_FRAGMENT_TAG = "SFTAG";

    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slots);

        // Inflate action bar
        setupActionBar(savedInstanceState);

        if (savedInstanceState == null) {
            SlotsFragment fragment = SlotsFragment.newInstance(getIntent().getData());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.tours_slots_detail_container, fragment, SLOTS_FRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slots, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Activate when pressing the action bar's back button.
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        if (id == R.id.action_settings) {
            Context context = this;
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.support.v7.app.ActionBar}.
     */
    private void setupActionBar(Bundle savedInstanceState) {
        // Show the Up button in the action bar.
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getDelegate().getSupportActionBar().show();
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
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

        // TODO: add sign in, login, logout menu buttons to activity
//        showSignInMenuItems(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Utility.getLoggedInUserIsGuide(getApplicationContext())) {
                SlotsFragment sf = (SlotsFragment)getSupportFragmentManager()
                        .findFragmentByTag(SLOTS_FRAGMENT_TAG);
                sf.showGuideOptions(true);
            }
        }
    }

    @Override
    public void onCreateSlot(Uri tourUri) {
        // always one-pane mode
        Intent intent = new Intent(this, CreateSlotActivity.class).setData(tourUri);
        startActivity(intent);
    }
}

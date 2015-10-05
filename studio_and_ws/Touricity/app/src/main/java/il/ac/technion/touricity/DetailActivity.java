package il.ac.technion.touricity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends FragmentActivity
        implements DeleteTourDialogFragment.DeleteTourDialogListener,
        DetailFragment.Callback {

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Inflate action bar
        setupActionBar(savedInstanceState);

        if (savedInstanceState == null) {
            DetailFragment fragment = DetailFragment.newInstance(getIntent().getData());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.tours_slots_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
        else if (id == R.id.action_settings) {
            Context context = this;
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else if (id == R.id.action_logout) {
            Utility.showLogoutDialog(this);
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
    // defined by the LogoutDialogFragment.LogoutDialogListener interface
//    @Override
//    public void onLogout(DialogFragment dialog) {
//        // User touched the dialog's login button
//        String logoutSuccess = getString(R.string.logout_success);
//        Toast.makeText(this, logoutSuccess, Toast.LENGTH_LONG).show();
//        Utility.saveLogoutState(this.getApplicationContext());
//        dialog.dismiss();
//
//        // TODO: add this
////        showSignInMenuItems(true);
//
//        DetailFragment df = (DetailFragment)getSupportFragmentManager()
//                .findFragmentByTag(DETAIL_FRAGMENT_TAG);
//        if (df != null) {
//            df.showGuideOptions();
//        }
//    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the DeleteTourDialogFragment.DeleteTourDialogListener interface
    @Override
    public void onDeleteTour(DialogFragment dialog) {
        dialog.dismiss();

        // Always one-pane mode, otherwise main activity or manage tours activity will be called.
        Intent intent = new Intent(this, ManageToursActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onViewSlots(Uri tourUri) {
        if (tourUri == null) {
            return;
        }

        Intent intent = new Intent(this, SlotsActivity.class).setData(tourUri);
        startActivity(intent);
    }
}

package il.ac.technion.touricity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ReservationDetailActivity extends FragmentActivity
        implements LogoutDialogFragment.LogoutDialogListener,
        DeleteReservationDialogFragment.DeleteReservationDialogListener,
        RatingDialogFragment.RatingDialogListener {

    static final String RESERVATION_DETAIL_FRAGMENT_TAG = "RDFTAG";

    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_detail);

        // Inflate action bar
        setupActionBar(savedInstanceState);

        if (savedInstanceState == null) {
            ReservationDetailFragment fragment = ReservationDetailFragment.newInstance(getIntent().getData());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.tours_slots_detail_container, fragment, RESERVATION_DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_tours, menu);
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
     @Override
     public void onLogout(DialogFragment dialog) {
         // User touched the dialog's login button
         String logoutSuccess = getString(R.string.logout_success);
         Toast.makeText(this, logoutSuccess, Toast.LENGTH_LONG).show();
         Utility.saveLogoutState(this.getApplicationContext());
         dialog.dismiss();

         Intent intent = new Intent(this, MainActivity.class);
         this.startActivity(intent);
     }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the DeleteReservationDialogFragment.DeleteReservationDialogListener interface
    @Override
    public void onDeleteReservation(DialogFragment dialog) {
        dialog.dismiss();

        // Always one-pane mode, otherwise main activity or manage slots activity will be called.
        Intent intent = new Intent(this, MyToursActivity.class);
        this.startActivity(intent);
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the RatingDialogFragment.RatingDialogListener interface
    @Override
    public void onRate(DialogFragment dialog) {
        dialog.dismiss();

        // Always two-pane mode, otherwise reservation detail activity will be called.
        ReservationDetailFragment rdf = (ReservationDetailFragment)getSupportFragmentManager()
                .findFragmentByTag(RESERVATION_DETAIL_FRAGMENT_TAG);
        rdf.onRate();
    }
}

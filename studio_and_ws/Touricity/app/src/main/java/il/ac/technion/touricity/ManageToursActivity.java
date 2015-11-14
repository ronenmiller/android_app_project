package il.ac.technion.touricity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ManageToursActivity extends ActionBarActivity
        implements LogoutDialogFragment.LogoutDialogListener,
        DeleteTourDialogFragment.DeleteTourDialogListener,
        ManageToursFragment.Callback,
        DetailFragment.Callback,
        SlotsFragment.Callback,
        DatePickerFragment.DatePickerDialogListener,
        TimePickerFragment.TimePickerDialogListener {

    private static final String LOG_TAG = ManageToursActivity.class.getSimpleName();

    private boolean mTwoPane;

    private FrameLayout mDetailContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_tours);

        mDetailContainer = (FrameLayout)findViewById(R.id.tours_slots_detail_container);
        if (mDetailContainer != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                mDetailContainer.setVisibility(View.INVISIBLE);
            }
        } else {
            mTwoPane = false;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0f);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_tours, menu);

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
        else if (id == R.id.action_about) {
            Utility.showAboutDialog(this);
        }
        else if (id == R.id.action_main) {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            return true;
        }
        else if (id == R.id.action_logout) {
            Utility.showLogoutDialog(this);
        }
        else if (id == R.id.action_my_tours) {
            Intent intent = new Intent(this, MyToursActivity.class);
            this.startActivity(intent);
        }
        else if (id == R.id.action_manage_slots) {
            Intent intent = new Intent(this, ManageSlotsActivity.class);
            this.startActivity(intent);
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
            mDetailContainer.setVisibility(View.VISIBLE);
            DetailFragment fragment = DetailFragment.newInstance(tourUri);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tours_slots_detail_container, fragment, DetailActivity.DETAIL_FRAGMENT_TAG)
                    .commit();
        }
        // one-pane mode
        else {
            Intent intent = new Intent(this, DetailActivity.class).setData(tourUri);
            startActivity(intent);
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

        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
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

        ManageToursFragment mtf = (ManageToursFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_manage_tours);
        mtf.onDeleteTour();
    }

    @Override
    public void onCreateSlot(Uri tourUri) {
        // always two-pane mode, otherwise pressing the create slot button will lead to
        // slots activity.
        mDetailContainer.setVisibility(View.VISIBLE);
        CreateSlotFragment fragment = CreateSlotFragment.newInstance(tourUri);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tours_slots_detail_container, fragment, CreateSlotActivity.CREATE_SLOT_FRAGMENT_TAG)
                .commit();
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the LoginDialogFragment.LoginDialogListener interface
    @Override
    public void onDateSelected(int julianDate) {
        CreateSlotFragment csf = (CreateSlotFragment)getSupportFragmentManager().findFragmentByTag(CreateSlotActivity.CREATE_SLOT_FRAGMENT_TAG);
        csf.applyDate(julianDate);
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the LoginDialogFragment.LoginDialogListener interface
    @Override
    public void onTimeSelected(long timeInMillis) {
        CreateSlotFragment csf = (CreateSlotFragment)getSupportFragmentManager().findFragmentByTag(CreateSlotActivity.CREATE_SLOT_FRAGMENT_TAG);
        csf.applyTime(timeInMillis);
    }

    @Override
    public void onViewSlots(Uri tourUri) {
        if (tourUri == null) {
            return;
        }

        // always two-pane mode, otherwise pressing the view slots button will lead to
        // detail activity.
        mDetailContainer.setVisibility(View.VISIBLE);
        SlotsFragment fragment = SlotsFragment.newInstance(tourUri);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tours_slots_detail_container, fragment, SlotsActivity.SLOTS_FRAGMENT_TAG)
                .commit();
    }

}

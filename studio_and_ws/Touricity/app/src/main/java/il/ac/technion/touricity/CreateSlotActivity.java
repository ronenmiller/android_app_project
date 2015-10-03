package il.ac.technion.touricity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;

public class CreateSlotActivity extends FragmentActivity implements
        DatePickerFragment.DatePickerDialogListener,
        TimePickerFragment.TimePickerDialogListener {

    private AppCompatDelegate mDelegate;

    static final String CREATE_SLOT_FRAGMENT_TAG = "CSFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_slot);

        // Inflate action bar
        setupActionBar(savedInstanceState);

        if (savedInstanceState == null) {
            CreateSlotFragment fragment = CreateSlotFragment.newInstance(getIntent().getData());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.tours_slots_detail_container, fragment, CREATE_SLOT_FRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_slot, menu);
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
}

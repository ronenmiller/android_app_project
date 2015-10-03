package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.CreateSlotService;


/**
 * A placeholder fragment containing a simple view.
 */
public class CreateSlotFragment extends Fragment {

    public final String LOG_TAG = CreateSlotFragment.class.getSimpleName();

    public static final String INTENT_EXTRA_TOUR_ID = "extra_tour_id";
    public static final String INTENT_EXTRA_DATE = "extra_date";
    public static final String INTENT_EXTRA_TIME = "extra_time";
    public static final String INTENT_EXTRA_CAPACITY = "extra_capacity";

    public static final String BROADCAST_INTENT_RESULT =  "result";

    public static final String BROADCAST_CREATE_SLOT_SERVICE_DONE = "broadcast_create_slot_service_done";

    private static final String SLOT_URI = "SLOT_URI";

    private Uri mUri = null;

    private EditText mDateView;
    private EditText mTimeView;
    private EditText mCapacityView;
    private Button mSelectTimeButton;

    private View mCreateSlotFormView;
    private View mProgressView;

    int mJulianDate = -1;
    long mTimeInMillis = -1L;

    public CreateSlotFragment() { }

    public static CreateSlotFragment newInstance(Uri uri) {
        CreateSlotFragment f = new CreateSlotFragment();

        if (uri != null) {
            Bundle args = new Bundle();
            args.putParcelable(SLOT_URI, uri);
            f.setArguments(args);
        }

        return f;
    }

    public Uri getShownUri() {
        if (getArguments() != null)
            return getArguments().getParcelable(SLOT_URI);
        else {
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUri = getShownUri();

        View rootView = inflater.inflate(R.layout.fragment_create_slot, container, false);

        mDateView = (EditText)rootView.findViewById(R.id.create_slot_date);
        mTimeView = (EditText)rootView.findViewById(R.id.create_slot_time);
        mCapacityView = (EditText)rootView.findViewById(R.id.create_slot_capacity);

        Button selectDateButton = (Button)rootView.findViewById(R.id.create_slot_date_btn);

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.showDatePickerDialog(getActivity());
            }
        });

        mSelectTimeButton = (Button)rootView.findViewById(R.id.create_slot_time_btn);
        mSelectTimeButton.setEnabled(false);
        mSelectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.showTimePickerDialog(getActivity());
            }
        });

        Button createSlotBtn = (Button)rootView.findViewById(R.id.create_slot_btn);

        createSlotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSlotCreation();
            }
        });

        mCreateSlotFormView = rootView.findViewById(R.id.create_slot_layout_form);
        mProgressView = rootView.findViewById(R.id.create_slot_progressbar);

        return rootView;
    }

    public void applyDate(int julianDate) {
        mJulianDate = julianDate;
        Time dayTime = new Time();

        int todayJulianDate = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        if (mJulianDate < todayJulianDate) {
            mDateView.setError(getString(R.string.error_date_passed));
            mDateView.setText(getString(R.string.create_slot_date));
            mSelectTimeButton.setEnabled(false);
            mJulianDate = -1;
            return;
        }

        mDateView.setError(null);
        dayTime = new Time();
        // Cheating to convert this to UTC time, which is what we want anyhow
        long dateInMillis = dayTime.setJulianDay(mJulianDate);
        String formattedDate = Utility.getFriendlyDayString(getActivity(), dateInMillis);
        mDateView.setText(formattedDate);
        mSelectTimeButton.setEnabled(true);
    }

    public void applyTime(long timeInMillis) {
        mTimeInMillis = timeInMillis;

        Time dayTime = new Time();
        long dateTime = dayTime.setJulianDay(mJulianDate);

        Calendar c = Calendar.getInstance();
        long currentTimeInMillis = c.getTimeInMillis();

        c.setTimeInMillis(dateTime);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        c.setTimeInMillis(mTimeInMillis);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // The full date and time, in milliseconds, selected by user.
        c.set(year, month, day, hour, minute);
        long selectedTimeInMillis = c.getTimeInMillis();

        if (selectedTimeInMillis < currentTimeInMillis) {
            mTimeView.setError(getString(R.string.error_time_passed));
            mTimeView.setText(getString(R.string.create_slot_time));
            mTimeInMillis = -1L;
            return;
        }

        mTimeView.setError(null);
        String formattedTime = Utility.getFriendlyTimeString(timeInMillis);
        mTimeView.setText(formattedTime);
    }

    /**
     * Attempts to create a tour and insert it to the server's database.
     * If there are form errors (invalid fields), the
     * errors are presented and no actual tour creation attempt is made.
     */
    public void attemptSlotCreation() {
        // Reset errors.
        mDateView.setError(null);
        mTimeView.setError(null);
        mCapacityView.setError(null);

        // Store values at the time of the slot creation attempt.
        String capacity = mCapacityView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid date.
        if (mJulianDate == -1) {
            mDateView.setError(getString(R.string.error_field_required));
            focusView = mDateView;
            cancel = true;
        }

        // Check for a valid time.
        if (mTimeInMillis == -1L) {
            mTimeView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mTimeView;
            cancel = true;
        }

        // Check for a valid capacity.
        if (TextUtils.isEmpty(capacity)) {
            mCapacityView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mCapacityView;
            cancel = true;
        }
        else if (Integer.parseInt(capacity) <= 0) {
            mCapacityView.setError(getString(R.string.error_positive_capacity));
            focusView = (focusView != null) ? focusView : mCapacityView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt to sign up and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the tour creation attempt.
            showProgress(true);
            Intent intent = new Intent(getActivity(), CreateSlotService.class);

            int tourId = -1;
            if (mUri != null) {
                tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
            }
            intent.putExtra(INTENT_EXTRA_TOUR_ID, tourId);
            intent.putExtra(INTENT_EXTRA_DATE, mJulianDate);
            intent.putExtra(INTENT_EXTRA_TIME, mTimeInMillis);
            intent.putExtra(INTENT_EXTRA_CAPACITY, capacity);

            getActivity().startService(intent);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCreateSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreateSlotFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreateSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCreateSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mCreateSlotReceiver,
                new IntentFilter(BROADCAST_CREATE_SLOT_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_CREATE_SLOT_SERVICE_DONE event
    private BroadcastReceiver mCreateSlotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and set visibility attribute
            Log.d(LOG_TAG, "Create slot broadcast received.");
            showProgress(false);

            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);

            if (!success) {
                String slotCreationFailed = getString(R.string.error_create_slot_failed);
                Toast.makeText(context, slotCreationFailed, Toast.LENGTH_LONG).show();
            } else {
                // TODO: enable manage slots activity
//                Intent manageSlotsIntent = new Intent(context, ManageSlotsActivity.class);
//                context.startActivity(manageSlotsIntent);
            }
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mCreateSlotReceiver);
        super.onPause();
    }
}

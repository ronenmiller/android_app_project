package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.CreateTourService;


/**
 * A placeholder fragment containing a simple view.
 */
public class CreateTourFragment extends Fragment {

    public final String LOG_TAG = CreateTourFragment.class.getSimpleName();

    public static final String INTENT_EXTRA_TITLE = "extra_title";
    public static final String INTENT_EXTRA_LANGUAGE = "extra_language";
    public static final String INTENT_EXTRA_DURATION = "extra_duration";
    public static final String INTENT_EXTRA_LOCATION = "extra_location";
    public static final String INTENT_EXTRA_DESCRIPTION = "extra_description";
    public static final String INTENT_EXTRA_PHOTOS = "extra_photos";
    public static final String INTENT_EXTRA_COMMENTS = "extra_comments";

    public static final String BROADCAST_INTENT_RESULT =  "result";

    public static final String BROADCAST_CREATE_TOUR_SERVICE_DONE = "broadcast_create_tour_service_done";

    private static final String[] LANGUAGE_COLUMNS = {
            ToursContract.LanguageEntry._ID,
            ToursContract.LanguageEntry.COLUMN_LANGUAGE_NAME
    };

    // These indices are tied to LANGUAGE_COLUMNS.  If LANGUAGE_COLUMNS changes, these
    // must change.
    public static final int COL_LANGUAGE_ID = 0;
    public static final int COL_LANGUAGE_NAME = 1;

    private EditText mTitleView;
    private Spinner mLanguageSpinnerView;
    private EditText mDurationView;
    private EditText mLocationView;
    private EditText mDescriptionView;
    private LinearLayout mPhotosView;
    private LinearLayout mCommentsView;

    private View mCreateTourFormView;
    private View mProgressView;

    public CreateTourFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_tour, container, false);

        mTitleView = (EditText)rootView.findViewById(R.id.create_tour_title);
        mLanguageSpinnerView = (Spinner)rootView.findViewById(R.id.create_tour_spinner);
        mDurationView = (EditText)rootView.findViewById(R.id.create_tour_duration);
        mLocationView = (EditText)rootView.findViewById(R.id.create_tour_location);
        mDescriptionView = (EditText)rootView.findViewById(R.id.create_tour_description);

        Cursor languageCursor = null;
        String sortOrder = ToursContract.LanguageEntry._ID + " ASC";
        List<String> list = new ArrayList<String>();
        try {
            languageCursor = getActivity().getContentResolver().query(
                    ToursContract.LanguageEntry.CONTENT_URI,
                    LANGUAGE_COLUMNS,
                    null,
                    null,
                    sortOrder
            );

            while (languageCursor.moveToNext()) {
                list.add(languageCursor.getString(COL_LANGUAGE_NAME));
            }
        }
        finally {
            if (languageCursor != null) {
                languageCursor.close();
            }
        }

        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLanguageSpinnerView.setAdapter(languageAdapter);

        mDescriptionView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.action_create_tour_id || id == EditorInfo.IME_NULL) {
                    attemptTourCreation();
                    // Return true if action was consumed, false otherwise.
                    return true;
                }
                return false;
            }
        });

        Button createTourBtn = (Button)rootView.findViewById(R.id.create_tour_btn);

        createTourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptTourCreation();
            }
        });

        mCreateTourFormView = rootView.findViewById(R.id.create_tour_layout_form);
        mProgressView = rootView.findViewById(R.id.create_tour_progressbar);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_create_tour, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_create_tour) {
            attemptTourCreation();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to create a tour and insert it to the server's database.
     * If there are form errors (invalid fields), the
     * errors are presented and no actual tour creation attempt is made.
     */
    public void attemptTourCreation() {
        // Reset errors.
        mTitleView.setError(null);
        mDurationView.setError(null);
        mLocationView.setError(null);

        // Store values at the time of the tour creation attempt.
        String title = mTitleView.getText().toString();
        String duration = mDurationView.getText().toString();
        String location = mLocationView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid title.
        if (TextUtils.isEmpty(title)) {
            mTitleView.setError(getString(R.string.error_field_required));
            focusView = mTitleView;
            cancel = true;
        }

        // This is not possible, but just in case.
        if (mLanguageSpinnerView.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            focusView = (focusView != null) ? focusView : mLanguageSpinnerView;
            cancel = true;
        }

        // Check for a valid duration.
        if (TextUtils.isEmpty(duration)) {
            mDurationView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mDurationView;
            cancel = true;
        }
        else if (Integer.parseInt(duration) <= 0) {
            mDurationView.setError(getString(R.string.error_positive_duration));
            focusView = (focusView != null) ? focusView : mDurationView;
            cancel = true;
        }

        if (TextUtils.isEmpty(location)) {
            mLocationView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mLocationView;
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
            Intent intent = new Intent(getActivity(), CreateTourService.class);
            intent.putExtra(INTENT_EXTRA_TITLE, title);
            intent.putExtra(INTENT_EXTRA_LANGUAGE, mLanguageSpinnerView.getSelectedItemPosition() + 1);
            intent.putExtra(INTENT_EXTRA_DURATION, duration);
            intent.putExtra(INTENT_EXTRA_LOCATION, location);
            intent.putExtra(INTENT_EXTRA_DESCRIPTION, mDescriptionView.getText().toString());

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

            mCreateTourFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreateTourFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreateTourFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mCreateTourFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mCreateTourReceiver,
                new IntentFilter(BROADCAST_CREATE_TOUR_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_CREATE_TOUR_SERVICE_DONE event
    private BroadcastReceiver mCreateTourReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the rotating animation and set visibility attribute
            Log.d(LOG_TAG, "Create tour broadcast received.");
            showProgress(false);

            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);

            if (!success) {
                String tourCreationFailed = getString(R.string.error_create_tour_failed);
                Toast.makeText(context, tourCreationFailed, Toast.LENGTH_LONG).show();
            } else {
                Intent manageToursIntent = new Intent(context, ManageToursActivity.class);
                getActivity().startActivity(manageToursIntent);
            }
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mCreateTourReceiver);
        super.onPause();
    }
}

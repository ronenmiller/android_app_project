package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import il.ac.technion.touricity.service.MyRatingsLoaderService;
import il.ac.technion.touricity.service.UpdateRatingService;


public class RatingDialogFragment extends DialogFragment {

    private static final String TOUR_ID = "TOUR_ID";
    private static final String GUIDE_ID = "GUIDE_ID";

    public static final String INTENT_EXTRA_TOUR_RATING = "extra_tour_rating";
    public static final String INTENT_EXTRA_GUIDE_RATING = "extra_guide_rating";
    public static final String INTENT_EXTRA_TOUR_ID = "extra_tour_id";
    public static final String INTENT_EXTRA_GUIDE_ID = "extra_guide_id";

    public static final String BROADCAST_MY_RATING_LOADER_SERVICE_DONE = "broadcast_my_rating_loader_service_done";
    public static final String BROADCAST_UPDATE_RATING_SERVICE_DONE = "broadcast_update_rate_service_done";
    public static final String BROADCAST_INTENT_RESULT =  "result";
    public static final String BROADCAST_INTENT_TOUR_RATING =  "tour_rating";
    public static final String BROADCAST_INTENT_GUIDE_RATING =  "guide_rating";

    private RatingBar mTourRatingBar;
    private RatingBar mGuideRatingBar;
    private View mProgressView;
    private View mRateFormView;

    private float mTourRating = -1;
    private float mGuideRating = -1;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface RatingDialogListener {
        void onRate(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    RatingDialogListener mListener;

    public static RatingDialogFragment newInstance(int tourId, String guideId) {
        RatingDialogFragment f = new RatingDialogFragment();

        Bundle args = new Bundle();
        args.putInt(TOUR_ID, tourId);
        args.putString(GUIDE_ID, guideId);
        f.setArguments(args);

        return f;
    }

    public int getShownTourId() {
        if (getArguments() != null)
            return getArguments().getInt(TOUR_ID);
        else {
            return -1;
        }
    }

    public String getShownGuideId() {
        if (getArguments() != null)
            return getArguments().getString(GUIDE_ID);
        else {
            return null;
        }
    }

    // Override the Fragment.onAttach() method to instantiate the RatingDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the RatingDialogListener so we can send events to the host
            mListener = (RatingDialogListener)activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement RatingDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View rootView = inflater.inflate(R.layout.dialog_rate, null);
        builder.setView(rootView);
        mTourRatingBar = (RatingBar)rootView.findViewById(R.id.dialog_rate_tour_rating_bar);
        mGuideRatingBar = (RatingBar)rootView.findViewById(R.id.dialog_rate_guide_rating_bar);
        Button cancelButton = (Button) rootView.findViewById(R.id.rate_cancel_btn);
        Button submitButton = (Button) rootView.findViewById(R.id.rate_submit_btn);

        if (mTourRating != -1) {
            mTourRatingBar.setRating(mTourRating);
        }
        if (mGuideRating != -1) {
            mGuideRatingBar.setRating(mGuideRating);
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRateSubmission();
            }
        });

        mRateFormView = rootView.findViewById(R.id.rate_linear_form);
        mProgressView = rootView.findViewById(R.id.rate_progressbar);

        return builder.create();
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

            mRateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRateFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void onRateSubmission() {
        // Nothing needs to be done if the user did not change is current rating.
        if (mTourRating == mTourRatingBar.getRating() && mGuideRating == mGuideRatingBar.getRating()) {
            dismiss();
        }

        mTourRating = mTourRatingBar.getRating();
        mGuideRating = mGuideRatingBar.getRating();

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        Intent intent = new Intent(getActivity(), UpdateRatingService.class);
        int tourId = getShownTourId();
        String guideId = getShownGuideId();
        intent.putExtra(INTENT_EXTRA_TOUR_ID, tourId);
        intent.putExtra(INTENT_EXTRA_GUIDE_ID, guideId);
        intent.putExtra(INTENT_EXTRA_TOUR_RATING, mTourRating);
        intent.putExtra(INTENT_EXTRA_GUIDE_RATING, mGuideRating);
        getActivity().startService(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        Intent intent = new Intent(getActivity(), MyRatingsLoaderService.class);
        int tourId = getShownTourId();
        String guideId = getShownGuideId();
        intent.putExtra(INTENT_EXTRA_TOUR_ID, tourId);
        intent.putExtra(INTENT_EXTRA_GUIDE_ID, guideId);
        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMyRatingMessageReceiver,
                new IntentFilter(BROADCAST_MY_RATING_LOADER_SERVICE_DONE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateRatingMessageReceiver,
                new IntentFilter(BROADCAST_UPDATE_RATING_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_RATING_SERVICE_DONE event
    private BroadcastReceiver mMyRatingMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This variable determines if the user insertion itself completed successfully, after
            // all the constraints have been checked.
            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);
            showProgress(false);
            if (success) {
                mTourRating = intent.getFloatExtra(BROADCAST_INTENT_TOUR_RATING, (float)2.5);
                mGuideRating = intent.getFloatExtra(BROADCAST_INTENT_GUIDE_RATING, (float)2.5);
                if (mTourRatingBar != null) {
                    mTourRatingBar.setRating(mTourRating);
                }
                if (mGuideRatingBar != null) {
                    mGuideRatingBar.setRating(mGuideRating);
                }
            }
            else {
                Toast.makeText(context, getString(R.string.dialog_rate_init_fail), Toast.LENGTH_LONG).show();
            }
        }
    };

    // handler for received Intents for the BROADCAST_RATING_SERVICE_DONE event
    private BroadcastReceiver mUpdateRatingMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This variable determines if the user insertion itself completed successfully, after
            // all the constraints have been checked.
            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);
            showProgress(false);
            if (success) {
                Toast.makeText(context, getString(R.string.dialog_rate_success), Toast.LENGTH_LONG).show();
                mListener.onRate(RatingDialogFragment.this);
            }
            else {
                Toast.makeText(context, getString(R.string.dialog_rate_fail), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMyRatingMessageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateRatingMessageReceiver);
        super.onPause();
    }
}

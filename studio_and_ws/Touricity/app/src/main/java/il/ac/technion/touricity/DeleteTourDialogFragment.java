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
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.DeleteTourService;


public class DeleteTourDialogFragment extends DialogFragment {

    private Button mCancelButton;
    private Button mDeleteTourButton;
    private View mProgressView;
    private View mDeleteTourFormView;

    public static final String INTENT_EXTRA_TOUR_ID =  "extra_tour_id";

    public static final String BROADCAST_DELETE_TOUR_SERVICE_DONE = "broadcast_delete_tour_service_done";
    public static final String BROADCAST_INTENT_RESULT =  "result";

    private static final String TOUR_URI = "URI";

    private Uri mUri = null;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DeleteTourDialogListener {
        void onDeleteTour(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    DeleteTourDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the DeleteTourDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DeleteTourDialogListener so we can send events to the host
            mListener = (DeleteTourDialogListener)activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DeleteTourDialogListener");
        }
    }

    public DeleteTourDialogFragment() {
    }

    public static DeleteTourDialogFragment newInstance(Uri uri) {
        DeleteTourDialogFragment f = new DeleteTourDialogFragment();

        if (uri != null) {
            Bundle args = new Bundle();
            args.putParcelable(TOUR_URI, uri);
            f.setArguments(args);
        }

        return f;
    }

    public Uri getShownUri() {
        if (getArguments() != null)
            return getArguments().getParcelable(TOUR_URI);
        else {
            return null;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mUri = getShownUri();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View rootView = inflater.inflate(R.layout.dialog_delete_tour, null);
        builder.setView(rootView);
        mCancelButton = (Button)rootView.findViewById(R.id.delete_tour_cancel_btn);
        mDeleteTourButton = (Button)rootView.findViewById(R.id.delete_tour_confirm_btn);
        mDeleteTourButton.setEnabled(false);

        Cursor slotsCursor = null;
        try {
            if (mUri != null) {
                int tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
                String selection = ToursContract.SlotEntry.TABLE_NAME +
                        "." + ToursContract.SlotEntry.COLUMN_SLOT_TOUR_ID + " = ?";
                slotsCursor = getActivity().getContentResolver().query(
                        ToursContract.SlotEntry.CONTENT_URI,
                        new String[]{ToursContract.SlotEntry.TABLE_NAME + "." + ToursContract.SlotEntry._ID},
                        selection,
                        new String[]{Integer.toString(tourId)},
                        null
                );

                // No slots found for this tour ID.
                if (slotsCursor == null || !slotsCursor.moveToFirst()) {
                    mDeleteTourButton.setEnabled(true);
                }
            }
        }
        finally {
            if (slotsCursor != null) {
                slotsCursor.close();
            }
        }

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mDeleteTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDeleteTour();
            }
        });

        mDeleteTourFormView = rootView.findViewById(R.id.delete_tour_linear_form);
        mProgressView = rootView.findViewById(R.id.delete_tour_progressbar);

        return builder.create();
    }

    /**
     * Shows the progress UI and hides the delete tour form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDeleteTourFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDeleteTourFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDeleteTourFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDeleteTourFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void attemptDeleteTour() {
        if (mUri == null) {
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the tour deletion attempt.
        showProgress(true);
        Intent intent = new Intent(getActivity(), DeleteTourService.class);
        int tourId = ToursContract.TourEntry.getTourIdFromUri(mUri);
        intent.putExtra(INTENT_EXTRA_TOUR_ID, tourId);

        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_DELETE_TOUR_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_DELETE_TOUR_SERVICE_DONE event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This variable determines if the user insertion itself completed successfully, after
            // all the constraints have been checked.
            showProgress(false);
            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);
            if (success) {
                Toast.makeText(context, getString(R.string.dialog_delete_tour_success), Toast.LENGTH_LONG).show();
                mListener.onDeleteTour(DeleteTourDialogFragment.this);
            }
            else {
                Toast.makeText(context, getString(R.string.dialog_delete_tour_fail), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}

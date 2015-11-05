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
import il.ac.technion.touricity.service.DeleteReservationService;


public class DeleteReservationDialogFragment extends DialogFragment {

    private View mProgressView;
    private View mDeleteSlotFormView;

    public static final String INTENT_EXTRA_SLOT_ID =  "extra_slot_id";

    public static final String BROADCAST_DELETE_RESERVATION_SERVICE_DONE = "broadcast_delete_reservation_service_done";
    public static final String BROADCAST_INTENT_RESULT =  "result";

    private static final String RESERVATION_URI = "URI";

    private Uri mUri = null;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DeleteReservationDialogListener {
        void onDeleteReservation(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    DeleteReservationDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the DeleteReservationDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DeleteReservationDialogListener so we can send events to the host
            mListener = (DeleteReservationDialogListener)activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DeleteReservationDialogListener");
        }
    }

    public DeleteReservationDialogFragment() {
    }

    public static DeleteSlotDialogFragment newInstance(Uri uri) {
        DeleteSlotDialogFragment f = new DeleteSlotDialogFragment();

        if (uri != null) {
            Bundle args = new Bundle();
            args.putParcelable(RESERVATION_URI, uri);
            f.setArguments(args);
        }

        return f;
    }

    public Uri getShownUri() {
        if (getArguments() != null)
            return getArguments().getParcelable(RESERVATION_URI);
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
        View rootView = inflater.inflate(R.layout.dialog_delete_reservation, null);
        builder.setView(rootView);
        Button cancelBtn = (Button)rootView.findViewById(R.id.delete_reservation_cancel_btn);
        Button deleteSlotBtn = (Button)rootView.findViewById(R.id.delete_reservation_confirm_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        deleteSlotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDeleteReservation();
            }
        });

        mDeleteSlotFormView = rootView.findViewById(R.id.delete_reservation_linear_form);
        mProgressView = rootView.findViewById(R.id.delete_reservation_progressbar);

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

            mDeleteSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDeleteSlotFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDeleteSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDeleteSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void attemptDeleteReservation() {
        if (mUri == null) {
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the tour deletion attempt.
        showProgress(true);
        Intent intent = new Intent(getActivity(), DeleteReservationService.class);
        long slotId = ToursContract.SlotEntry.getSlotIdFromUri(mUri);
        intent.putExtra(INTENT_EXTRA_SLOT_ID, slotId);

        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_DELETE_RESERVATION_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_DELETE_RESERVATION_SERVICE_DONE event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This variable determines if the user insertion itself completed successfully, after
            // all the constraints have been checked.
            showProgress(false);
            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);
            if (success) {
                Toast.makeText(context, getString(R.string.dialog_delete_reservation_success), Toast.LENGTH_LONG).show();
                mListener.onDeleteReservation(DeleteReservationDialogFragment.this);
            }
            else {
                Toast.makeText(context, getString(R.string.dialog_delete_reservation_fail), Toast.LENGTH_LONG).show();
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

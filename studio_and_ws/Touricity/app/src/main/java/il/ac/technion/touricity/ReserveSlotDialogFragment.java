package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.widget.SeekBar;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract;
import il.ac.technion.touricity.service.ReserveSlotService;


public class ReserveSlotDialogFragment extends DialogFragment {

    private static final String SLOT_URI = "SLOT_URI";

    public static final String BROADCAST_RESERVE_SLOT_SERVICE_DONE = "broadcast_reserve_slot_service_done";
    public static final String BROADCAST_INTENT_RESULT =  "result";
    public static final String INTENT_EXTRA_SLOT_ID =  "extra_slot_id";
    public static final String INTENT_EXTRA_USER_ID = "extra_user_id";
    public static final String INTENT_EXTRA_PLACES_REQUESTED = "extra_places_requested";

    private Uri mUri = null;

    private TextView mTitle;
    private TextView mSeekBarProgressView;
    private SeekBar mSeekBar;
    private Button mCancelButton;
    private Button mReserveButton;
    private View mProgressView;
    private View mReserveSlotFormView;

    public ReserveSlotDialogFragment() {

    }

    public static ReserveSlotDialogFragment newInstance(Uri uri) {
        ReserveSlotDialogFragment f = new ReserveSlotDialogFragment();

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mUri = getShownUri();

        if (mUri == null) {
            dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View rootView = inflater.inflate(R.layout.dialog_reserve_slot, null);
        builder.setView(rootView);

        mTitle = (TextView)rootView.findViewById(R.id.reserve_slot_title);

        final long slotId = ToursContract.SlotEntry.getSlotIdFromUri(mUri);
        final String userId = Utility.getLoggedInUserId(getActivity().getApplicationContext());

        String selection = ToursContract.ReservationEntry.TABLE_NAME + "." +
                ToursContract.ReservationEntry._ID + " = ? AND " +
                ToursContract.ReservationEntry.TABLE_NAME + "." +
                ToursContract.ReservationEntry.COLUMN_RESERVATION_USER_ID + " = ?";

        Cursor reservationCursor = null;
        boolean isAlreadyReserved = false;
        try {
            reservationCursor = getActivity().getContentResolver().query(
                    ToursContract.ReservationEntry.CONTENT_URI,
                    // Projection is not important in this case.
                    new String[]{ToursContract.ReservationEntry.TABLE_NAME +
                            "." + ToursContract.ReservationEntry._ID},
                    selection,
                    new String[]{Long.toString(slotId), userId},
                    null
            );

            isAlreadyReserved = reservationCursor != null && reservationCursor.moveToFirst();
        }
        finally {
            if (reservationCursor != null) {
                reservationCursor.close();
            }
        }

        if (isAlreadyReserved) {
            mTitle.setText(getString(R.string.dialog_change_reservation_title));
        }
        else {
            mTitle.setText(getString(R.string.dialog_create_reservation_title));
        }

        mSeekBar = (SeekBar)rootView.findViewById(R.id.reserve_slot_seekBar);
        mCancelButton = (Button)rootView.findViewById(R.id.reserve_slot_cancel_btn);
        mReserveButton = (Button)rootView.findViewById(R.id.reserve_slot_btn);

        mSeekBar.setMax(ToursContract.SlotEntry.getPlacesLeftFromUri(mUri));
        if (mSeekBar.getMax() == 0) {
            dismiss();
        }
        mSeekBar.incrementProgressBy(1);
        mSeekBar.setProgress(1);

        mSeekBarProgressView = (TextView)rootView.findViewById(R.id.reserve_slot_seekBar_progress);
        mSeekBarProgressView.setText(Integer.toString(mSeekBar.getProgress()));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSeekBarProgressView.setText(Integer.toString(progress));
                if (progress == 0) {
                    mReserveButton.setEnabled(false);
                    mReserveButton.setTextColor(getResources().getColor(R.color.touricity_light_grey));
                }
                else {
                    mReserveButton.setEnabled(true);
                    mReserveButton.setTextColor(getResources().getColor(R.color.touricity_teal));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mReserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: update server and local reservations table
                // Show a progress spinner, and kick off a background task to
                // perform the reservation attempt.
                showProgress(true);
                Intent intent = new Intent(getActivity(), ReserveSlotService.class);
                intent.putExtra(INTENT_EXTRA_SLOT_ID, slotId);
                String userId = Utility.getLoggedInUserId(getActivity().getApplicationContext());
                intent.putExtra(INTENT_EXTRA_USER_ID, userId);
                intent.putExtra(INTENT_EXTRA_PLACES_REQUESTED, mSeekBar.getProgress());
                getActivity().startService(intent);
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mReserveSlotFormView = rootView.findViewById(R.id.reserve_slot_linear_form);
        mProgressView = rootView.findViewById(R.id.reserve_slot_progressbar);

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

            mReserveSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mReserveSlotFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mReserveSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mReserveSlotFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_RESERVE_SLOT_SERVICE_DONE));
    }

    // handler for received Intents for the BROADCAST_RESERVE_SLOT_SERVICE_DONE event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This variable determines if the user insertion itself completed successfully, after
            // all the constraints have been checked.
            showProgress(false);
            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);
            if (success) {
//                Intent intent = new Intent(getActivity(), MyToursActivity.class);
//                getActivity().startActivity(intent);
            }
            else {
                // Nothing we can do.
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

package il.ac.technion.touricity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract;

/**
 * {@link ToursAdapter} exposes a list of tours
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class SlotsAdapter extends CursorAdapter {

    private boolean mTwoPane;

    /**
     * Cache of the children views for a tour list item.
     */
    public static class ViewHolder {
        private ImageView iconView;
        private TextView dateTimeView;
        private TextView capacityView;
        private RatingBar ratingBar;

        public ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.list_item_slot_icon);
            dateTimeView = (TextView)view.findViewById(R.id.list_item_slot_date_and_time);
            capacityView = (TextView)view.findViewById(R.id.list_item_slot_capacity);
            ratingBar = (RatingBar)view.findViewById(R.id.list_item_slot_rating_bar);
        }
    }

    public SlotsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_slot, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();

        long slotId = cursor.getLong(SlotsFragment.COL_SLOT_ID);
        String userId = Utility.getLoggedInUserId(context.getApplicationContext());

        String selection = ToursContract.ReservationEntry.TABLE_NAME + "." +
                ToursContract.ReservationEntry._ID + " = ? AND " +
                ToursContract.ReservationEntry.TABLE_NAME + "." +
                ToursContract.ReservationEntry.COLUMN_RESERVATION_USER_ID + " = ?";

        Cursor reservationCursor = context.getContentResolver().query(
                ToursContract.ReservationEntry.CONTENT_URI,
                // Projection is not important in this case.
                new String[]{ToursContract.ReservationEntry.TABLE_NAME +
                        "." + ToursContract.ReservationEntry._ID},
                selection,
                new String[]{Long.toString(slotId), userId},
                null
        );

        boolean isAlreadyReserved = reservationCursor.moveToFirst();

        if (isAlreadyReserved) {
            viewHolder.iconView.setImageResource(R.drawable.ic_check_circle_teal_24dp);
            viewHolder.iconView.setContentDescription(context.getString(R.string.slot_reserved));
        }
        else {
            viewHolder.iconView.setImageResource(R.drawable.ic_label_grey_24dp);
            viewHolder.iconView.setContentDescription(context.getString(R.string.slot_not_reserved));
        }

        Time dayTime = new Time();

        // Read julian date from cursor and translate it to a human-readable date string.
        int julianDate = cursor.getInt(SlotsFragment.COL_SLOT_DATE);
        long dateInMillis = dayTime.setJulianDay(julianDate);
        String formattedDate = Utility.getFriendlyDayString(context, dateInMillis);

        // Read local time string from the cursor.
        long timeInMillis = cursor.getLong(SlotsFragment.COL_SLOT_TIME);
        String formattedTime = Utility.getFriendlyTimeString(timeInMillis);
        int dateFormatId = R.string.format_full_friendly_date;

        // Set date and time text on the text view.
        viewHolder.dateTimeView.setText(context.getString(
                dateFormatId,
                formattedDate,
                formattedTime));

        // Read the number of places left for this slot from the cursor.
        String placesLeft = Integer.toString(cursor.getInt(SlotsFragment.COL_SLOT_VACANT));
        int vacantFormatId = R.string.slot_vacant;
        // Set the number of places left
        viewHolder.capacityView.setText(context.getString(
                vacantFormatId,
                placesLeft));

        double rating = cursor.getInt(SlotsFragment.COL_SLOT_GUIDE_RATING);
        viewHolder.ratingBar.setRating((float) rating);
    }
}
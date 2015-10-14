package il.ac.technion.touricity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ToursAdapter} exposes a list of tours
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ManageSlotsAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a tour list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateTimeView;
        public final TextView tourTitleView;
        public final TextView capacityView;

        public ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.list_item_slot_icon);
            dateTimeView = (TextView)view.findViewById(R.id.list_item_slot_title);
            tourTitleView = (TextView)view.findViewById(R.id.list_item_slot_subtitle);
            capacityView = (TextView)view.findViewById(R.id.list_item_slot_caption);
        }
    }

    public ManageSlotsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_manage_slot, parent, false);
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

        // Read language ID from cursor in order to set the icon.
        int languageId = cursor.getInt(ManageSlotsFragment.COL_TOUR_LANGUAGE);
        // Set image source based on the language ID.
        viewHolder.iconView.setImageResource(Utility.getLanguageIconIdForLanguageId(languageId));
        // For accessibility, add a content description to the icon field.
        String languageName = cursor.getString(ManageSlotsFragment.COL_TOUR_LANGUAGE_NAME);
        viewHolder.iconView.setContentDescription(languageName);

        Time dayTime = new Time();

        // Read julian date from cursor and translate it to a human-readable date string.
        int julianDate = cursor.getInt(ManageSlotsFragment.COL_SLOT_DATE);
        long dateInMillis = dayTime.setJulianDay(julianDate);
        String formattedDate = Utility.getFriendlyDayString(context, dateInMillis);

        // Read local time string from the cursor.
        long timeInMillis = cursor.getLong(ManageSlotsFragment.COL_SLOT_TIME);
        String formattedTime = Utility.getFriendlyTimeString(timeInMillis);
        int dateFormatId = R.string.format_full_friendly_date;

        // Set date and time text on the text view.
        viewHolder.dateTimeView.setText(context.getString(
                dateFormatId,
                formattedDate,
                formattedTime));

        int currentCapacity = cursor.getInt(ManageSlotsFragment.COL_SLOT_CURRENT_CAPACITY);
        int totalCapacity = cursor.getInt(ManageSlotsFragment.COL_SLOT_TOTAL_CAPACITY);
        int numReserved = totalCapacity - currentCapacity;
        int reservedFormatId = R.string.manage_slot_reserved;
        viewHolder.capacityView.setText(context.getString(
                reservedFormatId,
                numReserved,
                totalCapacity));

        // Read tour duration from cursor.
        String locationName = cursor.getString(ManageSlotsFragment.COL_TOUR_TITLE);
        // Find TextView and set the tour title on it.
        viewHolder.tourTitleView.setText(locationName);
    }
}
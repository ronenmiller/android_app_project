package il.ac.technion.touricity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link ToursAdapter} exposes a list of tours
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class SlotReservationsAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a slot's reservation list item.
     */
    public static class ViewHolder {
        public final TextView leftView;
        public final TextView rightView;

        public ViewHolder(View view) {
            leftView = (TextView)view.findViewById(R.id.list_item_slot_reservations_left);
            rightView = (TextView)view.findViewById(R.id.list_item_slot_reservations_right);
        }
    }

    public SlotReservationsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_slot_reservations, parent, false);
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

        // Read user email from cursor.
        String email = cursor.getString(SlotReservationsFragment.COL_USER_EMAIL);
        // Set image source based on the language ID.
        viewHolder.leftView.setText(email);

        // Read the number of participants registered for this slot under this user.
        int reserved = cursor.getInt(SlotReservationsFragment.COL_RESERVATION_PARTICIPANTS);
        // Set image source based on the language ID.
        viewHolder.rightView.setText(Integer.toString(reserved));
    }
}
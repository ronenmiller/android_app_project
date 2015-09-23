package il.ac.technion.touricity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link il.ac.technion.touricity.RecentLocationAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class RecentLocationAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView textView;

        public ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.list_item_recent_icon);
            textView = (TextView)view.findViewById(R.id.list_item_recent_textview);
        }
    }

    public RecentLocationAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /* Remember that these views are reused as needed. */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Obtain the layout for each list entry
        int layoutId = R.layout.list_item_recent_loc;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if (cursor != null) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            // Set the correct icon using an helper method.
            viewHolder.iconView.setImageResource(R.drawable.ic_history_white_24dp);

            // Read location display string from cursor
            String locationName = cursor.getString(MainFragment.COL_RECENT_LOCATION_NAME);
            viewHolder.textView.setText(locationName);
        }
    }
}
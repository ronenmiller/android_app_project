package il.ac.technion.touricity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * {@link ToursAdapter} exposes a list of tours
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ToursAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a tour list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView titleView;
        public final TextView durationView;
        public final RatingBar ratingBar;

        public ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.list_item_tour_icon);
            titleView = (TextView)view.findViewById(R.id.list_item_tour_title);
            durationView = (TextView)view.findViewById(R.id.list_item_tour_duration);
            ratingBar = (RatingBar)view.findViewById(R.id.list_item_tour_rating_bar);
        }
    }

    public ToursAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_tour, parent, false);
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
        int languageId = cursor.getInt(MainFragment.COL_TOUR_LANGUAGE);
        // Set image source based on the language ID.
        viewHolder.iconView.setImageResource(Utility.getLanguageIconIdForLanguageId(languageId));
        // For accessibility, add a content description to the icon field.
        String languageName = cursor.getString(MainFragment.COL_TOUR_LANGUAGE_NAME);
        viewHolder.iconView.setContentDescription(languageName);

        // Read title from cursor.
        String title = cursor.getString(MainFragment.COL_TOUR_TITLE);
        // Find TextView and set the title on it.
        viewHolder.titleView.setText(title);

        // Read tour duration from cursor.
        int duration = cursor.getInt(MainFragment.COL_TOUR_DURATION);
        // Find TextView and set the tour duration on it.
        int formatId = R.string.tour_duration;
        viewHolder.durationView.setText(context.getString(formatId, Integer.toString(duration)));

        // Read tour rating from cursor.
        double rating = cursor.getInt(MainFragment.COL_TOUR_RATING);
        viewHolder.ratingBar.setRating((float)rating);
    }
}
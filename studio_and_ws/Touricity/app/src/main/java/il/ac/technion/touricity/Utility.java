package il.ac.technion.touricity;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by Liron on 14/08/2015.
 */
public class Utility {

    /**
     * Helper method to tell if the location returned by Open Street Map service is a city, town,
     * village or hamlet. Generally, places can be administrative boundaries, counties and even
     * continents. Hence, we need to filter the results.
     * @param locationType from OpenStreetMap API response
     * @return true if the of the place is a city, town, village or hamlet, false otherwise.
     */
    public static boolean isPlace(String locationType) {
        if (locationType.equals("city") || locationType.equals("town")
                || locationType.equals("village") || locationType.equals("hamlet")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Helper method to provide the icon resource drawable according to the location type returned
     * by the OpenStreetMap call.
     * @param context Context to use for resource localization
     * @param locationType from OpenStreetMap API response
     * @return Drawable for the corresponding icon. null if no relation is found.
     * @see <a href="https://www.google.com/design/icons/index.html">Material icons site</a>.
     */
    public static int getIconResourceIdForLocationType(Context context, String locationType) {
        Drawable drawable = null;

        if (locationType.equals("city")) {
            return R.drawable.ic_city_24dp;
        } else if (locationType.equals("town")) {
            return R.drawable.ic_town_24dp;
        } else if (locationType.equals("village")) {
            return R.drawable.ic_town_24dp;
        } else if (locationType.equals("hamlet")) {
            return R.drawable.ic_hamlet_24dp;
        }

        return -1;
    }
}

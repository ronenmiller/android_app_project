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
    public static Drawable getIconResourceForLocationType(Context context, String locationType) {
        Drawable drawable = null;

        if (locationType.equals("city")) {
            drawable =  context.getResources().getDrawable(R.drawable.ic_city_black_24dp);
        } else if (locationType.equals("town")) {
            drawable =  context.getResources().getDrawable(R.drawable.ic_town_black_24dp);
        } else if (locationType.equals("village")) {
            drawable =  context.getResources().getDrawable(R.drawable.ic_town_black_24dp);
        } else if (locationType.equals("hamlet")) {
            drawable = context.getResources().getDrawable(R.drawable.ic_hamlet_black_24dp);
        }

        if (drawable != null) {
            // Android uses default opacity of 54% for icons. Since the icons obtained from
            // the material icons site are black, apple the opacity programmatically.
            // This is the value of opacity between 1-255. 138 is approximately 54%.
            drawable.setAlpha(138);
        }

        return drawable;
    }
}

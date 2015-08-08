/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package il.ac.technion.touricity.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class ToursContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "il.ac.technion.touricity";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://il.ac.technion.touricity/tours/ is a valid path for
    // looking at tours data. content://il.ac.technion.touricity/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_LOCATION = "location";
    public static final String PATH_USERS = "users";
    public static final String PATH_PEOPLE = "people";
    public static final String PATH_TOURS = "tours";
    public static final String PATH_SLOTS = "slots";
    public static final String PATH_REQUESTS = "requests";
    public static final String PATH_RATE_TOUR = "rate2tour";
    public static final String PATH_RATE_GUIDE = "rate2guide";

    // TODO: consider removing normalizeDate if unused
    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    /*public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }*/

    /*
        Inner class that defines the contents of the location table
     */
    public static final class LocationEntry implements BaseColumns {

        public static final String TABLE_NAME = "location";

        // Human readable location string, provided by the API.
        public static final String COLUMN_LOCATION_NAME = "location_name";

        // The display name string to be used in the settings activity
        public static final String COLUMN_LOCATION_TYPE = "location_type";

        // In order to uniquely pinpoint the location on the map when we launch the
        // map intent, we store the latitude and longitude as returned by openweathermap.
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // TODO: check out how this is used
        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the contents of the users table */
    public static final class UserEntry implements BaseColumns {

        public static final String TABLE_NAME = "users";

        // User name as chosen by the user.
        public static final String COLUMN_USER_NAME = "user_name";
        // Password, stored as a string
        public static final String COLUMN_USER_PASSWORD= "user_password";
        // User's e-mail
        public static final String COLUMN_USER_EMAIL = "user_email";
        // User's phone number, used for SMS confirmation upon sign-up
        public static final String COLUMN_USER_PHONE = "user_phone";

        // Two types of users exist: guides and non-guides.
        // Guides have the ability to add tours and slots, and approve
        // requests of users to join a tour (=an open slot).
        public static final String COLUMN_TYPE_GUIDE = "user_type";

        // TODO: figure this out
        // Time, stored as long in milliseconds since the epoch
        public static final String COLUMN_LAST_LOGIN = "user_last_login";

        // Rating, used only for guides
        public static final String COLUMN_USER_RATING = "user_rating";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;

        // TODO: check out how this is used
        public static Uri buildUsersUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // TODO: this methodology might be used later
        /*
        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        */
    }
}

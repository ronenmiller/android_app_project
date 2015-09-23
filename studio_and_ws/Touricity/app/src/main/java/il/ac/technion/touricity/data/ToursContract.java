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
    public static final String PATH_OSM = "osm";
    public static final String PATH_TOURS = "tours";
    public static final String PATH_LANGUAGES = "languages";
    public static final String PATH_SLOTS = "slots";
    public static final String PATH_USERS = "users";
    public static final String PATH_PEOPLE = "people";
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

    /* Inner class that defines basic characteristics of locations */
    public static class BasicLocationEntry implements BaseColumns {

        public static final String COLUMN_LOCATION_ID = "location_id";

        // Human readable location string, provided by the API.
        // Also the display name string to be used in the settings activity.
        public static final String COLUMN_LOCATION_NAME = "location_name";

        // The type of location in order to set the correct image in the list view
        public static final String COLUMN_LOCATION_TYPE = "location_type";

        // In order to uniquely pinpoint the location on the map when we launch the
        // map intent, we store the latitude and longitude as returned by open street map.
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";
    }

    /* Inner class that defines the contents of the location table */
    public static class LocationEntry extends BasicLocationEntry {

        public static final String TABLE_NAME = "location";

        // Column _ID serves as a sort order, by most recently used.

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // This URI is returned when inserting a row to the table.
        // The returned id is the row number of the inserted row.
        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the contents of the OSM table which holds location queries */
    public static final class OSMEntry extends BasicLocationEntry {

        public static final String TABLE_NAME = "osm";

        // Column _ID serves as a sort order, by relevance.

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_OSM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OSM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OSM;

        // This URI is returned when inserting a row to the table.
        // The returned id is the row number of the inserted row.
        public static Uri buildOSMUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines basic characteristics of tours */
    public static class TourEntry implements BaseColumns {

        public static final String TABLE_NAME = "tours";

        // _ID serves as a unique tour id.

        // This is the Open Street Map ID returned by the API.
        public static final String COLUMN_OSM_ID = "location_id";

        // The tour name to appear in the list of tours.
        public static final String COLUMN_TOUR_TITLE = "tour_title";

        // Approximate tour duration in minutes.
        public static final String COLUMN_TOUR_DURATION = "tour_duration";

        // The tour is instructed in this language.
        // Also determines the image to use in the list of tours.
        public static final String COLUMN_TOUR_LANGUAGE = "tour_language";

        // The name of the gathering place, where the tour departs.
        public static final String COLUMN_TOUR_LOCATION = "tour_location";

        // The tour rating, as given by users who actually took the tour.
        public static final String COLUMN_TOUR_RATING = "tour_rating";

        // This parameter defines whether there are active slots available for this tour.
        // Stored as an integer. 1 = Tour is available, 0 = otherwise.
        public static final String COLUMN_TOUR_AVAILABLE = "tour_available";

        // The tour's description appears in the tour's detail page.
        public static final String COLUMN_TOUR_DESCRIPTION = "tour_description";

        // Photos of key locations along the tour's route.
        // These photos will appear in the tour's detail page.
        public static final String COLUMN_TOUR_PHOTOS = "tour_photos";

        // Comments of users on this tour.
        // NOTE: NOT IMPLEMENTED IN THE INITIAL RELEASE.
        public static final String COLUMN_TOUR_COMMENTS = "tour_comments";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOURS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOURS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOURS;

        // This URI is returned when inserting a row to the table.
        // The returned id is the row number of the inserted row.
        public static Uri buildTourUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // location ID is actually the OSM ID, as returned from the OSM API.
        public static Uri buildTourLocationUri(long locationId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(locationId)).build();
        }

        public static Uri buildTourIdUri(int tourId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(tourId)).build();
        }

        // Retrieve the OSM ID or tour ID, depending on the supplied uri.
        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    /* Inner class that defines basic characteristics of locations */
    public static class LanguageEntry implements BaseColumns {

        public static final String TABLE_NAME = "languages";

        // _ID serves as a unique language id.

        // English, Spanish, French, German, Chinese, Hebrew.
        public static final String COLUMN_LANGUAGE_NAME = "language_name";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANGUAGES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LANGUAGES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LANGUAGES;

        // This URI is returned when inserting a row to the table.
        // The returned id is the row number of the inserted row.
        public static Uri buildLanguagesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines basic characteristics of locations */
    public static class SlotEntry implements BaseColumns {

        public static final String TABLE_NAME = "slots";

        // _ID serves as a unique slot id.

        // This is the unique user ID of the guide instructing this slot.
        public static final String COLUMN_SLOT_GUIDE_ID = "slot_guide_id";

        // The tour for which this slot is attached.
        public static final String COLUMN_SLOT_TOUR_ID = "slot_tour_id";

        // The day on which this slot takes place. Stored as REAL data type, representing a
        // julian day in SQLite.
        public static final String COLUMN_SLOT_DATE = "slot_date";

        // The *local* time in which this slot takes place. Stored as a TEXT data type,
        // representing an HH:MM format in SQLite.
        public static final String COLUMN_SLOT_TIME = "slot_time";

        // The number of open positions that can still be reserved for this slot.
        public static final String COLUMN_SLOT_VACANT = "slot_vacant";

        // A slot is active as long as its ending time did not already pass, or the guide didn't
        // delete the slot. 1 = Slot is active, 0 = otherwise.
        public static final String COLUMN_SLOT_ACTIVE = "slot_active";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SLOTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SLOTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SLOTS;

        // This URI is returned when inserting a row to the table.
        // The returned id is the row number of the inserted row.
        public static Uri buildSlotUri(long id) {
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

        // This URI is returned when inserting a row to the table.
        // The returned id is the row number of the inserted row.
        public static Uri buildUsersUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}

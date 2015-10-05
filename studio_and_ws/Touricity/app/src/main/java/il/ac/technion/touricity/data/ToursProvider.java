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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

public class ToursProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ToursDbHelper mOpenHelper;

    static final int LOCATION = 100;
    static final int OSM = 200;
    static final int TOURS = 300;
    static final int TOURS_WITH_LOCATION = 301;
    static final int TOURS_WITH_MANAGER = 302;
    static final int LANGUAGES = 400;
    static final int SLOTS = 500;
    static final int RESERVATIONS = 600;
    static final int USERS = 700;

    // A table of tours with the corresponding languages of those tours.
    private static final SQLiteQueryBuilder sToursWithLanguageNameQueryBuilder;
    // A table of tours with the corresponding locations and languages of those tours.
    private static final SQLiteQueryBuilder sToursWithLocationAndLanguageNameQueryBuilder;
    // A table of slots with the guides of those slots.
    private static final SQLiteQueryBuilder sSlotsWithUsersQueryBuilder;
    // All the tables together.
    private static final SQLiteQueryBuilder sReservationsQueryBuilder;

    static {
        sToursWithLanguageNameQueryBuilder = new SQLiteQueryBuilder();

        // This is an inner join which looks like
        // tours INNER JOIN languages ON tours.t_language = language._id
        sToursWithLanguageNameQueryBuilder.setTables(
                ToursContract.TourEntry.TABLE_NAME + " INNER JOIN " +
                        ToursContract.LanguageEntry.TABLE_NAME +
                        " ON " + ToursContract.TourEntry.TABLE_NAME +
                        "." + ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE +
                        " = " + ToursContract.LanguageEntry.TABLE_NAME +
                        "." + ToursContract.LanguageEntry._ID);

        sToursWithLocationAndLanguageNameQueryBuilder = new SQLiteQueryBuilder();

        // This is an inner join which looks like
        // tours INNER JOIN languages ON tours.t_language = language._id
        sToursWithLocationAndLanguageNameQueryBuilder.setTables(
                ToursContract.TourEntry.TABLE_NAME + " INNER JOIN " +
                        ToursContract.LocationEntry.TABLE_NAME +
                        " ON " + ToursContract.TourEntry.TABLE_NAME +
                        "." + ToursContract.TourEntry.COLUMN_OSM_ID +
                        " = " + ToursContract.LocationEntry.TABLE_NAME +
                        "." + ToursContract.LocationEntry.COLUMN_LOCATION_ID + " INNER JOIN " +
                        ToursContract.LanguageEntry.TABLE_NAME +
                        " ON " + ToursContract.TourEntry.TABLE_NAME +
                        "." + ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE +
                        " = " + ToursContract.LanguageEntry.TABLE_NAME +
                        "." + ToursContract.LanguageEntry._ID);

        sSlotsWithUsersQueryBuilder = new SQLiteQueryBuilder();

        // This is an inner join which looks like
        // slots INNER JOIN users ON slots.u_id = language._id
        sSlotsWithUsersQueryBuilder.setTables(
                ToursContract.SlotEntry.TABLE_NAME + " INNER JOIN " +
                        ToursContract.UserEntry.TABLE_NAME +
                        " ON " + ToursContract.SlotEntry.TABLE_NAME +
                        "." + ToursContract.SlotEntry.COLUMN_SLOT_GUIDE_ID +
                        " = " + ToursContract.UserEntry.TABLE_NAME +
                        "." + ToursContract.UserEntry._ID);

        sReservationsQueryBuilder = new SQLiteQueryBuilder();

        // This is an inner join which contains all the tables that are related to a tour.
        sReservationsQueryBuilder.setTables(
                ToursContract.ReservationEntry.TABLE_NAME + " INNER JOIN " +
                        ToursContract.SlotEntry.TABLE_NAME +
                        " ON " + ToursContract.ReservationEntry.TABLE_NAME +
                        "." + ToursContract.ReservationEntry._ID +
                        " = " + ToursContract.SlotEntry.TABLE_NAME +
                        "." + ToursContract.SlotEntry._ID + " INNER JOIN " +
                        ToursContract.UserEntry.TABLE_NAME +
                        " ON " + ToursContract.SlotEntry.TABLE_NAME +
                        "." + ToursContract.SlotEntry.COLUMN_SLOT_GUIDE_ID +
                        " = " + ToursContract.UserEntry.TABLE_NAME +
                        "." + ToursContract.UserEntry._ID + " INNER JOIN " +
                        ToursContract.TourEntry.TABLE_NAME +
                        " ON " + ToursContract.SlotEntry.TABLE_NAME +
                        "." + ToursContract.SlotEntry.COLUMN_SLOT_TOUR_ID +
                        " = " + ToursContract.TourEntry.TABLE_NAME +
                        "." + ToursContract.TourEntry._ID + " INNER JOIN " +
                        ToursContract.LanguageEntry.TABLE_NAME +
                        " ON " + ToursContract.TourEntry.TABLE_NAME +
                        "." + ToursContract.TourEntry.COLUMN_TOUR_LANGUAGE +
                        " = " + ToursContract.LanguageEntry.TABLE_NAME +
                        "." + ToursContract.LanguageEntry._ID);
    }

    // Tour with a specific location (i.e. OSM ID).
    private static final String sToursWithLocationSelection =
            ToursContract.TourEntry.TABLE_NAME +
                    "." + ToursContract.TourEntry.COLUMN_OSM_ID + " = ?";

    private Cursor getToursWithLocation(Uri uri, String[] projection, String sortOrder) {
        long locationId = ToursContract.TourEntry.getOsmIdFromUri(uri);

        return sToursWithLanguageNameQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                sToursWithLocationSelection,
                new String[]{Long.toString(locationId)},
                null,
                null,
                sortOrder
        );
    }

    // Tour with a specific manager.
    private static final String sToursWithManagerSelection =
            ToursContract.TourEntry.TABLE_NAME +
                    "." + ToursContract.TourEntry.COLUMN_TOUR_MANAGER_ID + " = ?";

    private Cursor getToursWithManager(Uri uri, String[] projection, String sortOrder) {
        String managerId = ToursContract.TourEntry.getTourManagerIdFromUri(uri);

        return sToursWithLocationAndLanguageNameQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                sToursWithManagerSelection,
                new String[]{managerId},
                null,
                null,
                sortOrder
        );
    }

    /*  The UriMatcher matches a URI to a constant number for the ease of using switch statements. */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ToursContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // ToursContract to help define the types to the UriMatcher.
        uriMatcher.addURI(authority, ToursContract.PATH_LOCATION, LOCATION);
        uriMatcher.addURI(authority, ToursContract.PATH_OSM, OSM);
        uriMatcher.addURI(authority, ToursContract.PATH_TOURS, TOURS);
        uriMatcher.addURI(authority, ToursContract.PATH_TOURS + "/#", TOURS_WITH_LOCATION);
        uriMatcher.addURI(authority, ToursContract.PATH_TOURS + "/*", TOURS_WITH_MANAGER);
        uriMatcher.addURI(authority, ToursContract.PATH_LANGUAGES, LANGUAGES);
        uriMatcher.addURI(authority, ToursContract.PATH_SLOTS, SLOTS);
        uriMatcher.addURI(authority, ToursContract.PATH_RESERVATIONS, RESERVATIONS);
        uriMatcher.addURI(authority, ToursContract.PATH_USERS, USERS);
        // 3) Return the new matcher!
        return uriMatcher;
    }

    /* We just create a new ToursDbHelper for later use here. */
    @Override
    public boolean onCreate() {
        mOpenHelper = new ToursDbHelper(getContext());
        return true;
    }

    /* Here's where you'll code the getType function that uses the UriMatcher. */
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case LOCATION:
                return ToursContract.LocationEntry.CONTENT_TYPE;
            case OSM:
                return ToursContract.OSMEntry.CONTENT_TYPE;
            case TOURS:
                return ToursContract.TourEntry.CONTENT_TYPE;
            case TOURS_WITH_LOCATION:
                return ToursContract.TourEntry.CONTENT_TYPE;
            case TOURS_WITH_MANAGER:
                return ToursContract.TourEntry.CONTENT_TYPE;
            case LANGUAGES:
                // Always returns a single language.
                return ToursContract.LanguageEntry.CONTENT_ITEM_TYPE;
            case SLOTS:
                return ToursContract.SlotEntry.CONTENT_TYPE;
            case RESERVATIONS:
                return ToursContract.ReservationEntry.CONTENT_TYPE;
            case USERS:
                // Always returns a single user.
                return ToursContract.UserEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ToursContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case OSM: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ToursContract.OSMEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TOURS: {
                retCursor = sToursWithLanguageNameQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TOURS_WITH_LOCATION: {
                retCursor = getToursWithLocation(uri, projection, sortOrder);
                break;
            }
            case TOURS_WITH_MANAGER: {
                retCursor = getToursWithManager(uri, projection, sortOrder);
                break;
            }
            case LANGUAGES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ToursContract.LanguageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SLOTS: {
                retCursor = sSlotsWithUsersQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case RESERVATIONS: {
                retCursor = sReservationsQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case USERS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ToursContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case LOCATION: {
                long _id = db.insert(ToursContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ToursContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case OSM: {
                long _id = db.insert(ToursContract.OSMEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ToursContract.OSMEntry.buildOSMUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TOURS: {
                long _id = db.insert(ToursContract.TourEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ToursContract.TourEntry.buildTourUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LANGUAGES: {
                long _id = db.insert(ToursContract.LanguageEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ToursContract.LanguageEntry.buildLanguageUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SLOTS: {
                long _id = db.insert(ToursContract.SlotEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ToursContract.SlotEntry.buildSlotUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case RESERVATIONS: {
                long _id = db.insert(ToursContract.ReservationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ToursContract.ReservationEntry.buildReservationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case USERS: {
                long _id = db.insert(ToursContract.UserEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ToursContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the uri listeners (using the content resolver) about the insertion.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        // This makes delete all rows return the number of rows deleted
        if (selection == null) selection = "1";
        switch (match) {
            case LOCATION: {
                rowsDeleted = db.delete(
                        ToursContract.LocationEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case OSM: {
                rowsDeleted = db.delete(
                        ToursContract.OSMEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case TOURS: {
                rowsDeleted = db.delete(
                        ToursContract.TourEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case LANGUAGES: {
                rowsDeleted = db.delete(
                        ToursContract.LanguageEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case SLOTS: {
                rowsDeleted = db.delete(
                        ToursContract.SlotEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case RESERVATIONS: {
                rowsDeleted = db.delete(
                        ToursContract.ReservationEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case USERS: {
                rowsDeleted = db.delete(
                        ToursContract.UserEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the uri listeners (using the content resolver) if the rowsDeleted != 0
        // or the selection null (all rows deleted).
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the actual number of rows that were deleted
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // This is a lot like the delete function. Return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case LOCATION: {
                rowsUpdated = db.update(
                        ToursContract.LocationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case OSM: {
                rowsUpdated = db.update(
                        ToursContract.OSMEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case TOURS: {
                rowsUpdated = db.update(
                        ToursContract.TourEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case LANGUAGES: {
                rowsUpdated = db.update(
                        ToursContract.LanguageEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case SLOTS: {
                rowsUpdated = db.update(
                        ToursContract.SlotEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case RESERVATIONS: {
                rowsUpdated = db.update(
                        ToursContract.ReservationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case USERS: {
                rowsUpdated = db.update(
                        ToursContract.UserEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // If any rows were updated, notify the uri listeners (using the content resolver).
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id;
                switch (match) {
                    case LOCATION: {
                        _id = db.insert(ToursContract.LocationEntry.TABLE_NAME, null, value);
                        break;
                    }
                    case OSM: {
                        _id = db.insert(ToursContract.OSMEntry.TABLE_NAME, null, value);
                        break;
                    }
                    case TOURS: {
                        _id = db.insert(ToursContract.TourEntry.TABLE_NAME, null, value);
                        break;
                    }
                    case LANGUAGES: {
                        _id = db.insert(ToursContract.LanguageEntry.TABLE_NAME, null, value);
                        break;
                    }
                    case SLOTS: {
                        _id = db.insert(ToursContract.SlotEntry.TABLE_NAME, null, value);
                        break;
                    }
                    case RESERVATIONS: {
                        _id = db.insert(ToursContract.ReservationEntry.TABLE_NAME, null, value);
                        break;
                    }
                    case USERS: {
                        _id = db.insert(ToursContract.UserEntry.TABLE_NAME, null, value);
                        break;
                    }
                    default:
                        return super.bulkInsert(uri, values);
                }
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        // Notify the uri listeners (using the content resolver) about the insertion.
        // TOURS adapter is notified using a local broadcast intent.
        if (match != TOURS) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnCount;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
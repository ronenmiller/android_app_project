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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import il.ac.technion.touricity.data.ToursContract.LanguageEntry;
import il.ac.technion.touricity.data.ToursContract.LocationEntry;
import il.ac.technion.touricity.data.ToursContract.OSMEntry;
import il.ac.technion.touricity.data.ToursContract.SlotEntry;
import il.ac.technion.touricity.data.ToursContract.TourEntry;
import il.ac.technion.touricity.data.ToursContract.UserEntry;
import il.ac.technion.touricity.data.ToursContract.ReservationEntry;

/**
 * Manages a local database for weather data.
 */
public class ToursDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 4;

    static final String DATABASE_NAME = "tours.db";

    public ToursDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // For available data types in SQLite, please see:
        // https://www.sqlite.org/datatype3.html
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY, " +

                LocationEntry.COLUMN_LOCATION_ID + " INTEGER NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_TYPE + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +

                // To assure the application have just one location per OSM ID.
                // Replacing the old entry will help manage history suggestions.
                " UNIQUE (" + LocationEntry.COLUMN_LOCATION_ID + ") ON CONFLICT REPLACE)";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);

        final String SQL_CREATE_OSM_TABLE = "CREATE TABLE " + OSMEntry.TABLE_NAME + " (" +
                OSMEntry._ID + " INTEGER PRIMARY KEY, " +

                OSMEntry.COLUMN_LOCATION_ID + " INTEGER UNIQUE NOT NULL, " +
                OSMEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                OSMEntry.COLUMN_LOCATION_TYPE + " TEXT NOT NULL, " +
                OSMEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                OSMEntry.COLUMN_COORD_LONG + " REAL NOT NULL)";

        sqLiteDatabase.execSQL(SQL_CREATE_OSM_TABLE);

        final String SQL_CREATE_TOURS_TABLE = "CREATE TABLE " + TourEntry.TABLE_NAME + " (" +
                TourEntry._ID + " INTEGER, " +

                TourEntry.COLUMN_OSM_ID + " INTEGER NOT NULL, " +
                TourEntry.COLUMN_TOUR_MANAGER_ID + " TEXT NOT NULL, " +
                TourEntry.COLUMN_TOUR_TITLE + " TEXT NOT NULL, " +
                TourEntry.COLUMN_TOUR_DURATION + " INTEGER NOT NULL CHECK (" +
                TourEntry.COLUMN_TOUR_DURATION + " > 0), " +
                TourEntry.COLUMN_TOUR_LANGUAGE + " INTEGER NOT NULL, " +
                TourEntry.COLUMN_TOUR_LOCATION + " TEXT NOT NULL, " +
                TourEntry.COLUMN_TOUR_RATING + " REAL CHECK (" +
                TourEntry.COLUMN_TOUR_RATING + " >= 0 AND " +
                TourEntry.COLUMN_TOUR_RATING + " <= 5 ), " +
                // 1 = Tour is available, 0 = otherwise.
                TourEntry.COLUMN_TOUR_AVAILABLE + " INTEGER NOT NULL, " +
                TourEntry.COLUMN_TOUR_DESCRIPTION + " TEXT, " +
                // TODO: figure out the next fields in later releases.
                // SQLite does not support arrays. Load photos directly from internet?
                TourEntry.COLUMN_TOUR_PHOTOS + " BLOB, " +
                TourEntry.COLUMN_TOUR_COMMENTS + " TEXT, " +

                " PRIMARY KEY (" + TourEntry._ID + ") ON CONFLICT REPLACE, " +
                // Set up the OSM ID column as a foreign key to location table.
                " FOREIGN KEY (" + TourEntry.COLUMN_OSM_ID + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + ") ON DELETE CASCADE," +
                // Set up the language column as a foreign key to languages table.
                " FOREIGN KEY (" + TourEntry.COLUMN_TOUR_LANGUAGE + ") REFERENCES " +
                LanguageEntry.TABLE_NAME + " (" + LanguageEntry._ID + ") ON DELETE RESTRICT)";

        sqLiteDatabase.execSQL(SQL_CREATE_TOURS_TABLE);

        final String SQL_CREATE_LANGUAGES_TABLE = "CREATE TABLE " + LanguageEntry.TABLE_NAME + " (" +
                LanguageEntry._ID + " INTEGER PRIMARY KEY, " +

                LanguageEntry.COLUMN_LANGUAGE_NAME + " TEXT NOT NULL, " +

                " UNIQUE (" + LanguageEntry.COLUMN_LANGUAGE_NAME + ") ON CONFLICT REPLACE)";

        sqLiteDatabase.execSQL(SQL_CREATE_LANGUAGES_TABLE);

        final String SQL_CREATE_SLOTS_TABLE = "CREATE TABLE " + SlotEntry.TABLE_NAME + " (" +
                SlotEntry._ID + " INTEGER, " +

                SlotEntry.COLUMN_SLOT_GUIDE_ID + " TEXT NOT NULL, " +
                SlotEntry.COLUMN_SLOT_TOUR_ID + " INTEGER NOT NULL, " +
                SlotEntry.COLUMN_SLOT_DATE + " REAL NOT NULL, " +
                SlotEntry.COLUMN_SLOT_TIME + " INTEGER NOT NULL, " +
                SlotEntry.COLUMN_SLOT_CURRENT_CAPACITY + " INTEGER NOT NULL CHECK (" +
                SlotEntry.COLUMN_SLOT_CURRENT_CAPACITY + " >= 0), " +
                SlotEntry.COLUMN_SLOT_TOTAL_CAPACITY + " INTEGER NOT NULL CHECK (" +
                SlotEntry.COLUMN_SLOT_TOTAL_CAPACITY + " >= 0), " +
                // 1 = Slot is active, 0 = otherwise.
                SlotEntry.COLUMN_SLOT_ACTIVE + " INTEGER NOT NULL, " +
                // 1 = Slot canceled, 0 = otherwise.
                SlotEntry.COLUMN_SLOT_CANCELED + " INTEGER NOT NULL, " +

                " PRIMARY KEY (" + SlotEntry._ID + ") ON CONFLICT REPLACE, " +
                // Set up the guide ID column as a foreign key to the users table.
                " FOREIGN KEY (" + SlotEntry.COLUMN_SLOT_GUIDE_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry._ID + ") ON DELETE RESTRICT, " +
                // Set up the tour ID column as a foreign key to the tours table.
                " FOREIGN KEY (" + SlotEntry.COLUMN_SLOT_TOUR_ID + ") REFERENCES " +
                TourEntry.TABLE_NAME + " (" + TourEntry._ID + ") ON DELETE RESTRICT)";

        sqLiteDatabase.execSQL(SQL_CREATE_SLOTS_TABLE);

        final String SQL_CREATE_RESERVATIONS_TABLE = "CREATE TABLE " + ReservationEntry.TABLE_NAME + " (" +
                ReservationEntry._ID + " INTEGER, " +

                ReservationEntry.COLUMN_RESERVATION_USER_ID + " TEXT, " +
                ReservationEntry.COLUMN_RESERVATION_PARTICIPANTS + " INTEGER NOT NULL, " +
                ReservationEntry.COLUMN_RESERVATION_ACTIVE + " INTEGER NOT NULL, " +

                "PRIMARY KEY (" + ReservationEntry._ID + ", " +
                ReservationEntry.COLUMN_RESERVATION_USER_ID + ") ON CONFLICT REPLACE, " +
                " FOREIGN KEY (" + ReservationEntry._ID + ") REFERENCES " +
                SlotEntry.TABLE_NAME + " (" + SlotEntry._ID + ") ON DELETE CASCADE, " +
                " FOREIGN KEY (" + ReservationEntry.COLUMN_RESERVATION_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry._ID + ") ON DELETE CASCADE)";

        sqLiteDatabase.execSQL(SQL_CREATE_RESERVATIONS_TABLE);

        final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " TEXT, " +

                UserEntry.COLUMN_USER_NAME + " TEXT, " +
                UserEntry.COLUMN_USER_EMAIL + " TEXT, " +
                UserEntry.COLUMN_USER_RATING + " REAL, " +

                // Set up the tour ID column as a foreign key to the tours table.
                "PRIMARY KEY (" + UserEntry._ID + ", " +
                UserEntry.COLUMN_USER_NAME + ", " +
                UserEntry.COLUMN_USER_EMAIL + ") ON CONFLICT REPLACE)";

        sqLiteDatabase.execSQL(SQL_CREATE_USERS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + OSMEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TourEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LanguageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SlotEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReservationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

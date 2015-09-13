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

import il.ac.technion.touricity.data.ToursContract.LocationEntry;
import il.ac.technion.touricity.data.ToursContract.OSMEntry;
import il.ac.technion.touricity.data.ToursContract.TourEntry;

/**
 * Manages a local database for weather data.
 */
public class ToursDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "tours.db";

    public ToursDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // For available data types in SQLite, please see:
        // https://www.sqlite.org/datatype3.html
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +

                LocationEntry.COLUMN_OSM_ID + " INTEGER UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_TYPE + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +

                // To assure the application have just one location per OSM ID.
                // Replacing the old entry will help manage history suggestions.
                " UNIQUE (" + LocationEntry.COLUMN_OSM_ID +  ", " +
                LocationEntry.COLUMN_LOCATION_NAME + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);

        final String SQL_CREATE_OSM_TABLE = "CREATE TABLE " + OSMEntry.TABLE_NAME + " (" +
                OSMEntry._ID + " INTEGER PRIMARY KEY," +

                OSMEntry.COLUMN_OSM_ID + " INTEGER UNIQUE NOT NULL, " +
                OSMEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                OSMEntry.COLUMN_LOCATION_TYPE + " TEXT NOT NULL, " +
                OSMEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                OSMEntry.COLUMN_COORD_LONG + " REAL NOT NULL)";

        sqLiteDatabase.execSQL(SQL_CREATE_OSM_TABLE);

        final String SQL_CREATE_TOURS_TABLE = "CREATE TABLE " + TourEntry.TABLE_NAME + " (" +
                TourEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                TourEntry.COLUMN_OSM_ID + " INTEGER NOT NULL, " +
                TourEntry.COLUMN_TOUR_TITLE + " TEXT NOT NULL, " +
                TourEntry.COLUMN_TOUR_DURATION + " INTEGER NOT NULL CHECK (" +
                        TourEntry.COLUMN_TOUR_DURATION + " > 0), " +
                TourEntry.COLUMN_TOUR_LOCATION + " TEXT NOT NULL, " +
                TourEntry.COLUMN_TOUR_RATING + " REAL CHECK (" +
                        TourEntry.COLUMN_TOUR_RATING + " >= 0 AND " +
                        TourEntry.COLUMN_TOUR_RATING + " <= 5 )," +
                // 1 = Tour is available, 0 = otherwise.
                TourEntry.COLUMN_TOUR_AVAILABLE + " INTEGER NOT NULL," +
                TourEntry.COLUMN_TOUR_DESCRIPTION + " TEXT," +
                TourEntry.COLUMN_TOUR_THUMBNAIL + " BLOB," +
                // TODO: figure out the next fields in later releases.
                // SQLite does not support arrays. Load photos directly from internet?
                TourEntry.COLUMN_TOUR_PHOTOS + " BLOB," +
                TourEntry.COLUMN_TOUR_LANGUAGES + " INTEGER," +
                TourEntry.COLUMN_TOUR_COMMENTS + " TEXT," +

                // Set up the OSM ID column as a foreign key to location table.
                " FOREIGN KEY (" + TourEntry.COLUMN_OSM_ID + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry.COLUMN_OSM_ID + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_TOURS_TABLE);
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
        onCreate(sqLiteDatabase);
    }
}

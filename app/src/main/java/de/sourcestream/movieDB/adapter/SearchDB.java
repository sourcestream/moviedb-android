/*
 *  Copyright 2015 sourcestream GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.sourcestream.movieDB.adapter;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;


public class SearchDB {

    private static final String DBNAME = "search";
    private static final int VERSION = 1;
    private SearchDBOpenHelper mSearchDBOpenHelper;
    private static final String FIELD_id = "_id";
    private static final String FIELD_searchID = "searchID";
    private static final String FIELD_title = "title";
    private static final String FIELD_subTitle = "subTitle";
    private static final String FIELD_imgUrl = "imgUrl";
    private static final String FIELD_mediaType = "mediaType";
    private static final String TABLE1_NAME = "search";
    private static final String TABLE2_NAME = "suggestions";
    private HashMap<String, String> mAliasMap;

    public SearchDB(Context context) {
        mSearchDBOpenHelper = new SearchDBOpenHelper(context, DBNAME, null, VERSION);
        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<>();
        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", FIELD_id + " as " + "_id");

        // This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, FIELD_searchID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);

        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, FIELD_title + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);

        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, FIELD_subTitle + " as " + SearchManager.SUGGEST_COLUMN_TEXT_2);

        // Icon for Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, FIELD_imgUrl + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);

        // Icon for Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, FIELD_mediaType + " as " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
    }

    public synchronized Cursor autoComplete() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);
        queryBuilder.setTables(TABLE1_NAME);

        SQLiteDatabase db = mSearchDBOpenHelper.getReadableDatabase();
        Cursor c = null;
        if (db.isOpen()) {
            c = queryBuilder.query(db,
                    new String[]{"_ID",
                            SearchManager.SUGGEST_COLUMN_TEXT_1,
                            SearchManager.SUGGEST_COLUMN_TEXT_2,
                            SearchManager.SUGGEST_COLUMN_ICON_1,
                            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA},
                    null, null, null, null, null, "10"
            );
        }

        return c;
    }


    /**
     * Returns Suggestions
     */
    public synchronized Cursor getSuggestions(String[] selectionArgs) {
        String selection = FIELD_title + " like ? ";

        if (selectionArgs != null) {
            if (!selectionArgs[0].isEmpty()) {
                selectionArgs[0].replaceAll("'", "");
                selectionArgs[0] = "%" + selectionArgs[0] + "%";
            } else {
                selection = null;
                selectionArgs = null;
            }
        }

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);
        queryBuilder.setTables(TABLE2_NAME);
        SQLiteDatabase db = mSearchDBOpenHelper.getReadableDatabase();
        Cursor c = null;
        if (db.isOpen()) {
            c = queryBuilder.query(db,
                    new String[]{"_ID",
                            SearchManager.SUGGEST_COLUMN_TEXT_1,
                            SearchManager.SUGGEST_COLUMN_TEXT_2,
                            SearchManager.SUGGEST_COLUMN_ICON_1,
                            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    FIELD_title + " asc ", "10"
            );
        }
        return c;
    }

    public int getSuggestionSize() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);

        queryBuilder.setTables(TABLE2_NAME);
        Cursor c = queryBuilder.query(mSearchDBOpenHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_TEXT_2,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                        SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA},
                null, null, null, null, null, "10"
        );
        return c.getCount();
    }


    public void insertAutoComplete(int id, String title, Uri posterPath, String subTitle, String mediaType) {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();

        // Defining insert statement
        String sql = "insert into " + TABLE1_NAME + " ( " +
                FIELD_searchID + " , " +
                FIELD_title + " , " +
                FIELD_imgUrl + " , " +
                FIELD_subTitle + " , " +
                FIELD_mediaType + ") " +
                " values ( " +
                " " + id + " ," +
                "  '" + title + "'  ," +
                "  '" + posterPath + "'  ," +
                "  '" + subTitle + "'  ," +
                " '" + mediaType + "' ) ";

        // Inserting values into table
        db.execSQL(sql);
    }

    public void cleanAutoCompleteRecords() {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Defining insert statement
        String sql = "DELETE FROM " + TABLE1_NAME + " ; ";

        // Inserting values into table
        db.execSQL(sql);
    }


    public void insertSuggestion(int id, String title, Uri posterPath, String subTitle, String mediaType) {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Defining insert statement
        String sql = "insert into " + TABLE2_NAME + " ( " +
                FIELD_searchID + " , " +
                FIELD_title + " , " +
                FIELD_imgUrl + ", " +
                FIELD_subTitle + ", " +
                FIELD_mediaType + " ) " +
                " values ( " +
                " " + id + " ," +
                "  '" + title + "'  ," +
                "  '" + posterPath + "'  ," +
                "  '" + subTitle + "'  ," +
                " '" + mediaType + "' ) ";

        // Inserting values into table
        db.execSQL(sql);
    }

    public void cleanSuggestionRecords() {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Defining insert statement
        String sql = "DELETE FROM " + TABLE2_NAME + " ; ";

        // Inserting values into table
        db.execSQL(sql);
    }

    public void updateImg(int currId, Uri uriFile) {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Defining insert statement
        String sql = "UPDATE " + TABLE1_NAME + " SET " + FIELD_imgUrl + "=\"" + uriFile + "\" WHERE " + FIELD_searchID + "=" + currId + "; ";

        // Inserting values into table
        db.execSQL(sql);
    }


    private class SearchDBOpenHelper extends SQLiteOpenHelper {

        public SearchDBOpenHelper(Context context,
                                  String name,
                                  CursorFactory factory,
                                  int version) {
            super(context, DBNAME, factory, VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            // Defining table structure
            String sql = " CREATE TABLE " + TABLE1_NAME + "" +
                    " ( " +
                    FIELD_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FIELD_searchID + " INTEGER, " +
                    FIELD_title + " VARCHAR(100), " +
                    FIELD_imgUrl + " VARCHAR(100), " +
                    FIELD_subTitle + " VARCHAR(100), " +
                    FIELD_mediaType + " VARCHAR(100) " +
                    " ) ";

            // Creating table
            db.execSQL(sql);

            // Defining table structure
            sql = " CREATE TABLE " + TABLE2_NAME + "" +
                    " ( " +
                    FIELD_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FIELD_searchID + " INTEGER, " +
                    FIELD_title + " VARCHAR(100), " +
                    FIELD_imgUrl + " VARCHAR(100), " +
                    FIELD_subTitle + " VARCHAR(100), " +
                    FIELD_mediaType + " VARCHAR(100) " +
                    " ) ";

            // Creating table
            db.execSQL(sql);


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
        }
    }

}
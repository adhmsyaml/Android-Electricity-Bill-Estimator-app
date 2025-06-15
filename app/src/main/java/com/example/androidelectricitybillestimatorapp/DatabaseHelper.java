package com.example.androidelectricitybillestimatorapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database information
    private static final String DATABASE_NAME = "electricity_bills.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_BILLS = "bills";

    // Table Columns
    public static final String COLUMN_ID = "_id"; // Primary key, auto-incrementing
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_KWH_USED = "kwh_used";
    public static final String COLUMN_REBATE_PERCENTAGE = "rebate_percentage";
    public static final String COLUMN_TOTAL_CHARGES_RM = "total_charges_rm";
    public static final String COLUMN_FINAL_COST_RM = "final_cost_rm";

    // SQL query to create the table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_BILLS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MONTH + " TEXT NOT NULL, " +
                    COLUMN_KWH_USED + " REAL NOT NULL, " + // REAL for floating point numbers
                    COLUMN_REBATE_PERCENTAGE + " INTEGER NOT NULL, " +
                    COLUMN_TOTAL_CHARGES_RM + " REAL NOT NULL, " +
                    COLUMN_FINAL_COST_RM + " REAL NOT NULL" +
                    ");";

    public DatabaseHelper(Context context) {
        // Call the super constructor to create or open the database
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This method is called when the database is created for the first time.
        // Execute the SQL to create the table.
        db.execSQL(TABLE_CREATE);
        Log.d("DatabaseHelper", "Database created and table " + TABLE_BILLS + " created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This method is called when the database needs to be upgraded
        // (i.e., DATABASE_VERSION is incremented).
        // For simplicity, we drop the old table and recreate it.
        // In a real app, you would handle data migration carefully.
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    /**
     * Adds a new electricity bill record to the database.
     * @param month The month of the bill.
     * @param kwhUsed The electricity units used in kWh.
     * @param rebatePercentage The rebate percentage applied.
     * @param totalChargesRm The total charges in Malaysian Ringgit before rebate.
     * @param finalCostRm The final cost in Malaysian Ringgit after rebate.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long addBill(String month, double kwhUsed, int rebatePercentage,
                        double totalChargesRm, double finalCostRm) {
        // Get writable database instance
        SQLiteDatabase db = this.getWritableDatabase();
        long newRowId = -1; // Default to -1 indicating failure

        try {
            // ContentValues is used to store key-value pairs
            ContentValues values = new ContentValues();
            values.put(COLUMN_MONTH, month);
            values.put(COLUMN_KWH_USED, kwhUsed);
            values.put(COLUMN_REBATE_PERCENTAGE, rebatePercentage);
            values.put(COLUMN_TOTAL_CHARGES_RM, totalChargesRm);
            values.put(COLUMN_FINAL_COST_RM, finalCostRm);

            // Insert the new row, returning the primary key value of the new row
            newRowId = db.insert(TABLE_BILLS, null, values);
            Log.d("DatabaseHelper", "Bill added with ID: " + newRowId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding bill: " + e.getMessage());
        } finally {
            db.close(); // Close the database connection
        }
        return newRowId;
    }

    /**
     * Retrieves all electricity bill records from the database.
     * @return A List of Bill objects.
     */
    public List<Bill> getAllBills() {
        List<Bill> billList = new ArrayList<>();
        // Select all query
        String selectQuery = "SELECT * FROM " + TABLE_BILLS;

        SQLiteDatabase db = this.getReadableDatabase(); // Get readable database instance
        Cursor cursor = null; // Cursor to hold results

        try {
            cursor = db.rawQuery(selectQuery, null); // Execute the query

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    // Create a new Bill object from the current row's data
                    Bill bill = new Bill(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONTH)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_KWH_USED)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REBATE_PERCENTAGE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_CHARGES_RM)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FINAL_COST_RM))
                    );
                    billList.add(bill); // Add the bill to the list
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting bills: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Close the cursor
            }
            db.close(); // Close the database connection
        }
        return billList;
    }

    /**
     * Deletes all electricity bill records from the database.
     * @return The number of rows affected.
     */
    public int deleteAllBills() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            rowsAffected = db.delete(TABLE_BILLS, null, null); // Delete all rows
            Log.d("DatabaseHelper", "Deleted " + rowsAffected + " bills.");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting all bills: " + e.getMessage());
        } finally {
            db.close(); // Close the database connection
        }
        return rowsAffected;
    }
}
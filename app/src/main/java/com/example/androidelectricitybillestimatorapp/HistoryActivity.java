package com.example.androidelectricitybillestimatorapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvBillHistory;
    private TextView tvEmptyHistoryMessage;
    private Button btnDeleteAllHistory;
    private Spinner spinnerSort;

    private DatabaseHelper databaseHelper;
    private List<Bill> billList; // The list of Bill objects
    private ArrayAdapter<String> billArrayAdapter; // Adapter for ListView
    private String[] sortOptions; // Array for spinner sort options

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.historyToolbar);
        setSupportActionBar(toolbar);
        // Enable the Up button (back arrow)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize UI Elements
        lvBillHistory = findViewById(R.id.lvBillHistory);
        tvEmptyHistoryMessage = findViewById(R.id.tvEmptyHistoryMessage);
        btnDeleteAllHistory = findViewById(R.id.btnDeleteAllHistory);
        spinnerSort = findViewById(R.id.spinnerSort);

        databaseHelper = new DatabaseHelper(this);

        // Load sort options from strings.xml
        sortOptions = new String[]{
                getString(R.string.sort_by_month_asc),
                getString(R.string.sort_by_month_desc),
                getString(R.string.sort_by_cost_asc),
                getString(R.string.sort_by_cost_desc)
        };

        // Setup Sort Spinner
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        // Load and display bills initially
        loadBills();

        // --- Event Listeners ---

        // ListView Item Click Listener
        lvBillHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the clicked Bill object
                Bill selectedBill = billList.get(position);

                // Create an Intent to start DetailActivity
                Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
                // Pass the Bill object to the DetailActivity
                intent.putExtra("selected_bill", selectedBill);
                startActivity(intent);
            }
        });

        // Delete All History Button Listener
        btnDeleteAllHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteAllBills();
            }
        });

        // Sort Spinner Listener
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortBills(position); // Sort based on selected option
                // Set the text color of the selected item in the spinner
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.md_theme_onSurface));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key or a system-generated back event.
     * This is crucial for the Up button in the Toolbar.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to the previous activity (MainActivity)
        return true;
    }

    /**
     * Loads all bill records from the database and updates the ListView.
     */
    private void loadBills() {
        billList = databaseHelper.getAllBills(); // Get all bills from DB

        if (billList.isEmpty()) {
            lvBillHistory.setVisibility(View.GONE);
            tvEmptyHistoryMessage.setVisibility(View.VISIBLE);
            btnDeleteAllHistory.setEnabled(false); // Disable delete button if no history
        } else {
            lvBillHistory.setVisibility(View.VISIBLE);
            tvEmptyHistoryMessage.setVisibility(View.GONE);
            btnDeleteAllHistory.setEnabled(true); // Enable delete button
            // Initialize ArrayAdapter with a list of formatted strings for display
            displayBills(billList);
        }
    }

    /**
     * Formats the list of Bill objects into strings for display in the ListView.
     * @param bills The list of Bill objects to display.
     */
    private void displayBills(List<Bill> bills) {
        ArrayList<String> billStrings = new ArrayList<>();
        DecimalFormat dfCurrency = new DecimalFormat("RM #,##0.00");

        for (Bill bill : bills) {
            String formattedBill = String.format(Locale.getDefault(),
                    getString(R.string.history_list_item_format),
                    bill.getMonth(),
                    String.format(Locale.getDefault(), "%.2f", bill.getKwhUsed()),
                    dfCurrency.format(bill.getFinalCostRm())
            );
            billStrings.add(formattedBill);
        }
        // Create a new ArrayAdapter each time to ensure updates reflect
        billArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, // Default Android list item layout
                billStrings);
        lvBillHistory.setAdapter(billArrayAdapter);
    }

    /**
     * Sorts the billList based on the selected option from the spinner.
     * @param position The selected position in the sort spinner.
     */
    private void sortBills(int position) {
        if (billList.isEmpty()) {
            return; // No bills to sort
        }

        switch (position) {
            case 0: // Sort by Month (Ascending)
                Collections.sort(billList, new Comparator<Bill>() {
                    @Override
                    public int compare(Bill b1, Bill b2) {
                        // Custom month comparison (January=1, February=2, etc.)
                        return getMonthNumber(b1.getMonth()) - getMonthNumber(b2.getMonth());
                    }
                });
                break;
            case 1: // Sort by Month (Descending)
                Collections.sort(billList, new Comparator<Bill>() {
                    @Override
                    public int compare(Bill b1, Bill b2) {
                        return getMonthNumber(b2.getMonth()) - getMonthNumber(b1.getMonth());
                    }
                });
                break;
            case 2: // Sort by Cost (Ascending)
                Collections.sort(billList, new Comparator<Bill>() {
                    @Override
                    public int compare(Bill b1, Bill b2) {
                        return Double.compare(b1.getFinalCostRm(), b2.getFinalCostRm());
                    }
                });
                break;
            case 3: // Sort by Cost (Descending)
                Collections.sort(billList, new Comparator<Bill>() {
                    @Override
                    public int compare(Bill b1, Bill b2) {
                        return Double.compare(b2.getFinalCostRm(), b1.getFinalCostRm());
                    }
                });
                break;
        }
        displayBills(billList); // Update ListView after sorting
    }

    /**
     * Helper method to convert month name to a numeric representation for sorting.
     * @param monthName The name of the month (e.g., "January").
     * @return The month number (1 for January, 12 for December).
     */
    private int getMonthNumber(String monthName) {
        switch (monthName) {
            case "January": return 1;
            case "February": return 2;
            case "March": return 3;
            case "April": return 4;
            case "May": return 5;
            case "June": return 6;
            case "July": return 7;
            case "August": return 8;
            case "September": return 9;
            case "October": return 10;
            case "November": return 11;
            case "December": return 12;
            default: return 0; // "Select Month" or invalid
        }
    }


    /**
     * Shows a confirmation dialog before deleting all bill records.
     */
    private void confirmDeleteAllBills() {
        if (billList.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_no_records_to_delete), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete All History")
                .setMessage("Are you sure you want to delete all electricity bill records? This action cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllBills(); // Proceed with deletion
                    }
                })
                .setNegativeButton("Cancel", null) // Do nothing on cancel
                .show();
    }

    /**
     * Deletes all bill records from the database and updates the UI.
     */
    private void deleteAllBills() {
        int rowsAffected = databaseHelper.deleteAllBills();
        if (rowsAffected > 0) {
            Toast.makeText(this, getString(R.string.toast_all_history_deleted), Toast.LENGTH_SHORT).show();
            loadBills(); // Reload to show empty state
        } else {
            Toast.makeText(this, "Failed to delete history.", Toast.LENGTH_SHORT).show();
        }
    }
}

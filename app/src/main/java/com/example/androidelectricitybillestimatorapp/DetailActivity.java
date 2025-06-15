package com.example.androidelectricitybillestimatorapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvDetailMonth;
    private TextView tvDetailKwh;
    private TextView tvDetailRebate;
    private TextView tvDetailTotalCharges;
    private TextView tvDetailFinalCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        // Enable the Up button (back arrow)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize UI elements
        tvDetailMonth = findViewById(R.id.tvDetailMonth);
        tvDetailKwh = findViewById(R.id.tvDetailKwh);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvDetailTotalCharges = findViewById(R.id.tvDetailTotalCharges);
        tvDetailFinalCost = findViewById(R.id.tvDetailFinalCost);

        // Retrieve the Bill object passed from HistoryActivity
        Bill selectedBill = (Bill) getIntent().getSerializableExtra("selected_bill");

        if (selectedBill != null) {
            // Format currency and kWh
            DecimalFormat dfCurrency = new DecimalFormat("RM #,##0.00");
            DecimalFormat dfKwh = new DecimalFormat("#.##");

            // Populate TextViews with bill details
            tvDetailMonth.setText(selectedBill.getMonth());
            tvDetailKwh.setText(String.format(Locale.getDefault(), "%s kWh", dfKwh.format(selectedBill.getKwhUsed())));
            tvDetailRebate.setText(String.format(Locale.getDefault(), "%d%%", selectedBill.getRebatePercentage()));
            tvDetailTotalCharges.setText(dfCurrency.format(selectedBill.getTotalChargesRm()));
            tvDetailFinalCost.setText(dfCurrency.format(selectedBill.getFinalCostRm()));
        } else {
            // Handle case where no bill object was passed (should not happen normally if navigated from list)
            Toast.makeText(this, "Error: Bill details not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key or a system-generated back event.
     * This is crucial for the Up button in the Toolbar.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to the previous activity (HistoryActivity)
        return true;
    }
}
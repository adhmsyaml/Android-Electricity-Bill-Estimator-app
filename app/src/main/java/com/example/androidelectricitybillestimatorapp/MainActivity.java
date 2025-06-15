package com.example.androidelectricitybillestimatorapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Elements Declaration
    private EditText etKwhUsed;
    private Button btnClearKwh;
    private Spinner spinnerMonth;
    private SeekBar seekBarRebate;
    private TextView tvRebateValue;
    private Button btnCalculate;
    private TextView tvChargeBreakdownTitle;
    private LinearLayout llChargeBreakdown;
    private TextView tvTotalCharges;
    private TextView tvFinalCost;
    private Button btnViewHistory;
    private Button btnAbout;

    // Database Helper Instance
    private DatabaseHelper databaseHelper;

    // Variables to store current input values
    private double currentKwhUsed = 0.0;
    private String selectedMonth = "";
    private int selectedRebatePercentage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Toolbar (Title Bar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set the toolbar as the app's action bar

        // Initialize UI Elements
        etKwhUsed = findViewById(R.id.etKwhUsed);
        btnClearKwh = findViewById(R.id.btnClearKwh);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        tvRebateValue = findViewById(R.id.tvRebateValue);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvChargeBreakdownTitle = findViewById(R.id.tvChargeBreakdownTitle);
        llChargeBreakdown = findViewById(R.id.llChargeBreakdown);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnAbout = findViewById(R.id.btnAbout);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Setup Month Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinnerMonth.setAdapter(adapter);

        // Set initial state for calculate button (disabled until valid inputs)
        updateCalculateButtonState();
        // Hide output fields initially
        hideOutputFields();


        // --- Event Listeners ---

        // kWh Input Field Listener
        etKwhUsed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update button state whenever text changes
                updateCalculateButtonState();
                // Hide outputs when input changes
                hideOutputFields();
            }
        });

        // Clear kWh Button Listener
        btnClearKwh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etKwhUsed.setText(""); // Clear the text in EditText
                hideOutputFields(); // Hide outputs when input is cleared
            }
        });


        // Month Spinner Listener
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected month string. Skip the first "Select Month" item.
                selectedMonth = (position > 0) ? parent.getItemAtPosition(position).toString() : "";
                // Update button state when month selection changes
                updateCalculateButtonState();
                // Hide outputs when selection changes
                hideOutputFields();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMonth = ""; // No month selected
                updateCalculateButtonState();
                hideOutputFields();
            }
        });

        // Rebate SeekBar Listener
        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the TextView to show current rebate percentage
                selectedRebatePercentage = progress;
                tvRebateValue.setText(String.format(Locale.getDefault(), "%d%%", progress));
                // Hide outputs when rebate changes
                hideOutputFields();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });

        // Calculate Button Listener
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBill();
            }
        });

        // View History Button Listener
        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start HistoryActivity
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // About Button Listener
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AboutActivity
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    // --- Helper Methods ---

    /**
     * Checks if all required inputs are valid and updates the calculate button's enabled state.
     */
    private void updateCalculateButtonState() {
        boolean isKwhValid = !TextUtils.isEmpty(etKwhUsed.getText()) && Double.parseDouble(etKwhUsed.getText().toString()) > 0;
        boolean isMonthSelected = !TextUtils.isEmpty(selectedMonth);

        btnCalculate.setEnabled(isKwhValid && isMonthSelected); // Enable only if both are valid
        // Visually disable/enable
        if (btnCalculate.isEnabled()) {
            btnCalculate.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_secondary));
            btnCalculate.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onSecondary));
        } else {
            btnCalculate.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
            btnCalculate.setTextColor(Color.parseColor("#CCCCCC")); // Lighter grey for disabled text
        }
    }

    /**
     * Hides the output text views and breakdown layout.
     */
    private void hideOutputFields() {
        tvTotalCharges.setVisibility(View.GONE);
        tvFinalCost.setVisibility(View.GONE);
        tvChargeBreakdownTitle.setVisibility(View.GONE);
        llChargeBreakdown.setVisibility(View.GONE);
        llChargeBreakdown.removeAllViews(); // Clear previous breakdown views
    }

    /**
     * Performs the electricity bill calculation based on kWh used and rebate.
     * Displays results and saves to database.
     */
    private void calculateBill() {
        String kwhString = etKwhUsed.getText().toString();

        // Input Validation
        if (TextUtils.isEmpty(kwhString)) {
            Toast.makeText(this, getString(R.string.error_empty_kwh), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            currentKwhUsed = Double.parseDouble(kwhString);
            if (currentKwhUsed <= 0) {
                Toast.makeText(this, getString(R.string.error_invalid_kwh), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.error_invalid_kwh), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedMonth)) {
            Toast.makeText(this, getString(R.string.error_select_month), Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Electricity Tariff Calculation ---
        double totalChargesInSen = 0.0;
        double remainingKwh = currentKwhUsed;
        DecimalFormat dfCurrency = new DecimalFormat("RM #,##0.00"); // Currency format for output
        DecimalFormat dfKwh = new DecimalFormat("#.##"); // kWh format for breakdown

        llChargeBreakdown.removeAllViews(); // Clear previous breakdown views

        // Block 1: First 200 kWh (1-200 kWh) @ 21.8 sen/kWh
        if (remainingKwh > 0) {
            double kwhInBlock = Math.min(remainingKwh, 200);
            double charge = kwhInBlock * 21.8;
            totalChargesInSen += charge;
            remainingKwh -= kwhInBlock;
            addBreakdownRow(String.format(Locale.getDefault(), getString(R.string.breakdown_block_format),
                    dfKwh.format(kwhInBlock), 21.8, dfCurrency.format(charge / 100))); // Convert sen to RM for display
        }

        // Block 2: Next 100 kWh (201-300 kWh) @ 33.4 sen/kWh
        if (remainingKwh > 0) {
            double kwhInBlock = Math.min(remainingKwh, 100);
            double charge = kwhInBlock * 33.4;
            totalChargesInSen += charge;
            remainingKwh -= kwhInBlock;
            addBreakdownRow(String.format(Locale.getDefault(), getString(R.string.breakdown_block_format),
                    dfKwh.format(kwhInBlock), 33.4, dfCurrency.format(charge / 100)));
        }

        // Block 3: Next 300 kWh (301-600 kWh) @ 51.6 sen/kWh
        if (remainingKwh > 0) {
            double kwhInBlock = Math.min(remainingKwh, 300);
            double charge = kwhInBlock * 51.6;
            totalChargesInSen += charge;
            remainingKwh -= kwhInBlock;
            addBreakdownRow(String.format(Locale.getDefault(), getString(R.string.breakdown_block_format),
                    dfKwh.format(kwhInBlock), 51.6, dfCurrency.format(charge / 100)));
        }

        // Block 4: Next 300 kWh (601-900 kWh) @ 54.6 sen/kWh
        if (remainingKwh > 0) {
            double kwhInBlock = Math.min(remainingKwh, 300);
            double charge = kwhInBlock * 54.6;
            totalChargesInSen += charge;
            remainingKwh -= kwhInBlock;
            addBreakdownRow(String.format(Locale.getDefault(), getString(R.string.breakdown_block_format),
                    dfKwh.format(kwhInBlock), 54.6, dfCurrency.format(charge / 100)));
        }

        // Block 5: Subsequent kWh (Above 900 kWh) @ 57.1 sen/kWh (assuming this is the implied next block after 900kWh based on typical tariff structures, though the assignment only lists up to 900. I will use the next logical rate if there is further consumption as per typical scenarios, or you can specify if the assignment implies only up to 900kWh applies)
        // Re-reading assignment: "For the next 300 kWh (601-900 kWh) per month onwards". This phrase is slightly ambiguous.
        // It could mean:
        // 1. All kWh above 600 up to 900 are 54.6 sen/kWh.
        // 2. All kWh above 900 are *also* 54.6 sen/kWh.
        // Given the typical increasing block structure, it's more common for a new rate to apply "onwards" or a higher rate for very high consumption.
        // However, sticking strictly to "nothing more, nothing less" and the *provided table*, it stops at 900kWh.
        // So, if remainingKwh > 0 after 900, it would still fall under 54.6 if "onwards" implies that rate continues.
        // Let's assume for now, any kWh beyond 900 is *still* 54.6 sen/kWh as per the latest given block's "onwards" language.
        // If your lecturer clarified a different rate for >900kWh, we can update.
        if (remainingKwh > 0) {
            // Apply the last known rate for any remaining kWh if the prompt says "onwards" for that block
            double charge = remainingKwh * 54.6; // Assuming 54.6 sen/kWh for >900 kWh
            totalChargesInSen += charge;
            addBreakdownRow(String.format(Locale.getDefault(), getString(R.string.breakdown_block_format),
                    dfKwh.format(remainingKwh), 54.6, dfCurrency.format(charge / 100)));
        }


        // Convert total charges from sen to Malaysian Ringgit (RM)
        double totalChargesRm = totalChargesInSen / 100.0;

        // Apply Rebate
        double rebateAmount = totalChargesRm * (selectedRebatePercentage / 100.0);
        double finalCostRm = totalChargesRm - rebateAmount;

        // Display Results
        tvTotalCharges.setText(getString(R.string.label_total_charges, dfCurrency.format(totalChargesRm)));
        tvFinalCost.setText(getString(R.string.label_final_cost, dfCurrency.format(finalCostRm)));

        // Make output fields visible and animate them
        tvChargeBreakdownTitle.setVisibility(View.VISIBLE);
        llChargeBreakdown.setVisibility(View.VISIBLE);
        tvTotalCharges.setVisibility(View.VISIBLE);
        tvFinalCost.setVisibility(View.VISIBLE);

        animateOutputFields();

        // Save to Database
        long newRowId = databaseHelper.addBill(selectedMonth, currentKwhUsed,
                selectedRebatePercentage, totalChargesRm, finalCostRm);
        if (newRowId != -1) {
            Toast.makeText(this, getString(R.string.toast_bill_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save bill.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dynamically adds a TextView for each charge breakdown block.
     * @param text The text to display for the breakdown line.
     */
    private void addBreakdownRow(String text) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onSurface)); // White text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        llChargeBreakdown.addView(tv);
    }

    /**
     * Applies a fade-in and subtle scale-up animation to the output text views.
     */
    private void animateOutputFields() {
        // Create ObjectAnimator for alpha (fade-in) and scale (grow)
        ObjectAnimator alphaAnimatorTotal = ObjectAnimator.ofFloat(tvTotalCharges, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnimatorTotal = ObjectAnimator.ofFloat(tvTotalCharges, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleYAnimatorTotal = ObjectAnimator.ofFloat(tvTotalCharges, "scaleY", 0.9f, 1f);

        ObjectAnimator alphaAnimatorFinal = ObjectAnimator.ofFloat(tvFinalCost, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnimatorFinal = ObjectAnimator.ofFloat(tvFinalCost, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleYAnimatorFinal = ObjectAnimator.ofFloat(tvFinalCost, "scaleY", 0.9f, 1f);

        // Set durations and interpolators
        long duration = 500; // milliseconds
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

        alphaAnimatorTotal.setDuration(duration);
        scaleXAnimatorTotal.setDuration(duration);
        scaleYAnimatorTotal.setDuration(duration);
        alphaAnimatorTotal.setInterpolator(interpolator);
        scaleXAnimatorTotal.setInterpolator(interpolator);
        scaleYAnimatorTotal.setInterpolator(interpolator);

        alphaAnimatorFinal.setDuration(duration);
        scaleXAnimatorFinal.setDuration(duration);
        scaleYAnimatorFinal.setDuration(duration);
        alphaAnimatorFinal.setInterpolator(interpolator);
        scaleXAnimatorFinal.setInterpolator(interpolator);
        scaleYAnimatorFinal.setInterpolator(interpolator);

        // Play animations together using an AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                alphaAnimatorTotal, scaleXAnimatorTotal, scaleYAnimatorTotal,
                alphaAnimatorFinal, scaleXAnimatorFinal, scaleYAnimatorFinal
        );
        animatorSet.start(); // Start the animation
    }
}
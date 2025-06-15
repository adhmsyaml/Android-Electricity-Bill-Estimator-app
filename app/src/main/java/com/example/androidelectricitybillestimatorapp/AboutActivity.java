package com.example.androidelectricitybillestimatorapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    private TextView tvGithubUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.aboutToolbar);
        setSupportActionBar(toolbar);
        // Enable the Up button (back arrow)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvGithubUrl = findViewById(R.id.tvGithubUrl);

        // Set an OnClickListener for the URL TextView to make it clickable.
        // Even with autoLink="web", providing an explicit click listener is often safer
        // especially if you want to perform other actions or ensure it works consistently.
        tvGithubUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the URL from strings.xml
                String url = getString(R.string.about_github_url);
                // Create an Intent to open a web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                // Check if there's an app that can handle this intent
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                } else {
                    // Optional: Show a toast if no browser found (unlikely on modern Android)
                    // Toast.makeText(AboutActivity.this, "No web browser found.", Toast.LENGTH_SHORT).show();
                }
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
}
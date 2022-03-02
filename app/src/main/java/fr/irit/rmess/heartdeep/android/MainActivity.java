/***********************************************************************
 Name............ : MainActivity.java
 Description..... : The main activity of the app. The first one to be started
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import fr.irit.rmess.heartdeep.R;

/**
 * The entry point activity of the app. The first one to be started
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("HeartDeep");
    }

    /**
     * Starts the next Activity. Called when 'Start' button is clicked
     *
     * @param view The view which generates the event (typically the 'Start' button)
     */
    public void startNextActivity(View view) {
        // Start session activity
        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);
    }
}

package fr.irit.rmess.heartdeep.android;

import androidx.appcompat.app.AppCompatActivity;
import fr.irit.rmess.heartdeep.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.google.android.material.snackbar.Snackbar;

public class SessionActivity extends AppCompatActivity {

    LinearLayout topLinLayout;
    RadioButton radioButtonGenderFemale;

    /**
     * EditText on which to input age
     */
    private EditText editTextAge;
    /**
     * EditText on which to input the user id
     */
    private EditText editTextId;
    /**
     * EditText on which to input recording sample duration
     */
    private EditText editTextDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        setTitle("New Session");

        topLinLayout = findViewById(R.id.lin_lyt_top);
        radioButtonGenderFemale = findViewById(R.id.radioButtonGenderFemale);
        editTextAge = findViewById(R.id.edit_text_age);
        editTextId = findViewById(R.id.edit_text_id);
        editTextDuration = findViewById(R.id.edit_text_duration);
    }

    /**
     * Starts the next Activity. Called when 'Go' button is clicked
     *
     * @param view The view which generates the event (typically the 'Go' button)
     */
    public void validateAndGoToNextActivity(View view) {
        boolean inputOk = true;

        // Get the gender
        String gender = "MALE";
        if(radioButtonGenderFemale.isChecked()){
            gender = "FEMALE";
        }

        // Get the age
        int age = 0;
        try {
            age = Integer.parseInt(editTextAge.getText().toString());
        } catch (Exception e) {
            inputOk = false;
        }

        // Get the id
        String userId = editTextId.getText().toString().trim();
        if(userId.isEmpty()){
            inputOk = false;
        }

        // Get the recording sample duration
        int recordingDuration = 0;
        try {
            recordingDuration = Integer.parseInt(editTextDuration.getText().toString());
        } catch (Exception e) {
            inputOk = false;
        }

        if(!inputOk){
            Snackbar.make(topLinLayout, "Please fill all the required fields", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Save the infos
        HeartDeepService.setSessionInfos(gender, age, userId, recordingDuration);

        // Start recording activity
        Intent intent = new Intent(this, RecordingActivity.class);
        startActivity(intent);
    }
}

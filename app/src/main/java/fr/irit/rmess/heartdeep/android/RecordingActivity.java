/***********************************************************************
 Name............ : RecordingActivity.java
 Description..... : To visualize
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.google.android.material.snackbar.Snackbar;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.appcompat.app.AppCompatActivity;
import fr.irit.rmess.heartdeep.R;
import fr.irit.rmess.heartdeep.datatype.HeartDeepObservable;
import fr.irit.rmess.heartdeep.datatype.HeartDeepObserver;
import fr.irit.rmess.heartdeep.datatype.SensorReadings;

public class RecordingActivity extends AppCompatActivity implements HeartDeepObserver<Bundle> {

    /**
     * A constant to indicate that we are going to plot accelerometer readings
     */
    private static final int READINGS_TYPE_ACC = 0;
    /**
     * A constant to indicate that we are going to plot gyroscope readings
     */
    private static final int READINGS_TYPE_GYR = 1;
    /**
     * Indicates which type of readings we are to plot. Should be one of the {@code READINGS_TYPE_*} constants
     */
    private static int readingsType = READINGS_TYPE_ACC;
    /**
     * The time duration to plot (in seconds)
     */
    private static final int N_SECONDS_TO_PLOT = 3;
    /**
     * The number of sensor readings periods to maintain on the graph
     */
    private static int N_PERIODS_TO_PLOT = 1;
    /**
     * The scenario for which we are to record data
     */
    public static String scenario = "";
    /**
     * The position for which we are to record data
     */
    public static String position = "";
    /**
     * Button to start or stop HeartDeep internal functionality
     */
    private Button btnStartOrStopHeartDeepInternals;
    /**
     * Indicates whether we are displaying x axis series or not
     */
    private boolean displayXAxis = false;
    /**
     * Indicates whether we are displaying y axis series or not
     */
    private boolean displayYAxis = true;
    /**
     * Indicates whether we are displaying z axis series or not
     */
    private boolean displayZAxis = false;
    /**
     * Graph series for x axis
     */
    LineGraphSeries<DataPoint> xSeries;
    /**
     * Graph series for y axis
     */
    LineGraphSeries<DataPoint> ySeries;
    /**
     * Graph series for z axis
     */
    LineGraphSeries<DataPoint> zSeries;
    /**
     * The view to plot charts in
     */
    GraphView graphView;

    /**
     * The timestamp of the first values to be received
     */
    private double firstTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        setTitle("Recording");

        btnStartOrStopHeartDeepInternals = findViewById(R.id.btn_start_stop);

        // The view must be coherent with the internal state
        if (!HeartDeepSensorListenerService.heartDeepInternalsRunning()) { // HeartDeep is not running
            btnStartOrStopHeartDeepInternals.setText("Start HeartDeep");
            btnStartOrStopHeartDeepInternals.setBackgroundResource(R.color.colorPrimary);
        } else { // HeartDeep is running
            btnStartOrStopHeartDeepInternals.setText("Stop HeartDeep");
            btnStartOrStopHeartDeepInternals.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        ((CheckBox) findViewById(R.id.checkBoxDisplayX)).setChecked(displayXAxis);
        ((CheckBox) findViewById(R.id.checkBoxDisplayY)).setChecked(displayYAxis);
        ((CheckBox) findViewById(R.id.checkBoxDisplayZ)).setChecked(displayZAxis);

        ((RadioButton) findViewById(R.id.radioButtonAcc)).setChecked(readingsType==READINGS_TYPE_ACC);
        ((RadioButton) findViewById(R.id.radioButtonGyr)).setChecked(readingsType==READINGS_TYPE_GYR);

        // Set the number of periods to plot
        N_PERIODS_TO_PLOT = (int) (N_SECONDS_TO_PLOT * 1e9 / HeartDeepSensorListenerService.getSampleLength());

        initGraph();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HeartDeepService.getLastInstance().registerObserver(this);
    }

    @Override
    protected void onPause() {
        HeartDeepService.getLastInstance().unRegisterObserver(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        HeartDeepService.getLastInstance().unRegisterObserver(this);
        super.onStop();
    }

    /**
     * Helper function to initialize the graph and its parameters
     */
    private void initGraph(){
        // Set graph parameters
        graphView = findViewById(R.id.graph);

        // enable scaling and scrolling
//        graphView.getViewport().setScalable(true);
//        graphView.getViewport().setScalableY(true);
//        graphView.getViewport().setYAxisBoundsManual(true);
//        graphView.getViewport().setMinY(-0.4);
//        graphView.getViewport().setMaxY(0.4);

        // Show legend
        graphView.getLegendRenderer().setVisible(true);

        // Add series to graph
        graphView.removeAllSeries();

        xSeries = new LineGraphSeries<>();
        xSeries.setTitle("x");
        xSeries.setColor(Color.BLUE);
        if(displayXAxis){
            graphView.addSeries(xSeries);
        }

        ySeries = new LineGraphSeries<>();
        ySeries.setTitle("y");
        ySeries.setColor(Color.RED);
        if(displayYAxis){
            graphView.addSeries(ySeries);
        }

        zSeries = new LineGraphSeries<>();
        zSeries.setTitle("z");
        zSeries.setColor(Color.GREEN);
        if(displayZAxis){
            graphView.addSeries(zSeries);
        }

        // In order to reinitialize the time axis on the graph
        firstTimestamp = 0;
    }

    /**
     * Plots 3-axis sensor readings data.
     *
     * @param sensorReadings
     */
    public void plot(SensorReadings sensorReadings) {
        if (sensorReadings == null) {
            return;
        }

        Log.d("PLOT", "" + sensorReadings.hashCode());

        int n = sensorReadings.getN();
        if(n<=0){
            return;
        }

        double[] timestamps = sensorReadings.getTimestamps();
        double[] x = sensorReadings.getX();
        double[] y = sensorReadings.getY();
        double[] z = sensorReadings.getZ();
        double timestamp;

        if(firstTimestamp <= 0){
            firstTimestamp = timestamps[0];
        }

        for (int i = 0; i < n-1; i++) {
            timestamp = toTimeAxisValue(timestamps[i]);
            xSeries.appendData(new DataPoint(timestamp, x[i]), false, N_PERIODS_TO_PLOT * n);
            ySeries.appendData(new DataPoint(timestamp, y[i]), false, N_PERIODS_TO_PLOT * n);
            zSeries.appendData(new DataPoint(timestamp, z[i]), false, N_PERIODS_TO_PLOT * n);
        }

        // Add the last values and update the view (scroll to end)
        timestamp = toTimeAxisValue(timestamps[n-1]);
        xSeries.appendData(new DataPoint(timestamp, x[n-1]), true, N_PERIODS_TO_PLOT * n, true);
        ySeries.appendData(new DataPoint(timestamp, y[n-1]), true, N_PERIODS_TO_PLOT * n, true);
        zSeries.appendData(new DataPoint(timestamp, z[n-1]), true, N_PERIODS_TO_PLOT * n, false);

        // Update the viewport x axis
        graphView.getViewport().setXAxisBoundsManual(true);
        double minX = Math.min(xSeries.getLowestValueX(), Math.min(ySeries.getLowestValueX(), zSeries.getLowestValueX()));
        double maxX = Math.max(xSeries.getHighestValueX(), Math.max(ySeries.getHighestValueX(), zSeries.getHighestValueX()));
        graphView.getViewport().setMinX(minX);
        graphView.getViewport().setMaxX(maxX);
    }

    /**
     * Helper function to prepare a timestamp value to be printed on the time graph's time axis
     * @param timestamp The timestamp value to be formatted (in nanoseconds)
     * @return The timestamp value ready to be printed (in seconds)
     */
    private double toTimeAxisValue(double timestamp){
        return (timestamp - firstTimestamp) * 1e-9;
    }

    @Override
    /**
     * Stands as a wrapper to launch {@link #internalUpdate(HeartDeepObservable, Bundle)} on the UI thread
     * This is to avoid getting "android.view.ViewRootImpl$CalledFromWrongThreadException: only the original thread that created a view hierarchy can touch its views."
     * @param eventSource The object that called this method
     * @param bundle A The bundle sent by the eventSource
     *               It may contain a lot of information
     */
    public void update(final HeartDeepObservable<Bundle> eventSource, final Bundle bundle) {
        if (bundle == null) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                internalUpdate(eventSource, bundle);
            }
        });
    }

    /**
     * Update the view according to the data given in the bundle
     * This method gets 'all' the extra fields of the bundle (if set) and prints them on the screen
     *
     * @param eventSource The object that called this method
     * @param bundle      A The bundle sent by the eventSource
     *                    It may contain a lot of information
     */
    private void internalUpdate(HeartDeepObservable<Bundle> eventSource, Bundle bundle) {
        String bundleType = bundle.getString(HeartDeepService.BUNDLE_TYPE);
        if (bundleType == null) {
            return;
        }

        if (bundleType.equals(HeartDeepService.BUNDLE_TYPE_COMPLETE_RESULT)) {
            SensorReadings sensorReadingsToBePlotted = null;

            switch (readingsType) {
                case READINGS_TYPE_ACC: {
                    sensorReadingsToBePlotted = (SensorReadings) bundle.getSerializable(HeartDeepService.OUTPUT_ACC_READINGS_TEMPORAL);
                    break;
                }
                case READINGS_TYPE_GYR: {
                    sensorReadingsToBePlotted = (SensorReadings) bundle.getSerializable(HeartDeepService.OUTPUT_GYR_READINGS_TEMPORAL);
                    break;
                }
            }

            if(sensorReadingsToBePlotted != null){
//                graphView.removeAllSeries();
                plot(sensorReadingsToBePlotted);
            }
        } else if (bundleType.equals(HeartDeepService.BUNDLE_TYPE_EVENT_STATUS)) {
            String status = bundle.getString(HeartDeepService.EVENT_SENSOR_LISTENER_STATUS);

            if (HeartDeepService.EVENT_SENSOR_LISTENER_STARTED.equals(status)) { // HeartDeep is started
                btnStartOrStopHeartDeepInternals.setText("Stop HeartDeep");
                btnStartOrStopHeartDeepInternals.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

                initGraph();

                Snackbar.make(this.graphView, "Started HeartDeep", Snackbar.LENGTH_LONG).show();
            } else if (HeartDeepService.EVENT_SENSOR_LISTENER_STOPPED.equals(status)) { // HeartDeep is stopped
                btnStartOrStopHeartDeepInternals.setText("Start HeartDeep");
                btnStartOrStopHeartDeepInternals.setBackgroundResource(R.color.colorPrimary);

                Snackbar.make(this.graphView, "Stopped HeartDeep", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /* Layout interaction */

    /**
     * Called when a display-axis checkbox is clicked
     *
     * @param view The checkbox which generated the event
     */
    public void onDisplayAxisCheckboxClicked(View view) {
//        displayXAxis = ((CheckBox) findViewById(R.id.checkBoxDisplayX)).isChecked();
//        displayYAxis = ((CheckBox) findViewById(R.id.checkBoxDisplayY)).isChecked();
//        displayZAxis = ((CheckBox) findViewById(R.id.checkBoxDisplayZ)).isChecked();

        switch (view.getId()){
            case R.id.checkBoxDisplayX:{
                displayXAxis = ((CheckBox) findViewById(R.id.checkBoxDisplayX)).isChecked();

                if(displayXAxis){
                    graphView.addSeries(xSeries);
                } else {
                    graphView.removeSeries((xSeries));
                }

                break;
            }
            case R.id.checkBoxDisplayY:{
                displayYAxis = ((CheckBox) findViewById(R.id.checkBoxDisplayY)).isChecked();

                if(displayYAxis){
                    graphView.addSeries(ySeries);
                } else {
                    graphView.removeSeries((ySeries));
                }

                break;
            }
            case R.id.checkBoxDisplayZ:{
                displayZAxis = ((CheckBox) findViewById(R.id.checkBoxDisplayZ)).isChecked();

                if(displayZAxis){
                    graphView.addSeries(zSeries);
                } else {
                    graphView.removeSeries((zSeries));
                }

                break;
            }
            default: break;
        }
    }

    /**
     * Called when a reading type radio is clicked
     *
     * @param view The radio button which generated the event
     */
    public void onReadingsTypeRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButtonAcc: {
                if (checked) {
                    readingsType = READINGS_TYPE_ACC;
                    initGraph();
//                    graphView.getViewport().setMinY(-15);
//                    graphView.getViewport().setMaxY(15);
                }
                break;
            }
            case R.id.radioButtonGyr: {
                if (checked) {
                    readingsType = READINGS_TYPE_GYR;
                    initGraph();
//                    graphView.getViewport().setMinY(-5);
//                    graphView.getViewport().setMaxY(5);
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Starts (if previously stopped) or stops (if previously started) HeartDeep internal processing.
     * This method is called when the 'Start/Stop HeartDeep internals' button is clicked
     *
     * @param view The view which triggered the event (typically the 'Start/Stop HeartDeep internals' button)
     */
    public void startOrStopHeartDeepInternals(View view) {
        if (!HeartDeepSensorListenerService.heartDeepInternalsRunning()) { // HeartDeep is not running
            Intent serviceIntent = new Intent(this, HeartDeepSensorListenerService.class);
            startService(serviceIntent);
        } else { // HeartDeep is running
            Intent serviceIntent = new Intent(this, HeartDeepSensorListenerService.class);
            stopService(serviceIntent);
        }
    }


    /**
     * Called when a scenario radio button is clicked
     *
     * @param view The radio button which generated the event
     */
    public void onScenarioRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButtonScenarioNormal: {
                if (checked) {
                    scenario = "NORMAL";
                }
                break;
            }
            case R.id.radioButtonScenarioExercising: {
                if (checked) {
                    scenario = "EXERCISING";
                }
                break;
            }
            case R.id.radioButtonScenarioIntermediate: {
                if (checked) {
                    scenario = "INTERMEDIATE";
                }
                break;
            }
            default:
                break;
        }

        // Update SensorListener service information
        HeartDeepSensorListenerService.scenario = scenario;
    }

    /**
     * Called when a scenario radio button is clicked
     *
     * @param view The radio button which generated the event
     */
    public void onPositionRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButtonPositionSitting: {
                if (checked) {
                    position = "SITTING";
                }
                break;
            }
            case R.id.radioButtonPositionLying: {
                if (checked) {
                    position = "LYING";
                }
                break;
            }
            case R.id.radioButtonPositionReclining: {
                if (checked) {
                    position = "RECLINING";
                }
                break;
            }
            default:
                break;
        }

        // Update SensorListener service information
        HeartDeepSensorListenerService.position = position;
    }
}

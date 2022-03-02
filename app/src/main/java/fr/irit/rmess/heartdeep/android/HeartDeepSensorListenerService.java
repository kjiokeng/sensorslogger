/***********************************************************************
 Name............ : HeartDeepSensorListenerService.java
 Description..... : The class listening to sensor readings
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.irit.rmess.heartdeep.datatype.SensorReadings;
import fr.irit.rmess.heartdeep.helpers.HeartDeepFileWriter;

public class HeartDeepSensorListenerService extends Service implements SensorEventListener {

    /**
     * The scenario for which we are to record data
     */
    public static String scenario = "NORMAL";
    /**
     * The position for which we are to record data
     */
    public static String position = "SITTING";
    /**
     * The sensor manager to be used
     */
    private static SensorManager sensorManager;
    /**
     * (Linear) Accelerometer sensor
     */
    private static Sensor accelerometerSensor;
    /**
     * Gyroscope sensor
     */
    private static Sensor gyroscopeSensor;
    /**
     * Variable to handle the state of HeartDeep internal functionality: 0 ==> stopped; els e==> started.
     */
    private static int heartDeepInternalState = 0;
    /**
     * The length in time of a to-be-processed sample, given in nanoseconds
     */
    private static long sampleLength = (long) (0.02 * 1000 * 1000 * 1000L);
    /**
     * The last not yet processed accelerometer readings
     */
    private static List<double[]> accReadings = new ArrayList<>();
    /**
     * The last not yet processed accelerometer readings' timestamps
     */
    private static List<Long> accReadingsTimestamps = new ArrayList<>();
    /**
     * The last not yet processed gyroscope readings
     */
    private static List<double[]> gyrReadings = new ArrayList<>();
    /**
     * The last not yet processed gyroscope readings's timestamps
     */
    private static List<Long> gyrReadingsTimestamps = new ArrayList<>();
    /**
     * The beginning timestamp of the current sample
     */
    private static long beginningTimestamp = -1;
    /**
     * The timestamp at which recording is begun (should correspond to the timestamp of the first sensor event received).
     */
    private static long recordingBeginningTimestamp = -1;
    /**
     * The amount of time (in nanoseconds) a recording phase should last
     */
    private static long recordingTimeLength = (long) (60 * 1000 * 1000 * 1000L);
    /**
     * The amount of time (in nanoseconds) to wait before starting processing or even recording
     */
    private static long beginningDelay = (long) 2 * 1000 * 1000 * 1000L;
    /**
     * Indicates whether we are still waiting for the beginning delay to be elapsed
     */
    private static boolean waitingBegin = true;
    /**
     * A string builder to store to-be-written-to-file data
     */
    private static StringBuilder recordsStringBuilder = new StringBuilder();
    /**
     * A wake lock to prevent the phone from entering idle mode.
     * This service needs indeed to continue its execution even when the phone screen is off
     */
    private static PowerManager.WakeLock wakeLock = null;
    /**
     * The dateformat instance used to format dates in order to append them to filenames
     */
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");;


    public HeartDeepSensorListenerService() {

    }

    /**
     * Helper function to know if heartDeep internal functionality is running
     *
     * @return {@code true} if heartDeep is working, {@code false} otherwise
     */
    public static boolean heartDeepInternalsRunning() {
        return heartDeepInternalState != 0;
    }

    /**
     * Helper function to save readings to file.
     * The file will be created in the internal directory of the app.
     *
     * @param filename The name of the file to be written
     * @param context  The Android context from which to write
     */
    public String saveReadingsToFile(String filename, Context context) {
        if (context == null) {
            return null;
        }

        if (filename == null || filename == "") {
            filename = dateFormat.format(new Date()) + ".csv";
        } else if (!filename.endsWith(".csv")) {
            filename += ".csv";
        }

        String fileContent = "timestamp,sensor,x,y,z\n" + recordsStringBuilder.toString();
        String result = HeartDeepFileWriter.writeToFile(filename, fileContent, context);

        if (result != null) {
            recordsStringBuilder.setLength(0);
            recordsStringBuilder.trimToSize();

            Toast.makeText(getApplicationContext(), "Saved " + filename, Toast.LENGTH_LONG).show();
        }

        return result;
    }

    /**
     * Gets the sample length. See {@link #sampleLength}
     *
     * @return The sample length (given in nanoseconds)
     */
    public static long getSampleLength() {
        return sampleLength;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (accelerometerSensor == null || gyroscopeSensor == null) {
            Log.e("ERROR", "This device doesn't have all the required sensors: accelerometer and gyroscope");
            Toast.makeText(getApplicationContext(), "ERROR: This device doesn't have all the required sensors: accelerometer and gyroscope", Toast.LENGTH_LONG).show();
        }

        // Prevent the phone from entering idle mode
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "HeartDeep:HeartDeepWakeLock");
                wakeLock.acquire();
            }
        }


        // List all available sensors
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d("SENSORS", "NAME,TYPE,VENDOR,RESOLUTION,POWER (mA),MIN_DELAY (us),MAX_DELAY (us),MAX_RANGE");
        for(Sensor sensor: sensors){
            Log.d("SENSORS", "---SENSOR:" + sensor.getName() + "," + sensor.getStringType() + "," + sensor.getVendor() + ","
            + sensor.getResolution() + "," + sensor.getPower() + ","
                    + sensor.getMinDelay() + "," + sensor.getMaxDelay()  + ","  + sensor.getMaximumRange());
        }
    }


    /* Service functionality */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!heartDeepInternalsRunning()) {
            startHeartDeepInternals();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        Log.i("DESTROY", "HeartDeepSensorListenerService.onDestroy()");

        if(heartDeepInternalState != 0){
            stopHeartDeepInternals();
        }

        // Release the wake lock in order to permit the phone to enter idle mode
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Helper method to start listening to sensors, which will automatically trigger their processing
     *
     * @return {@code true} if everything went well, {@code false} if at least one of the required sensors is absent on the device
     */
    private boolean startHeartDeepInternals() {
//        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);

        int[] sensorsToRecord = {
                Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_MAGNETIC_FIELD,
                Sensor.TYPE_ORIENTATION,
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_ROTATION_VECTOR,
        };
        for (int sensorType : sensorsToRecord) {
            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }

        heartDeepInternalState = 1;

        // Notify other components about start event
        Bundle statusBundle = new Bundle();
        statusBundle.putString(HeartDeepService.BUNDLE_TYPE, HeartDeepService.BUNDLE_TYPE_EVENT_STATUS);
        statusBundle.putString(HeartDeepService.EVENT_SENSOR_LISTENER_STATUS, HeartDeepService.EVENT_SENSOR_LISTENER_STARTED);
        HeartDeepService.publishEvent(statusBundle);

        return true;
    }

    /**
     * Helper method to stop listening to sensors and thus stop processing their readings
     *
     * @return {@code true} if everything went well, {@code false} if at least one of the required sensors is absent on the device
     */
    private boolean stopHeartDeepInternals() {
        sensorManager.unregisterListener(this);

        // Update the state variables
        heartDeepInternalState = 0;
        waitingBegin = true;
        recordingBeginningTimestamp = -1;
        beginningTimestamp = -1;

        // Notify other components about start event
        Bundle statusBundle = new Bundle();
        statusBundle.putString(HeartDeepService.BUNDLE_TYPE, HeartDeepService.BUNDLE_TYPE_EVENT_STATUS);
        statusBundle.putString(HeartDeepService.EVENT_SENSOR_LISTENER_STATUS, HeartDeepService.EVENT_SENSOR_LISTENER_STOPPED);
        HeartDeepService.publishEvent(statusBundle);

        // Delete the recorded readings
        clearAllReadingsBuffers();
        recordsStringBuilder.setLength(0);
        recordsStringBuilder.trimToSize();

        // Actually stop the service (in the sense of Android)
        stopSelf();

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (waitingBegin) {
            if (beginningTimestamp < 0) {
                beginningTimestamp = sensorEvent.timestamp;
            }

            if (sensorEvent.timestamp - beginningTimestamp < beginningDelay) {
                return;
            } else {
                beginningTimestamp = -1;
                waitingBegin = false;
            }
            return;
        }


        int sensorEventType = sensorEvent.sensor.getType();
//        if ((sensorEventType != Sensor.TYPE_LINEAR_ACCELERATION) && (sensorEventType != Sensor.TYPE_GYROSCOPE))
//            return;


        if (recordingBeginningTimestamp < 0) { // First value to be received
            recordingBeginningTimestamp = sensorEvent.timestamp;

            Log.i("RECORDING", "RECORDING STARTED");
//            Toast.makeText(getApplicationContext(), "RECORDING STARTED", Toast.LENGTH_SHORT).show();

            // Play a notification sound
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 65);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);
        }


        // Add to list of to-be-processed data
        switch (sensorEventType){
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                accReadings.add(new double[]{sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]});
                accReadingsTimestamps.add(sensorEvent.timestamp);
                break;
            }
            case Sensor.TYPE_GYROSCOPE: {
                gyrReadings.add(new double[]{sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]});
                gyrReadingsTimestamps.add(sensorEvent.timestamp);
                break;
            }
        }

        // Record for logging
        String data = sensorEvent.timestamp + "," + sensorEvent.sensor.getStringType() + ","
                + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2];
        recordsStringBuilder.append(data + "\n");
        Log.d("SENSOR-EVENT", data);


        if (recordingBeginningTimestamp < 0) { // First value to be received
            recordingBeginningTimestamp = sensorEvent.timestamp;
            beginningTimestamp = sensorEvent.timestamp;
        }

        // Check to know whether a sampling time length has elapsed
        if (sensorEvent.timestamp - beginningTimestamp >= sampleLength) {
            beginningTimestamp = sensorEvent.timestamp;
            triggerReadingsProcessing();
        }


        if (sensorEvent.timestamp - recordingBeginningTimestamp >= recordingTimeLength) {
//            Log.e("TOTO", "timestamp="+sensorEvent.timestamp + "; recordingBeginningTimestamp=" +
//                    recordingBeginningTimestamp + "; diff=" + (sensorEvent.timestamp - recordingBeginningTimestamp)
//                    + "; recordingTimeLength=" + recordingTimeLength);

            // Send the last recorded readings
            triggerReadingsProcessing();

            // Play a notification sound
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 65);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);

            Log.i("RECORDING", "RECORDING COMPLETED");
//            Toast.makeText(getApplicationContext(), "RECORDING COMPLETED", Toast.LENGTH_SHORT).show();

            // Save the readings to file
            Date date = new Date();
            String filename = HeartDeepService.getSessionId() + "-" + position + "-" + scenario;
            filename += "_" + dateFormat.format(date) + ".csv";
            saveReadingsToFile(filename, getApplicationContext());

            // For the next time
            recordingBeginningTimestamp = -1;
            beginningTimestamp = -1;

            // Stop receiving sensor readings
            stopHeartDeepInternals();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int newAccuracy) {

    }

    /**
     * Helper function to encapsulate the last sensor readings in a SensorReadings instance
     *
     * @param type           The type of SensorReadings instance to build
     * @param readingsList   The raw readings list. Each element of this list must be a 3-length array.
     * @param timestampsList The timestamps list²
     * @return A SensorReadings instance corresponding to the last sensor readings
     */
    private SensorReadings buildLastSensorReadings(String type, List<double[]> readingsList, List<Long> timestampsList) {
        int size = readingsList.size();
        double[] x = new double[size];
        double[] y = new double[size];
        double[] z = new double[size];
        double[] timestamps = new double[size];
        double[] readings;
        for (int i = 0; i < size; i++) {
            readings = readingsList.get(i);
            x[i] = readings[0];
            y[i] = readings[1];
            z[i] = readings[2];
            timestamps[i] = timestampsList.get(i);
        }
        SensorReadings sensorReadings = new SensorReadings(type,
                size, x, y, z, timestamps);

        return sensorReadings;
    }

    /**
     * Helper function to clear all the {@code List} which serve as buffer for sensor readings and their timestamps
     */
    private void clearAllReadingsBuffers() {
        accReadings.clear();
        accReadingsTimestamps.clear();

        gyrReadings.clear();
        gyrReadingsTimestamps.clear();
    }

    /**
     * Helper function which converts the readings in the lists to SensorReadings instances,
     * triggers their processing (by HeartDeepService), and empty the readings lists
     */
    private void triggerReadingsProcessing() {
        // Build last sensor readings
        SensorReadings accelerometerReadings = buildLastSensorReadings(SensorReadings.TYPE_ACCELEROMETER_READINGS, accReadings, accReadingsTimestamps);
        SensorReadings gyroscopeReadings = buildLastSensorReadings(SensorReadings.TYPE_GYROSCOPE_READINGS, gyrReadings, gyrReadingsTimestamps);

        // Launch the processing of those readings (in background) and clear the lists
        HeartDeepService.processReadings(this, accelerometerReadings, gyroscopeReadings);

        clearAllReadingsBuffers();
    }

    /**
     * Setter for {@link #recordingTimeLength} variable
     * @param newVal The new value
     */
    public static void setRecordingTimeLength(long newVal){
        recordingTimeLength = newVal;
    }
}

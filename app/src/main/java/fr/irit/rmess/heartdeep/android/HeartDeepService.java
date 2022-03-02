/***********************************************************************
 Name............ : HeartDeepService.java
 Description..... : The central service of HeartDeep
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.android;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.irit.rmess.heartdeep.datatype.HeartDeepObservable;
import fr.irit.rmess.heartdeep.datatype.HeartDeepObserver;
import fr.irit.rmess.heartdeep.datatype.SensorReadings;

/**
 * An Android IntentService used to handle the (actual) function of HeartDeep.
 * It takes sensor readings as inputs, runs the internal algorithm of HeartDeep, and notify all its observers with the resulting outputs (encapsulated in a Bundle instance)
 */
public class HeartDeepService extends IntentService implements HeartDeepObservable<Bundle> {

    /**
     * A string key to access the temporal accelerometer readings in an output bundle
     */
    public static final String OUTPUT_ACC_READINGS_TEMPORAL = "fr.irit.rmess.heartdeep.output.ACC_READINGS_TEMPORAL";

    /**
     * A string key to access the temporal gyroscope readings in an output bundle
     */
    public static final String OUTPUT_GYR_READINGS_TEMPORAL = "fr.irit.rmess.heartdeep.output.GYR_READINGS_TEMPORAL";


    /**
     * A string key to access the result type field in an output bundle.
     * It is intended to provide a generic mechanism for handling multiple output types.
     * This means that the service can be extended to support more operations and thus more output types.
     * In such a case, we will just add another BUNDLE_TYPE_*
     */
    public static final String BUNDLE_TYPE = "fr.irit.rmess.heartdeep.BUNDLE_TYPE";

    /**
     * A bundle type to indicate a complete result (including all the possible output fields)
     */
    public static final String BUNDLE_TYPE_COMPLETE_RESULT = "fr.irit.rmess.heartdeep.BUNDLE_TYPE.COMPLETE_RESULT";
    /**
     * A bundle type to indicate a component status event
     */
    public static final String BUNDLE_TYPE_EVENT_STATUS = "fr.irit.rmess.heartdeep.BUNDLE_TYPE.EVENT_STATUS";
    /**
     * A string key to indicate sensor listener event status
     */
    public static final String EVENT_SENSOR_LISTENER_STATUS = "fr.irit.rmess.heartdeep.event.SENSOR_LISTENER_STATUS";
    /**
     * A string key to indicate sensor listener start event
     */
    public static final String EVENT_SENSOR_LISTENER_STARTED = "fr.irit.rmess.heartdeep.event.SENSOR_LISTENER_STARTED";
    /**
     * A string key to indicate sensor listener stop event
     */
    public static final String EVENT_SENSOR_LISTENER_STOPPED = "fr.irit.rmess.heartdeep.event.SENSOR_LISTENER_STOPPED";


    /**
     * A String describing 'process readings' as the action to be executed
     */
    private static final String ACTION_PROCESS_READINGS = "fr.irit.rmess.heartdeep.action.PROCESS_READINGS";



    /* CLASS MEMBERS */

    /**
     * The last created instance of HeartDeepService.
     */
    private static HeartDeepService lastInstance = null;

    /**
     * A list of observers registered to listen to this service
     */
    private static List<HeartDeepObserver<Bundle>> observers = new ArrayList<>();

    /**
     * A string to identify the current measurement session.
     * Useful to generate the name of the file to which the measurements will be saved.
     */
    private static String sessionId = "";

    /**
     * Creates a new HeartDeepService and initialize its fields
     */
    public HeartDeepService() {
        super("HeartDeepService");
    }

    /**
     * Gets the last created instance of this class.
     * If not yet created, will be created and returned.
     *
     * Almost useless method.
     * Useful only to return any instance of this class for {@link #registerObserver(fr.irit.rmess.heartdeep.datatype.HeartDeepObserver)} to be called on.
     *
     * @return The last created instance of this class
     */
    public static HeartDeepService getLastInstance() {
        if (lastInstance == null) {
            lastInstance = new fr.irit.rmess.heartdeep.android.HeartDeepService();
        }

        return lastInstance;
    }

    /**
     * Public function to launch the processing of sensor readings
     * It stands as a wrapper for {@link #handleProcessReadingsAction(SensorReadings, SensorReadings)}
     * @param context The context from which the function is called
     * @param accReadings Accelerometer readings to be processed
     * @param gyrReadings Gyroscope readings to be processed
     */
    public static void processReadings(Context context, SensorReadings accReadings, SensorReadings gyrReadings) {
        Intent intent = new Intent(context, HeartDeepService.class);
        intent.setAction(ACTION_PROCESS_READINGS);
        intent.putExtra(SensorReadings.TYPE_ACCELEROMETER_READINGS, accReadings);
        intent.putExtra(SensorReadings.TYPE_GYROSCOPE_READINGS, gyrReadings);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        lastInstance = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_READINGS.equals(action)) {
                final SensorReadings accReadings = (SensorReadings) intent.getSerializableExtra(SensorReadings.TYPE_ACCELEROMETER_READINGS);
                final SensorReadings gyrReadings = (SensorReadings) intent.getSerializableExtra(SensorReadings.TYPE_GYROSCOPE_READINGS);

                // Measure the total processing time
                long processingTime = System.currentTimeMillis();

                handleProcessReadingsAction(accReadings, gyrReadings);

                processingTime = System.currentTimeMillis() - processingTime;
                Log.d("PROCESSING-TIME", "processingTime=" + processingTime);
            }
        }
    }

    /**
     * Actual processing of the readings
     * @param accReadings Accelerometer readings to be processed
     * @param gyrReadings Gyroscope readings to be processed
     */
    private void handleProcessReadingsAction(SensorReadings accReadings, SensorReadings gyrReadings) {
        // Create the output bundle
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TYPE, BUNDLE_TYPE_COMPLETE_RESULT);
        bundle.putSerializable(OUTPUT_ACC_READINGS_TEMPORAL, accReadings);
        bundle.putSerializable(OUTPUT_GYR_READINGS_TEMPORAL, gyrReadings);

        // Notify all the registered listeners
        notifyAllObservers(bundle);
    }


    @Override
    public void registerObserver(HeartDeepObserver<Bundle> observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unRegisterObserver(HeartDeepObserver<Bundle> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver(HeartDeepObserver<Bundle> observer, Bundle bundle) {
        if (observer != null) {
            observer.update(this, bundle);
        }
    }

    @Override
    public void notifyAllObservers(Bundle newValue) {
        for (HeartDeepObserver observer : observers) {
            notifyObserver(observer, newValue);
        }
    }

    /**
     * Helper function to publish an event
     * @param eventBundle A bundle whose fields indicate the type of event and the corresponding fields and values
     */
    public static void publishEvent(Bundle eventBundle){
        getLastInstance().notifyAllObservers(eventBundle);
    }

    /**
     * Helper function to set the infos of the current measurement session.
     * @param sex
     * @param age
     * @param userId
     * @param recordingDuration
     */
    public static void setSessionInfos(String sex, int age, String userId, long recordingDuration){
        sessionId = userId.toUpperCase() + "-" + sex + "-" + age + "yrs-" + recordingDuration + "s";
        
        HeartDeepSensorListenerService.setRecordingTimeLength(recordingDuration * 1000 * 1000 * 1000L);
    }

    /**
     * Getter for sessionId
     * @return Current session id
     */
    public static String getSessionId(){
        return sessionId;
    }
}

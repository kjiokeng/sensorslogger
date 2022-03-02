/***********************************************************************
 Name............ : TimelinedData.java
 Description..... : A class to store multiple values that are associated with timestamps
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.datatype;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A class to store multiple values that are associated with timestamps
 */
public class TimelinedData implements Serializable {
    /* Constants */

    public static final String TYPE_TIMELINED_ORIENTATIONS = "fr.irit.rmess.heartdeep.datatype.TimelinedData.ORIENTATIONS";

    public static final String TYPE_TIMELINED_BARO_READINGS = "fr.irit.rmess.heartdeep.datatype.TimelinedData.BARO_READINGS";


    /* Fields */

    /**
     * The type of data stored in this instance.
     * Should be one of the known types: TYPE_TIMELINED_*
     */
    private String type;

    /**
     * The length (number) of the values
     */
    private int n;

    /**
     * An array storing the values
     */
    private double[] values;

    /**
     * An array storing the timestamps
     */
    private long[] timestamps;

    /**
     * Creates a TimelinedData instance with the specified length, values and timestamps
     *
     * @param n          The number of values
     * @param values     The values to be stored
     * @param timestamps The timestamps corresponding to the given values
     */
    public TimelinedData(int n, double[] values, long[] timestamps) {
        this.n = n;
        this.values = values;
        this.timestamps = timestamps;
        type = "";
    }

    /**
     * Creates a TimelinedData instance with the specified type, length, values and timestamps
     *
     * @param type       The type of the data to be stored.
     *                   Should be one of the known types: TYPE_TIMELINED_*
     * @param n          The number of values
     * @param values     The values to be stored
     * @param timestamps The timestamps corresponding to the given values
     */
    public TimelinedData(String type, int n, double[] values, long[] timestamps) {
        this.type = type;
        this.n = n;
        this.values = values;
        this.timestamps = timestamps;
    }

    /**
     * Copy constructor
     */
    public TimelinedData(TimelinedData otherInstance){
        this.type = otherInstance.type;
        this.n = otherInstance.n;
        this.values = otherInstance.values;
        this.timestamps = otherInstance.timestamps;
    }


    /* Getters and setters */

    /**
     * Gets the number of data elements stored in this instance
     *
     * @return The number of data
     */
    public int getN() {
        return n;
    }

    /**
     * Sets the number of data elements for this instance
     * @param n The number of data elements to be set
     */
    public void setN(int n) {
        this.n = n;
    }

    /**
     * Gets the data values stored in this instance
     * @return The values stored in this instance
     */
    public double[] getValues() {
        return values;
    }

    /**
     * Sets the values to be stored in this instance
     * @param values The values to be stored in this instance
     */
    public void setValues(double[] values) {
        this.values = values;
    }

    /**
     * Gets the timestamps of the values stored in this instance
     * @return The timestamps of the values stored in this instance
     */
    public long[] getTimestamps() {
        return timestamps;
    }

    /**
     * Sets the timestamps of the values to be stored in this instance
     * @param timestamps The timestamps of the values to be stored in this instance
     */
    public void setTimestamps(long[] timestamps) {
        this.timestamps = timestamps;
    }

    /**
     * Gets the value at a given index
     *
     * @param index The index for which we want the value
     * @return The value stored at the given index
     */
    public double getValue(int index) {
        return values[index];
    }

    /**
     * Sets the value to be stored at a given index
     *
     * @param index The index at which to store the value
     * @param value The value to be stored
     */
    public void setValue(int index, double value) {
        this.values[index] = value;
    }

    /**
     * Gets the timestamp of the value at a given index
     *
     * @param index The index for which we want the timestamp
     * @return The timestamp of the value at the given index
     */
    public long getTimestamp(int index) {
        return timestamps[index];
    }

    /**
     * Sets the timestamp for the value at a given index
     *
     * @param index     The index for which we want to modify the timestamp
     * @param timestamp The new value of that timestamp
     */
    public void setTimestamp(int index, long timestamp) {
        this.timestamps[index] = timestamp;
    }


    /* Equals, hashCode and toString methods */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimelinedData)) return false;

        TimelinedData that = (TimelinedData) o;

        if (n != that.n) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (!Arrays.equals(values, that.values)) return false;
        return Arrays.equals(timestamps, that.timestamps);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + n;
        result = 31 * result + Arrays.hashCode(values);
        result = 31 * result + Arrays.hashCode(timestamps);
        return result;
    }

    @Override
    public String toString() {
        return "TimelinedData{" +
                "n=" + n +
                ", values=" + Arrays.toString(values) +
                ", timestamps=" + Arrays.toString(timestamps) +
                '}';
    }
}

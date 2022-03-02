/***********************************************************************
 Name............ : SensorReadings.java
 Description..... : A class representing sensor readings
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.datatype;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A class representing sensor readings
 */
public class SensorReadings implements Serializable {

    /**
     * A constant type for accelerometer readings (obtained from Sensor.TYPE_LINEAR_ACCELERATION sensor)
     */
    public static String TYPE_ACCELEROMETER_READINGS = "fr.irit.rmess.heartdeep.datatype.SensorReadings.ACCELEROMETER";

    /**
     * A constant type for accelerometer readings in frequential domain
     */
    public static String TYPE_ACCELEROMETER_FREQUENTIAL_READINGS = "fr.irit.rmess.heartdeep.datatype.SensorReadings.ACCELEROMETER_FREQUENTIAL";

    /**
     * A constant type for gyroscope readings (obtained from Sensor.TYPE_GYROSCOPE sensor)
     */
    public static String TYPE_GYROSCOPE_READINGS = "fr.irit.rmess.heartdeep.datatype.SensorReadings.GYROSCOPE";

    /**
     * A constant type for gyroscope readings in frequential domain
     */
    public static String TYPE_GYROSCOPE_FREQUENTIAL_READINGS = "fr.irit.rmess.heartdeep.datatype.SensorReadings.GYROSCOPE_FREQUENTIAL";

    /**
     * A constant type for raw accelerometer readings (obtained from Sensor.TYPE_ACCELEROMETER sensor)
     */
    public static String TYPE_RAW_ACCELEROMETER_READINGS = "fr.irit.rmess.heartdeep.datatype.SensorReadings.RAW_ACCELEROMETER";

    /**
     * A constant type for magnetometer readings (obtained from Sensor.TYPE_MAGNETIC_FIELD sensor)
     */
    public static String TYPE_MAGNETOMETER_READINGS = "fr.irit.rmess.heartdeep.datatype.SensorReadings.MAGNETOMETER";


    /* Fields */

    /**
     * The type of the readings.
     * Should be one of the known types: TYPE_*_READINGS
     */
    private String type;

    /**
     * The length (number) of the readings
     */
    private int n;

    /**
     * The readings along x axis
     */
    private double[] x;

    /**
     * The readings along y axis
     */
    private double[] y;

    /**
     * The readings along z axis
     */
    private double[] z;

    /**
     * The timestamp at which those readings were delivered.
     * This attribute is of type double[] (and not long[]) because it can be used to store frequencies (for frequential SensorReadings instances)
     */
    private double[] timestamps;


    /* Constructors */

    /**
     * Creates an empty SensorReadings instance, without any initialization
     */
    public SensorReadings() {
    }

    /**
     * Creates an empty SensorReadings instance of the specified type
     * @param type The type of the readings
     *             Should be one of the known types: TYPE_*_READINGS
     */
    public SensorReadings(String type) {
        this.type = type;
    }

    /**
     * Creates a SensorReadings instance of the specified type and the specified length (number of readings)
     * @param type The type of the readings
     *             Should be one of the known types: TYPE_*_READINGS
     * @param n The length of the readings
     */
    public SensorReadings(String type, int n) {
        this.type = type;
        this.n = n;
        if (n > 0) {
            x = new double[n];
            y = new double[n];
            z = new double[n];
            timestamps = new double[n];
        }
    }

    /**
     * Creates a SensorReadings instance of the specified length (number of readings)
     * @param n The length of the readings
     */
    public SensorReadings(int n) {
        this.n = n;
        if (n > 0) {
            x = new double[n];
            y = new double[n];
            z = new double[n];
            timestamps = new double[n];
        }
    }

    /**
     * Creates a SensorReadings instance with the specified values
     * The value of {@link #n} will be set as the minimum of the three array length. This means that only the nth first values will be considered.
     * @param x The readings along x axis
     * @param y The readings along y axis
     * @param z The readings along z axis
     */
    public SensorReadings(double[] x, double[] y, double[] z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (x != null && y != null && z != null) {
            this.n = x.length;
            if (y.length < this.n) {
                this.n = y.length;
            }
            if (z.length < this.n) {
                this.n = z.length;
            }
        }
        timestamps = new double[n];
    }

    /**
     * Creates a SensorReadings instance with the specified type and the specified values
     * The value of {@link #n} will be set as the minimum of the three array length
     * @param type The type of the readings
     *             Should be one of the known types: TYPE_*_READINGS
     * @param x The readings along x axis
     * @param y The readings along y axis
     * @param z The readings along z axis
     */
    public SensorReadings(String type, double[] x, double[] y, double[] z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        if (x != null && y != null && z != null) {
            this.n = x.length;
            if (y.length < this.n) {
                this.n = y.length;
            }
            if (z.length < this.n) {
                this.n = z.length;
            }
        }
        timestamps = new double[n];
    }

    /**
     * Creates a SensorReadings instance with the specified length and values.
     * The value of {@link #n} will be set as the minimum of the three array length. This means that only the nth first values will be considered.
     * @param n The length of the readings
     * @param x The readings along x axis
     * @param y The readings along y axis
     * @param z The readings along z axis
     */
    public SensorReadings(int n, double[] x, double[] y, double[] z) {
        this.n = n;
        this.x = x;
        this.y = y;
        this.z = z;
        timestamps = new double[n];
    }

    /**
     * Creates a SensorReadings instance with the specified type, length and values.
     * The value of {@link #n} will be set as the minimum of the three array length. This means that only the nth first values will be considered.
     * @param type The type of the readings
     *             Should be one of the known types: TYPE_*_READINGS
     * @param n The length of the readings
     * @param x The readings along x axis
     * @param y The readings along y axis
     * @param z The readings along z axis
     */
    public SensorReadings(String type, int n, double[] x, double[] y, double[] z) {
        this.type = type;
        this.n = n;
        this.x = x;
        this.y = y;
        this.z = z;
        timestamps = new double[n];
    }

    /**
     * Creates a SensorReadings instance with the specified type, length and values.
     * The value of {@link #n} will be set as the minimum of the three array length. This means that only the nth first values will be considered.
     *
     * @param type       The type of the readings
     *                   Should be one of the known types: TYPE_*_READINGS
     * @param n          The length of the readings
     * @param x          The readings along x axis
     * @param y          The readings along y axis
     * @param z          The readings along z axis
     * @param timestamps The timestamps at which the readings where delivered
     */
    public SensorReadings(String type, int n, double[] x, double[] y, double[] z, double[] timestamps) {
        this.type = type;
        this.n = n;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamps = timestamps;
    }

    /**
     * Copy constructor: creates a new instance of SensorReadings similar to the given one
     *
     * @param sensorReadings The instance to be copied
     */
    public SensorReadings(SensorReadings sensorReadings) {
        this.type = sensorReadings.getType();
        this.n = sensorReadings.getN();
        this.x = sensorReadings.getX().clone();
        this.y = sensorReadings.getY().clone();
        this.z = sensorReadings.getZ().clone();
        this.timestamps = sensorReadings.getTimestamps().clone();
    }



    /* Getters and Setters */

    /**
     * Gets the type of this SensorReadings instance
     * @return The value of type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this SensorReadings instance
     * @param type The new type to be set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the number of readings of this SensorReadings instance
     * @return The number of readings
     */
    public int getN() {
        return n;
    }

    /**
     * Sets the number of readings of this SensorReadings instance
     * @param n The number of readings to be set
     */
    public void setN(int n) {
        this.n = n;
    }

    /**
     * Gets the readings along x axis
     * @return The readings along x axis
     */
    public double[] getX() {
        return x;
    }

    /**
     * Sets the readings along x axis
     * The value of n will also be set as the minimum of its previous value and the length of the given array
     * @param x The readings along x axis
     */
    public void setX(double[] x) {
        this.x = x;
        if (x != null && x.length < this.n) {
            this.n = x.length;
        }
    }

    /**
     * Gets the readings along y axis
     * @return The readings along y axis
     */
    public double[] getY() {
        return y;
    }

    /**
     * Sets the readings along y axis
     * The value of n will also be set as the minimum of its previous value and the length of the given array
     * @param y The readings along y axis
     */
    public void setY(double[] y) {
        this.y = y;
        if (y != null && y.length < this.n) {
            this.n = y.length;
        }
    }

    /**
     * Gets the readings along z axis
     * @return The readings along z axis
     */
    public double[] getZ() {
        return z;
    }

    /**
     * Sets the readings along z axis
     * The value of n will also be set as the minimum of its previous value and the length of the given array
     * @param z The readings along z axis
     */
    public void setZ(double[] z) {
        this.z = z;
        if (z != null && z.length < this.n) {
            this.n = z.length;
        }
    }

    /**
     * Gets the timestamps of the readings
     *
     * @return The timestamps of the readings
     */
    public double[] getTimestamps() {
        return timestamps;
    }

    /**
     * Sets the timestamps of the readings
     *
     * @param timestamps The timestamps of the readings
     */
    public void setTimestamps(double[] timestamps) {
        if (timestamps != null) {
            this.timestamps = timestamps;
        }
    }


    /**
     * Gets the index-th reading along x axis
     *
     * @param index The index we are looking for its value
     * @return The value at x[index]
     */
    public double getX(int index) {
        return x[index];
    }

    /**
     * Sets {@code value} as the index-th reading along x axis
     *
     * @param index The index at which the value should be set
     * @param value The value to set
     */
    public void setX(int index, double value) {
        x[index] = value;
    }

    /**
     * Gets the index-th readings along y axis
     *
     * @param index The index we are looking for its value
     * @return The value at y[index]
     */
    public double getY(int index) {
        return y[index];
    }

    /**
     * Sets {@code value} as the index-th readings along y axis
     *
     * @param index The index at which the value should be set
     * @param value The value to set
     */
    public void setY(int index, double value) {
        y[index] = value;
    }

    /**
     * Gets the index-th readings along z axis
     *
     * @param index The index we are looking for its value
     * @return The value at y[index]
     */
    public double getZ(int index) {
        return z[index];
    }

    /**
     * Sets {@code value} as the index-th readings along z axis
     *
     * @param index The index at which the value should be set
     * @param value The value to set
     */
    public void setZ(int index, double value) {
        z[index] = value;
    }

    /**
     * Gets the index-th timestamp of the readings
     *
     * @return The index-th timestamp of the readings
     */
    public double getTimestamp(int index) {
        return timestamps[index];
    }


    /* Equals, hashcode and toString*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SensorReadings)) return false;

        SensorReadings that = (SensorReadings) o;

        if (n != that.n) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (!Arrays.equals(x, that.x)) return false;
        if (!Arrays.equals(y, that.y)) return false;
        if (!Arrays.equals(z, that.z)) return false;
        return Arrays.equals(timestamps, that.timestamps);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + n;
        result = 31 * result + Arrays.hashCode(x);
        result = 31 * result + Arrays.hashCode(y);
        result = 31 * result + Arrays.hashCode(z);
        result = 31 * result + Arrays.hashCode(timestamps);
        return result;
    }

    @Override
    public String toString() {
        return "SensorReadings{" +
                "type='" + type + '\'' +
                ", n=" + n +
                ", x=" + Arrays.toString(x) +
                ", y=" + Arrays.toString(y) +
                ", z=" + Arrays.toString(z) +
                ", timestamps=" + Arrays.toString(timestamps) +
                '}';
    }

    @Override
    protected SensorReadings clone() {
        return new SensorReadings(this);
    }
}

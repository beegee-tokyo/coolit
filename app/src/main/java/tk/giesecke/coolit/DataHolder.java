package tk.giesecke.coolit;

import android.app.PendingIntent;

/**
 * DataHolder
 * <p/>
 * Storage for values that are used by activities and background services
 *
 * @author Bernd Giesecke
 * @version 1.0 April 5, 2015.
 */
public class DataHolder {

    /**
     * Storage for pendingIntent of background service
     */
    private static PendingIntent pendingIntent;
    /**
     * Storage for current temperature
     */
    private static int currentTemp;

    /**
     * Initialization of some values
     */
    private DataHolder() {
        pendingIntent = null;
    }

    /**
     * Storage for PendingIntent
     * get handler
     *
     * @return pendingIntent
     *            pending intent of BGService for AlarmManager
     */
    //public static PendingIntent getPendingIntent() {
    //    return pendingIntent;
    //}

    /**
     * Storage for PendingIntent
     * store handler
     *
     * @param value
     *            pending intent of BGService for AlarmManager
     */
    //public static void writePendingIntent(PendingIntent value) {
    //    pendingIntent = value;
    //}

    /**
     * Storage for current temperature
     * get temperature
     *
     * @return pendingIntent
     * current temperature
     */
    public static int getCurrentTemp() {
        return currentTemp;
    }

    /**
     * Storage for current temperature
     * store temperature
     *
     * @param value current temperature
     */
    public static void writeCurrentTemp(int value) {
        currentTemp = value;
    }
}

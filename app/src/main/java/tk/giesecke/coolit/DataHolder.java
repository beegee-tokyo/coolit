package tk.giesecke.coolit;

/**
 * DataHolder
 * <p/>
 * Storage for values that are used by activities and background services
 *
 * @author Bernd Giesecke
 * @version 1.0 May 2, 2015.
 */
class DataHolder {

    /** Storage for current temperature */
    private static int currentTemp;

    /**
     * Initialization of some values
     */
    private DataHolder() {

    }

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

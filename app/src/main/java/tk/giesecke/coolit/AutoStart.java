package tk.giesecke.coolit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * AutoStart
 * <p/>
 * Broadcast receiver for boot completed
 *
 * @author Bernd Giesecke
 * @version 1.0 April 5, 2015.
 */
public class AutoStart extends BroadcastReceiver {
    /**
     * Debug tag
     */
    final static String LOG_TAG = "CoolIt AutoStart";

    public AutoStart() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onReceive");
            /** Access to shared preferences */
            SharedPreferences myPrefs = context.getSharedPreferences("CoolIt", 0);
            if (BuildConfig.DEBUG)
                Log.d(LOG_TAG, "alarmTempIndex" + myPrefs.getInt("alarmTempIndex", 99));
            if (myPrefs.getInt("alarmTempIndex", 99) != 99) {
            /* Setting the alarm here */
                /** Intent of background service */
                Intent alarmIntent = new Intent(context, BGService.class);
                /** Pending intent of background service */
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // TODO change repeat value to 30min (1800000) instead of test value of 30s (30000)
                /** AlarmManager for repeated call of background service */
                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30000, pendingIntent);
            }
        }
    }
}

package tk.giesecke.coolit;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * BGService
 * <p/>
 * Background service to check current temperature, called every 30 minutes
 *
 * @author Bernd Giesecke
 * @version 1.0 April 5, 2015.
 */
public class BGService extends Service {
    /**
     * Debug tag
     */
    private final static String LOG_TAG = "CoolIt Service";
    /**
     * Receiver for battery temperature display
     */
    private mBatInfoReceiver myBatInfoReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onCreate");
        /* Receiver for battery temperature sensor */
        myBatInfoReceiver = new mBatInfoReceiver();
        this.registerReceiver(myBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Get battery temperature
     *
     * @see <a href="http://stackoverflow.com/questions/3997289/get-temperature-of-battery-on-android">
     * Get temperature of battery on android</a>
     */
    public class mBatInfoReceiver extends BroadcastReceiver {

        int temp = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            DataHolder.writeCurrentTemp(temp);

            /** Access to shared preferences */
            SharedPreferences myPrefs = context.getSharedPreferences("CoolIt", 0);
            /** Selected temperature for kill all apps and switch off screen */
            float alarmTemp = myPrefs.getFloat("alarmTemp", 500);
            if (alarmTemp < temp / 10) {
                if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Temperature exceeds alarmTemp " + alarmTemp);
                // TODO remove this toast
                Toast.makeText(context.getApplicationContext(),
                        context.getString(R.string.alarmTitle) + "\n" +
                                context.getString(R.string.alarmText),
                        Toast.LENGTH_LONG).show();
                /** Package name of home launcher */
                String launcherName = CoolIt.getLauncherName(context);

                // Try to get all running apps and kill them
                CoolIt.killRunningApps(context, launcherName);

                // Switch off the display
                DevicePolicyManager deviceManger = (DevicePolicyManager) getSystemService(
                        Context.DEVICE_POLICY_SERVICE);
                ComponentName compName = new ComponentName(context, MyAdmin.class);

                /** Check if we are device admin */
                boolean active = deviceManger.isAdminActive(compName);
                if (active) {
                    deviceManger.lockNow();
                } else {
                    Toast.makeText(context, getString(R.string.notDeviceAdmin), Toast.LENGTH_LONG).show();
                }
            }
            // unregister listener until next call of the service
            context.unregisterReceiver(myBatInfoReceiver);
            // Update widgets if any
            if (myPrefs.getBoolean("widgetActive", false)) {
                if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Updating widgets");
                CoolItWidget.forceUpdate(context);
            }
            stopSelf();
        }
    }
}

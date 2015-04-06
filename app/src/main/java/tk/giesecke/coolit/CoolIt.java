package tk.giesecke.coolit;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


/**
 * CoolIt
 * <p/>
 * main activity
 *
 * @author Bernd Giesecke
 * @version 1.0 April 5, 2015.
 */
public class CoolIt extends ActionBarActivity implements View.OnClickListener {
    /**
     * Debug tag
     */
    private final static String LOG_TAG = "CoolIt";
    /**
     * Activity context
     */
    private static Activity activity;
    /**
     * Receiver for battery temperature display
     */
    private mBatInfoReceiver myBatInfoReceiver;
    /**
     * TextView for battery temperature display
     */
    private TextView tv_battTemp;
    /**
     * Selected temperature for kill all apps and switch off screen
     */
    private int alarmTemp;
    /**
     * Access to shared preferences
     */
    private SharedPreferences myPrefs;
    /**
     * Access to DevicePolicyManager
     */
    private DevicePolicyManager deviceManger;
    /**
     * ComponentName of Admin activity
     */
    private ComponentName compName;
    /**
     * Result of request for device admin
     */
    private static final int RESULT_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceManger = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        /*
      Access to ActivityManager
     */
        ActivityManager activityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        setContentView(R.layout.cool_it);

        activity = this;
        myPrefs = getSharedPreferences("CoolIt", 0);

        // Check if we are already DeviceAdmin
        if (!myPrefs.getBoolean("DeviceManager", false)) {
            /** Intent for request to be device admin */
            Intent intent = new Intent(DevicePolicyManager
                    .ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Device Admin rights are necessary to switch off the screen.");
            startActivityForResult(intent, RESULT_ENABLE);
        }

        /* Receiver for battery temperature sensor */
        myBatInfoReceiver = new mBatInfoReceiver();
        this.registerReceiver(myBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        /** TextView for title above temperature */
        TextView tv_temp_title = (TextView) this.findViewById(R.id.tv_temp_title);
        tv_battTemp = (TextView) this.findViewById(R.id.tv_battTemp);
        /** TextView for title above temperature */
        TextView tv_help = (TextView) this.findViewById(R.id.tv_help);
        /** TextView for alarm temperature */
        TextView tv_selectTemp = (TextView) this.findViewById(R.id.tv_selectTemp);
        /** Button for exit */
        Button bt_exit = (Button) this.findViewById(R.id.bt_exit);
        /** Button for save and exit */
        Button bt_set = (Button) this.findViewById(R.id.bt_set);
        /** Typeface for this apps font */
        Typeface type = Typeface.createFromAsset(getAssets(), "IDroid Bold.otf");
        tv_temp_title.setTypeface(type);
        tv_help.setTypeface(type);
        tv_selectTemp.setTypeface(type);
        bt_exit.setTypeface(type);
        bt_set.setTypeface(type);
        tv_battTemp.setTypeface(type);

        // Initialize the NumberPicker with selectable reset interval
        NumberPicker.OnValueChangeListener onIntervalChanged
                = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(
                    NumberPicker picker,
                    int oldVal,
                    int newVal) {
                alarmTemp = newVal;
            }
        };

        // Get list with available intervals for reboot
        /*
      Array of available temperatures
     */
        String[] alarmTempArray = getResources().getStringArray(R.array.alarmTemp);
        /** pointer to NumberPicker for interval list */
        NumberPicker np_alarmTemp =
                (NumberPicker) findViewById(R.id.np_alarmTemp);
        np_alarmTemp.setSaveFromParentEnabled(false);
        np_alarmTemp.setSaveEnabled(true);
        np_alarmTemp.setMaxValue(alarmTempArray.length - 1);
        np_alarmTemp.setMinValue(0);
        np_alarmTemp.setDisplayedValues(alarmTempArray);
        np_alarmTemp.setOnValueChangedListener(onIntervalChanged);
        np_alarmTemp.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        alarmTemp = alarmTempArray.length - 1;
        alarmTemp = myPrefs.getInt("alarmTempIndex", alarmTemp);
        np_alarmTemp.setValue(alarmTemp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_cool:
                if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Button kill and shut down");
                /** Package name of home launcher */
                String launcherName = getLauncherName(this);

                // Try to get all running apps and kill them
                killRunningApps(this, launcherName);

                // Switch off the display
                switchScreenOff(this);

                // Stop yourself
                activity.finish();
                System.exit(0);

                break;
            case R.id.bt_exit:
                if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Button clear and exit");
                if (!myPrefs.getBoolean("widgetActive", false)) {
                    /** Intent of background service */
                    Intent alarmIntent = new Intent(this, BGService.class);
                    /** Pending intent of background service */
                    PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    /** AlarmManager for repeated call of background service */
                    AlarmManager stopManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    stopManager.cancel(pendingStopIntent);
                }
                myPrefs.edit().remove("alarmTemp").apply();
                myPrefs.edit().remove("alarmTempIndex").apply();
                try {
                    this.unregisterReceiver(myBatInfoReceiver);
                } catch (Exception ignore) {
                }
                activity.finish();
                break;
            case R.id.bt_set:
                if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Button save and exit");
                int[] floatAlarmTemp = getResources().getIntArray(R.array.intAlarmTemp);
                myPrefs.edit().putFloat("alarmTemp", (float) floatAlarmTemp[alarmTemp]).apply();
                myPrefs.edit().putInt("alarmTempIndex", alarmTemp).apply();
                /** Intent of background service */
                Intent alarmIntent = new Intent(this, BGService.class);
                /** Pending intent of background service */
                PendingIntent pendingStartIntent = PendingIntent.getService(this, 0, alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                /** AlarmManager for repeated call of background service */
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                // TODO change repeat value to 30min (1800000) instead of test value of 30s (30000)
                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        30000, pendingStartIntent);
                //Intent startIntent = new Intent(this, BGService.class);
                //startService(startIntent);
                try {
                    this.unregisterReceiver(myBatInfoReceiver);
                } catch (Exception ignore) {
                }
                activity.finish();
                break;
        }
    }

    protected void onPause() {
        super.onPause();
        try {
            this.unregisterReceiver(myBatInfoReceiver);
        } catch (Exception ignore) {
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    myPrefs.edit().putBoolean("DeviceManager", true).apply();
                } else {
                    myPrefs.edit().putBoolean("DeviceManager", false).apply();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Get launcher name
     *
     * @param context application context. Can not be null.
     * @see <a href="https://www.linkedin.com/groups/How-close-all-activities-in-86481.S.235755042">
     * Neeraj R. - How-close-all-activities...</a>
     */
    public static String getLauncherName(Context context) {
        // Get name of launcher (we don't want to kill him yet!)
        /** Intent for home screen launcher */
        Intent home = new Intent("android.intent.action.MAIN");
        home.addCategory("android.intent.category.HOME");
        /** Package info of home screen launcher */
        final ResolveInfo mInfo = context.getPackageManager().resolveActivity(home, 0);
        /** Package name of home screen launcher */
        return mInfo.activityInfo.processName;
    }

    /**
     * Kill all running apps
     *
     * @param context      application context. Can not be null.
     * @param launcherName package name of home launcher.
     * @see <a href="http://stackoverflow.com/questions/6996536/how-to-close-all-active-applications-from-my-android-app/6996635#6996635">
     * How to close all active applications...</a>
     */
    public static void killRunningApps(Context context, String launcherName) {
        // Try to get all running apps and kill them before rebooting
        /** Package manager */
        PackageManager pm = context.getPackageManager();
        /** List holding package info of all installed apps */
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);

        /** Activity manager */
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context
                .ACTIVITY_SERVICE);

        for (ApplicationInfo packageInfo : packages) {
            // Don't kill ourselves and certain system apps
            if (packageInfo.packageName.equals("tk.giesecke.coolit") ||
                    packageInfo.packageName.equals("system") ||
                    packageInfo.packageName.equals("com.android.systemui") ||
                    packageInfo.packageName.equals(launcherName)) {
                if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Not killing " + packageInfo.packageName);
                continue;
            }
            if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Killing " + packageInfo.packageName);
            mActivityManager.killBackgroundProcesses(packageInfo.packageName);
        }
    }

    /**
     * Switch off the screen
     *
     * @param context application context. Can not be null.
     * @see <a href="http://rdcworld-android.blogspot.in/2012/03/lock-phone-screen-programmtically.html">
     * Lock Phone Screen Programmtically</a>
     */
    private void switchScreenOff(Context context) {
        /** Check if we are device admin */
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            deviceManger.lockNow();
        } else {
            Toast.makeText(context, getString(R.string.notDeviceAdmin), Toast.LENGTH_LONG).show();
        }
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
            /** Current temperature converted from int to string */
            String currTemp = Integer.toString(temp / 10) + "." + Integer.toString(temp - (temp / 10 * 10)) +
                    getString(R.string.degreeSign) + "C";
            if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onReceive battery temperature " + currTemp);
            tv_battTemp.setText(currTemp);
            if (temp < 160) {
                tv_battTemp.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            } else if (temp < 320) {
                tv_battTemp.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else if (temp < 480) {
                tv_battTemp.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else if (temp < 640) {
                tv_battTemp.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (temp < 800) {
                tv_battTemp.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else {
                tv_battTemp.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}

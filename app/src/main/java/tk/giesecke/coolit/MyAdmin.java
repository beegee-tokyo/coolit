package tk.giesecke.coolit;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * MyAdmin
 * <p/>
 * device admin stuff required to be a device admin
 *
 * @author Bernd Giesecke
 * @version 1.0 April 5, 2015.
 */
public class MyAdmin extends DeviceAdminReceiver {

    private void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.deviceAdminEnabled));
        /** Access to shared preferences */
        SharedPreferences myPrefs = context.getSharedPreferences("CoolIt", 0);
        // Change status of  DeviceAdmin
        myPrefs.edit().putBoolean("DeviceManager", true).apply();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.deviceAdminDisableReq);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.deviceAdminDisabled));
        /** Access to shared preferences */
        SharedPreferences myPrefs = context.getSharedPreferences("CoolIt", 0);
        // Change status of  DeviceAdmin
        myPrefs.edit().putBoolean("DeviceManager", false).apply();
    }
}

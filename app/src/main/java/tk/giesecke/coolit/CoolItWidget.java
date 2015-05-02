package tk.giesecke.coolit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * CoolItWidget
 * <p/>
 * widget to show current temperature
 *
 * @author Bernd Giesecke
 * @version 1.0 May 2, 2015.
 */
public class CoolItWidget extends AppWidgetProvider {
    /** Debug tag */
    private static final String LOG_TAG = "CoolIt Widget";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {

        super.onReceive(context, intent);
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onReceive");
        /** AppWidgetManager for this widget */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        /** ComponentName for this widget */
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                CoolItWidget.class.getName());
        /** Array of existing widgets */
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        /** View for this widgets */
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cool_it_widget);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    @Override
    public void onEnabled(Context context) {
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onEnabled");
        /** Access to shared preferences */
        SharedPreferences myPrefs = context.getSharedPreferences("CoolIt", 0);
        myPrefs.edit().putBoolean("widgetActive", true).apply();
        /** Intent of background service */
        Intent alarmIntent = new Intent(context, BGService.class);
        /** Pending intent of background service */
        PendingIntent pendingStartIntent = PendingIntent.getBroadcast(context, 30011962, alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        /** AlarmManager for repeated call of background service */
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                1800000, pendingStartIntent);
        Intent startIntent = new Intent(context, BGService.class);
        context.startService(startIntent);
    }

    @Override
    public void onDisabled(Context context) {
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onDisabled");
        /** Access to shared preferences */
        SharedPreferences myPrefs = context.getSharedPreferences("CoolIt", 0);
        myPrefs.edit().remove("widgetActive").apply();
        if (myPrefs.getInt("alarmTempIndex", 99) != 99) {
            /** Intent of background service */
            Intent alarmIntent = new Intent(context, BGService.class);
            /** Pending intent of background service */
            PendingIntent pendingStopIntent = PendingIntent.getService(context, 0, alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            /** AlarmManager for repeated call of background service */
            AlarmManager stopManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            stopManager.cancel(pendingStopIntent);
        }
    }

    /**
     * Update widgets with current temperature
     *
     * @param context          Application context.
     * @param appWidgetManager AppWidgetManager of this widget.
     * @param appWidgetId      Array with all widget IDs.
     */
    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        // Construct the RemoteViews object
        /** View for this widgets */
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cool_it_widget);

        // Create an Intent to launch MainActivity
        /** Intent for main activity */
        Intent intent1 = new Intent(context, CoolIt.class);
        intent1.putExtra("appWidgetId", appWidgetId);
        // Creating a pending intent, which will be invoked when the user
        // clicks on the widget
        /** PendingIntent for main activity */
        PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 0,
                intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        //  Attach an on-click listener to the battery icon
        views.setOnClickPendingIntent(R.id.tv_widgetTemp, pendingIntent1);

        /** Current temperature */
        int temp = DataHolder.getCurrentTemp();
        /** Current temperature converted from int to string */
        String currTemp = Integer.toString(temp / 10) + "." + Integer.toString(temp - (temp / 10 * 10)) +
                context.getString(R.string.degreeSign);
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "currTemp " + currTemp);
        if (temp < 160) {
            views.setTextColor(R.id.tv_widgetTemp, context.getResources().getColor(android.R.color.holo_blue_light));
        } else if (temp < 320) {
            views.setTextColor(R.id.tv_widgetTemp, context.getResources().getColor(android.R.color.holo_blue_dark));
        } else if (temp < 480) {
            views.setTextColor(R.id.tv_widgetTemp, context.getResources().getColor(android.R.color.holo_green_light));
        } else if (temp < 640) {
            views.setTextColor(R.id.tv_widgetTemp, context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (temp < 800) {
            views.setTextColor(R.id.tv_widgetTemp, context.getResources().getColor(android.R.color.holo_red_light));
        } else {
            views.setTextColor(R.id.tv_widgetTemp, context.getResources().getColor(android.R.color.holo_red_dark));
        }

        views.setTextViewText(R.id.tv_widgetTemp, currTemp);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Initiate update of widgets with current temperature
     *
     * @param context Application context.
     */
    public static void forceUpdate(Context context) {
        /** AppWidgetManager for this widget */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        /** ComponentName for this widget */
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                CoolItWidget.class.getName());
        /** Array of existing widgets */
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        for (int appWidgetId : appWidgetIds) {
            CoolItWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}


package me.rigfox.pmschedulewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class ScheduleWidget extends AppWidgetProvider {

    final static String ACTION_NEXT = "me.rigfox.PMscheduleWidgetNext";
    final static String ACTION_BACK = "me.rigfox.PMscheduleWidgetBack";

    final static int[] DAYS = {
            R.drawable.mon1,
            R.drawable.tue2,
            R.drawable.wed3,
            R.drawable.thu4,
            R.drawable.fri5,
            R.drawable.sat6
    };

    SharedPreferences sp;

    int day;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i], true);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        SharedPreferences sp = context.getSharedPreferences("WidgetDay", context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();

        editor.putInt("day", 0);
        editor.apply();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, boolean systemUpdate) {

        SharedPreferences sp = context.getSharedPreferences("WidgetDay", context.MODE_PRIVATE);

        int day = sp.getInt("day", 0);

        if (systemUpdate) {
            // Обновление вызвано системой
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            //Пон = 2, поэтому отнимаем 2
            dayOfWeek-=2;
            if (dayOfWeek == -1) {
                //Превращаем воскресенье в понедельник
                dayOfWeek = 0;
            }

            day = dayOfWeek;
            sp.edit().putInt("day", day).commit();
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_widget);

        views.setImageViewResource(R.id.imageView, DAYS[day]);

        // Задаем отложенный Intent для кнопки вперед
        Intent nextIntent = new Intent(context, ScheduleWidget.class);
        nextIntent.setAction(ACTION_NEXT);
        nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, appWidgetId, nextIntent, 0);

        views.setOnClickPendingIntent(R.id.nextButton, pIntent);

        // Задаем отложенный Intent для кнопки назад
        Intent backIntent = new Intent(context, ScheduleWidget.class);
        backIntent.setAction(ACTION_BACK);
        backIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pIntent = PendingIntent.getBroadcast(context, appWidgetId, backIntent, 0);

        views.setOnClickPendingIntent(R.id.backButton, pIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Проверяем, что это Intent кнопки вперед
        if (intent.getAction().equalsIgnoreCase(ACTION_NEXT)) {

            // извлекаем ID экземпляра
            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                sp = context.getSharedPreferences("WidgetDay", context.MODE_PRIVATE);

                day = sp.getInt("day", 0);

                if (day < 5) {
                    day++;
                } else {
                    day = 0;
                }

                sp.edit().putInt("day", day).commit();

                // Обновляем виджет
                updateAppWidget(context, AppWidgetManager.getInstance(context),
                        mAppWidgetId, false);
            }
        }

        if (intent.getAction().equalsIgnoreCase(ACTION_BACK)) {

            // извлекаем ID экземпляра
            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                sp = context.getSharedPreferences("WidgetDay", context.MODE_PRIVATE);

                day = sp.getInt("day", 0);

                if (day > 0) {
                    day--;
                } else {
                    day = 5;
                }

                sp.edit().putInt("day", day).commit();

                // Обновляем виджет
                updateAppWidget(context, AppWidgetManager.getInstance(context),
                        mAppWidgetId, false);
            }
        }
    }
}


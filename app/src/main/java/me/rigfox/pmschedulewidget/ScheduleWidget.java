package me.rigfox.pmschedulewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScheduleWidget extends AppWidgetProvider {

    final static String ACTION_NEXT = "me.rigfox.PMscheduleWidgetNext";
    final static String ACTION_BACK = "me.rigfox.PMscheduleWidgetBack";
    final static String ACTION_TODAY = "me.rigfox.PMscheduleWidgetToday";

    final static Long MILLISECUNDOFDAY = (long) 86400000;

    SharedPreferences sp;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {
        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.schedule_widget);

        setUpdateTV(rv, context, appWidgetId);
        setList(rv, context, appWidgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                R.id.listView);
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    void setUpdateTV(RemoteViews rv, Context context, int appWidgetId) {
        sp = context.getSharedPreferences("WidgetDay", Context.MODE_PRIVATE);

        Long timestamp = sp.getLong("timestamp", System.currentTimeMillis());

        Date date = new Date(timestamp);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;

        String dayOfWeekString = "";

        switch (dayOfWeek) {
            case 0:
                dayOfWeekString = "Понедельник";
                break;
            case 1:
                dayOfWeekString = "Вторник";
                break;
            case 2:
                dayOfWeekString = "Среда";
                break;
            case 3:
                dayOfWeekString = "Четверг";
                break;
            case 4:
                dayOfWeekString = "Пятница";
                break;
            case 5:
                dayOfWeekString = "Суббота";
                break;
        }

        int dayOfYear = calendar.get(Calendar.WEEK_OF_YEAR) - 35;

        String numWeek = String.valueOf(dayOfYear) + " неделя";

        rv.setTextViewText(R.id.dayOfWeek, dayOfWeekString);
        rv.setTextViewText(R.id.numWeek, numWeek);

        // Задаем отложенный Intent для кнопки вперед
        setPendingIntent(ACTION_NEXT, R.id.nextButton, context, appWidgetId, rv);

        // Задаем отложенный Intent для кнопки назад
        setPendingIntent(ACTION_BACK, R.id.backButton, context, appWidgetId, rv);

        // Задаем отложенный Intent для кнопки сегодня
        setPendingIntent(ACTION_TODAY, R.id.todayButton, context, appWidgetId, rv);

    }

    void setList(RemoteViews rv, Context context, int appWidgetId) {
        Intent adapter = new Intent(context, MyService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME));
        adapter.setData(data);
        rv.setRemoteAdapter(R.id.listView, adapter);
    }

    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        int mAppWidgetId;

        // Проверяем, что это Intent кнопки вперед
        if (intent.getAction().equalsIgnoreCase(ACTION_NEXT)) {
            Bundle extras = intent.getExtras();
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            sp = context.getSharedPreferences("WidgetDay", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            Long timestamp = sp.getLong("timestamp", System.currentTimeMillis());

            Date date = new Date(timestamp);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);

            Long addMS = MILLISECUNDOFDAY;

            if (calendar.get(Calendar.DAY_OF_WEEK) == 7) { //Проверка на cубботу
                addMS *= 2;
            }

            editor.putLong("timestamp", timestamp + addMS);
            editor.apply();

            updateAppWidget(context, AppWidgetManager.getInstance(context),
                    mAppWidgetId);
        }

        if (intent.getAction().equalsIgnoreCase(ACTION_BACK)) {
            Bundle extras = intent.getExtras();
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            sp = context.getSharedPreferences("WidgetDay", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            Long timestamp = sp.getLong("timestamp", System.currentTimeMillis());

            Date date = new Date(timestamp);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);

            Long addMS = -MILLISECUNDOFDAY;

            if (calendar.get(Calendar.DAY_OF_WEEK) == 2) { //Проверка на понедельник
                addMS *= 2;
            }

            editor.putLong("timestamp", timestamp + addMS);
            editor.apply();

            updateAppWidget(context, AppWidgetManager.getInstance(context),
                    mAppWidgetId);
        }

        if (intent.getAction().equalsIgnoreCase(ACTION_TODAY)) {
            Bundle extras = intent.getExtras();
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            sp = context.getSharedPreferences("WidgetDay", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            GregorianCalendar calendar = new GregorianCalendar();

            Long timestamp = System.currentTimeMillis();

            if (calendar.get(Calendar.DAY_OF_WEEK) == 1) { //Проверка на воскресенье
                timestamp += MILLISECUNDOFDAY;
            }

            editor.putLong("timestamp", timestamp);
            editor.apply();

            updateAppWidget(context, AppWidgetManager.getInstance(context),
                    mAppWidgetId);
        }
    }


    @Override
    public void onEnabled(Context context) {
        sp = context.getSharedPreferences("WidgetDay", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        GregorianCalendar calendar = new GregorianCalendar();

        Long timestamp = System.currentTimeMillis();

        if (calendar.get(Calendar.DAY_OF_WEEK) == 1) { //Проверка на воскресенье
            timestamp += MILLISECUNDOFDAY;
        }

        editor.putLong("timestamp", timestamp);
        editor.apply();
    }

    private void setPendingIntent(String Action, int id, Context context, int appWidgetId, RemoteViews rv) {
        Intent intent = new Intent(context, ScheduleWidget.class);
        intent.setAction(Action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);

        rv.setOnClickPendingIntent(id, pIntent);
    }
}
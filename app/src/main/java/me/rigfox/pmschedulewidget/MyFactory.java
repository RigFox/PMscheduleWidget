package me.rigfox.pmschedulewidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class MyFactory implements RemoteViewsFactory {

    scheduleContainer container;
    ArrayList<scheduleItem> scheduleData;
    Context context;
    int widgetID;

    MyFactory(Context ctx, Intent intent) {
        context = ctx;
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        try {
            scheduleGenerator generator = new scheduleGenerator(context);
            container = generator.container;
        } catch (IOException e) {
            e.printStackTrace();
        }
        scheduleData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return scheduleData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rView = new RemoteViews(context.getPackageName(),
                R.layout.schedule_item);

        scheduleItem currentItem = scheduleData.get(position);

        if (currentItem.subject_id == -1) {
            rView.setTextViewText(R.id.subject, "");
            rView.setTextViewText(R.id.teacher, "");
            rView.setTextViewText(R.id.week, "");
            rView.setTextViewText(R.id.timeText, "");
            if (position == 0) {
                rView.setTextViewText(R.id.subject, "Ура! Первой пары нет");
            }
        } else {
            subjectItem subjectItm = container.subjects.get(currentItem.subject_id);

            String startWeek = String.valueOf(currentItem.startWeek);
            String endWeek = String.valueOf(currentItem.endWeek);
            String subject = subjectItm.name;
            String teacher = subjectItm.teacher;
            String classroom = subjectItm.classroom;

            String week = "("+startWeek+"-"+endWeek+")\n"+classroom;

            String timeText = "";
            switch (currentItem.num) {
                case 1: timeText = "8:00-9:35"; break;
                case 2: timeText = "9:45-11:20"; break;
                case 3: timeText = "11:30-13:05"; break;
                case 4: timeText = "13:30-15:05"; break;
                case 5: timeText = "15:15-16:50"; break;
            }

            rView.setTextViewText(R.id.subject, subject);
            rView.setTextViewText(R.id.teacher, teacher);
            rView.setTextViewText(R.id.week, week);
            rView.setTextViewText(R.id.timeText, timeText);
        }

        return rView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        SharedPreferences sp = context.getSharedPreferences("WidgetDay", Context.MODE_PRIVATE);

        Long timestamp = sp.getLong("timestamp", System.currentTimeMillis());

        Date date = new Date(timestamp);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-2;
        int numWeek = calendar.get(Calendar.WEEK_OF_YEAR) - 35;

        scheduleData.clear();

        int countSubject = container.schedule.get(dayOfWeek).size();
        int numLastSubject = container.schedule.get(dayOfWeek).get(countSubject-1).num;

        scheduleItem emptyItem = new scheduleItem();
        emptyItem.subject_id = -1;

        for (int i = 0; i < numLastSubject; i++) {
            scheduleData.add(emptyItem);
        }

        int lastSubject = 0;
        for (scheduleItem i: container.schedule.get(dayOfWeek)) {
            if ((numWeek >= i.startWeek) && (i.endWeek >= numWeek)) {
                scheduleData.set(i.num - 1, i);
                lastSubject = i.num;
            }
        }

        int countDelete = numLastSubject-lastSubject;

        for (int i = numLastSubject-1; i > numLastSubject-1-countDelete; i--) {
            scheduleData.remove(i);
        }
    }

    @Override
    public void onDestroy() {

    }

}

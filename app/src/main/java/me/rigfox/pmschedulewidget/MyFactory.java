package me.rigfox.pmschedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class MyFactory implements RemoteViewsFactory {

    private scheduleContainer container;
    private ArrayList<scheduleItem> scheduleData;
    private Context context;

    MyFactory(Context ctx, Intent intent) {
        context = ctx;
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
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

            String week = "(" + startWeek + "-" + endWeek + ")\n";
            if (currentItem.isAlternation) {
                week += "черд.";
            }
            week += "\n" + classroom;

            String timeText = "";
            switch (currentItem.num) {
                case 1:
                    timeText = "8:00-\n9:35";
                    break;
                case 2:
                    timeText = "9:45-\n11:20";
                    break;
                case 3:
                    timeText = "11:30-\n13:05";
                    break;
                case 4:
                    timeText = "13:30-\n15:05";
                    break;
                case 5:
                    timeText = "15:15-\n16:50";
                    break;
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

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;

        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        if (currentWeek < 35) {
            currentWeek += 17 + 35;
        }

        int numWeek = currentWeek - 35;

        scheduleData.clear();

        scheduleItem emptyItem = new scheduleItem();
        emptyItem.subject_id = -1;

        int countSubject = container.schedule.get(dayOfWeek).size();

        int numLastSubject = 0;
        if (countSubject != 0) {
            numLastSubject = container.schedule.get(dayOfWeek).get(countSubject - 1).num;
        }

        for (int i = 0; i < numLastSubject; i++) {
            scheduleData.add(emptyItem);
        }

        int lastSubject = 0;
        for (scheduleItem i : container.schedule.get(dayOfWeek)) {
            if ((numWeek >= i.startWeek) && (i.endWeek >= numWeek)) {
                if (i.isAlternation) {
                    if (numWeek % 2 == i.startWeek % 2) {
                        scheduleData.set(i.num - 1, i);
                        lastSubject = i.num;
                    }
                } else {
                    scheduleData.set(i.num - 1, i);
                    lastSubject = i.num;
                }
            }
        }

        int countDelete = numLastSubject - lastSubject;

        for (int i = numLastSubject - 1; i > numLastSubject - 1 - countDelete; i--) {
            scheduleData.remove(i);
        }
    }

    @Override
    public void onDestroy() {

    }

}

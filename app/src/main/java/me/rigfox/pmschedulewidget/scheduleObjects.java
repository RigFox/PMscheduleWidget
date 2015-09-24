package me.rigfox.pmschedulewidget;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

class subjectItem {
    public String name;
    public String teacher;
    public String classroom;
}

class scheduleItem {
    public int num;
    public int subject_id;
    public int startWeek;
    public int endWeek;
}

class scheduleContainer {
    public ArrayList<subjectItem> subjects;
    public ArrayList<ArrayList<scheduleItem>> schedule;
}

class scheduleGenerator {
    public scheduleContainer container;

    scheduleGenerator(Context context) throws IOException {
        InputStream is = context.getResources().openRawResource(R.raw.schedule);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        String jsonString = writer.toString();

        Gson gson = new Gson();

        container = gson.fromJson(jsonString, scheduleContainer.class);
    }
}
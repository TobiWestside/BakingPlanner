package de.tobiasfraenzel.backplanner;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;

import de.tobiasfraenzel.backplanner.database.DatabaseHelper;

public class Plan {
    private String name;
    private Date startTime;
    final private ArrayList<Step> steps;
    private long id;

    public Plan() {
        name = "";
        steps = new ArrayList<>();
        id = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addStep(Step s) {
        steps.add(s);
    }

    public void removeStep(Step s) {
        steps.remove(s);
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }

    public Step getStepById(long id) {
        for (Step s : steps) {
            if (id == s.getId()) {
                return s;
            }
        }
        return null;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date st) {
        startTime = st;
    }

    public int getDuration() {
        int duration = 0;
        for (Step s : steps) {
            duration += s.getDuration();
        }
        return duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean save(Context c) {
        final DatabaseHelper db = new DatabaseHelper(c);
        final long id = db.savePlan(this);
        if (id != -1) {
            setId(id);
            return true;
        } else {
            return false;
        }
    }

    public boolean update(Context c) {
        final DatabaseHelper db = new DatabaseHelper(c);
        return db.updatePlan(this);
    }

    public static Plan load(Context c, long id) {
        final DatabaseHelper db = new DatabaseHelper(c);
        return db.loadPlan(id);
    }
}

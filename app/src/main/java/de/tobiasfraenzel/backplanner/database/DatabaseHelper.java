package de.tobiasfraenzel.backplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import de.tobiasfraenzel.backplanner.Plan;
import de.tobiasfraenzel.backplanner.Step;
import de.tobiasfraenzel.backplanner.utils.TimeUtils;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = "Back Planner";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "backplanner_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create tables
        sqLiteDatabase.execSQL(PlanTable.CREATE_TABLE);
        sqLiteDatabase.execSQL(StepTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // No upgrades yet
    }

    /*
     * Save a plan in the database
     */
    public long savePlan(Plan p) {
        Log.d(TAG, "Saving Plan: " + p.getName());
        Log.d(TAG, "with start time: " + p.getStartTime());

        final SQLiteDatabase db = this.getWritableDatabase();
        boolean success;
        // Save plan
        final ContentValues values = new ContentValues();
        // Explicitly set ID to keep it the same as it was before
        if (p.getId() > -1) {
            values.put(PlanTable.COLUMN_ID, p.getId());
        }
        values.put(PlanTable.COLUMN_NAME, p.getName());
        values.put(PlanTable.COLUMN_START_TIME, TimeUtils.formatTime(p.getStartTime()));
        final long id = db.insert(PlanTable.TABLE_NAME, null, values);
        success = id != -1;
        // Save steps
        int order = 0;
        for (Step s : p.getSteps()) {
            success = success && saveStep(s, id, order);
            order++;
        }

        Log.d(TAG, "Saved with ID: " + id);

        db.close();
        if (success) {
            return id;
        } else {
            return -1;
        }
    }

    /*
     * Save a step in the database
     */
    public boolean saveStep(Step s, long planId, int order) {
        Log.d(TAG, "Saving Step: " + s.getName());
        Log.d(TAG, "with duration: " + s.getDuration());
        Log.d(TAG, "for Plan with id: " + planId);
        Log.d(TAG, "on position: " + order);

        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues values = new ContentValues();
        // ID inserted automatically
        values.put(StepTable.COLUMN_PLAN_ID, planId);
        values.put(StepTable.COLUMN_NAME, s.getName());
        values.put(StepTable.COLUMN_DURATION, s.getDuration());
        values.put(StepTable.COLUMN_ORDER, order);
        final long id = db.insert(StepTable.TABLE_NAME, null, values);

        Log.d(TAG, "Saved with ID: " + id);

        db.close();
        return id != -1;
    }

    /*
     * Delete plan, then save it again
     * TODO: save, then delete, then update the id to be the same as before
     */
    public boolean updatePlan(Plan p) {
        if (deletePlan(p)) {
            return savePlan(p) != -1;
        } else {
            return false;
        }
    }

    /*
     * Delete a plan
     */
    public boolean deletePlan(Plan p) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(PlanTable.TABLE_NAME, PlanTable.COLUMN_ID + " = ?",
                new String[]{String.valueOf(p.getId())});
        db.close();
        Log.d(TAG, "Deleted Plan: " + p.getName());
        Log.d(TAG, "with ID: " + p.getId());
        Log.d(TAG, "Deleted rows (should be 1): " + deletedRows);
        deleteSteps(p.getId());

        return deletedRows == 1;
    }

    /*
     * Delete all steps of a plan
     */
    private void deleteSteps(long planId) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(StepTable.TABLE_NAME, StepTable.COLUMN_PLAN_ID + " = ?",
                new String[]{String.valueOf(planId)});
        Log.d(TAG, "Deleted Steps for Plan with ID: " + planId);
        Log.d(TAG, "Deleted rows: " + deletedRows);
        db.close();
    }

    /*
     * Load a plan from the database
     */
    public Plan loadPlan(long id) {
        Log.d(TAG, "Loading Plan with ID: " + id);

        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(PlanTable.TABLE_NAME,
                new String[]{PlanTable.COLUMN_ID, PlanTable.COLUMN_NAME,
                        PlanTable.COLUMN_START_TIME},
                PlanTable.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        assert cursor != null;
        Plan plan = new Plan();
        if (cursor.moveToFirst()) {
            plan.setName(cursor.getString(cursor.getColumnIndex(PlanTable.COLUMN_NAME)));
            final String startTimeString = cursor.getString(cursor.getColumnIndex(PlanTable.COLUMN_START_TIME));
            plan.setStartTime(TimeUtils.stringToTime(startTimeString));
        }

        Log.d(TAG, "Loaded Plan: " + plan.getName());
        Log.d(TAG, "with start time: " + plan.getStartTime());

        cursor.close();
        db.close();

        for (Step s : loadSteps(id)) {
            plan.addStep(s);
            Log.d(TAG, "+ Added Step: " + s.getName());
        }

        plan.setId(id);

        return plan;
    }

    /*
     * Load all steps of a plan from the database
     */
    private ArrayList<Step> loadSteps(long planId) {
        Log.d(TAG, "Loading Steps for planId: " + planId);

        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(StepTable.TABLE_NAME,
                new String[]{StepTable.COLUMN_ID, StepTable.COLUMN_NAME,
                        StepTable.COLUMN_DURATION},
                StepTable.COLUMN_PLAN_ID + "=?",
                new String[]{String.valueOf(planId)}, null, null, StepTable.COLUMN_ORDER, null);

        assert cursor != null;
        ArrayList<Step> steps = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Step step = new Step();
            step.setId(cursor.getLong(cursor.getColumnIndex(StepTable.COLUMN_ID)));
            step.setName(cursor.getString(cursor.getColumnIndex(StepTable.COLUMN_NAME)));
            step.setDuration(cursor.getInt(cursor.getColumnIndex(StepTable.COLUMN_DURATION)));
            steps.add(step);

            Log.d(TAG, "Loaded Step: " + step.getName());
            Log.d(TAG, "with ID: " + step.getId());
            Log.d(TAG, "and duration: " + step.getDuration());
        }
        cursor.close();
        db.close();

        return steps;
    }

    /*
     * Get the ids of all plans in the database
     */
    public ArrayList<Long> getAllPlanIds() {
        Log.d(TAG, "Loading all planIds");

        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(PlanTable.TABLE_NAME,
                new String[]{PlanTable.COLUMN_ID}, null, null, null, null, null);

        assert cursor != null;
        ArrayList<Long> ids = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ids.add(cursor.getLong(cursor.getColumnIndex(PlanTable.COLUMN_ID)));

            Log.d(TAG, "Loaded ID: " + ids.get(ids.size() - 1));
        }
        cursor.close();
        db.close();

        return ids;
    }
}

package de.tobiasfraenzel.backplanner.database;

public class StepTable {
    public static final String TABLE_NAME = "steps";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PLAN_ID = "plan_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_ORDER = "order_index";

    // Create table SQL
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PLAN_ID + " INTEGER,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_DURATION + " INTEGER,"
                    + COLUMN_ORDER + " INTEGER"
                    + ")";

    // Delete table SQL
    public static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

}

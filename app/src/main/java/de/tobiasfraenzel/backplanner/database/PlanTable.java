package de.tobiasfraenzel.backplanner.database;

public class PlanTable {
    public static final String TABLE_NAME = "plan";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_START_TIME = "starttime";

    // Create table SQL
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_START_TIME + " TEXT"
                    + ")";

    // Delete table SQL
    public static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
}

package model;

/**
 * Created by MOlawale on 6/11/2015.
 */
public class DbInfo {

    public static final String DATABASE_NAME = "studentsgrades.db";
    public static final int DATABASE_VERSION = 1;
    //For the user's table
    public static final String USER_TABLE = "users";
    public static final String GRADE_TABLE = "grades";

    //columns for the first table
    public static final String ID= "_id";
    public static final String NAME = "name";
    public static final String MATRIC_NO = "matric_no";
    public static final String PASSWORD = "password";
    public static final String DEPARTMENT = "department";
    public static final String EMAIL = "email";
    public static final String GP_SYSTEM = "gp_system";
    public static final String CURRENT_LEVEL = "current_level";

    //columns for the second table
    public static final String COURSE_CODE = "course_code";
    public static final String UNIT = "unit";
    public static final String SCORE = "score";
    public static final String GRADE = "grade";
    public static final String LEVEL = "level";
    public static final String SEMESTER= "semester";


    public static final String CREATE_TABLE =
            "CREATE TABLE " + USER_TABLE +
                    "("+ID + " INTEGER PRIMARY KEY," + NAME + " TEXT NOT NULL," + MATRIC_NO + " TEXT NOT NULL,"
                    + PASSWORD + " TEXT NOT NULL," + DEPARTMENT + " TEXT NOT NULL," +
                    EMAIL + " TEXT NOT NULL," + GP_SYSTEM + " TEXT NOT NULL," + CURRENT_LEVEL + " TEXT NOT NULL" +")";


    public static final String CREATE_SECOND_TABLE =
            "CREATE TABLE " + GRADE_TABLE +
                    "("+ID + " INTEGER PRIMARY KEY," + MATRIC_NO + " TEXT NOT NULL,"
                    + COURSE_CODE + " TEXT NOT NULL," + UNIT + " TEXT NOT NULL, " +SCORE + " TEXT NOT NULL, "+
                    GRADE + " TEXT NOT NULL, "  + LEVEL + " TEXT NOT NULL, " +SEMESTER + " TEXT NOT NULL" +")";
}

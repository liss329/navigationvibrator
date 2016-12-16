package com.example.terasaka.navi_vib;

/**
 * Created by Terasaka on 2016/09/27.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper{
    //Database名称
    private static final String DB_NAME_ITEM = "sample.db";

    //コンストラクタ
    //  public DatabaseOpenHelperItem(){

    //  }


    private static final String TABLE = "account";
    private static final int DB_VERSION = 1;
    private static final String CREATE_TABLE =
            "create table " + TABLE + "("
                    +"id integer primary key autoincrement,"
                    +"name varchar(30) not null,"
                    +"money integer);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME_ITEM, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

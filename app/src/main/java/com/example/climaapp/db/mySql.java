package com.example.climaapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.jetbrains.annotations.Nullable;

public class mySql extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NOMBRE="paises.db";
    public static final String TABLE_PAISES="t_paises";

    public mySql(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_PAISES + "(" +
                "id INTEGER PRIMARY KEY," +
                "nombre Text NOT NULL,"+
                "timeZone Text NOT NULL,"+
                "dia1 NUMERIC NOT NULL,"+
                "dia2 NUMERIC NOT NULL,"+
                "dia3 NUMERIC NOT NULL,"+
                "dia4 NUMERIC NOT NULL,"+
                "dia5 NUMERIC NOT NULL,"+
                "dia6 NUMERIC NOT NULL,"+
                "dia7 NUMERIC NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(" DROP TABLE " + TABLE_PAISES );
        onCreate(sqLiteDatabase);
    }
}

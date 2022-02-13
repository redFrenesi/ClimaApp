package com.example.climaapp.db;

import android.content.Context;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.climaapp.Paises.paises;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
public class paisesDb extends mySql {
  Context context;
    public paisesDb(@Nullable Context context) {
        super(context);
        this.context=context;
    }

    public long insertarPais(int idx,String nombre,String timeZone, double d1, double d2, double d3, double d4, double d5, double d6, double d7) {
        long id=0;
        try {
            mySql dbHelper = new mySql(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id", idx);
            values.put("nombre", nombre);
            values.put("timeZone", timeZone);
            values.put("dia1", d1);
            values.put("dia2", d2);
            values.put("dia3", d3);
            values.put("dia4", d4);
            values.put("dia5", d5);
            values.put("dia6", d6);
            values.put("dia7", d7);

            id = db.insert(TABLE_PAISES, null, values);

        } catch (Exception e) {
            e.toString();
        }
        return id;
    }

    public boolean UpdatePais(int id,String nombre,String timeZone, double d1, double d2, double d3, double d4, double d5, double d6, double d7)
    {
        boolean retorno;
        mySql dbHelper = new mySql(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try
        {
            db.execSQL("UPDATE "+ TABLE_PAISES + " SET nombre = '" + nombre + " ', timeZone = '"+timeZone+"',dia1 = '"+d1+"', dia2= '"+d2+"', dia3= '"+d3+"', dia4= '"+d4+"', dia5= '"+d5+"', dia6= '"+d6+"', dia7= '"+d7+ "' WHERE id='"+ id+ "'" );
            retorno = true;
        } catch (SQLException e) {
            retorno = false;
        }
        db.close();

        return retorno;

    }
    public ArrayList<paises> mostrarPaises()
    {
        mySql dbHelper = new mySql(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<paises> ListaPaises = new ArrayList<paises>();
        paises pais =null;
        Cursor cursorPais= null;
        cursorPais = db.rawQuery("SELECT * FROM " + TABLE_PAISES, null);
        if(cursorPais.moveToFirst())
        {
            do{
                pais=new paises();
                pais.setId(cursorPais.getInt(0));
                pais.setNombre(cursorPais.getString(1));
                pais.setTimeZone(cursorPais.getString(2));
                pais.setD1(cursorPais.getDouble(3));
                pais.setD2(cursorPais.getDouble(4));
                pais.setD3(cursorPais.getDouble(5));
                pais.setD4(cursorPais.getDouble(6));
                pais.setD5(cursorPais.getDouble(7));
                pais.setD6(cursorPais.getDouble(8));
                pais.setD7(cursorPais.getDouble(9));
                ListaPaises.add(pais);
            }
            while(cursorPais.moveToNext());
            cursorPais.close();


        }
        return ListaPaises;

    }
}

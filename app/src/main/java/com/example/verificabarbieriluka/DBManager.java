package com.example.verificabarbieriluka;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "db";
    static final int DATABASE_VERSION = 1;
    void createTables(SQLiteDatabase db){
        String query = "CREATE TABLE film ( id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, genere TEXT, annoproduzione INTEGER ,image TEXT )";
        db.execSQL(query);
    }
    @SuppressLint("Range")
    ArrayList<Bundle> getFilm(){
        ArrayList<Bundle> films = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM film";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            Bundle film = new Bundle();
            film.putInt("id", cursor.getInt(cursor.getColumnIndex("id")));
            film.putString("title", cursor.getString(cursor.getColumnIndex("title")));
            film.putString("genere", cursor.getString(cursor.getColumnIndex("genere")));
            film.putString("annoproduzione", cursor.getString(cursor.getColumnIndex("annoproduzione")));
            film.putString("image", cursor.getString(cursor.getColumnIndex("image")));

            films.add(film);
            cursor.moveToNext();
        }
        db.close();
        return films;
    }

    void saveFilm(Bundle film, boolean editMode){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put("title", film.getString("title"));
        data.put("genere", film.getString("genere"));
        data.put("annoproduzione", film.getString("annoproduzione"));
        data.put("image", film.getString("image"));

        if(editMode){
            db.update("film", data, "id=?", new String[]{String.valueOf(film.getInt("id"))});
        }else{
            db.insert("film", null, data);
        }
        db.close();
    }
    void deleteFilm(Bundle filmB){
        SQLiteDatabase db = getWritableDatabase();
        if(filmB == null){
            db.delete("film", null, null);
        }else{
            db.delete("film", "id=?", new String[]{String.valueOf(filmB.getInt("id"))});
        }
    }
    public DBManager(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

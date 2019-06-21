package com.cleanup.todoc.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import com.cleanup.todoc.model.Project;
import com.cleanup.todoc.model.Task;

@Database(entities = {Task.class, Project.class}, version = 1, exportSchema = false)
public abstract class SaveMyData extends RoomDatabase {

    // --- SINGLETON ---
    private static volatile SaveMyData INSTANCE;

    // --- DAO ---
    public abstract TaskDao taskDao();
    public abstract ProjectDao projectDao();

    // --- INSTANCE ---
    public static SaveMyData getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SaveMyData.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SaveMyData.class, "Database.db")
                            .addCallback(prepopulateDatabase())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ---

    private static Callback prepopulateDatabase(){
        return new Callback() {

            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {

                        super.onCreate(db);

                ContentValues contentValues = new ContentValues();
                contentValues.put("id", 1L);
                contentValues.put("name", "Projet Tartampion");
                contentValues.put("color", 0xFFEADAD1);

                db.insert("Project", OnConflictStrategy.IGNORE, contentValues);

                ContentValues contentValues2 = new ContentValues();
                contentValues2.put("id", 2L);
                contentValues2.put("name", "Projet Lucidia");
                contentValues2.put("color", 0xFFB4CDBA);

                db.insert("Project", OnConflictStrategy.IGNORE, contentValues2);

                ContentValues contentValues3 = new ContentValues();
                contentValues3.put("id", 3L);
                contentValues3.put("name", "Projet Circus");
                contentValues3.put("color", 0xFFA3CED2);

                db.insert("Project", OnConflictStrategy.IGNORE, contentValues3);
            }
        };
    }
}

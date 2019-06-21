package com.cleanup.todoc.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.cleanup.todoc.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM Task ")
    LiveData<List<Task>> getTasks();

    @Insert
    long insertTask(Task item);


    @Query("DELETE FROM Task WHERE id = :itemId")
    int deleteTask(long itemId);
}

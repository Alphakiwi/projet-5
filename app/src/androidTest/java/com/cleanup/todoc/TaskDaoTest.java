package com.cleanup.todoc;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cleanup.todoc.database.SaveMyData;
import com.cleanup.todoc.model.Project;
import com.cleanup.todoc.model.Task;
import com.cleanup.todoc.util.LiveDataTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TaskDaoTest {

    // FOR DATA
    private SaveMyData database;

    // DATA SET FOR TEST
    private static long PROJECT_ID = 1L;
    private static Project PROJECT_DEMO = new Project(PROJECT_ID,"Projet Tartampion", 0xFFEADAD1);

    private static Task NEW_TASK_VAISELLE = new Task(1,PROJECT_ID, "faire la vaiselle", 12-12-2019);
    private static Task NEW_TASK_SOL = new Task(2,PROJECT_ID, "laver le sol", 13-12-2019);
    private static Task NEW_TASK_ADMINISTRATIF = new Task(3,PROJECT_ID, "engager une nouvelle nettoyeuse", 14-12-2019);



    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void initDb() throws Exception {
        this.database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                SaveMyData.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() throws Exception {
        database.close();
    }




    @Test
    public void GetProjects() throws InterruptedException {
        this.database.projectDao().createProject(PROJECT_DEMO);
        List<Project> projects = LiveDataTestUtil.getValue(this.database.projectDao().getProjects());
        assertTrue(projects.get(0).getName().equals(PROJECT_DEMO.getName()) && projects.get(0).getId() == PROJECT_ID);
    }


    @Test
    public void getTasksWhenNoTaskInserted() throws InterruptedException {
        // TEST
        List<Task> tasks = LiveDataTestUtil.getValue(this.database.taskDao().getTasks());
        assertTrue(tasks.isEmpty());
    }


    @Test
    public void insertAndGetTask() throws InterruptedException {

        this.database.projectDao().createProject(PROJECT_DEMO);


        this.database.taskDao().insertTask(NEW_TASK_VAISELLE);
        this.database.taskDao().insertTask(NEW_TASK_SOL);
        this.database.taskDao().insertTask(NEW_TASK_ADMINISTRATIF);

        // TEST
        List<Task> tasks = LiveDataTestUtil.getValue(this.database.taskDao().getTasks());
        assertTrue(tasks.size() == 3);
    }


    @Test
    public void insertAndDeleteTask() throws InterruptedException {

        this.database.projectDao().createProject(PROJECT_DEMO);


        this.database.taskDao().insertTask(NEW_TASK_VAISELLE);
        Task taskAdded = LiveDataTestUtil.getValue(this.database.taskDao().getTasks()).get(0);
        this.database.taskDao().deleteTask(taskAdded.getId());

        //TEST
        List<Task> tasks = LiveDataTestUtil.getValue(this.database.taskDao().getTasks());
        assertTrue(tasks.isEmpty());
    }
}

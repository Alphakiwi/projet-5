package com.cleanup.todoc.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cleanup.todoc.R;
import com.cleanup.todoc.database.TaskDao;
import com.cleanup.todoc.injections.Injection;
import com.cleanup.todoc.injections.ViewModelFactory;
import com.cleanup.todoc.model.Project;
import com.cleanup.todoc.model.Task;
import com.cleanup.todoc.todolist.TaskViewModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.preference.PreferenceManager.*;

/**
 * <p>Home activity of the application which is displayed when the user opens the app.</p>
 * <p>Displays the list of tasks.</p>
 *
 * @author Gaëtan HERFRAY
 */
public class MainActivity extends AppCompatActivity implements TasksAdapter.DeleteTaskListener{
    /**
     * List of all projects available in the application
     */
    private final Project[] allProjects = Project.getAllProjects();

    /**
     * List of all current tasks of the application
     */
    @NonNull
    private int tasks = 0 ;

    /**
     * The adapter which handles the list of tasks
     */
    private  TasksAdapter adapter = new TasksAdapter(this); ;

    /**
     * The sort method to be used to display tasks
     */
    @NonNull
    private SortMethod sortMethod = SortMethod.NONE;

    /**
     * Dialog to create a new task
     */
    @Nullable
    public AlertDialog dialog = null;

    /**
     * EditText that allows user to set the name of a task
     */
    @Nullable
    private EditText dialogEditText = null;

    /**
     * Spinner that allows the user to associate a project to a task
     */
    @Nullable
    private Spinner dialogSpinner = null;

    /**
     * The RecyclerView which displays the list of tasks
     */
    // Suppress warning is safe because variable is initialized in onCreate
    @SuppressWarnings("NullableProblems")
    @NonNull
    private RecyclerView listTasks;

    /**
     * The TextView displaying the empty state
     */
    // Suppress warning is safe because variable is initialized in onCreate
    @SuppressWarnings("NullableProblems")
    @NonNull
    private TextView lblNoTasks;

    private TaskViewModel taskViewModel;

    List<Task> mlistTask ;

    SharedPreferences sharedPref;




    public final static String FAVORITE_SORTING_METHOD = "OLD_FIRST";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        listTasks = findViewById(R.id.list_tasks);

        lblNoTasks = findViewById(R.id.lbl_no_task);
        lblNoTasks.setVisibility(View.INVISIBLE);


        listTasks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listTasks.setAdapter(adapter);



        this.configureViewModel();


        findViewById(R.id.fab_add_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTaskDialog();
            }
        });

        sharedPref = getBaseContext().getSharedPreferences("TheFileName",
                        Context.MODE_PRIVATE);

        String sortMethodString = sharedPref.getString(FAVORITE_SORTING_METHOD, "OLD_FIRST");


        switch (sortMethodString) {
            case "ALPHABETICAL":
                sortMethod = SortMethod.ALPHABETICAL;
                break;
            case "ALPHABETICAL_INVERTED":
                sortMethod = SortMethod.ALPHABETICAL_INVERTED;
                break;
            case "OLD_FIRST":
                sortMethod = SortMethod.OLD_FIRST;
                break;
            case "RECENT_FIRST":
                sortMethod = SortMethod.RECENT_FIRST;
                break;
        }


        this.getTasks();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        SharedPreferences.Editor editor = sharedPref.edit();

        int id = item.getItemId();

        if (id == R.id.filter_alphabetical) {
            sortMethod = SortMethod.ALPHABETICAL;
            editor.putString(FAVORITE_SORTING_METHOD, "ALPHABETICAL");
        } else if (id == R.id.filter_alphabetical_inverted) {
            sortMethod = SortMethod.ALPHABETICAL_INVERTED;
            editor.putString(FAVORITE_SORTING_METHOD, "ALPHABETICAL_INVERTED");
        } else if (id == R.id.filter_oldest_first) {
            sortMethod = SortMethod.OLD_FIRST;
            editor.putString(FAVORITE_SORTING_METHOD, "OLD_FIRST");
        } else if (id == R.id.filter_recent_first) {
            sortMethod = SortMethod.RECENT_FIRST;
            editor.putString(FAVORITE_SORTING_METHOD, "RECENT_FIRST");

        }

        editor.apply();


        updateTasks();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteTask(Task task) {
        this.taskViewModel.deleteTask(task.getId());
        tasks -= 1;

        updateTasks();

    }

    /**
     * Called when the user clicks on the positive button of the Create Task Dialog.
     *
     * @param dialogInterface the current displayed dialog
     */
    private void onPositiveButtonClick(DialogInterface dialogInterface) {
        // If dialog is open
        if (dialogEditText != null && dialogSpinner != null) {
            // Get the name of the task
            String taskName = dialogEditText.getText().toString();

            // Get the selected project to be associated to the task
            Project taskProject = null;
            if (dialogSpinner.getSelectedItem() instanceof Project) {
                taskProject = (Project) dialogSpinner.getSelectedItem();
            }

            // If a name has not been set
            if (taskName.trim().isEmpty()) {
                dialogEditText.setError(getString(R.string.empty_task_name));
            }
            // If both project and name of the task have been set
            else if (taskProject != null) {

                Task task = new Task(0,
                        taskProject.getId(),
                        taskName,
                        new Date().getTime()
                );

                addTask(task);


                dialogInterface.dismiss();
            }
            // If name has been set, but project has not been set (this should never occur)
            else{
                dialogInterface.dismiss();
            }
        }
        // If dialog is aloready closed
        else {
            dialogInterface.dismiss();
        }
    }

    /**
     * Shows the Dialog for adding a Task
     */
    private void showAddTaskDialog() {
        final AlertDialog dialog = getAddTaskDialog();

        dialog.show();

        dialogEditText = dialog.findViewById(R.id.txt_task_name);
        dialogSpinner = dialog.findViewById(R.id.project_spinner);

        populateDialogSpinner();
    }

    /**
     * Adds the given task to the list of created tasks.
     *
     * @param task the task to be added to the list
     */
    private void addTask(@NonNull Task task) {
        this.taskViewModel.createTask(task);
        tasks += 1;
        updateTasks();
    }

    /**
     * Updates the list of tasks in the UI
     */
    private void updateTasks() {


        if (tasks == 0) {
            lblNoTasks.setVisibility(View.VISIBLE);
            listTasks.setVisibility(View.GONE);
        } else {
            lblNoTasks.setVisibility(View.GONE);
            listTasks.setVisibility(View.VISIBLE);

            switch (sortMethod) {
                case ALPHABETICAL:
                    Collections.sort(mlistTask, new Task.TaskAZComparator());
                    break;
                case ALPHABETICAL_INVERTED:
                    Collections.sort(mlistTask, new Task.TaskZAComparator());
                    break;
                case RECENT_FIRST:
                    Collections.sort(mlistTask, new Task.TaskRecentComparator());
                    break;
                case OLD_FIRST:
                    Collections.sort(mlistTask, new Task.TaskOldComparator());
                    break;

            }



            adapter.updateTasks(mlistTask);
        }
    }

    /**
     * Returns the dialog allowing the user to create a new task.
     *
     * @return the dialog allowing the user to create a new task
     */
    @NonNull
    private AlertDialog getAddTaskDialog() {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Dialog);

        alertBuilder.setTitle(R.string.add_task);
        alertBuilder.setView(R.layout.dialog_add_task);
        alertBuilder.setPositiveButton(R.string.add, null);
        alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogEditText = null;
                dialogSpinner = null;
                dialog = null;
            }
        });

        dialog = alertBuilder.create();

        // This instead of listener to positive button in order to avoid automatic dismiss
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        onPositiveButtonClick(dialog);
                    }
                });
            }
        });

        return dialog;
    }

    /**
     * Sets the data of the Spinner with projects to associate to a new task
     */
    private void populateDialogSpinner() {
        final ArrayAdapter<Project> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allProjects);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (dialogSpinner != null) {
            dialogSpinner.setAdapter(adapterSpinner);
        }
    }

    /**
     * List of all possible sort methods for task
     */
    private enum SortMethod {
        /**
         * Sort alphabetical by name
         */
        ALPHABETICAL,
        /**
         * Inverted sort alphabetical by name
         */
        ALPHABETICAL_INVERTED,
        /**
         * Lastly created first
         */
        RECENT_FIRST,
        /**
         * First created first
         */
        OLD_FIRST,
        /**
         * No sort
         */
        NONE
    }

    private void configureViewModel(){
        ViewModelFactory mViewModelFactory = Injection.provideViewModelFactory(this);
        this.taskViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TaskViewModel.class);
        this.taskViewModel.init();

    }

    // 3 - Get all tasks
    private void getTasks(){
        this.taskViewModel.getTasks().observe(this, this::updateTasksList);
    }

    private void updateTasksList(List<Task> listTask) {

        this.adapter.updateTasks(listTask);
        mlistTask = listTask;
        tasks = adapter.getItemCount();
        updateTasks();

    }

}

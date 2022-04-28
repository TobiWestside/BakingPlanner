package de.tobiasfraenzel.backplanner.editPlan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.tobiasfraenzel.backplanner.MainActivity;
import de.tobiasfraenzel.backplanner.Plan;
import de.tobiasfraenzel.backplanner.R;
import de.tobiasfraenzel.backplanner.Step;
import de.tobiasfraenzel.backplanner.showPlan.ShowPlan;
import de.tobiasfraenzel.backplanner.utils.TimeUtils;

public class EditPlan extends AppCompatActivity {

    private final String TAG = "Back Planner";
    private Plan plan;
    private EditPlanViewModel viewModel;

    /*
     * Save changes when the user is leaving the activity
     * https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "On Pause");
        save();
    }

    /*
     * Update the plan in the database
     */
    private void save() {
        plan.update(EditPlan.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan);

        // Get intent to indicate whether it's a new plan or the user edits an existing one
        long planId = getIntent().getExtras().getLong("planId", -1);

        // Create view model to store the plan
        viewModel = new ViewModelProvider(this).get(EditPlanViewModel.class);

        if (planId == -1) {
            initializeEmptyPlan();
        } else {
            initializeExistingPlan(planId);
            // Hide keyboard, so the user can see the whole plan and choose what to edit
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        final EditText titleInput = findViewById(R.id.title_input);
        // Update plan title when the user changed it
        titleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                plan.setName(titleInput.getText().toString());
            }
        });

        // Add new empty step to the plan when the FAB is clicked
        final FloatingActionButton fab = findViewById(R.id.fab_add_step);
        fab.setOnClickListener(view -> addEmptyStep());
    }

    /*
     * Create a new plan
     */
    private void initializeEmptyPlan() {
        // Create new temporary plan and save it in the DB directly.
        // This way, the viewModel can load and handle it
        final Plan tempPlan = new Plan();
        tempPlan.setStartTime(TimeUtils.getCurrentTime());
        tempPlan.save(EditPlan.this);
        viewModel.loadPlan(this, tempPlan.getId());
        plan = viewModel.getPlan().getValue();
        addEmptyStep();
        setTitle(R.string.new_plan);
        calculateEndTime();

        // Focus the name input and show keyboard
        final EditText planNameInput = findViewById(R.id.title_input);
        planNameInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(planNameInput, InputMethodManager.SHOW_IMPLICIT);
    }

    /*
     * Load an existing plan
     */
    private void initializeExistingPlan(long id) {
        viewModel.loadPlan(this, id);
        plan = viewModel.getPlan().getValue();
        Log.d(TAG, "Activity loaded plan name: " + plan.getName());

        final EditText titleInput = findViewById(R.id.title_input);
        titleInput.setText(plan.getName());
        for (Step s : plan.getSteps()) {
            addStep(s);
        }
        setTitle(R.string.edit_plan);
        calculateEndTime();
    }

    /*
     * Set the end time of the plan
     */
    private void calculateEndTime() {
        final EditText endTimeView = findViewById(R.id.end_time_input);
        endTimeView.setText(TimeUtils.getEndTimeOfPlan(plan));
    }

    /*
     * Add a new step
     */
    private void addEmptyStep() {
        final Step s = new Step();
        plan.addStep(s);
        addStep(s);
    }

    /*
     * Add an existing step
     */
    private void addStep(Step step) {
        final LinearLayout allStepsContainer = findViewById(R.id.edit_all_steps_container);

        // Fragment container
        FragmentContainerView fragmentContainer = new FragmentContainerView(this);
        fragmentContainer.setId(View.generateViewId());
        allStepsContainer.addView(fragmentContainer);

        // Add views for the step via fragment manager
        Bundle bundle = new Bundle();
        bundle.putString("stepName", step.getName());
        bundle.putInt("stepDuration", step.getDuration());
        final int stepIndex = plan.getSteps().indexOf(step);
        if (stepIndex == 0) {
            bundle.putString("stepStartTime", TimeUtils.formatTime(plan.getStartTime()));
        } else {
            bundle.putString("stepStartTime", TimeUtils.formatTime(TimeUtils.getStartTimeOfStep(step, plan)));
        }
        bundle.putInt("stepIndex", stepIndex);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(fragmentContainer.getId(), EditStepFragment.class, bundle)
                .commit();

        fragmentContainer.setOnLongClickListener(view -> {
            new AlertDialog.Builder(EditPlan.this)
                    .setTitle(R.string.delete_step_title)
                    .setMessage(R.string.delete_step_question)
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete
                            plan.removeStep(step);
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .remove(fragmentManager.findFragmentById(fragmentContainer.getId()))
                                    .commit();
                            allStepsContainer.removeView(fragmentContainer);
                            TimeUtils.updateEndTimeForAllSteps(plan, allStepsContainer, findViewById(R.id.end_time_input));
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        final Intent switchToShowPlanActivityIntent = new Intent(EditPlan.this, ShowPlan.class);
        switchToShowPlanActivityIntent.putExtra("planId", plan.getId());
        EditPlan.this.startActivity(switchToShowPlanActivityIntent);
        finish();
    }
}
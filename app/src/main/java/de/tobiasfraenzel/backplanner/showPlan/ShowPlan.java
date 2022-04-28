package de.tobiasfraenzel.backplanner.showPlan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.tobiasfraenzel.backplanner.editPlan.EditPlan;
import de.tobiasfraenzel.backplanner.Plan;
import de.tobiasfraenzel.backplanner.R;
import de.tobiasfraenzel.backplanner.Step;
import de.tobiasfraenzel.backplanner.utils.TimeUtils;

public class ShowPlan extends AppCompatActivity {

    private final String TAG = "Back Planner";

    private Plan plan;

    private ShowPlanViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_plan);

        // Get intent to indicate whether new plan or edit existing
        long planId = getIntent().getExtras().getLong("planId", -1);
        if (planId == -1) {
            // If the plan id is invalid, start the edit activity, so the user can create a new plan
            startEditActivity(planId);
        } else {
            // If the plan id is valid, display the plan
            initializePlan(planId);
            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        // Start the edit activity when the FAB is clicked
        FloatingActionButton fab = findViewById(R.id.fab_edit_plan);
        fab.setOnClickListener(view -> startEditActivity(planId));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    // Start the edit activity
    private void startEditActivity(long planId) {
        final Intent switchToEditPlanActivityIntent = new Intent(ShowPlan.this, EditPlan.class);
        switchToEditPlanActivityIntent.putExtra("planId", planId);
        ShowPlan.this.startActivity(switchToEditPlanActivityIntent);
        finish();
    }

    // Load plan from database and display it
    private void initializePlan(long id) {
        // Create view model
        viewModel = new ViewModelProvider(this).get(ShowPlanViewModel.class);
        // Load plan
        viewModel.loadPlan(this, id);
        plan = viewModel.getPlan().getValue();
        // Display steps
        for (Step s : plan.getSteps()) {
            addStep(s);
        }
        setTitle(plan.getName());
        // Set end time
        TextView endTimeView = findViewById(R.id.end_time_view);
        endTimeView.setText(TimeUtils.getEndTimeOfPlan(plan));
    }

    // Display a step
    private void addStep(Step step) {
        // Create Layouts and Views for the Step
        LinearLayout allStepsContainerBlau = findViewById(R.id.all_steps_container_neu);

        // Fragment container
        FragmentContainerView fragmentContainer = new FragmentContainerView(this);
        fragmentContainer.setId(View.generateViewId());
        allStepsContainerBlau.addView(fragmentContainer);

        // Add views via fragment manager
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFirstStep", plan.getSteps().indexOf(step) == 0);
        bundle.putString("stepStartTime", TimeUtils.formatTime(TimeUtils.getStartTimeOfStep(step, plan)));
        bundle.putLong("stepId", step.getId());

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(fragmentContainer.getId(), ViewStepFragment.class, bundle)
                .commit();
    }
}
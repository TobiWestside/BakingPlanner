package de.tobiasfraenzel.backplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.tobiasfraenzel.backplanner.editPlan.EditPlan;
import de.tobiasfraenzel.backplanner.showPlan.ShowPlan;
import de.tobiasfraenzel.backplanner.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Back Planner";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            displayPlans();
            setTitle(getString(R.string.my_recipes));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Also call displayPlans() on resume, so plans are updated
        // when a user returns to the activity
        displayPlans();
    }

    /*
     * Load and display all plans
     */
    private void displayPlans() {
        setContentView(R.layout.activity_main);
        // Load and display all plans
        final DatabaseHelper db = new DatabaseHelper(this);
        final ArrayList<Long> planIds = db.getAllPlanIds();
        for (final long id : planIds) {
            displayPlan(id, db);
        }

        // Start the edit activity with id -1 when the FAB is clicked,
        // because the user wants to create a new plan
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            final Intent switchToEditPlanActivityIntent = new Intent(MainActivity.this, EditPlan.class);
            switchToEditPlanActivityIntent.putExtra("planId", -1);
            MainActivity.this.startActivity(switchToEditPlanActivityIntent);
        });
    }

    /*
     * Display one plan
     */
    private void displayPlan(long planId, DatabaseHelper db) {
        // Container for all plans
        final LinearLayout allPlansContainer = findViewById(R.id.all_plans_container);
        // Fragment container
        FragmentContainerView fragmentContainer = new FragmentContainerView(this);
        fragmentContainer.setId(View.generateViewId());
        allPlansContainer.addView(fragmentContainer);

        // Load a plan from database
        final Plan plan = db.loadPlan(planId);

        // Add views via fragment manager
        Bundle bundle = new Bundle();
        bundle.putString("planName", plan.getName());
        bundle.putInt("planDuration", plan.getDuration());

        // Divider line between the plans
        final View dividerLine = new View(this);

        // Add views view fragment manager
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(fragmentContainer.getId(), PlanFragment.class, bundle)
                .commit();

        // Show plan when a user clicks on it
        fragmentContainer.setOnClickListener(view -> {
            final Intent switchToShowPlanActivityIntent = new Intent(MainActivity.this, ShowPlan.class);
            switchToShowPlanActivityIntent.putExtra("planId", planId);
            MainActivity.this.startActivity(switchToShowPlanActivityIntent);
        });

        // Show "delete plan" dialog on long click
        fragmentContainer.setOnLongClickListener(view -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.delete_title)
                    .setMessage(R.string.delete_question)
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete and remove views
                            db.deletePlan(plan);
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .remove(fragmentManager.findFragmentById(fragmentContainer.getId()))
                                    .commit();
                            allPlansContainer.removeView(dividerLine);
                            allPlansContainer.removeView(fragmentContainer);
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        });

        // Add divider line
        allPlansContainer.addView(dividerLine);
        final LinearLayout.LayoutParams dividerLineParams = (LinearLayout.LayoutParams)dividerLine.getLayoutParams();
        dividerLineParams.setMargins(0, 50, 0, 50);
        dividerLineParams.width = 300;
        dividerLineParams.height = 2;
        dividerLineParams.gravity = Gravity.CENTER_HORIZONTAL;
        dividerLine.setLayoutParams(dividerLineParams);
        dividerLine.setBackgroundColor(getResources().getColor(R.color.dark_text));
    }
}
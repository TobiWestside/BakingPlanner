package de.tobiasfraenzel.backplanner.showPlan;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import de.tobiasfraenzel.backplanner.Plan;

public class ShowPlanViewModel extends ViewModel {
    private MutableLiveData<Plan> plan;

    // Load the plan from the database
    public void loadPlan(Context c, long id) {
        if (plan == null) {
            plan = new MutableLiveData<>();
        }
        plan.setValue(Plan.load(c, id));
    }

    public MutableLiveData<Plan> getPlan() {
        if (plan == null) {
            plan = new MutableLiveData<Plan>();
        }
        return plan;
    }
}

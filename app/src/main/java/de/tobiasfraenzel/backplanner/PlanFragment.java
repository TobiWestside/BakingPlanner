package de.tobiasfraenzel.backplanner;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import de.tobiasfraenzel.backplanner.utils.TimeUtils;

public class PlanFragment extends Fragment {
    public PlanFragment() {
        super(R.layout.fragment_plan);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Set name and duration of the plan
        final String planName = requireArguments().getString("planName");
        final int planDuration = requireArguments().getInt("planDuration");

        final TextView planNameView = view.findViewById(R.id.plan_name_view);
        planNameView.setText(planName);
        final TextView planDurationView = view.findViewById(R.id.plan_duration_view);
        planDurationView.setText(TimeUtils.convertMinToDisplayFormat(planDuration));
    }
}

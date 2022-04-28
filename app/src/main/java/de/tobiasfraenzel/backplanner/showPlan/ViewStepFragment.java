package de.tobiasfraenzel.backplanner.showPlan;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import de.tobiasfraenzel.backplanner.Plan;
import de.tobiasfraenzel.backplanner.R;
import de.tobiasfraenzel.backplanner.Step;
import de.tobiasfraenzel.backplanner.utils.TimeUtils;

public class ViewStepFragment extends Fragment {
    private final String TAG = "Back Planner";
    private ShowPlanViewModel viewModel;
    private Plan plan;

    public ViewStepFragment() {
        super(R.layout.fragment_view_step);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(ShowPlanViewModel.class);
        plan = viewModel.getPlan().getValue();
        Log.d(TAG, "Fragment loaded plan name: " + plan.getName());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Load step data
        final boolean isFirstStep = requireArguments().getBoolean("isFirstStep");
        final String stepStartTime = requireArguments().getString("stepStartTime");
        final long stepId = requireArguments().getLong("stepId");
        Step step = plan.getStepById(stepId);
        final String stepName = step.getName();
        final int stepDuration = step.getDuration();

        // Display step data in views
        final TextView stepNameView = view.findViewById(R.id.step_title_view);
        stepNameView.setText(stepName);
        final TextView stepDurationView = view.findViewById(R.id.step_duration_view);
        stepDurationView.setText(TimeUtils.convertMinToDisplayFormat(stepDuration));
        final TextView startTimeView = view.findViewById(R.id.start_time_view);
        if (isFirstStep) {
            // The start time of the first step is also the start time of the whole plan,
            // therefore increase font size a bit
            startTimeView.setTextSize(25);
        }
        startTimeView.setText(stepStartTime);

        Button nowBtn = view.findViewById(R.id.now_button);
        nowBtn.setTransformationMethod(null);

        nowBtn.setOnClickListener(view1 -> {
            // Calculate end time from step
            final int indexOfStep = plan.getSteps().indexOf(step);
            if (indexOfStep >= 0) {
                final LinearLayout allStepsContainer = getActivity().findViewById(R.id.all_steps_container_neu);
                final TextView endTimeView = getActivity().findViewById(R.id.end_time_view);
                TimeUtils.updateEndTimeFromStepNow(plan, indexOfStep, allStepsContainer, endTimeView);
            } else {
                Log.e(TAG, "Step " + stepName +" is not in Plan " + plan.getName());
            }
        });
    }
}

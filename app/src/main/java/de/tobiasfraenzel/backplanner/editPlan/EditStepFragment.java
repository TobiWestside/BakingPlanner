package de.tobiasfraenzel.backplanner.editPlan;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Date;

import de.tobiasfraenzel.backplanner.Plan;
import de.tobiasfraenzel.backplanner.R;
import de.tobiasfraenzel.backplanner.Step;
import de.tobiasfraenzel.backplanner.utils.TimeUtils;

public class EditStepFragment extends Fragment {
    private final String TAG = "Back Planner";
    private EditPlanViewModel viewModel;
    private Plan plan;

    public EditStepFragment() {
        super(R.layout.fragment_edit_step);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(EditPlanViewModel.class);
        plan = viewModel.getPlan().getValue();
        Log.d(TAG, "Fragment loaded plan name: " + plan.getName());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "Fragment loaded plan name: " + plan.getName());
        // Load step data
        final String stepStartTime = requireArguments().getString("stepStartTime", null);
        final int stepIndex = requireArguments().getInt("stepIndex");
        final Step step = plan.getSteps().get(stepIndex);
        final String stepName = step.getName();
        final int stepDuration = step.getDuration();

        // Display step data in views
        final EditText stepNameInput = view.findViewById(R.id.step_title_input);
        stepNameInput.setText(stepName);

        final String[] hoursAndMinutes = TimeUtils.convertMinToHoursAndMins(stepDuration);
        final EditText stepDurationHourInput = view.findViewById(R.id.step_duration_hour_input);
        stepDurationHourInput.setText(hoursAndMinutes[0]);
        final EditText stepDurationMinuteInput = view.findViewById(R.id.step_duration_minute_input);
        stepDurationMinuteInput.setText(hoursAndMinutes[1]);

        final Button nowBtn = view.findViewById(R.id.edit_now_button);
        // Don't force the button to be all caps
        nowBtn.setTransformationMethod(null);

        final LinearLayout allStepsContainer = getActivity().findViewById(R.id.edit_all_steps_container);
        final EditText startTimeInput = view.findViewById(R.id.start_time_input);
        final EditText endTimeInput = getActivity().findViewById(R.id.end_time_input);

        if (stepStartTime != null && !stepStartTime.isEmpty()) {
            startTimeInput.setText(stepStartTime);
        }

        // The first step gets special handling:
        // - The start time of the first step is also the start time of the whole plan,
        //   therefore increase font size a bit and update the start time of the plan if it's
        //   changed.
        // - Only the start time of the first step can be changed directly by the user, therefore
        //   only add a listener in this case.
        // - Only the now button of the first step is displayed, therefore, only add a listener
        //   in this case.
        // - Only add a listener for the end time input if this is the first step, because if the
        //   user changes the end time, it should only update the start time of the first step, not
        //   of all steps.
        if (stepIndex == 0) {
            startTimeInput.setTextSize(25);
            startTimeInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    final Date startTime = TimeUtils.stringToTime(startTimeInput.getText().toString());
                    if (startTime != null) {
                        plan.setStartTime(startTime);
                        if (!TimeUtils.getEndTimeOfPlan(plan).equals(endTimeInput.getText().toString())) {
                            Log.d(TAG, "Updating end time from step: " + stepName);
                            // Only update from the second step, because the first step is the one
                            // that was modified and therefore doesn't need an update
                            TimeUtils.updateEndTimeFromSecondStep(plan, allStepsContainer, endTimeInput);
                        }
                    }
                }
            });

            nowBtn.setOnClickListener(view1 -> {
                startTimeInput.setText(TimeUtils.formatTime(TimeUtils.getCurrentTime()));
            });

            endTimeInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Convert string to date and back, to handle cases like 22:4,
                    // that will be converted to 22:04
                    final Date calculatedStartTime = TimeUtils.calculateStartTime(plan, endTimeInput);
                    final Date startTimeFromInput = TimeUtils.stringToTime(startTimeInput.getText().toString());
                    final Date endTime = TimeUtils.stringToTime(endTimeInput.getText().toString());
                    // Proceed only if the start time, the end time and the calculated start time are valid
                    if (calculatedStartTime != null && startTimeFromInput != null && endTime != null) {
                        // Only update the start time if
                        // the currently set start time of the plan is different from
                        // the time in the input
                        // or
                        // if the newly calculated start time is different from
                        // the time in the input
                        if (!plan.getStartTime().toString().equals(startTimeFromInput.toString())) {
                            // Update the start time of the plan
                            TimeUtils.updateStartTime(plan, endTimeInput, startTimeInput);
                        } else if (!calculatedStartTime.toString().equals(startTimeFromInput.toString())) {
                            final String endTimeString = TimeUtils.formatTime(endTime);
                            if (endTimeString.equals(endTimeInput.getText().toString())) {
                                // Update the start time of the plan
                                TimeUtils.updateStartTime(plan, endTimeInput, startTimeInput);
                            }
                        }
                    }
                }
            });
        } else {
            // If it's not the first step, make the start time view non-editable and remove the
            // now button
            disableInput(startTimeInput);
            ((ViewGroup) nowBtn.getParent()).removeView(nowBtn);
        }

        stepNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                // Update the name of the step
                step.setName(stepNameInput.getText().toString());
            }
        });

        // When the text in one of the duration inputs is changed, recalculate the duration of the
        // step, then update all following steps and the end time
        stepDurationHourInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                final String newTimeHoursString = stepDurationHourInput.getText().toString();
                final String newTimeMinutesString = stepDurationMinuteInput.getText().toString();
                final int newTimeHours = newTimeHoursString.isEmpty() ? 0 : Integer.parseInt(newTimeHoursString);
                final int newTimeMinutes = newTimeMinutesString.isEmpty() ? 0 : Integer.parseInt(newTimeMinutesString);
                step.setDuration(TimeUtils.convertHoursAndMinsToMins(newTimeHours, newTimeMinutes));
                TimeUtils.updateEndTimeForAllSteps(plan, allStepsContainer, endTimeInput);
            }
        });

        stepDurationMinuteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                final String newTimeHoursString = stepDurationHourInput.getText().toString();
                final String newTimeMinutesString = stepDurationMinuteInput.getText().toString();
                final int newTimeHours = newTimeHoursString.isEmpty() ? 0 : Integer.parseInt(newTimeHoursString);
                final int newTimeMinutes = newTimeMinutesString.isEmpty() ? 0 : Integer.parseInt(newTimeMinutesString);
                step.setDuration(TimeUtils.convertHoursAndMinsToMins(newTimeHours, newTimeMinutes));
                TimeUtils.updateEndTimeForAllSteps(plan, allStepsContainer, endTimeInput);
            }
        });
    }

    /*
     * Disable the input of an EditText to make it similar to a TextView
     */
    private void disableInput(EditText et) {
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setClickable(false);
        et.setBackground(null);
        et.setInputType(InputType.TYPE_NULL);
        et.setTextIsSelectable(false);
        et.setOnKeyListener((v, keyCode, event) -> {
            return true;  // Blocks input from hardware keyboards.
        });
    }
}

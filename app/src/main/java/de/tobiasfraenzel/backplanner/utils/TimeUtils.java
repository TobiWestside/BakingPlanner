package de.tobiasfraenzel.backplanner.utils;

import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentContainerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.tobiasfraenzel.backplanner.Plan;
import de.tobiasfraenzel.backplanner.Step;

public class TimeUtils {

    private static final String TAG = "Back Planner";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    public static Date getCurrentTime() {
        return Calendar.getInstance().getTime();
    }

    /*
     * Parse date from String s using the dateFormat
     */
    public static Date stringToTime(String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Format date as a string
     */
    public static String formatTime(Date d) {
        return dateFormat.format(d);
    }

    /*
     * Take hours and minutes as inputs and return the corresponding sum in minutes
     */
    public static int convertHoursAndMinsToMins(int hours, int minutes) {
        return (hours * 60) + minutes;
    }

    /*
     * Take an amount of time in minutes and return the same amount of time in hours and minutes
     */
    public static String[] convertMinToHoursAndMins(long minutes) {
        int hours = 0;
        while (minutes >= 60) {
            hours++;
            minutes -= 60;
        }
        String minutesString = String.valueOf(minutes);
        if (minutes == 0) minutesString += "0";
        final String[] returnArray = {String.valueOf(hours), minutesString};
        return returnArray;
    }

    /*
     * Take an amount of time in minutes and return the corresponding formatted String
     */
    public static String convertMinToDisplayFormat(long minutes) {
        final String[] hoursAndMins = convertMinToHoursAndMins(minutes);
        final String hours = hoursAndMins[0];
        final String mins = hoursAndMins[1];
        String returnStr = "";
        if (Integer.parseInt(hours) > 0) {
            returnStr += hours + " h";
        }
        if (Integer.parseInt(mins) > 0) {
            if (!returnStr.isEmpty()) {
                // Add separator space between hours and minutes
                returnStr += " ";
            }
            returnStr += mins + " min";
        }
        if (returnStr.isEmpty()) {
            returnStr = "0 min";
        }
        return returnStr;
    }

    /*
     * Calculate the end time of a plan
     */
    public static String getEndTimeOfPlan(Plan plan) {
        final Date startTime = plan.getStartTime();
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.add(Calendar.MINUTE, plan.getDuration());
        return formatTime(c.getTime());
    }

    /*
     * Update the end time and the start times of all steps
     */
    public static void updateEndTimeForAllSteps(Plan plan, LinearLayout allStepsContainer, TextView endTimeView) {
        if (plan.getSteps().size() > 0) {
            updateEndTimeFromStep(plan.getStartTime(),  plan, 0, allStepsContainer, endTimeView);
        } else {
            endTimeView.setText(dateFormat.format(plan.getStartTime()));
        }
    }

    /*
     * Update the end time and the start times of all steps except the first
     */
    public static void updateEndTimeFromSecondStep(Plan plan, LinearLayout allStepsContainer, TextView endTimeView) {
        Log.d(TAG, "updateEndTimeFromSecondStep");
        // Calculate start time of second step
        Calendar c = Calendar.getInstance();
        c.setTime(plan.getStartTime());
        c.add(Calendar.MINUTE, plan.getSteps().get(0).getDuration());
        updateEndTimeFromStep(c.getTime(), plan, 1, allStepsContainer, endTimeView);
    }

    /*
     * Update the end time and start times of all steps starting from the step at stepIndex
     * The start time of the step at stepIndex is set to the current time
     */
    public static void updateEndTimeFromStepNow(Plan plan, int stepIndex, LinearLayout allStepsContainer, TextView endTimeView) {
        Log.d(TAG, "updateEndTimeFromStepNow");
        updateEndTimeFromStep(getCurrentTime(), plan, stepIndex, allStepsContainer, endTimeView);
    }

    /*
     * Update the end time and start times of all steps starting from the step at stepIndex
     * The start time of the step at stepIndex is set to startTimeOfStep
     */
    public static void updateEndTimeFromStep(Date startTimeOfStep, Plan plan, int stepIndex, LinearLayout allStepsContainer, TextView endTimeView) {
        Log.d(TAG, "updateEndTimeFromStep");
        final Calendar c = Calendar.getInstance();
        c.setTime(startTimeOfStep);
        final ArrayList<Step> steps = plan.getSteps();

        // Iterate over all steps and update the start time of each one
        for (int i = stepIndex; i < plan.getSteps().size(); i++) {
            final FragmentContainerView stepFragmentContainer = (FragmentContainerView) allStepsContainer.getChildAt(i);
            final LinearLayout linesContainer = (LinearLayout) stepFragmentContainer.getChildAt(0);
            final LinearLayout timeContainer = (LinearLayout) linesContainer.getChildAt(0);
            final TextView timeView = (TextView) timeContainer.getChildAt(0);
            timeView.setText(formatTime(c.getTime()));
            c.add(Calendar.MINUTE, steps.get(i).getDuration());
        }
        // Update the end time
        final String newEndTime = formatTime(c.getTime());
        if (!newEndTime.equals(endTimeView.getText().toString())) {
            endTimeView.setText(newEndTime);
        }
    }

    /*
     * Calculate start time of the plan
     */
    public static Date calculateStartTime(Plan plan, EditText endTimeInput) {
        Log.d(TAG, "calculateStartTime");
        Log.d(TAG, "startTimeBefore: " + plan.getStartTime());
        final Calendar c = Calendar.getInstance();
        final Date endTime;
        try {
            endTime = dateFormat.parse(endTimeInput.getText().toString());
            if (endTime != null) {
                c.setTime(endTime);
                c.add(Calendar.MINUTE, plan.getDuration() * -1);
                final Date startTime = c.getTime();
                Log.d(TAG, "startTimeAfter: " + startTime);
                return startTime;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateStartTime(Plan plan, EditText endTimeInput, EditText startTimeInput) {
        final Date newStartTime = calculateStartTime(plan, endTimeInput);
        if (newStartTime != null) {
            final String newStartTimeString = dateFormat.format(newStartTime);
            if (!startTimeInput.getText().toString().equals(newStartTimeString)) {
                startTimeInput.setText(newStartTimeString);
                plan.setStartTime(newStartTime);
            }
        }
    }

    /*
     * Calculate the start time of a step
     */
    public static Date getStartTimeOfStep(Step step, Plan plan) {
        final Calendar c = Calendar.getInstance();
        c.setTime(plan.getStartTime());

        int durationOfAllPreviousSteps = 0;
        final int indexOfCurrentStep = plan.getSteps().indexOf(step);
        int counter = 0;
        while (counter < indexOfCurrentStep) {
            durationOfAllPreviousSteps += plan.getSteps().get(counter).getDuration();
            counter++;
        }
        c.add(Calendar.MINUTE, durationOfAllPreviousSteps);
        return c.getTime();
    }
}

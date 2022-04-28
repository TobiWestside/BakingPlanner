package de.tobiasfraenzel.backplanner;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import de.tobiasfraenzel.backplanner.utils.TimeUtils;

/**
 * Unit test for TimeUtils
 */
public class TimeUtilsTest {
    @Test
    public void stringToTime() {
        final String time = "23:19";
        final Date d = TimeUtils.stringToTime(time);
        final Calendar c = Calendar.getInstance();
        c.set(1970,0,1,23,19,0);
        // Use toString to reduce precision to seconds
        assertEquals(c.getTime().toString(), d.toString());
    }

    @Test
    public void formatTime() {
        final String time = "23:19";
        final Calendar c = Calendar.getInstance();
        c.set(1970,0,1,23,19,0);
        final String formattedTime = TimeUtils.formatTime(c.getTime());
        assertEquals(time, formattedTime);
    }

    @Test
    public void hoursAndMinsToMins() {
        final int mins = 13;
        final int hours = 2;
        final int expectedSum = 133;
        final int actualSum = TimeUtils.convertHoursAndMinsToMins(hours, mins);
        assertEquals(expectedSum, actualSum);
    }

    @Test
    public void hoursAndMinsToMins_zeroMins() {
        final int mins = 0;
        final int hours = 2;
        final int expectedSum = 120;
        final int actualSum = TimeUtils.convertHoursAndMinsToMins(hours, mins);
        assertEquals(expectedSum, actualSum);
    }

    @Test
    public void hoursAndMinsToMins_zeroHours() {
        final int mins = 13;
        final int hours = 0;
        final int expectedSum = 13;
        final int actualSum = TimeUtils.convertHoursAndMinsToMins(hours, mins);
        assertEquals(expectedSum, actualSum);
    }

    @Test
    public void minToHoursAndMins() {
        final int mins = 611;
        final int actualHours = 10;
        final int actualMins = 11;
        final String[] hoursAndMins = TimeUtils.convertMinToHoursAndMins(mins);
        assertEquals(String.valueOf(actualHours), hoursAndMins[0]);
        assertEquals(String.valueOf(actualMins), hoursAndMins[1]);
    }

    @Test
    public void minToDisplayFormat() {
        final int mins = 702;
        final String expected = "11 h 42 min";
        assertEquals(expected, TimeUtils.convertMinToDisplayFormat(mins));
    }

    @Test
    public void minToDisplayFormat_onlyMinutes() {
        final int mins = 2;
        final String expected = "2 min";
        assertEquals(expected, TimeUtils.convertMinToDisplayFormat(mins));
    }

    @Test
    public void minToDisplayFormat_onlyHours() {
        final int mins = 120;
        final String expected = "2 h";
        assertEquals(expected, TimeUtils.convertMinToDisplayFormat(mins));
    }

    @Test
    public void minToDisplayFormat_zero() {
        final int mins = 0;
        final String expected = "0 min";
        assertEquals(expected, TimeUtils.convertMinToDisplayFormat(mins));
    }

    @Test
    public void getEndTimeOfPlan() {
        final Plan p = new Plan();
        final Calendar c = Calendar.getInstance();
        final int duration = 11;
        p.setStartTime(c.getTime());
        final Step s = new Step();
        s.setDuration(duration);
        p.addStep(s);
        c.add(Calendar.MINUTE, duration);
        final Date expected = c.getTime();
        assertEquals(TimeUtils.formatTime(expected), TimeUtils.getEndTimeOfPlan(p));
    }

    @Test
    public void getStartTimeOfStep() {
        final Plan p = new Plan();
        final Calendar c = Calendar.getInstance();
        p.setStartTime(c.getTime());
        for (int i = 0; i < 4; i++) {
            final Step s = new Step();
            s.setDuration(11 + i);
            p.addStep(s);
        }
        c.add(Calendar.MINUTE, 23);
        assertEquals(c.getTime(), TimeUtils.getStartTimeOfStep(p.getSteps().get(2), p));
    }
}
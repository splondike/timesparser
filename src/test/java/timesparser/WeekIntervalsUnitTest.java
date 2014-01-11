package timesparser;

import java.util.Calendar;

import junit.framework.TestCase;
import timesparser.WeekIntervals.DayLocalInterval;
import timesparser.WeekIntervals.DayLocalTime;

public class WeekIntervalsUnitTest extends TestCase {
	public void testIntervalsContains() {
		final DayLocalTime monday9am = new DayLocalTime(Calendar.MONDAY, new LocalTime(9, 0));
		final DayLocalTime monday9pm = new DayLocalTime(Calendar.MONDAY, new LocalTime(21, 0));

		final WeekIntervals intervals = new WeekIntervals().add(new DayLocalInterval(monday9am, monday9pm));

		Calendar calMonday9am = this.dltToCalendar(Calendar.MONDAY, 9, 0);
		Calendar calMonday9pm = this.dltToCalendar(Calendar.MONDAY, 21, 0);
		Calendar calTuesday9am = this.dltToCalendar(Calendar.TUESDAY, 9, 0);

		assertTrue(intervals.contains(calMonday9am));
		assertTrue(intervals.contains(calMonday9pm));
		assertFalse(intervals.contains(calTuesday9am));
	}

	private Calendar dltToCalendar(Integer day, Integer hour, Integer minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);

		return cal;
	}

	public void testIntervalsEquality() {
		final DayLocalTime monday9am = new DayLocalTime(Calendar.MONDAY, new LocalTime(9, 0));
		final DayLocalTime monday9pm = new DayLocalTime(Calendar.MONDAY, new LocalTime(18, 0));
		final DayLocalTime monday11pm = new DayLocalTime(Calendar.MONDAY, new LocalTime(23, 0));
		final DayLocalInterval earlyInterval = new DayLocalInterval(monday9am, monday9pm);
		final DayLocalInterval lateInterval = new DayLocalInterval(monday9pm, monday11pm);
		final DayLocalInterval wholeInterval = new DayLocalInterval(monday9am, monday11pm);

		final WeekIntervals actual = new WeekIntervals().add(earlyInterval).add(lateInterval);
		assertEquals(actual, new WeekIntervals().add(wholeInterval));
	}

	public void testIntervalsInequality() {
		final DayLocalTime monday9am = new DayLocalTime(Calendar.MONDAY, new LocalTime(9, 0));
		final DayLocalTime monday9pm = new DayLocalTime(Calendar.MONDAY, new LocalTime(18, 0));
		final DayLocalTime monday11pm = new DayLocalTime(Calendar.MONDAY, new LocalTime(23, 0));
		final DayLocalInterval earlyInterval = new DayLocalInterval(monday9am, monday9pm);
		final DayLocalInterval lateInterval = new DayLocalInterval(monday9pm, monday11pm);

		assertNotSame(new WeekIntervals().add(earlyInterval), new WeekIntervals().add(lateInterval));
	}
}
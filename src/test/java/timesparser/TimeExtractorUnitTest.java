package timesparser;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import timesparser.WeekIntervals.DayLocalInterval;
import timesparser.WeekIntervals.DayLocalTime;

public class TimeExtractorUnitTest extends TestCase {
	public void testExtractIntervals() {
		List<DayLocalInterval> monToSun = new LinkedList<DayLocalInterval>();
		for(Integer i=1;i<8;i++) {
			monToSun.add(buildCase(i,7,30,23,0));
		}
		Object[][] testCases = {
			{"Mon-Tue", buildIntervals(buildCase(Calendar.MONDAY), buildCase(Calendar.TUESDAY))},
			{"Monday-Tue", buildIntervals(buildCase(Calendar.MONDAY), buildCase(Calendar.TUESDAY))},
			{"Mon 8am-5:30pm", buildIntervals(buildCase(Calendar.MONDAY,8,0,17,30))},
			{"Mon 8am-5:30pm, Fri 7:30am-4pm", buildIntervals(buildCase(Calendar.MONDAY,8,0,17,30), buildCase(Calendar.FRIDAY,7,30,16,0))},
			{"Sun-Mon 8am-5:30pm", buildIntervals(buildCase(Calendar.SUNDAY,8,0,17,30), buildCase(Calendar.MONDAY,8,0,17,30))},
			{"Mon 8-5:30pm", buildIntervals(buildCase(Calendar.MONDAY,8,0,17,30))},
			{"daily 7:30am-11pm", buildIntervals(monToSun.toArray(new DayLocalInterval[]{}))},
			{"Mon lunch 11:30am-2:30pm, dinner 5-10pm, closed Sun, Sat closed",
				buildIntervals(buildCase(Calendar.MONDAY,11,30,14,30), buildCase(Calendar.MONDAY,17,0,22,0))},
			{"Mon 11am-9.30pm", buildIntervals(buildCase(Calendar.MONDAY,11,0,21,30))},
			{"Tue 11am-2am", buildIntervals(buildCase(Calendar.TUESDAY,11,0,23,59),buildCase(Calendar.WEDNESDAY,0,0,2,0))},
			{"Mon 10-12am, Tue 10-2am",
				buildIntervals(buildCase(Calendar.MONDAY,22,0,23,59),buildCase(Calendar.TUESDAY,0,0,0,0),buildCase(Calendar.TUESDAY,22,0,23,59),buildCase(Calendar.WEDNESDAY,0,0,2,0))},
			// Intended parse failure cases
			{"Sun-Mon, Tue 11am-9pm", Maybe.unknown()},
			{"Mon, Wed 11am-2am", Maybe.unknown()},
		};

		for (Object[] testCase : testCases) {
			String input = (String) testCase[0];
			@SuppressWarnings("unchecked")
			Maybe<WeekIntervals> expected = (Maybe<WeekIntervals>) testCase[1];
			Maybe<WeekIntervals> actual = TimeExtractor.parseTimes(input);

			if (expected.isKnown()) {
				assertTrue(actual.isKnown());
				assertTrue(input + " " + actual.iterator().next().toString(),
						expected.iterator().next().equals(actual.iterator().next()));
			}
			else {
				assertFalse(input, actual.isKnown());
			}
		}
	}

	private static Maybe<WeekIntervals> buildIntervals(DayLocalInterval... intervals) {
		WeekIntervals wi = new WeekIntervals();
		for (DayLocalInterval interval : intervals) {
			wi = wi.add(interval);
		}

		return Maybe.definitely(wi);
	}

	private static DayLocalInterval buildCase(Integer day, Integer startHour, Integer startMinute, Integer endHour, Integer endMinute) {
		DayLocalTime start = new DayLocalTime(day, new LocalTime(startHour, startMinute));
		assert(start != null);
		DayLocalTime end = new DayLocalTime(day, new LocalTime(endHour, endMinute));
		return new DayLocalInterval(start, end);
	}

	/**
	 * The whole day.
	 */
	private static DayLocalInterval buildCase(Integer day) {
		DayLocalTime start = new DayLocalTime(day, TimeDescriptionParser.TimeRange.START_OF_DAY);
		DayLocalTime end = new DayLocalTime(day, TimeDescriptionParser.TimeRange.END_OF_DAY);
		return new DayLocalInterval(start, end);
	}
}
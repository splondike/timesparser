package timesparser;

import java.util.List;

import timesparser.TimeDescriptionParser.TimeDescriptionElement;
import timesparser.WeekIntervals.DayLocalInterval;
import timesparser.WeekIntervals.DayLocalTime;
import timesparser.TimeDescriptionParser.*;

/**
 * Responsible for extracting a set of time ranges from a description string. This is the entry point to the module.
 *
 * Will attempt to parse things which look like this: Mon-Fri 11-3pm, Saturday closed, Sun 11pm-2
 */
public class TimeExtractor {
	/**
	 * Attempts to parse the given string into a set of time ranges for the week.
	 * 
	 * @param times A string to parse.
	 * @return unknown if a parse error occurred, definitely if successful parse.
	 */
	public static Maybe<WeekIntervals> parseTimes(String times) {
		List<TimeDescriptionElement> tokens = TimeDescriptionParser.parse(times);
		if (tokens.isEmpty()) return Maybe.unknown();

		WeekIntervals intervals = new WeekIntervals();
		Integer index = 0;
		TimeDescriptionElement dayBuffer = null;
		while (index < tokens.size()) {
			Integer separatorIndex = findNextCommaOrEnd(tokens, index);
			Integer dayIndex = findNextDay(tokens, index);
			Integer timeIndex = findNextTimeRange(tokens, index);
			boolean separatorIsEnd = separatorIndex.equals(tokens.size());
			boolean noDayInCurrentChunk = (dayIndex.equals(-1)) || (dayIndex > separatorIndex);
			boolean noTimeInCurrentChunk = (timeIndex.equals(-1)) || (timeIndex > separatorIndex);
			if (noTimeInCurrentChunk && !separatorIsEnd) return Maybe.unknown();

			if (dayBuffer == null && noDayInCurrentChunk) return Maybe.unknown();
			if (!noDayInCurrentChunk) dayBuffer = tokens.get(dayIndex);

			TimeRange timeBuffer = (noTimeInCurrentChunk) ? TimeRange.WHOLE_DAY : (TimeRange) tokens.get(timeIndex);

			if (!timeBuffer.startTime.equals(timeBuffer.endTime)) {
				// Account for the case where we're going over the day boundary
				TimeRange currDayTimes = timeBuffer;
				TimeRange nextDayTimes = null;
				if (timeBuffer.startTime.isAfter(timeBuffer.endTime)) {
					currDayTimes = new TimeRange(timeBuffer.startTime, TimeRange.END_OF_DAY);
					nextDayTimes = new TimeRange(TimeRange.START_OF_DAY, timeBuffer.endTime);
				}

				@SuppressWarnings("unchecked")
				Iterable<Integer> days = (Iterable<Integer>) dayBuffer;
				for (Integer day : days) {
					if (nextDayTimes != null) {
						Integer nextDay = Day.nextDay(day);
						DayLocalTime startDayTime = new DayLocalTime(nextDay, nextDayTimes.startTime);
						DayLocalTime endDayTime = new DayLocalTime(nextDay, nextDayTimes.endTime);
						intervals = intervals.add(new DayLocalInterval(startDayTime, endDayTime));
					}

					DayLocalTime startDayTime = new DayLocalTime(day, currDayTimes.startTime);
					DayLocalTime endDayTime = new DayLocalTime(day, currDayTimes.endTime);
					intervals = intervals.add(new DayLocalInterval(startDayTime, endDayTime));
				}
			}

			index = separatorIndex.equals(-1) ? tokens.size() : separatorIndex + 1;
		}

		return Maybe.definitely(intervals);
	}

	private static Integer findNextDay(List<TimeDescriptionElement> tokens, Integer startIndex) {
		assert(startIndex < tokens.size());
		for(Integer i=startIndex;i<tokens.size();i++) {
			boolean isDayRange = tokens.get(i) instanceof DayRange;
			boolean isDay = tokens.get(i) instanceof Day;
			if (isDayRange || isDay) {
				return i;
			}
		}

		return -1;
	}

	private static Integer findNextCommaOrEnd(List<TimeDescriptionElement> tokens, Integer startIndex) {
		assert(startIndex < tokens.size());
		for(Integer i=startIndex;i<tokens.size();i++) {
			boolean isComma = tokens.get(i) instanceof Comma;
			if (isComma) {
				return i;
			}
		}

		return tokens.size();
	}

	private static Integer findNextTimeRange(List<TimeDescriptionElement> tokens, Integer startIndex) {
		assert(startIndex < tokens.size());
		for(Integer i=startIndex;i<tokens.size();i++) {
			boolean isTimeRange = tokens.get(i) instanceof TimeRange;
			if (isTimeRange) {
				return i;
			}
		}

		return -1;
	}
}
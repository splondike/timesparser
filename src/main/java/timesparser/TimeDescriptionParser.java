package timesparser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/* package */ class TimeDescriptionParser {
	/**
	 * Utility class
	 */
	private TimeDescriptionParser() {}

	/**
	 * The element classes we try to parse out, in the order they should be tried.
	 */
	@SuppressWarnings("unchecked")
	private static List<Class<? extends TimeDescriptionElement>> elementClasses =
		Arrays.asList(DayRange.class, Day.class, TimeRange.class, Comma.class);
	/**
	 * Turns a time description string into a list of tokens .
	 */
	public static List<TimeDescriptionElement> parse(String timeSentence) {
		List<TimeDescriptionElement> result = new LinkedList<TimeDescriptionElement>();
		String remainder = timeSentence.toLowerCase(Locale.ENGLISH);

		while(remainder.length() > 0) {
			// Using reflection makes this code much more concise
			boolean foundMatch = false;
			for (Class<? extends TimeDescriptionElement> c : elementClasses) {
				try {
					Method parseMethod = c.getDeclaredMethod("parse", String.class);
					@SuppressWarnings("unchecked")
					Maybe<ParseResult<TimeDescriptionElement>> rtn = 
						(Maybe<ParseResult<TimeDescriptionElement>>) parseMethod.invoke(null, remainder);
					if (rtn.isKnown()) {
						result.add(rtn.iterator().next().element);
						remainder = rtn.iterator().next().remainder;
						foundMatch = true;
						break;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			if (!foundMatch) {
				remainder = remainder.substring(1);
			}
		}

		return result;
	}

	/* package */ static class DayRange implements TimeDescriptionElement, Iterable<Integer> {
		public final Integer startDay;
		public final Integer endDay;
		private final static Pattern sepPattern = Pattern.compile(" ?- ?");

		public DayRange(Integer startDay, Integer endDay) {
			this.startDay = startDay;
			this.endDay = endDay;
		}

		public static Maybe<ParseResult<DayRange>> parse(String desc) {
			if (desc.startsWith("daily")) {
				String remainder = desc.substring(5);
				DayRange wholeWeek = new DayRange(Calendar.MONDAY, Calendar.SUNDAY);
				return Maybe.definitely(new ParseResult<DayRange>(wholeWeek, remainder));
			}

			Maybe<ParseResult<Day>> startDayRtn = Day.parse(desc);
			if (startDayRtn.isKnown()) {
				Day startDay = startDayRtn.iterator().next().element;
				String afterStart = startDayRtn.iterator().next().remainder;
				Matcher matcher = sepPattern.matcher(afterStart);
				if (matcher.find()) {
					String last = afterStart.substring(matcher.end());
					Maybe<ParseResult<Day>> endDayRtn = Day.parse(last);
					if (endDayRtn.isKnown()) {
						ParseResult<Day> endDayResult = endDayRtn.iterator().next();
						DayRange dayRange = new DayRange(startDay.day, endDayResult.element.day);
						return Maybe.definitely(new ParseResult<DayRange>(dayRange, endDayResult.remainder));
					}
				}
			}

			return Maybe.unknown();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((endDay == null) ? 0 : endDay.hashCode());
			result = prime * result
					+ ((startDay == null) ? 0 : startDay.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DayRange other = (DayRange) obj;
			if (endDay == null) {
				if (other.endDay != null)
					return false;
			} else if (!endDay.equals(other.endDay))
				return false;
			if (startDay == null) {
				if (other.startDay != null)
					return false;
			} else if (!startDay.equals(other.startDay))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DayRange [" + startDay + "-" + endDay + "]";
		}

		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				Integer current = null;

				public boolean hasNext() {
					return this.current != endDay;
				}

				public Integer next() {
					if (this.current == endDay)
						throw new NoSuchElementException("Out of days");

					this.current = (current == null) ? startDay : Day.nextDay(this.current);
					return this.current;
				}

				public void remove() {
					assert(false);
				}
			};
		}
	}

	/* package */ static class Day implements TimeDescriptionElement, Iterable<Integer> {
		private static final String[] longDays = new String[8];
		private static final String[] shortDays = new String[8];
		static {
			Object[][] mappings = new Object[][] {
                {Calendar.MONDAY, "mon", "monday"},
                {Calendar.TUESDAY, "tue", "tuesday"},
                {Calendar.WEDNESDAY, "wed", "wednesday"},
                {Calendar.THURSDAY, "thur", "thursday"},
                {Calendar.FRIDAY, "fri", "friday"},
                {Calendar.SATURDAY, "sat", "saturday"},
                {Calendar.SUNDAY, "sun", "sunday"},
			};
			for (Object[] dayData : mappings) {
                shortDays[(Integer) dayData[0]] = (String) dayData[1];
                longDays[(Integer) dayData[0]] = (String) dayData[2];
			}
		}
		public final Integer day;
		public Day(Integer day) {
			assert(day > 0 && day < 8);
			this.day = day;
		}

		public static Integer nextDay(Integer currDay) {
			assert(currDay > 0 && currDay < 8);
			return currDay.equals(7) ? 1 : currDay+1;
		}

		public static Maybe<ParseResult<Day>> parse(String desc) {
			// Check this first, because the shortDays are a substring of the longDays
			final Maybe<ParseResult<Day>> longResult = parse(longDays, desc);
			if (longResult.isKnown()) {
				return longResult;
			}
			else {
				return parse(shortDays, desc);
			}
		}

		private static Maybe<ParseResult<Day>> parse(String[] days, String desc) {
			for (int i=1;i<days.length;i++) {
				Integer len = days[i].length();
				boolean endsWithNonAlpha = (desc.length() <= len) || !isAlpha(desc.charAt(len));
				if (desc.startsWith(days[i]) && endsWithNonAlpha) {
					Day elm = new Day(i);
					String rest = desc.substring(len);
					return Maybe.definitely(new ParseResult<Day>(elm, rest));
				}
			}
			return Maybe.unknown();
		}

		private static boolean isAlpha(char c) {
			return c > 96 && c < 123;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((day == null) ? 0 : day.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Day other = (Day) obj;
			if (day == null) {
				if (other.day != null)
					return false;
			} else if (!day.equals(other.day))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Day [" + longDays[this.day] + "]";
		}

		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				boolean returned = false;

				public boolean hasNext() {
					return !returned;
				}

				public Integer next() {
					returned = true;
					return day;
				}

				public void remove() {
					assert(false);
				}
			};
		}
	}

	/* package */ static class TimeRange implements TimeDescriptionElement {
		// This can be after endTime if we're going over the day boundary
		public final LocalTime startTime;
		public final LocalTime endTime;
		private final static Pattern timePattern =
			Pattern.compile("(1?\\d)((?:|.)[0-5]\\d)?(am|pm)?( ?- ?)(1?\\d)((?:|.)[0-5]\\d)?(am|pm)?");

		public final static LocalTime START_OF_DAY = new LocalTime(0,0);
		public final static LocalTime END_OF_DAY = new LocalTime(23,59);
		public final static TimeRange WHOLE_DAY = new TimeRange(START_OF_DAY, END_OF_DAY);

		public TimeRange(LocalTime startTime, LocalTime endTime) {
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public static final Maybe<ParseResult<TimeRange>> parse(String desc) {
			if (desc.startsWith("closed")) {
				String remainder = desc.substring(6);
				TimeRange empty = new TimeRange(new LocalTime(0,0), new LocalTime(0,0));
				return Maybe.definitely(new ParseResult<TimeRange>(empty, remainder));
			}

			Matcher matcher = timePattern.matcher(desc);

			if (!matcher.find() || matcher.start() != 0) {
				return Maybe.unknown();
			}
			
			Integer startHourBase = Integer.parseInt(matcher.group(1));
			startHourBase = (startHourBase.equals(12)) ? 0 : startHourBase;
			Integer endHourBase = Integer.parseInt(matcher.group(5));
			endHourBase = (endHourBase.equals(12)) ? 0 : endHourBase;

			Integer endHour;
			if ("am".equals(matcher.group(7))) {
				endHour = endHourBase;
			}
			else if ("pm".equals(matcher.group(7))) {
				endHour = endHourBase + 12;
			}
			else {
				return Maybe.unknown();
			}

			Integer startHour;
			if ("am".equals(matcher.group(3))) {
				startHour = startHourBase;
			}
			else if ("pm".equals(matcher.group(3))) {
				startHour = startHourBase + 12;
			}
			else {
				if (startHourBase >= endHourBase) {
					if (endHour >= 12) {
						startHour = startHourBase;
					}
					else {
						startHour = startHourBase + 12;
					}
				}
				else {
					startHour = (endHour > 12) ? startHourBase + 12 : startHourBase;
				}
			}	

			Integer startMinute = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2).substring(1));
			Integer endMinute = matcher.group(6) == null ? 0 : Integer.parseInt(matcher.group(6).substring(1));

			LocalTime startTime = new LocalTime(startHour, startMinute);
			LocalTime endTime = new LocalTime(endHour, endMinute);
			String rest = desc.substring(matcher.end());
			return Maybe.definitely(new ParseResult<TimeRange>(new TimeRange(startTime, endTime), rest));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((endTime == null) ? 0 : endTime.hashCode());
			result = prime * result
					+ ((startTime == null) ? 0 : startTime.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TimeRange other = (TimeRange) obj;
			if (endTime == null) {
				if (other.endTime != null)
					return false;
			} else if (!endTime.equals(other.endTime))
				return false;
			if (startTime == null) {
				if (other.startTime != null)
					return false;
			} else if (!startTime.equals(other.startTime))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TimeRange [" + startTime.toString() + "-" + endTime.toString() + "]";
		}
	}

	/* package */ static final class Comma implements TimeDescriptionElement {
		public static Maybe<ParseResult<Comma>> parse(String desc) {
			if (desc.startsWith(",")) {
				return Maybe.definitely(new ParseResult<Comma>(new Comma(), desc.substring(1)));
			}
			return Maybe.unknown();
		}

		@Override
		public int hashCode() {
			return 1;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Comma;
		}

		@Override
		public String toString() {
			return "Comma";
		}
	}

	// I could probably make this an abstract class using generics, but why bother?
	/* package */ interface TimeDescriptionElement { }

	/* package */ static class ParseResult<T extends TimeDescriptionElement> {
		public final T element;
		public final String remainder;
		public ParseResult(T element, String remainder) {
			this.element = element;
			this.remainder = remainder;
		}
	}
}
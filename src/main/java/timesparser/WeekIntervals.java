package timesparser;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * An immutable collection of time intervals in a week, e.g. Mon-Fri 11-2pm, Sat 9am-3pm. Allows user to check
 * whether a time is within the included intervals.
 *
 * Note that WeekIntervals doesn't know about timezones.
 */
public class WeekIntervals {
	private final Set<DayLocalInterval> intervals;

	/* package */ WeekIntervals() {
		this.intervals = new HashSet<DayLocalInterval>();
	}

	private WeekIntervals(Set<DayLocalInterval> intervals) {
		this.intervals = intervals;
	}

	/* package */ boolean contains(DayLocalTime time) {
		for(DayLocalInterval interval : this.intervals) {
			if (interval.contains(time)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether WeekIntervals contains the given time.
	 * 
	 * Note that WeekIntervals doesn't know about timezones.
	 *
	 * @param time The time to check for
	 * @return true if the given time is contained, false otherwise.
	 */
	public boolean contains(Calendar time) {
		Integer day = time.get(Calendar.DAY_OF_WEEK);
		Integer hour = time.get(Calendar.HOUR_OF_DAY);
		Integer minute = time.get(Calendar.MINUTE);

		return this.contains(new DayLocalTime(day, new LocalTime(hour, minute)));
	}

	/**
	 * Adds an interval to the collection, and returns a reference to the new collection.
	 * 
	 * @param newInterval An interval to add to the collection.
	 * @return A new WeekIntervals object with the given range added.
	 */
	/* package */ WeekIntervals add(DayLocalInterval newInterval) {
		Set<DayLocalInterval> newSet = new HashSet<DayLocalInterval>();
		DayLocalInterval intToAdd = newInterval;

		for (DayLocalInterval interval : this.intervals) {
			if (interval.intersects(intToAdd)) {
				intToAdd = interval.mergeWith(intToAdd);
			}
			else {
				newSet.add(interval);
			}
		}

		newSet.add(intToAdd);
		return new WeekIntervals(newSet);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((intervals == null) ? 0 : intervals.hashCode());
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
		WeekIntervals other = (WeekIntervals) obj;
		if (intervals == null) {
			if (other.intervals != null)
				return false;
		} else if (!intervals.equals(other.intervals))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WeekIntervals " + intervals;
	}

	/**
	 * Immutable interval on a given day of the week.
	 */
	/* package */ static class DayLocalInterval {
		private final DayLocalTime startTime;
		private final DayLocalTime endTime;

		public DayLocalInterval(DayLocalTime startTime, DayLocalTime endTime) {
			assert(startTime.day.equals(endTime.day));
			assert(startTime.time.compareTo(endTime.time) <= 0);
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public boolean contains(DayLocalTime other) {
			if (other.day != this.startTime.day) return false;
			final boolean moreThanOrEqualsStart = this.startTime.time.compareTo(other.time) <= 0;
			final boolean lessThanOrEqualsEnd = this.endTime.time.compareTo(other.time) >= 0;

			return moreThanOrEqualsStart && lessThanOrEqualsEnd;
		}

		private boolean intersects(DayLocalInterval other) {
			boolean otherHasMe = other.contains(this.startTime) || other.contains(this.endTime);
			boolean iHaveOther = this.contains(other.startTime) || this.contains(other.endTime);
			return iHaveOther || otherHasMe;
		}

		private DayLocalInterval mergeWith(DayLocalInterval other) {
			assert(this.intersects(other));
			DayLocalTime startTime = this.chooseTime("minimum", this.startTime, other.startTime);
			DayLocalTime endTime = this.chooseTime("maximum", this.endTime, other.endTime);
			return new DayLocalInterval(startTime, endTime);
		}

		private DayLocalTime chooseTime(String extreme, DayLocalTime time1, DayLocalTime time2) {
			if (time1.time.compareTo(time2.time) > 0) {
				return extreme.equals("minimum") ? time2 : time1;
			}
			else {
				return extreme.equals("minimum") ? time1 : time2;
			}
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
			DayLocalInterval other = (DayLocalInterval) obj;
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
			return "DayLocalInterval [startTime=" + startTime + ", endTime="
					+ endTime + "]";
		}
	}

	/**
	 * Immutable time on a day of the week.
	 */
	/* package */ static class DayLocalTime {
		private final Integer day;
		private final LocalTime time;
		public DayLocalTime(Integer day, LocalTime time) {
			assert(day > 0 && day < 8);
			this.day = day;
			this.time = time;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((day == null) ? 0 : day.hashCode());
			result = prime * result + ((time == null) ? 0 : time.hashCode());
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
			DayLocalTime other = (DayLocalTime) obj;
			if (day == null) {
				if (other.day != null)
					return false;
			} else if (!day.equals(other.day))
				return false;
			if (time == null) {
				if (other.time != null)
					return false;
			} else if (!time.equals(other.time))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DayLocalTime [day=" + day + ", time=" + time + "]";
		}
	}
}

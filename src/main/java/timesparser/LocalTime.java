package timesparser;


/**
 * Represents an hour/minute/second with no accompanying timezone information.
 */
/* package */ class LocalTime implements Comparable<LocalTime> {
	private Integer seconds;

	public LocalTime(Integer hour, Integer minute) {
		this(hour, minute, 0);
	}

	/*
	 * Seconds aren't produced by the parser.
	 */
	private LocalTime(Integer hour, Integer minute, Integer second) {
		this.seconds = hour * 3600 + minute * 60 + second;
	}

	public int getHours() {
		return this.seconds / 3600;
	}

	public int getMinutes() {
		int remainder = this.seconds % 3600;
		return remainder / 60;
	}

	public int getSeconds() {
		return this.seconds % 60;
	}

	public boolean isAfter(LocalTime other) {
		return this.compareTo(other) > 0;
	}

	public int compareTo(LocalTime otherTime) {
		if (this.seconds.equals(otherTime.seconds)) {
			return 0;
		}
		else if (this.seconds < otherTime.seconds) {
			return -1;
		}
		else {
			return 1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((seconds == null) ? 0 : seconds.hashCode());
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
		LocalTime other = (LocalTime) obj;
		if (seconds == null) {
			if (other.seconds != null)
				return false;
		} else if (!seconds.equals(other.seconds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LocalTime [" + this.getHours() + ":" +  this.getMinutes() + ":" + this.getSeconds() + "]";
	}
}
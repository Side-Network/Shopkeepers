package com.nisovin.shopkeepers.util.java;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public final class TimeUtils {

	/**
	 * The number of nanoseconds in one second.
	 */
	public static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);

	/**
	 * Converts a duration between the given {@link TimeUnit time units} while preserving double
	 * precision.
	 * 
	 * @param duration
	 *            the duration in the source time unit
	 * @param from
	 *            the source time unit
	 * @param to
	 *            the target time unit
	 * @return the duration in the target time unit
	 */
	public static double convert(double duration, TimeUnit from, TimeUnit to) {
		if (from == to) {
			return duration;
		}
		// Smaller ordinal indicates the smaller time unit:
		if (from.ordinal() < to.ordinal()) {
			return duration / from.convert(1, to);
		} else {
			return duration * to.convert(1, from);
		}
	}

	/**
	 * Gets a display string representing the time elapsed since the given instant.
	 * <p>
	 * Example: "2d 3h 4m", or "5s" for durations less than one minute.
	 * 
	 * @param instant
	 *            the instant to calculate the elapsed time from
	 * @return the string representing the elapsed time
	 */
	public static String getTimeAgoString(Instant instant) {
		var duration = Duration.between(instant, Instant.now());
		var negative = duration.isNegative(); // instant is in the future
		duration = duration.abs();

		var days = duration.toDays();
		var hours = duration.toHoursPart();
		var minutes = duration.toMinutesPart();
		var seconds = duration.toSecondsPart();

		var timeAgoString = new StringBuilder();
		if (negative) {
			timeAgoString.append('-');
		}
		if (days > 0) {
			timeAgoString.append(days).append("d ");
		}
		if (hours > 0) {
			timeAgoString.append(hours).append("h ");
		}
		if (minutes > 0) {
			timeAgoString.append(minutes).append("m ");
		}

		// Only include the seconds part for durations less than 1 minute:
		if (days == 0 && hours == 0 && minutes == 0) {
			timeAgoString.append(seconds).append("s ");
		}

		// Return the string without the trailing space:
		return timeAgoString.substring(0, timeAgoString.length() - 1);
	}

	private TimeUtils() {
	}
}

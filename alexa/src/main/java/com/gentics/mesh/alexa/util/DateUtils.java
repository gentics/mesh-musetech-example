package com.gentics.mesh.alexa.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class DateUtils {

	private DateUtils() {
	}

	public static OffsetDateTime parse(String dateStr) {
		return OffsetDateTime.parse(dateStr).minusHours(2);
	}

	public static String toTime(OffsetDateTime time) {
		time = time.plusHours(2);
		int min = time.getMinute();
		String minStr = min < 10 ? "0" + min : String.valueOf(min);
		return time.getHour() + ":" + minStr;
	}

	public static boolean isPast(OffsetDateTime time) {
		return now().isAfter(time);
	}

	public static OffsetDateTime now() {
		return OffsetDateTime.now(ZoneId.of("GMT")).minusHours(1);
	}
}

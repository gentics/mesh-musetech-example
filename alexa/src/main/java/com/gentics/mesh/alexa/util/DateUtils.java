package com.gentics.mesh.alexa.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class DateUtils {

	private DateUtils() {
	}

	public static OffsetDateTime now() {
		return OffsetDateTime.now(ZoneId.of("GMT"));
	}
}

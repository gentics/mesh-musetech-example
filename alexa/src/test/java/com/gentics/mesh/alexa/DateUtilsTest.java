package com.gentics.mesh.alexa;

import java.time.OffsetDateTime;

import org.junit.Test;

import com.gentics.mesh.alexa.util.DateUtils;

public class DateUtilsTest {

	@Test
	public void testDateHandling() {
		OffsetDateTime now = DateUtils.now();
		OffsetDateTime date = DateUtils.parse("2020-02-16T15:30:00Z");
		System.out.println("Date: " + date.getHour() + ":" + date.getMinute() + " Fixed: " + DateUtils.toTime(date));
		System.out.println("Now: " + now.getHour() + ":" + now.getMinute() + " Fixed: " + DateUtils.toTime(now));
		System.out.println("In Past: " + date.isBefore(now) + " Fixed: " + DateUtils.isPast(date));
	}
}

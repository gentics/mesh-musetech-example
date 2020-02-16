package com.gentics.mesh.alexa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.mesh.alexa.action.MeshActions;
import com.gentics.mesh.alexa.dagger.config.SkillConfig;
import com.gentics.mesh.alexa.model.TourInfo;

public class MeshActionsTest {

	private static MeshActions connector;

	@BeforeClass
	public static void setupActions() throws IOException {
		connector = new MeshActions(new SkillConfig());
	}

	@Test
	public void testStockLevel() {
		Locale locale = Locale.GERMAN;
		String stockText = connector.loadStockLevel(locale, "space").blockingGet().getSpeech();
		System.out.println(stockText);

		String stock2 = connector.loadStockLevel(locale, "space").blockingGet().getSpeech();
		System.out.println(stock2);

	}

	@Test
	public void testLoadTourByUuid() {
		// loadTourByUuid
		Locale locale = Locale.GERMAN;
		TourInfo tourInfo = connector.loadNextTour(locale).blockingGet();

		TourInfo tourByUuid = connector.loadTourByUuid(locale, tourInfo.getUuid(), tourInfo.getDateStr()).blockingGet();
		assertEquals(tourByUuid.getUuid(), tourInfo.getUuid());
	}

	@Test
	public void testReserveNextTour() {
		Locale locale = Locale.GERMAN;
		TourInfo tourInfo = connector.loadNextTour(locale).blockingGet();
		String text = connector.reserveTourByUuid(locale, tourInfo.getUuid(), tourInfo.getDateStr()).blockingGet().getSpeech();
		System.out.println(text);
	}

	@Test
	public void testLoadNextTour() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadNextTourInfo(locale).blockingGet().getSpeech();
		System.out.println(text);
	}

	@Test
	public void testLoadTourInfos() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadTourInfos(locale).blockingGet().getSpeech();
		System.out.println(text);
	}

	@Test
	public void testPrice() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadTourPrice(locale, "space").blockingGet().getSpeech();

		System.out.println("Price: " + text);
		text = connector.loadTourPrice(locale, "empire").blockingGet().getSpeech();
		System.out.println("Price: " + text);
	}

	@Test
	public void testBogus() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadTourPrice(locale, "autowagen").blockingGet().getSpeech();
		System.out.println("Price: " + text);
	}
}

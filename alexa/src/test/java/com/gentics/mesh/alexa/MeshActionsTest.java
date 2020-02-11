package com.gentics.mesh.alexa;

import java.io.IOException;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.mesh.alexa.action.MeshActions;
import com.gentics.mesh.alexa.dagger.config.SkillConfig;

public class MeshActionsTest {

	private static MeshActions connector;

	@BeforeClass
	public static void setupActions() throws IOException {
		connector = new MeshActions(new SkillConfig());
	}

	@Test
	public void testStockLevel() {
		Locale locale = Locale.GERMAN;
		String stockText = connector.loadStockLevel(locale, "space").blockingGet();
		System.out.println(stockText);

		String stock2 = connector.loadStockLevel(locale, "space").blockingGet();
		System.out.println(stock2);

	}

	@Test
	public void testReserveNextTour() {
		Locale locale = Locale.GERMAN;
		String text = connector.reserveNextTour(locale).blockingGet();
		System.out.println(text);
	}

	@Test
	public void testNextTour() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadNextTourInfo(locale).blockingGet();
		System.out.println(text);
	}

	@Test
	public void testLoadTourInfos() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadTourInfos(locale).blockingGet();
		System.out.println(text);
	}

	@Test
	public void testPrice() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadTourPrice(locale, "space").blockingGet();

		System.out.println("Price: " + text);
		text = connector.loadTourPrice(locale, "empire").blockingGet();
		System.out.println("Price: " + text);
	}

	@Test
	public void testBogus() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadTourPrice(locale, "autowagen").blockingGet();
		System.out.println("Price: " + text);
	}
}

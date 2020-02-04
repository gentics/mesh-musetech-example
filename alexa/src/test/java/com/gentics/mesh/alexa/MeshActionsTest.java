package com.gentics.mesh.alexa;

import java.io.IOException;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.mesh.alexa.action.MeshActions;
import com.gentics.mesh.alexa.dagger.config.SkillConfig;

import io.reactivex.Single;

public class MeshActionsTest {

	private static MeshActions connector;

	@BeforeClass
	public static void setupActions() throws IOException {
		connector = new MeshActions(new SkillConfig());
	}

	@Test
	public void testStockLevel() {
		Locale locale = Locale.GERMAN;
		String stockText = connector.loadStockLevel(locale, "tesla").blockingGet();
		System.out.println(stockText);

		String text = connector.reserveVehicle(locale, "tesla").blockingGet();
		System.out.println(text);

		String stock2 = connector.loadStockLevel(locale, "tesla").blockingGet();
		System.out.println(stock2);

	}

	@Test
	public void testPrice() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadVehiclePrice(locale, "tesla").blockingGet();
		
		System.out.println("Price: " + text);
		text = connector.loadVehiclePrice(locale, "space shuttle").blockingGet();
		System.out.println("Price: " + text);
	}

	@Test
	public void testBogus() {
		Locale locale = Locale.GERMAN;
		String text = connector.loadVehiclePrice(locale, "autowagen").blockingGet();
		System.out.println("Price: " + text);
	}
}

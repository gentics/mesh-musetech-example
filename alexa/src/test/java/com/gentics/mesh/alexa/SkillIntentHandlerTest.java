package com.gentics.mesh.alexa;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.gentics.mesh.alexa.intent.SkillIntentHandler;
import com.gentics.mesh.alexa.intent.impl.GetVehiclePriceIntent;
import com.gentics.mesh.alexa.intent.impl.ReserveVehicleIntent;
import com.gentics.mesh.alexa.intent.impl.StockLevelIntentHandler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class SkillIntentHandlerTest extends AbstractSkillTest {

	@Test
	public void testStream() throws IOException {
		JsonObject input = loadJson("open");

		GetVehiclePriceIntent priceIntent = Mockito.mock(GetVehiclePriceIntent.class);
		StockLevelIntentHandler stockLevelIntent = Mockito.mock(StockLevelIntentHandler.class);
		ReserveVehicleIntent reserveIntent = Mockito.mock(ReserveVehicleIntent.class);

		SkillIntentHandler handler = new SkillIntentHandler(priceIntent, stockLevelIntent, reserveIntent);
		handler.handleRequest(input, h -> {
			Buffer buffer = Buffer.buffer(h.getRawResponse());
			JsonObject output = new JsonObject(buffer);
			System.out.println(output);
		});
	}
}

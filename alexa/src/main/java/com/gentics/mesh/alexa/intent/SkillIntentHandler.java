package com.gentics.mesh.alexa.intent;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.request.impl.BaseSkillRequest;
import com.amazon.ask.response.SkillResponse;
import com.gentics.mesh.alexa.intent.impl.CancelandStopIntentHandler;
import com.gentics.mesh.alexa.intent.impl.FallbackIntentHandler;
import com.gentics.mesh.alexa.intent.impl.GetVehiclePriceIntent;
import com.gentics.mesh.alexa.intent.impl.HelpIntentHandler;
import com.gentics.mesh.alexa.intent.impl.LaunchRequestHandler;
import com.gentics.mesh.alexa.intent.impl.ReserveVehicleIntent;
import com.gentics.mesh.alexa.intent.impl.SessionEndedRequestHandler;
import com.gentics.mesh.alexa.intent.impl.StockLevelIntentHandler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@Singleton
public class SkillIntentHandler {

	public static final String VEHICLE_SLOT = "vehicle";
	public static final String CATEGORY_SLOT = "category";

	private final Skill skill;

	@Inject
	public SkillIntentHandler(GetVehiclePriceIntent priceIntent, StockLevelIntentHandler stockLevelIntent, ReserveVehicleIntent reserveIntent) {
		this.skill = Skills.custom()
			.addRequestHandlers(
				new CancelandStopIntentHandler(),
				reserveIntent,
				stockLevelIntent,
				priceIntent,
				new HelpIntentHandler(), // OK
				new LaunchRequestHandler(),
				new SessionEndedRequestHandler(),
				new FallbackIntentHandler()) // OK
			// Add your skill id below
			// .withSkillId("")
			.build();
	}

	public void handleRequest(JsonObject input, Handler<SkillResponse<?>> responseHandler) throws IOException {
		SkillResponse<?> response = skill.execute(new BaseSkillRequest(input.toBuffer().getBytes()), null);
		if (response != null) {
			if (response.isPresent()) {
				responseHandler.handle(response);
			}
			return;
		} else {
			throw new AskSdkException("Could not find a skill to handle the incoming request");
		}
	}

}

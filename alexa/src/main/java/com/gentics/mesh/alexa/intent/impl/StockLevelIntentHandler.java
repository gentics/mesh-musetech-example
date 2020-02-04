package com.gentics.mesh.alexa.intent.impl;

import static com.amazon.ask.request.Predicates.intentName;
import static com.gentics.mesh.alexa.util.I18NUtil.i18n;

import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.gentics.mesh.alexa.action.MeshActions;
import com.gentics.mesh.alexa.intent.AbstractGenticsIntent;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Singleton
public class StockLevelIntentHandler extends AbstractGenticsIntent {

	private static final Logger log = LoggerFactory.getLogger(StockLevelIntentHandler.class);

	private MeshActions mesh;

	@Inject
	public StockLevelIntentHandler(MeshActions mesh) {
		this.mesh = mesh;
	}

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("GetStockLevel"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		String speechText;
		Locale locale = getLocale(input);
		Slot vehicleSlot = getVehicleSlot(input);

		if (vehicleSlot == null) {
			log.info("Slot not found in request");
			speechText = i18n(locale, "vehicle_not_found");
		} else {
			String name = vehicleSlot.getValue();
			if (name == null) {
				speechText = i18n(locale, "vehicle_not_found");
			} else {
				speechText = mesh.loadStockLevel(locale, name).blockingGet();
			}
		}

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "shop_name"), speechText)
			.withReprompt(i18n(locale, "fallback_answer"))
			.withShouldEndSession(false)
			.build();
	}

}

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
import com.gentics.mesh.alexa.model.AlexaResponse;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Singleton
public class TourInfoIntentHandler extends AbstractGenticsIntent {

	private static final Logger log = LoggerFactory.getLogger(TourInfoIntentHandler.class);

	private MeshActions mesh;

	@Inject
	public TourInfoIntentHandler(MeshActions mesh) {
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
		Slot tourSlot = getTourSlot(input);

		if (tourSlot == null) {
			log.info("Slot not found in request");
			speechText = i18n(locale, "tour_not_found");
		} else {
			String name = tourSlot.getValue();
			if (name == null) {
				speechText = i18n(locale, "tour_not_found");
			} else {
				AlexaResponse response = mesh.loadStockLevel(locale, name).blockingGet();
				speechText = response.getSpeech();
			}
		}

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "museum_name"), speechText)
			.withReprompt(i18n(locale, "fallback_answer"))
			.withShouldEndSession(false)
			.build();
	}

}

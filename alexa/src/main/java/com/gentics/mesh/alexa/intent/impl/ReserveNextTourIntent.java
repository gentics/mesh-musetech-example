package com.gentics.mesh.alexa.intent.impl;

import static com.amazon.ask.request.Predicates.intentName;
import static com.gentics.mesh.alexa.GenticsSkill.BLACKSPRING_PHONETIC_DE;
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

@Singleton
public class ReserveNextTourIntent extends AbstractGenticsIntent {

	private final MeshActions mesh;

	@Inject
	public ReserveNextTourIntent(MeshActions mesh) {
		this.mesh = mesh;

	}

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ReserveNextTour"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		String speechText;
		Locale locale = getLocale(input);
		Slot tourSlot = getTourSlot(input);

		if (tourSlot == null) {
			speechText = i18n(locale, "tour_not_found");
		} else {
			speechText = mesh.reserveNextTour(locale).blockingGet();
		}

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "museum_name"), speechText)
			.withReprompt(i18n(locale, "fallback_answer"))
			.withShouldEndSession(false)
			.build();
	}

}

package com.gentics.mesh.alexa.intent.impl;

import static com.amazon.ask.request.Predicates.intentName;
import static com.gentics.mesh.alexa.util.I18NUtil.i18n;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.gentics.mesh.alexa.intent.AbstractGenticsIntent;

public class DebugIntent extends AbstractGenticsIntent {

	@Inject
	public DebugIntent() {
	}

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("Debug"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		Locale locale = getLocale(input);

		OffsetDateTime dateTime = OffsetDateTime.now();
		String speechText = "Uhrzeit: " + dateTime.getHour() + ":" + dateTime.getMinute();
		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "museum_name"), speechText)
			.withShouldEndSession(true)
			.withReprompt(i18n(locale, "fallback_answer"))
			.build();
	}

}

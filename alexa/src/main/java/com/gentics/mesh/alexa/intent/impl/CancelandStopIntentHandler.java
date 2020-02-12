package com.gentics.mesh.alexa.intent.impl;

import static com.amazon.ask.request.Predicates.intentName;
import static com.gentics.mesh.alexa.GenticsSkill.BLACKSPRING_PHONETIC_DE;
import static com.gentics.mesh.alexa.util.I18NUtil.i18n;

import java.util.Locale;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.gentics.mesh.alexa.intent.AbstractGenticsIntent;

public class CancelandStopIntentHandler extends AbstractGenticsIntent {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		Locale locale = getLocale(input);
		String speechText = i18n(locale, "end");

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "museum_name"), speechText)
			.build();
	}
}
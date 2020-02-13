package com.gentics.mesh.alexa.intent.impl;

import static com.amazon.ask.request.Predicates.intentName;
import static com.gentics.mesh.alexa.util.I18NUtil.i18n;
import static com.gentics.mesh.alexa.MuseTechSkill.BLACKSPRING_PHONETIC_DE;

import java.util.Locale;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.gentics.mesh.alexa.intent.AbstractGenticsIntent;

// 2018-July-09: AMAZON.FallackIntent is only currently available in en-US locale.
//              This handler will not be triggered except in that locale, so it can be
//              safely deployed for any locale.
public class FallbackIntentHandler extends AbstractGenticsIntent {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.FallbackIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		Locale locale = getLocale(input);
		String speechText = i18n(locale, "fallback_answer");

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "museum_name"), speechText)
			.withReprompt(speechText)
			.build();
	}

}
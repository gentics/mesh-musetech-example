package com.gentics.mesh.alexa.intent.impl;

import static com.amazon.ask.request.Predicates.intentName;
import static com.gentics.mesh.alexa.util.I18NUtil.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.gentics.mesh.alexa.action.MeshActions;
import com.gentics.mesh.alexa.intent.AbstractGenticsIntent;
import com.gentics.mesh.alexa.model.AlexaResponse;

public class GetNextTourIntent extends AbstractGenticsIntent {

	private final MeshActions mesh;

	@Inject
	public GetNextTourIntent(MeshActions mesh) {
		this.mesh = mesh;
	}

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("GetNextTour"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		Locale locale = getLocale(input);
		AlexaResponse response = mesh.loadNextTourInfo(locale).blockingGet();
		String speechText = response.getSpeech();

		AttributesManager attributesManager = input.getAttributesManager();
		Map<String, Object> attributes = attributesManager.getSessionAttributes();

		attributes.putAll(response.getAttributes());
		attributesManager.setSessionAttributes(attributes);

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "museum_name"), speechText)
			.withShouldEndSession(false)
			.withReprompt(i18n(locale, "fallback_answer"))
			.build();
	}

}

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

@Singleton
public class ReserveVehicleIntent extends AbstractGenticsIntent {

	private final MeshActions mesh;

	@Inject
	public ReserveVehicleIntent(MeshActions mesh) {
		this.mesh = mesh;

	}

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ReserveVehicle"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		String speechText;
		Locale locale = getLocale(input);
		Slot vehicleSlot = getVehicleSlot(input);

		if (vehicleSlot == null) {
			speechText = i18n(locale, "vehicle_not_found");
		} else {
			String name = vehicleSlot.getValue();
			speechText = mesh.reserveVehicle(locale, name).blockingGet();
		}

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "shop_name"), speechText)
			.withReprompt(i18n(locale, "fallback_answer"))
			.withShouldEndSession(false)
			.build();
	}

}

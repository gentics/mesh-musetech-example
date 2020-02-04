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
public class GetVehiclePriceIntent extends AbstractGenticsIntent {

	private final MeshActions mesh;

	@Inject
	public GetVehiclePriceIntent(MeshActions mesh) {
		this.mesh = mesh;
	}

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("GetVehiclePrice"));
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
			speechText = mesh.loadVehiclePrice(locale, name).blockingGet();
		}

		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withSimpleCard(i18n(locale, "shop_name"), speechText)
			.withShouldEndSession(false)
			.withReprompt(i18n(locale, "fallback_answer"))
			.build();

	}

}

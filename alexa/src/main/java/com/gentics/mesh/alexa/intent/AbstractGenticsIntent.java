package com.gentics.mesh.alexa.intent;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Slot;

public abstract class AbstractGenticsIntent implements RequestHandler {

	public static final Logger log = LoggerFactory.getLogger(AbstractGenticsIntent.class);

	protected Locale getLocale(HandlerInput input) {
		String localeStr = input.getRequest().getLocale();
		log.debug("Input locale {" + localeStr + "}");
		Locale locale = Locale.forLanguageTag(localeStr);
		log.debug("Found locale {" + locale + "}");
		return locale;
	}

	protected Slot getTourSlot(HandlerInput input) {
		Request request = input.getRequestEnvelope().getRequest();
		IntentRequest intentRequest = (IntentRequest) request;
		Intent intent = intentRequest.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		return slots.get(SkillIntentHandler.TOUR_SLOT);
	}
}

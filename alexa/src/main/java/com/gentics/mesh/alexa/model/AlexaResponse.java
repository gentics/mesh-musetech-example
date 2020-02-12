package com.gentics.mesh.alexa.model;

import static com.gentics.mesh.alexa.util.I18NUtil.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AlexaResponse {

	private String speech;

	private Map<String, Object> attributes = new HashMap<>();

	public AlexaResponse(String speech) {
		this.speech = speech;
	}

	public AlexaResponse addAttribute(String key, Object value) {
		attributes.put(key, value);
		return this;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public String getSpeech() {
		return speech;
	}

	public static AlexaResponse create(Locale locale, String i18nKey, String... params) {
		return new AlexaResponse(i18n(locale, i18nKey, params));
	}

	public static AlexaResponse create(String speech) {
		return new AlexaResponse(speech);
	}
}

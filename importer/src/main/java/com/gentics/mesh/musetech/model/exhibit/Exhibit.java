package com.gentics.mesh.musetech.model.exhibit;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gentics.mesh.core.rest.common.RestModel;

public class Exhibit implements RestModel {

	@JsonProperty("public_number")
	private String publicNumber;

	@JsonProperty("en")
	private ExhibitContent english;

	@JsonProperty("de")
	private ExhibitContent german;

	private String titleImage;

	private List<String> images;

	public Exhibit() {
	}

	public ExhibitContent getEnglish() {
		return english;
	}

	public ExhibitContent getGerman() {
		return german;
	}

	public String getPublicNumber() {
		return publicNumber;
	}

	public String getTitleImage() {
		return titleImage;
	}

	public List<String> getImages() {
		return images;
	}

	@Override
	public String toString() {
		return toJson();
	}
}

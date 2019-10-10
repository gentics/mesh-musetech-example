package com.gentics.mesh.musetech.model.exhibit;

import com.gentics.mesh.core.rest.common.RestModel;

public class ExhibitContent implements RestModel {

	private String name;
	private String description;
	private String audioName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAudioName() {
		return audioName;
	}

	public void setAudioName(String audioName) {
		this.audioName = audioName;
	}
}

package com.gentics.mesh.musetech.model.video;

import com.gentics.mesh.core.rest.common.RestModel;

public class Video implements RestModel {

	private String name;
	private String description;

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
}

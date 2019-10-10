package com.gentics.mesh.musetech.model.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gentics.mesh.core.rest.common.RestModel;

public class Video implements RestModel {

	@JsonProperty("import")
	private Boolean importFlag;

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

	public Boolean getImportFlag() {
		return importFlag;
	}

	public void setImportFlag(Boolean importFlag) {
		this.importFlag = importFlag;
	}
}

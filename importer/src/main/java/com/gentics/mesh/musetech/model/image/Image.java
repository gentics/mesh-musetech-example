package com.gentics.mesh.musetech.model.image;

import com.gentics.mesh.core.rest.common.RestModel;

public class Image implements RestModel {

	private String name;
	private String attribution;
	private String license;
	private String source;

	public Image() {
	}

	public String getAttribution() {
		return attribution;
	}

	public String getLicense() {
		return license;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	@Override
	public String toString() {
		return toJson();
	}
}

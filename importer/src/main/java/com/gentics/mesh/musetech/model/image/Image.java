package com.gentics.mesh.musetech.model.image;

import com.gentics.mesh.core.rest.common.RestModel;

public class Image implements RestModel {

	private String uuid;
	private String name;
	private String attribution;
	private String license;
	private String source;
	private Float fpx;
	private Float fpy;

	public Image() {
	}

	public String getUuid() {
		return uuid;
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

	public Float getFpx() {
		return fpx;
	}

	public Float getFpy() {
		return fpy;
	}

	@Override
	public String toString() {
		return toJson();
	}
}

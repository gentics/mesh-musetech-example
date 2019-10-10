package com.gentics.mesh.musetech.model.screen;

import java.util.List;

import com.gentics.mesh.core.rest.common.RestModel;

public class Screen implements RestModel {

	private String id;
	private String name;
	private String location;
	private String description;

	private List<ScreenContent> contents;

	public Screen() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ScreenContent> getContents() {
		return contents;
	}

	public void setContents(List<ScreenContent> contents) {
		this.contents = contents;
	}

	@Override
	public String toString() {
		return toJson();
	}
}

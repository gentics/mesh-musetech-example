package com.gentics.mesh.musetech.model.exhibit;

import com.gentics.mesh.core.rest.common.RestModel;

public class ExhibitLocation implements RestModel {

	private String building;
	private int level;
	private String section;

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

}

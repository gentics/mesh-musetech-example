package com.gentics.mesh.musetech.model.screen;

import com.gentics.mesh.core.rest.common.RestModel;

public class ScreenContent implements RestModel {

	private ScreenContentType type;
	private String title;
	private String teaser;
	private String start;
	private Long duration;
	private String location;
	private String image;
	private String video;

	public ScreenContent() {
		// TODO Auto-generated constructor stub
	}

	public ScreenContentType getType() {
		return type;
	}

	public void setType(ScreenContentType type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTeaser() {
		return teaser;
	}

	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	@Override
	public String toString() {
		return toJson();
	}
}

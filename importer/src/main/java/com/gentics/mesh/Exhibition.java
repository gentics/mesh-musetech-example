package com.gentics.mesh;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Exhibition {

	private String id;

	private String publicNumber;

	private String title;

	private String description;

	private List<String> images = new ArrayList<>();

	private String audio;

	private String titleImage;

	public Exhibition(JsonObject json) {
		// System.out.println(json.encodePrettily());

		Object audioFiles = json.getValue("audio_files");
		if (audioFiles instanceof JsonArray) {
			JsonArray audioFilesArr = (JsonArray) audioFiles;
			JsonObject audio = audioFilesArr.getJsonObject(0);
			String audioUrl = audio.getString("audio_url");
			String filename = Paths.get(audioUrl).getFileName().toString();
			this.audio = filename;
		}

		this.description = StringEscapeUtils.unescapeHtml4(json.getString("description"));
		this.title = StringEscapeUtils.unescapeHtml4(json.getString("title"));
		
		this.id = json.getString("exhibitions_id");
		this.publicNumber = json.getString("public_number");
		Object t = json.getValue("images");
		if (t instanceof JsonArray) {
			JsonArray images = (JsonArray) t;
			for (int i = 0; i < images.size(); i++) {
				JsonObject image = images.getJsonObject(i);
				String url = image.getString("image_url");
				url = url.replace("public/", "/");
				this.images.add(url);
			}
		}
		String titleUrl = json.getString("dm_detail_image");
		titleUrl = titleUrl.replace("public/", "/");
		this.titleImage = titleUrl;

	}

	public String getId() {
		return id;
	}

	public String getAudio() {
		return audio;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getImages() {
		return images;
	}

	public String getPublicNumber() {
		return publicNumber;
	}

	public String getTitleImage() {
		return titleImage;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		JsonObject json = JsonObject.mapFrom(this);
		return json.encodePrettily();
	}
}

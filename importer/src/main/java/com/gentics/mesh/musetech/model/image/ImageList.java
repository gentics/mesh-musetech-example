package com.gentics.mesh.musetech.model.image;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.gentics.mesh.json.JsonUtil;

public class ImageList {

	private List<Image> images;

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public static ImageList load() throws IOException {
		String jsonStr = FileUtils.readFileToString(new File("data/image/images.json"));
		return JsonUtil.readValue(jsonStr, ImageList.class);
	}
}

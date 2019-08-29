package com.gentics.mesh.musetech.model.video;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.gentics.mesh.core.rest.common.RestModel;
import com.gentics.mesh.json.JsonUtil;

public class VideoList implements RestModel {

	private List<Video> videos;

	public List<Video> getVideos() {
		return videos;
	}

	public void setVideos(List<Video> videos) {
		this.videos = videos;
	}

	public static VideoList load() throws IOException {
		String jsonStr = FileUtils.readFileToString(new File("data/video/videos.json"));
		return JsonUtil.readValue(jsonStr, VideoList.class);
	}
}

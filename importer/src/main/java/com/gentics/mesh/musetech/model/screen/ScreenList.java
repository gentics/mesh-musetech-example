package com.gentics.mesh.musetech.model.screen;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.gentics.mesh.core.rest.common.RestModel;
import com.gentics.mesh.json.JsonUtil;

public class ScreenList implements RestModel {

	private List<Screen> screens;

	public ScreenList() {
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}

	public static ScreenList load() throws IOException {
		String jsonStr = FileUtils.readFileToString(new File("data/screen/screens.json"));
		return JsonUtil.readValue(jsonStr, ScreenList.class);
	}

}

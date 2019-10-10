package com.gentics.mesh.musetech.model.exhibit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.gentics.mesh.json.JsonUtil;

public class ExhibitList {

	private List<Exhibit> exhibits = new ArrayList<>();

	public List<Exhibit> getExhibits() {
		return exhibits;
	}

	public static ExhibitList load() throws IOException {
		String jsonStr = FileUtils.readFileToString(new File("data/exhibits.json"));
		return JsonUtil.readValue(jsonStr, ExhibitList.class);
	}

}

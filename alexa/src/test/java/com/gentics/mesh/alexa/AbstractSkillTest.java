package com.gentics.mesh.alexa;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import io.vertx.core.json.JsonObject;

public class AbstractSkillTest {
	
	public JsonObject loadJson(String name) throws IOException {
		return new JsonObject(IOUtils.toString(this.getClass().getResourceAsStream("/alexa/" + name + ".json"), "UTF-8"));
	}
}

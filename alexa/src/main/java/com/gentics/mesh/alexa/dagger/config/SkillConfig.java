package com.gentics.mesh.alexa.dagger.config;

/**
 * Skill server configuration POJO.
 */
public class SkillConfig {

	private String meshApiKey;

	private String host = "demo.getmesh.io";

	private int port = 443;

	private boolean ssl = true;

	public String getMeshApiKey() {
		return meshApiKey;
	}

	public SkillConfig setMeshApiKey(String meshApiKey) {
		this.meshApiKey = meshApiKey;
		return this;
	}

	public String getHost() {
		return host;
	}

	public SkillConfig setHost(String host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public SkillConfig setPort(int port) {
		this.port = port;
		return this;
	}

	public boolean isSsl() {
		return ssl;
	}

	public SkillConfig setSsl(boolean ssl) {
		this.ssl = ssl;
		return this;
	}
}

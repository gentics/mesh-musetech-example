package com.gentics.mesh.alexa.dagger.config;

/**
 * Skill server configuration POJO.
 */
public class SkillConfig {

	private String meshApiKey;

	private String meshHost = "cms.musetech.getmesh.io";

	private int serverPort = 4445;

	private int meshPort = 443;

	private boolean meshSsl = true;

	public String getMeshApiKey() {
		return meshApiKey;
	}

	public SkillConfig setMeshApiKey(String meshApiKey) {
		this.meshApiKey = meshApiKey;
		return this;
	}

	public String getMeshServerHost() {
		return meshHost;
	}

	public SkillConfig setMeshHost(String host) {
		this.meshHost = host;
		return this;
	}

	public int getMeshServerPort() {
		return meshPort;
	}

	public SkillConfig setMeshPort(int meshPort) {
		this.meshPort = meshPort;
		return this;
	}

	public boolean isMeshServerSslFlag() {
		return meshSsl;
	}

	public SkillConfig setMeshSsl(boolean ssl) {
		this.meshSsl = ssl;
		return this;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
}

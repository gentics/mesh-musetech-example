package com.gentics.mesh.musetech;

public class ImporterConfig {

	private static final String DEFAULT_PROJECT_NAME = "musetech";

	private String projectName = DEFAULT_PROJECT_NAME;
	private String hostname = "localhost";
	private int port = 8080;
	private boolean ssl = false;

	public ImporterConfig() {
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}

package com.gentics.mesh.musetech;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.musetech.importer.impl.ImporterImpl;

/**
 * Importer which will purge the musetech project and recreate it with the provided data.
 */
public class ImportRunner {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ImporterConfig config = new ImporterConfig();
		applyEnvs(config);
		Importer importer = new ImporterImpl(config);
		importer.login("admin", "admin");
		importer.run();
	}

	private static void applyEnvs(ImporterConfig config) {
		String hostStr = System.getenv("MUSETECH_MESH_HOST");
		if (hostStr != null) {
			config.setHostname(hostStr);
		}
		String portStr = System.getenv("MUSETECH_MESH_PORT");
		if (portStr != null) {
			config.setPort(Integer.valueOf(portStr));
		}
		String sslStr = System.getenv("MUSETECH_MESH_SSL");
		if (sslStr != null) {
			config.setSsl(Boolean.valueOf(sslStr));
		}
		String projectNameStr = System.getenv("MUSETECH_PROJECT_NAME");
		if (projectNameStr != null) {
			config.setProjectName(projectNameStr);
		}
	}

}

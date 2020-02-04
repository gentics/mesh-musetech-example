package com.gentics.mesh.musetech;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.musetech.importer.impl.ImporterImpl;

/**
 * Importer which will purge the musetech project and recreate it with the provided data.
 */
public class ImportRunner {

	public static Importer importer;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ImporterConfig config = new ImporterConfig();
		applyEnvs(config);
		importer = new ImporterImpl(config);
		importer.login("admin", "admin");
		run();
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



	public static void run() {
		importer.purge();
		long start = System.currentTimeMillis();
		importer.loadOrCreateProject()
			.flatMapCompletable(project -> {
				return importer.createMicroschemas().andThen(importer.createSchemas())
					.andThen(importer.createFolders(project))
					.andThen(importer.importNodes(project))
					.andThen(importer.publishNodes(project))
					.andThen(importer.grantPermissions(project));
			})
			.subscribe(() -> {
				long dur = System.currentTimeMillis() - start;
				System.out.println("Import done. Took: " + dur + "[ms]");
			}, err -> {
				err.printStackTrace();
			});
	}

}

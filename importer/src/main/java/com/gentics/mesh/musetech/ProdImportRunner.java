package com.gentics.mesh.musetech;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.musetech.importer.impl.ImporterImpl;

/**
 * Importer which will purge the musetech project and recreate it with the provided data.
 */
public class ProdImportRunner {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ImporterConfig config = new ImporterConfig();
		config.setHostname("cms.musetech.getmesh.io");
		config.setSsl(true);
		config.setPort(443);
		Importer importer = new ImporterImpl(config);
		importer.login("admin", "admin");
		importer.run();
	}
}

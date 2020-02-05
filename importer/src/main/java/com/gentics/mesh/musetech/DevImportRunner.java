package com.gentics.mesh.musetech;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.musetech.importer.impl.ImporterImpl;

/**
 * Importer which will purge the musetech project and recreate it with the provided data.
 */
public class DevImportRunner {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ImporterConfig config = new ImporterConfig();
		Importer importer = new ImporterImpl(config);
		importer.login("admin", "admin");
		importer.run();
	}
}

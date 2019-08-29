package com.gentics.mesh.musetech;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.musetech.importer.impl.ImporterImpl;

public class ImportRunner {

	private static final String PROJECT_NAME = "musetech";

	public final Importer importer;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new ImportRunner().run();
	}

	public ImportRunner() throws IOException {
		importer = new ImporterImpl(PROJECT_NAME);
		importer.login("admin", "admin");
	}

	private void run() {

		importer.purge();
		long start = System.currentTimeMillis();
		// Setup project with schemas
		// -> create folders
		// -> create contents
		// -> grant permissions
		importer.loadOrCreateProject()
			.flatMapCompletable(project -> {
				return importer.createMicroschemas().andThen(importer.createSchemas())
					.andThen(
						importer.createFolders(project))
					.andThen(
						importer.grantPermissions(project));
			})
			.subscribe(() -> {
				long dur = System.currentTimeMillis() - start;
				System.out.println("Import done. Took: " + dur + "[ms]");
			}, err -> {
				err.printStackTrace();
			});
	}

}

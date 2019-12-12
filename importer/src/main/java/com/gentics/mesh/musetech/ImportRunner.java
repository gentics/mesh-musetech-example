package com.gentics.mesh.musetech;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.musetech.importer.impl.ImporterImpl;

/**
 * Importer which will purge the musetech project and recreate it with the provided data.
 */
public class ImportRunner {

	private static final String PROJECT_NAME = "musetech";

	public final Importer importer;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new ImportRunner(args).run();
	}

	public ImportRunner(String[] args) throws IOException {
		//boolean local = checkLocalArg(args);
		boolean local = true;
		if (local) {
			System.out.println("Running on localhost");
			importer = new ImporterImpl("localhost", 8080, false, PROJECT_NAME);
		} else {
			System.out.println("Running on cms.musetech.getmesh.io");
			importer = new ImporterImpl("cms.musetech.getmesh.io", 443, true, PROJECT_NAME);
		}
		importer.login("admin", "admin");
	}

	private boolean checkLocalArg(String[] args) {
		// TODO use command line library to parse args
		if (args.length == 1) {
			String modeArg = args[0];
			if (modeArg.equalsIgnoreCase("--local")) {
				return true;
			} else if (modeArg.equalsIgnoreCase("--prod")) {
				return false;
			}
			return true;
		} else {
			return true;
		}
	}

	private void run() {
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

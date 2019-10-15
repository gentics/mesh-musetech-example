package com.gentics.mesh.musetech.importer;

import com.gentics.mesh.core.rest.project.ProjectResponse;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface Importer {

	/**
	 * Create all schemas.
	 * 
	 * @return
	 */
	Completable createSchemas();

	/**
	 * Create all needed microschemas.
	 * 
	 * @return
	 */
	Completable createMicroschemas();

	/**
	 * Grant permissions for the anonymous access.
	 * 
	 * @param project
	 * @return
	 */
	Completable grantPermissions(ProjectResponse project);

	/**
	 * Create the needed folder structure.
	 * 
	 * @param project
	 * @return
	 */
	Completable createFolders(ProjectResponse project);

	/**
	 * Publish the nodes in the project.
	 * 
	 * @param project
	 * @return
	 */
	Completable publishNodes(ProjectResponse project);

	/**
	 * Set the login for the importer.
	 * 
	 * @param user
	 * @param pass
	 */
	void login(String user, String pass);

	/**
	 * Load the project or create it if it does not exist.
	 * 
	 * @return
	 */
	Single<ProjectResponse> loadOrCreateProject();

	/**
	 * Purge the current project. This is useful if you want to re-import everything from scratch.
	 */
	void purge();

	/**
	 * Return the project name to be used for the import.
	 * 
	 * @return
	 */
	String projectName();

	Completable importNodes(ProjectResponse project);

}

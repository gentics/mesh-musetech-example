package com.gentics.mesh.musetech.importer;

import com.gentics.mesh.core.rest.project.ProjectResponse;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface Importer {

	Completable createSchemas();

	Completable createMicroschemas();


	Completable grantPermissions(ProjectResponse project);

	Completable createFolders(ProjectResponse project);

	void login(String user, String pass);

	Single<ProjectResponse> loadOrCreateProject();

	void purge();

	String projectName();

}

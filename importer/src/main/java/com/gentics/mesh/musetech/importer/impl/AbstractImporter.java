package com.gentics.mesh.musetech.importer.impl;

import java.util.HashMap;
import java.util.Map;

import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.role.RolePermissionRequest;
import com.gentics.mesh.importer.helper.ImportUtils;
import com.gentics.mesh.musetech.ImporterConfig;
import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.rest.client.MeshRestClient;

import io.reactivex.Completable;
import io.reactivex.Single;

public abstract class AbstractImporter implements Importer {

	protected final MeshRestClient client;

	protected Map<String, String> fileIdMap = new HashMap<>();

	protected ImporterConfig config;

	public AbstractImporter(ImporterConfig config) {
		this.config = config;
		client = MeshRestClient.create(config.getHostname(), config.getPort(),config.isSsl());
	}

	@Override
	public void login(String user, String pass) {
		client.setLogin(user, pass);
		client.login().blockingGet();
	}

	@Override
	public Single<ProjectResponse> loadOrCreateProject() {
		return ImportUtils.loadOrCreateProject(client, projectName());
	}

	@Override
	public Completable publishNodes(ProjectResponse project) {
		return ImportUtils.publishNodes(client, project);
	}

	@Override
	public Completable grantPermissions(ProjectResponse project) {
		return client.findRoles().toSingle().flatMapCompletable(list -> {
			String roleUuid = list.getData().stream().filter(u -> u.getName().equals("anonymous")).map(u -> u.getUuid()).findFirst().get();
			RolePermissionRequest request = new RolePermissionRequest();
			request.setRecursive(true);
			request.getPermissions().setRead(false);
			request.getPermissions().setUpdate(false);
			request.getPermissions().setDelete(false);
			request.getPermissions().setReadPublished(true);
			return client.updateRolePermissions(roleUuid, "projects/" + project.getUuid(), request).toCompletable()
				.andThen(client.updateRolePermissions(roleUuid, "schemas", request).toCompletable());
		});
	}

}

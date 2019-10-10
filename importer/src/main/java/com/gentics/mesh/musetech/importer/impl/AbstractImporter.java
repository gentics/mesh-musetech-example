package com.gentics.mesh.musetech.importer.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.gentics.mesh.core.rest.microschema.impl.MicroschemaCreateRequest;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaUpdateRequest;
import com.gentics.mesh.core.rest.project.ProjectCreateRequest;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.role.RolePermissionRequest;
import com.gentics.mesh.core.rest.schema.impl.SchemaCreateRequest;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;
import com.gentics.mesh.core.rest.schema.impl.SchemaUpdateRequest;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.musetech.importer.Importer;
import com.gentics.mesh.parameter.client.PublishParametersImpl;
import com.gentics.mesh.rest.client.MeshRestClient;

import io.reactivex.Completable;
import io.reactivex.Single;

public abstract class AbstractImporter implements Importer {

	protected final MeshRestClient client;

	protected Map<String, String> fileIdMap = new HashMap<>();

	public AbstractImporter(String hostname, int port, boolean ssl) {
		client = MeshRestClient.create(hostname, port, ssl);
	}

	@Override
	public void login(String user, String pass) {
		client.setLogin(user, pass);
		client.login().blockingGet();
	}

	@Override
	public Single<ProjectResponse> loadOrCreateProject() {
		return client.findProjectByName(projectName()).toSingle().onErrorResumeNext(err -> {
			ProjectCreateRequest request = new ProjectCreateRequest();
			request.setName(projectName());
			request.setSchemaRef("folder");
			return client.createProject(request).toSingle();
		});
	}

	@Override
	public Completable publishNodes(ProjectResponse project) {
		String nodeUuid = project.getRootNode().getUuid();
		return client.publishNode(projectName(), nodeUuid, new PublishParametersImpl().setRecursive(true)).toCompletable();
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

	protected Single<SchemaResponse> createOrUpdateSchema(SchemaCreateRequest request) {
		return client.findSchemas().toSingle().flatMap(list -> {
			Optional<SchemaResponse> op = list.getData().stream().filter(s -> s.getName().equals(request.getName())).findFirst();
			if (!op.isPresent()) {
				return client.createSchema(request).toSingle();
			} else {
				SchemaResponse schema = op.get();
				SchemaUpdateRequest updateRequest = JsonUtil.readValue(request.toJson(), SchemaUpdateRequest.class);
				return client.updateSchema(schema.getUuid(), updateRequest).toCompletable()
					.andThen(client.findSchemaByUuid(schema.getUuid()).toSingle());
			}
		});
	}

	protected Single<MicroschemaResponse> createOrUpdateMicroschema(MicroschemaCreateRequest request) {
		return client.findMicroschemas().toSingle().flatMap(list -> {
			Optional<MicroschemaResponse> op = list.getData().stream().filter(s -> s.getName().equals(request.getName())).findFirst();
			if (!op.isPresent()) {
				return client.createMicroschema(request).toSingle();
			} else {
				MicroschemaResponse schema = op.get();
				MicroschemaUpdateRequest updateRequest = JsonUtil.readValue(request.toJson(), MicroschemaUpdateRequest.class);
				return client.updateMicroschema(schema.getUuid(), updateRequest).toCompletable()
					.andThen(client.findMicroschemaByUuid(schema.getUuid()).toSingle());
			}
		});
	}

	public Completable linkSchema(Single<SchemaResponse> schema, String projectName) {
		return schema.flatMapCompletable(response -> {
			return client.assignSchemaToProject(projectName, response.getUuid()).toCompletable();
		});
	}

	public Completable linkMicroschema(Single<MicroschemaResponse> schema, String projectName) {
		return schema.flatMapCompletable(response -> {
			return client.assignMicroschemaToProject(projectName, response.getUuid()).toCompletable();
		});
	}
}

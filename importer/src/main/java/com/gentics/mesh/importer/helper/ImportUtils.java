package com.gentics.mesh.importer.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.gentics.mesh.core.rest.microschema.impl.MicroschemaCreateRequest;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaUpdateRequest;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.project.ProjectCreateRequest;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.schema.impl.SchemaCreateRequest;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;
import com.gentics.mesh.core.rest.schema.impl.SchemaUpdateRequest;
import com.gentics.mesh.core.rest.user.NodeReference;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.parameter.client.PublishParametersImpl;
import com.gentics.mesh.rest.client.MeshRestClient;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public final class ImportUtils {

	private static final Logger log = LoggerFactory.getLogger(ImportUtils.class);

	private ImportUtils() {
	}

	public static List<SchemaCreateRequest> loadSchemas(String path) throws IOException {
		List<SchemaCreateRequest> schemas = Files.list(Paths.get(path))
			.filter(f -> f.toString().endsWith(".json"))
			.map(f -> {
				try {
					log.info("Reading {" + f + "}");
					return FileUtils.readFileToString(f.toFile());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Could not read file {" + f + "}");
				}
			})
			.map(json -> JsonUtil.readValue(json, SchemaCreateRequest.class))
			.collect(Collectors.toList());
		return schemas;
	}

	public static List<MicroschemaCreateRequest> loadMicroschemas(String path) throws IOException {
		List<MicroschemaCreateRequest> microschemas = Files.list(Paths.get(path))
			.filter(f -> f.toString().endsWith(".json"))
			.map(f -> {
				try {
					log.info("Reading {" + f + "}");
					return FileUtils.readFileToString(f.toFile());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Could not read file {" + f + "}");
				}
			})
			.map(json -> JsonUtil.readValue(json, MicroschemaCreateRequest.class))
			.collect(Collectors.toList());
		return microschemas;
	}

	public static List<NodeResponse> loadNodes(String path) throws IOException {
		List<NodeResponse> nodes = Files.list(Paths.get(path))
			.filter(f -> f.toString().endsWith(".json"))
			.map(f -> {
				try {
					return FileUtils.readFileToString(f.toFile());
				} catch (Exception e) {
					throw new RuntimeException("Could not read file {" + f + "}");
				}
			})
			.map(json -> JsonUtil.readValue(json, NodeResponse.class))
			.collect(Collectors.toList());
		return nodes;
	}

	public static Completable importNodes(MeshRestClient client, List<NodeResponse> nodes, ProjectResponse project) {
		return Observable.fromIterable(nodes)
			.map(node -> {
				NodeReference nodeRef = new NodeReference();
				nodeRef.setUuid(project.getRootNode().getUuid());
				node.setParentNode(nodeRef);
				return node;
			})
			.flatMapCompletable(node -> {
				String uuid = node.getUuid();
				NodeCreateRequest request = JsonUtil.readValue(node.toJson(), NodeCreateRequest.class);
				return client.createNode(uuid, project.getName(), request).toCompletable();
			});
	}

	public static void purge(MeshRestClient client, String projectName) {
		client.findProjectByName(projectName).toSingle().flatMap(p -> {
			return client.deleteProject(p.getUuid()).toSingle();
		}).ignoreElement().onErrorComplete().blockingAwait();
	}

	/**
	 * Create or update the schemas and link them to the project.
	 * 
	 * @param client
	 * @param schemas
	 * @param projectName
	 * @return
	 */
	public static Completable createSchemas(MeshRestClient client, List<SchemaCreateRequest> schemas, String projectName) {
		return Observable.fromIterable(schemas)
			.flatMapCompletable(schema -> {
				log.info("Creating schema {" + schema.getName() + "}");
				return linkSchema(client, createOrUpdateSchema(client, schema), projectName);
			});

	}

	/**
	 * Create or update the microschemas and link them to the project.
	 * 
	 * @param client
	 * @param microschemas
	 * @param projectName
	 * @return
	 */
	public static Completable createMicroschemas(MeshRestClient client, List<MicroschemaCreateRequest> microschemas, String projectName) {
		return Observable.fromIterable(microschemas)
			.flatMapCompletable(microschema -> {
				log.info("Creating microschema {" + microschema.getName() + "}");
				return linkMicroschema(client, createOrUpdateMicroschema(client, microschema), projectName);
			});
	}

	public static Single<SchemaResponse> createOrUpdateSchema(MeshRestClient client, SchemaCreateRequest request) {
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

	public static Single<MicroschemaResponse> createOrUpdateMicroschema(MeshRestClient client, MicroschemaCreateRequest request) {
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

	public static Completable linkSchema(MeshRestClient client, Single<SchemaResponse> schema, String projectName) {
		return schema.flatMapCompletable(response -> {
			return client.assignSchemaToProject(projectName, response.getUuid()).toCompletable();
		});
	}

	public static Completable linkMicroschema(MeshRestClient client, Single<MicroschemaResponse> schema, String projectName) {
		return schema.flatMapCompletable(response -> {
			return client.assignMicroschemaToProject(projectName, response.getUuid()).toCompletable();
		});
	}

	public static Single<ProjectResponse> loadOrCreateProject(MeshRestClient client, String projectName) {
		return client.findProjectByName(projectName).toSingle().onErrorResumeNext(err -> {
			ProjectCreateRequest request = new ProjectCreateRequest();
			request.setName(projectName);
			request.setSchemaRef("folder");
			return client.createProject(request).toSingle();
		});
	}

	public static Completable publishNodes(MeshRestClient client, ProjectResponse project) {
		String nodeUuid = project.getRootNode().getUuid();
		return client.publishNode(project.getName(), nodeUuid, new PublishParametersImpl().setRecursive(true)).toCompletable();
	}

}